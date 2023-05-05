package com.rezolvemc.bundles.bundleBuilder;

import java.util.ArrayList;

import com.rezolvemc.Rezolve;
import com.rezolvemc.bundles.BundleItem;
import com.rezolvemc.common.ITooltipHint;
import com.rezolvemc.common.ItemBase;
import org.torchmc.inventory.VirtualInventory;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.RezolveRegistry;
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
				String colorName = Rezolve.instance().getColorName(dye);
				props.add(colorName);
			}
			
			if (props.size() > 0) {
				return Component.empty()
						.append(localizedName)
						.append(" (")
						.append(String.join(", ", props) + ")")
						;
			} else {
				return Component.empty()
						.append(localizedName)
						.append(" (")
						.append(Component.translatable("screens.rezolve.configured"))
						.append(")")
						;
			}
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
