package com.astronautlabs.mc.rezolve.storage;

public interface IStorageTileEntity {
	/**
	 * Retrieve an accessor for this tile entity's storage
	 * @return Returns the storage accessor, or null if the storage is unavailable
	 */
	IStorageAccessor getStorageAccessor();

	boolean hasView();
}
