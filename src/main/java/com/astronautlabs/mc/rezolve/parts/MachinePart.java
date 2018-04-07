package com.astronautlabs.mc.rezolve.parts;

import net.minecraft.item.ItemStack;

public class MachinePart {
	public MachinePart(String name) {
		this.name = name;
	}

	private String name;

	public String getName() {
		return this.name;
	}

	public String getTooltipHint(ItemStack itemStack) {
		return null;
	}

	public String getItemStackDisplayName(ItemStack stack) {
		return null;
	}

	public static void register(String name) {
		MachinePartItem.registerPart(name);
	}

	public static void register(MachinePart part) {
		MachinePartItem.registerPart(part);
	}
}
