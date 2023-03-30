package com.astronautlabs.mc.rezolve.bundles.unbundler;

import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@RegistryId("unbundler")
@WithBlockEntity(UnbundlerEntity.class)
@WithMenu(UnbundlerMenu.class)
public class Unbundler extends Machine {
	public Unbundler() {
		super(BlockBehaviour.Properties.of(Material.METAL));
	}
	
//	@Override
//	public void registerRecipes() {
//
//		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
//
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"BCB",
//				"EME",
//				"BcB",
//
//				'B', RezolveMod.BUNDLE_PATTERN_ITEM.blank(),
//				'C', "block|enderio:blockCapBank",
//				'E', "item|enderio:itemMagnet",
//				'M', "item|enderio:itemMachinePart|0",
//				'c', "item|enderio:itemItemConduit"
//			);
//
//		} else {
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"PcP",
//				"CpC",
//				"PHP",
//
//				'P', RezolveMod.BUNDLE_PATTERN_ITEM.blank(),
//				'c', Blocks.CRAFTING_TABLE,
//				'C', Blocks.CHEST,
//				'p', Blocks.PISTON,
//				'H', Blocks.HOPPER
//			);
//		}
//	}
}
