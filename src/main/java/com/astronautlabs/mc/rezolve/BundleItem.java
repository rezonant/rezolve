package com.astronautlabs.mc.rezolve;

import com.astronautlabs.mc.rezolve.common.ItemBase;

public class BundleItem extends ItemBase {
	public BundleItem(String color) {
		super("item_bundle_"+color);
	}
	
	public BundleItem() {
		super("item_bundle");
	}
}
