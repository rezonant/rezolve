package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@RegistryId("remote_shell")
@WithBlockEntity(RemoteShellEntity.class)
@WithMenu(RemoteShellMenu.class)
public class RemoteShellBlock extends Machine {
	public RemoteShellBlock() {
		super(Block.Properties.of(Material.METAL));
	}

//	@Override
//	public void registerRecipes() {
//		ItemStack enderPearlBundle = RezolveRegistry.item(BundleItem.class).withContents(
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

	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
//		RemoteShellActivateMessageHandler.register();
//		RemoteShellReturnMessageHandler.register();
//		RemoteShellRenameMachineMessageHandler.register();
	}
}
