//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import com.astronautlabs.mc.rezolve.RezolveMod;
//import com.astronautlabs.mc.rezolve.common.machines.Machine;
//import com.astronautlabs.mc.rezolve.common.blocks.BlockEntityBase;
//
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.Container;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//
//public class RemoteShellBlock extends Machine {
//	public static final String ID = "remote_shell";
//
//	public RemoteShellBlock(Properties properties) {
//		super(properties);
//	}
//
//	@Override
//	public void registerRecipes() {
//		ItemStack enderPearlBundle = RezolveMod.BUNDLE_ITEM.withContents(
//			1,
//			new ItemStack(Items.ENDER_PEARL, 16)
//		);
////
////		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
////			RezolveMod.addRecipe(
////				new ItemStack(this.itemBlock),
////				"cCc",
////				"EME",
////				"cec",
////
////				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
////				'C', Items.COMPARATOR,
////				'E', enderPearlBundle,
////				'M', Item.REGISTRY.getObject(new ResourceLocation("enderio:itemMachinePart")),
////				'e', Blocks.ENDER_CHEST
////			);
////		} else {
////			RezolveMod.addRecipe(
////				new ItemStack(this.itemBlock),
////				"cCc",
////				"ERE",
////				"tet",
////
////				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
////				'C', Items.COMPARATOR,
////				'E', enderPearlBundle,
////				'R', Blocks.REDSTONE_BLOCK,
////				't', Blocks.REDSTONE_TORCH,
////				'e', Blocks.ENDER_CHEST
////			);
////		}
//	}
//
//	@Override
//	public void init(RezolveMod mod) {
//		super.init(mod);
////		RemoteShellActivateMessageHandler.register();
////		RemoteShellReturnMessageHandler.register();
////		RemoteShellRenameMachineMessageHandler.register();
//	}
//
//	@Override
//	public Class<? extends BlockEntityBase> getTileEntityClass() {
//		return RemoteShellEntity.class;
//	}
//
//}
