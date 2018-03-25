package com.astronautlabs.mc.rezolve.bundles.machines.bundleBuilder;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class BundleBuilderUpdateMessageHandler extends MessageHandler<BundleBuilderUpdateMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(BundleBuilderUpdateMessageHandler.class, BundleBuilderUpdateMessage.class, Side.SERVER);
	}
	
	@Override
	public void handleInGame(BundleBuilderUpdateMessage message, World world) {
		TileEntity entity = world.getTileEntity(message.getEntityPos());
		
		if (!(entity instanceof BundleBuilderEntity)) {
			System.err.println("No bundle builder entity at "+message.getEntityPos().toString());
			return;
		}
		
		BundleBuilderEntity bundleBuilder = (BundleBuilderEntity)entity;
		bundleBuilder.setPatternName(message.getPatternName());
	}
}
