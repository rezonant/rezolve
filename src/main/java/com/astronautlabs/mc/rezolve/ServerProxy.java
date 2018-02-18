package com.astronautlabs.mc.rezolve;

public class ServerProxy extends CommonProxy {
	public ServerProxy() {
		super();
	}
	
	@Override()
	public void init(RezolveMod mod) {
		super.init(mod);
    	this.log("Initializing server-side proxy...");
	}
}
