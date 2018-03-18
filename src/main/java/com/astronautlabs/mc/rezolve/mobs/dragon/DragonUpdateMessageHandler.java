package com.astronautlabs.mc.rezolve.mobs.dragon;

import java.util.UUID;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;

import com.astronautlabs.mc.rezolve.common.IGuiProvider;
import com.astronautlabs.mc.rezolve.inventory.DragonUpdateMessage;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class DragonUpdateMessageHandler extends MessageHandler<DragonUpdateMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(DragonUpdateMessageHandler.class, DragonUpdateMessage.class, Side.CLIENT);
	}

	@Override
	public void handleInGame(DragonUpdateMessage message, World world) {
		for (Object obj : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			Entity ent = (Entity) obj;

			if (ent.getEntityId() == message.entityId) {
				EntityDragon dargon = ((EntityDragon) ent);
				dargon.rotationHeadPitch = message.headPitch;
				dargon.rotationHeadYaw2 = message.headYaw;
				break;
			}
		}

	}
}
