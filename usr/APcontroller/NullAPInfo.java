package usr.APcontroller;

/** Implements Extra information for the Null AP controller (essentially none) */


public class NullAPInfo implements APInfo {
    boolean controller_ = false;  // Is this router an AP controller

    public NullAPInfo() {
    }

    /** Returns true if this router is an AP Controller */
    @Override
	public boolean isAPController() {
        return controller_;
    }

    /** Sets whether this router is an AP Controller */
    @Override
	public void setAPController(boolean controller) {
        controller_ = controller;
    }

}