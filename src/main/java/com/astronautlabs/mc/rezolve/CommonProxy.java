package com.astronautlabs.mc.rezolve;

import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {
	public CommonProxy() {
	}

	protected RezolveMod mod;
	
	protected void log(String message) {
		this.mod.log(message);
	}
	public void init(RezolveMod mod) {
		this.mod = mod;
    	this.log("Initializing common proxy...");
    	
        NetworkRegistry.INSTANCE.registerGuiHandler(mod, mod.getGuiHandler());

	}
}
