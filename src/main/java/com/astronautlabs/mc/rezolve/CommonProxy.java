package com.astronautlabs.mc.rezolve;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonProxy {
	public CommonProxy() {
	}

	protected RezolveMod mod;

	private static final Logger LOGGER = LogManager.getLogger();
	protected void log(String message) {
		LOGGER.info(message);
	}
	public void init(RezolveMod mod) {
		this.mod = mod;
    	this.log("Initializing common proxy...");

        //NetworkRegistry.INSTANCE.registerGuiHandler(mod, mod.getGuiHandler());

	}
}
