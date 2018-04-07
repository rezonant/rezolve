package com.astronautlabs.mc.rezolve.parts;

import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.MetaItemBase;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskAccessor;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public class MachinePartItem extends MetaItemBase implements ITooltipHint {

	public MachinePartItem() {
		super("item_machine_part");

	}

	private static List<MachinePart> registeredMachineParts = new ArrayList<>();
	private static boolean registrationClosed = false;

	public static void registerPart(String name) {
		registerPart(new MachinePart(name));
	}

	public static void registerPart(MachinePart part) {
		if (registrationClosed)
			throw new RuntimeException("Registration is already closed!");

		registeredMachineParts.add(part);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		MachinePart part = this.getMachinePart(stack);

		if (part != null) {
			String partString = part.getItemStackDisplayName(stack);

			if (partString != null)
				return partString;
		}

		return super.getItemStackDisplayName(stack);
	}

	@Override
	public String getTooltipHint(ItemStack itemStack) {
		MachinePart part = this.getMachinePart(itemStack);

		if (part == null)
			return null;

		return part.getTooltipHint(itemStack);

	}

	public MachinePart getMachinePart(int index) {
		return registeredMachineParts.get(index);
	}

	public MachinePart getMachinePart(ItemStack stack) {
		if (stack.getMetadata() >= registeredMachineParts.size())
			return null;

		return registeredMachineParts.get(stack.getMetadata());
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0, max = registeredMachineParts.size(); i < max; ++i)
			subItems.add(new ItemStack(itemIn, 1, i));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		if (stack.getMetadata() >= registeredMachineParts.size())
			return this.getUnlocalizedName()+"_unregistered";

		MachinePart part = this.getMachinePart(stack.getMetadata());

		return this.getUnlocalizedName()+"_"+part.getName();
	}

	public int metadataFor(String name) {
		int index = 0;
		for (MachinePart part : registeredMachineParts) {
			if (name.equals(part.getName()))
				return index;
			++index;
		}

		return -1;
	}
}
