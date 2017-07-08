package com.astronautlabs.mc.rezolve.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class BundlerPacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("bundler");
	private static int nextPacketID = 0;

	public static <REQ extends IMessage, REPLY extends IMessage> 
	void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> handler, Class<REQ> message, Side side) {
		BundlerPacketHandler.INSTANCE.registerMessage(handler, message, nextPacketID++, side);
	}
}
