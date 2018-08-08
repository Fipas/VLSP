package usr.test;

import java.net.SocketException;
import java.net.NoRouteToHostException;
import java.util.Scanner;
import java.nio.ByteBuffer;

import usr.logging.Logger;
import usr.logging.USR;
import usr.net.Datagram;
import usr.net.Address;
import usr.net.AddressFactory;
import usr.net.NetworkException;
import usr.protocol.Protocol;
import usr.applications.*;
import usr.router.Intercept;
import usr.dcap.DcapNetworkInterface;

/**
 * An application for Receiving some data
 */
public class InterceptApp implements Application {
    int count = 0;

    boolean running = false;

    // Address of remote router for interface to listen on
    int arg;
    Address addr;

    Intercept intercept = null;

    /**
     * Constructor for Recv
     */
    public InterceptApp() {
    }

    /**
     * Initialisation for Recv.
     * Recv port
     */
    public ApplicationResponse init(String[] args) {
        if (args.length == 1) {
            // try port
            Scanner scanner = new Scanner(args[0]);

            if (scanner.hasNextInt()) {
                arg = scanner.nextInt();
                scanner.close();
            } else {
            	scanner.close();
                return new ApplicationResponse(false, "Bad port " + args[1]);
            }

            return new ApplicationResponse(true, "");

        } else {
            return new ApplicationResponse(false, "Usage: Recv port");
        }
    }

    /** Start application with argument  */
    public ApplicationResponse start() {
        try {
            // set up capture
            addr = AddressFactory.newAddress(arg);

            DcapNetworkInterface interceptNIF = DcapNetworkInterface.getIFByAddress(addr);

            if (interceptNIF != null) {
                intercept = interceptNIF.intercept();
            } else {
                Logger.getLogger("log").logln(USR.ERROR, "No InterceptNetworkInterface for Address: " + addr);
                return new ApplicationResponse(false, "No InterceptNetworkInterface for Address: " + addr);
            }


            // Logger.getLogger("log").logln(USR.ERROR, "Socket has source port "+socket.getLocalPort());

        } catch (Exception e) {
            e.printStackTrace();

            Logger.getLogger("log").logln(USR.ERROR, "Cannot open DatagramCapture " + e.getMessage());
            return new ApplicationResponse(false, "Cannot open DatagramCapture " + e.getMessage());
        }

        running = true;

        return new ApplicationResponse(true, "");
    }

    /** Implement graceful shut down */
    public ApplicationResponse stop() {
        running = false;

        if (intercept != null) {
            intercept.close();

            Logger.getLogger("log").logln(USR.STDOUT, "Recv stop");
        }

        return new ApplicationResponse(true, "");
    }

    /** Run the ping application */
    public void run() {
        Datagram datagram;

        try {
            while ((datagram = intercept.receive()) != null) {

                if (datagram.getProtocol() == Protocol.CONTROL) {
                    Logger.getLogger("log").log(USR.STDOUT, "INTERCEPT: " + "CONTROL" + ". \n");
                } else {
                Logger.getLogger("log").log(USR.STDOUT, "INTERCEPT: " + count + ". ");
                Logger.getLogger("log").log(USR.STDOUT, "HdrLen: " + datagram.getHeaderLength() +
                                            " Len: " + datagram.getTotalLength() +
                                            " Time: " + (System.currentTimeMillis() - datagram.getTimestamp()) +
                                            " From: " + datagram.getSrcAddress() +
                                            " To: " + datagram.getDstAddress() +
                                            ". ");

                byte[] payload = datagram.getPayload();

                if (payload == null) {
                    Logger.getLogger("log").log(USR.STDOUT, "No payload");
                } else {
                    Logger.getLogger("log").log(USR.STDOUT, new String(payload));
                }
                Logger.getLogger("log").log(USR.STDOUT, "\n");

                count++;

                // send onwards
                intercept.send(datagram);
            }
            }
        } catch (NetworkException se) {
            Logger.getLogger("log").log(USR.ERROR, se.getMessage());
        } catch (NoRouteToHostException ne) {
            Logger.getLogger("log").log(USR.ERROR, ne.getMessage());
        }


    }

}
