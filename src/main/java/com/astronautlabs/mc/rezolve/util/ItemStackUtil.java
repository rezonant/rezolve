package com.astronautlabs.mc.rezolve.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class ItemStackUtil {

	private static String sha1(byte[] data) {

		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No SHA-1 support available");
		}

		Formatter formatter = new Formatter();

		for (byte b : md.digest(data))
			formatter.format("%02x", b);

		return formatter.toString();
	}

	public static String hashOfStack(ItemStack stack) {
		NBTTagCompound nbt = new NBTTagCompound();
		ByteBuf buf = Unpooled.buffer();
		stack.writeToNBT(nbt);
		ByteBufUtils.writeTag(buf, nbt);

		return sha1(buf.array());
	}

	public static ItemStack getSingleItem(ItemStack stack) {
		return getStackSized(stack, 1);
	}

	public static ItemStack getEmptyStack(ItemStack stack) {
		return getStackSized(stack, 0);
	}

	public static ItemStack getStackSized(ItemStack stack, int amount) {
		ItemStack newStack = new ItemStack(stack.getItem(), amount, stack.getMetadata());

		if (stack.getTagCompound() != null)
			newStack.setTagCompound(stack.getTagCompound().copy());

		return newStack;
	}

	public static ItemStack cloneStack(ItemStack stack) {
		ItemStack clone = new ItemStack(stack.getItem(), stack.stackSize, stack.getMetadata());
		if (stack.getTagCompound() != null)
			clone.setTagCompound(stack.getTagCompound().copy());

		return clone;
	}
}
