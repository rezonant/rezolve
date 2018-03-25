package com.astronautlabs.mc.rezolve.core.inventory;

import java.util.UUID;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;

import com.astronautlabs.mc.rezolve.common.IGuiProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class GhostSlotUpdateMessageHandler extends MessageHandler<GhostSlotUpdateMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(GhostSlotUpdateMessageHandler.class, GhostSlotUpdateMessage.class, Side.SERVER);
	}
	
	@Override
	public void handleInGame(GhostSlotUpdateMessage message, World world) {
		TileEntity entity = world.getTileEntity(message.getEntityPos());
		IInventory inventory;
		
		if (entity == null) {
			System.err.println("No tile entity at "+message.getEntityPos().toString());
			return;
		}
		
		if (!(entity instanceof IInventory)) {
			System.err.println("Cannot update ghost slot on a tile entity ("+entity.getClass().getCanonicalName()+") which does not have an inventory");
			return;
		}
		
		inventory = (IInventory)entity;
		
		Block block = entity.getBlockType();
		
		if (block == null) {
			System.err.println("Cannot update ghost slot "+message.slot+" on "+entity.getClass().getCanonicalName()+": This entity has no block type.");
			return;
		}
		
		EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(UUID.fromString(message.playerId));
		
		if (!(block instanceof IGuiProvider)) {
			System.err.println("Cannot update ghost slot "+message.slot+" on "+entity.getClass().getCanonicalName()+": Block "+block.getRegistryName()+" does not provide a Container instance (IGuiProvider).");
			return;
		}
		
		IGuiProvider guiProvider = (IGuiProvider)block;
		Container container = guiProvider.createServerGui(player, world, message.x, message.y, message.z);
		Slot slot = container.getSlotFromInventory(inventory, message.slot);
		
		if (slot == null) {
			System.err.println("Cannot update ghost slot "+message.slot+": Tile entity "+entity.getClass().getCanonicalName()+" does not have a container slot for this inventory slot.");
			return;
		}
		
		if (!(slot instanceof GhostSlot)) {
			System.err.println("Cannot update ghost slot "+message.slot+" on "+entity.getClass().getCanonicalName()+": This slot is not a ghost slot.");
			return;
		}
		
		GhostSlot ghostSlot = (GhostSlot)slot;
		
		if (ghostSlot.isSingleItemOnly() && message.stack != null && message.stack.stackSize != 1) {
			System.err.println("Cannot update ghost slot "+message.slot+" on "+entity.getClass().getCanonicalName()+": Given item stack must be null or stackSize = 1");
		}
		
		// OK, we can finally update it.
		
		inventory.setInventorySlotContents(message.slot, message.stack);
	}
}
