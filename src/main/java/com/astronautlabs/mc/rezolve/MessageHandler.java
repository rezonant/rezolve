package com.astronautlabs.mc.rezolve;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHandler<TMessage extends IMessage, TResponse extends IMessage> implements IMessageHandler<TMessage, TResponse> {
	
	/**
	 * Provide a response packet for the given message
	 * @param message
	 * @return
	 */
	public TResponse respondTo(TMessage message) {
		return null;
	}
	
	/**
	 * Handle the given message in-game
	 * @param message
	 */
	public void handleInGame(TMessage message, World world) {
		
	}
	
	@Override
	public final TResponse onMessage(final TMessage message, MessageContext ctx) {

		// Respond to the message directly 
		
		TResponse response = this.respondTo(message);

		// Schedule an opportunity to handle this message in game
		
		final MessageHandler<TMessage, TResponse> self = this;
		final World world = ctx.getServerHandler().playerEntity.worldObj;
		IThreadListener listener = null;
		
		if (world.isRemote) {
			listener = (WorldServer)world; 
		} else {
			listener = Minecraft.getMinecraft();
		}
		
		listener.addScheduledTask(new Runnable() {
			@Override
			public void run() {	
				self.handleInGame(message, world);
			}
		});
		
		// Return our response, if any
		
		return response;
	}

}
