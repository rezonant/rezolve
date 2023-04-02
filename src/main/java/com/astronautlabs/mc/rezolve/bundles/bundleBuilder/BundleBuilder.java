package com.astronautlabs.mc.rezolve.bundles.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.blocks.BlockEntityBase;

import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

@RegistryId("bundle_builder")
@WithBlockEntity(BundleBuilderEntity.class)
@WithMenu(BundleBuilderMenu.class)
public class BundleBuilder extends Machine implements ITooltipHint {
	public BundleBuilder() {
		super(BlockBehaviour.Properties.of(Material.METAL));
	}


	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		//BundleBuilderUpdateMessageHandler.register();
	}
	
	@Override
	public Class<? extends BlockEntityBase> getBlockEntityClass() {
		return BundleBuilderEntity.class;
	}
	
//	@Override
//	public void registerRecipes() {
//		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
//			RezolveMod.addRecipe(new ItemStack(this.itemBlock),
//				"RCR",
//				"FMF",
//				"RcR",
//
//				'R', "item|enderio:itemAlloy|3",
//				'C', Items.COMPARATOR,
//				'F', "item|enderio:itemBasicFilterUpgrade",
//				'M', "item|enderio:itemMachinePart|0",
//				'c', "item|enderio:itemBasicCapacitor|2"
//			);
//		} else {
//			RezolveMod.addRecipe(new ItemStack(this.itemBlock),
//				"QEQ",
//				"CRC",
//				"QNQ",
//
//				'Q', Blocks.QUARTZ_BLOCK,
//				'E', Blocks.ENCHANTING_TABLE,
//				'C', Blocks.CRAFTING_TABLE,
//				'R', Items.COMPARATOR,
//				'N', Items.NETHER_STAR
//			);
//		}
//	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		if (pStack.hasCustomHoverName()) {
			((BundleBuilderEntity) pLevel.getBlockEntity(pPos)).setCustomName(pStack.getHoverName().getString());
		}

		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
	}

	@Override
	public String getTooltipHint(ItemStack itemStack) {
		return "I am a Bundle Builder!";
	}
	
}