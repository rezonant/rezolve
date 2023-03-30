package com.astronautlabs.mc.rezolve.bundles.bundleBuilder;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.bundles.BundleItem;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.inventory.VirtualInventory;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

@RegistryId("bundle_pattern")
public class BundlePatternItem extends ItemBase implements ITooltipHint {
	public BundlePatternItem() {
		super(new Item.Properties());
	}

	@Override
	public void registerRecipes() {
//
//		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
//
//			RezolveMod.addRecipe(
//				this.blank(),
//				"PSP",
//				"GFG",
//				"PSP",
//
//				'P', "item|enderio:itemAlloy|5",
//				'S', "item|enderio:itemMaterial",
//				'G', Items.GLOWSTONE_DUST,
//				'F', "item|enderio:itemBasicFilterUpgrade"
//			);
//
//		} else {
//			RezolveMod.addRecipe(
//				this.blank(),
//				"OSO",
//				"GIG",
//				"OSO",
//
//				'O', Blocks.OBSIDIAN,
//				'S', Items.SLIME_BALL,
//				'G', Items.GLOWSTONE_DUST,
//				'I', Items.ITEM_FRAME
//			);
//		}
	}
	
	@Override
	public Component getName(ItemStack stack) {
		
		if (stack.hasTag()) {
			CompoundTag nbt = stack.getTag();
			Component localizedName = super.getName(stack);
			
			ArrayList<String> props = new ArrayList<String>();
			
			if (nbt.contains("Name")) {
				props.add(nbt.getString("Name"));
			}
			
			if (nbt.contains("Color")) {
				int dye = nbt.getInt("Color");
				String colorName = RezolveMod.instance().getColorName(dye);
				props.add(colorName);
			}
			
			if (props.size() > 0)
				return Component.literal(localizedName + " ("+String.join(", ", props)+")");
			else
				return Component.literal(localizedName + " (Configured)");
		}
		
		return super.getName(stack);
	}

	private VirtualInventory dummyInventory = new VirtualInventory();
	
	@Override
	public String getTooltipHint(ItemStack itemStack) {
		return RezolveRegistry.item(BundleItem.class).describeContents(itemStack);
	}

	public boolean isBlank(ItemStack stack) {
		if (stack.getTag() == null)
			return true;

		return !stack.getTag().getBoolean("encoded");
	}

	public ItemStack blank() {
		return this.blank(1);
	}
	
	public ItemStack blank(int size) {
		var tag = new CompoundTag();
		tag.putBoolean("encoded", false);
		return new ItemStack(this, size, tag);
	}
}
