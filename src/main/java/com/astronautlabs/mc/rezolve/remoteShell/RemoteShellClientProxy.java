package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.IOverlay;

public class RemoteShellClientProxy extends RemoteShellProxy {
	@Override
	public Object addRemoteShellOverlay(RemoteShellEntity remoteShellEntity) {
		RemoteShellOverlay overlay = new RemoteShellOverlay(remoteShellEntity);
		RezolveMod.instance().getGuiHandler().addOverlay(overlay);
		return overlay;
	}
	
	@Override
	public void removeRemoteShellOverlay(Object obj) {
		IOverlay overlay = (IOverlay)obj;
		RezolveMod.instance().getGuiHandler().removeOverlay(overlay);
	}
}
