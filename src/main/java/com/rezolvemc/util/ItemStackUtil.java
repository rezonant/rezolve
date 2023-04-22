package com.rezolvemc.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

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
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		var nbt = stack.serializeNBT();
		buf.writeNbt(nbt);

		return sha1(buf.array());
	}

	public static ItemStack getSingleItem(ItemStack stack) {
		return getStackSized(stack, 1);
	}

	public static ItemStack getEmptyStack(ItemStack stack) {
		return getStackSized(stack, 0);
	}

	public static ItemStack getStackSized(ItemStack stack, int amount) {
		ItemStack newStack = new ItemStack(stack.getItem(), amount);

		if (stack.getTag() != null)
			newStack.setTag(stack.getTag().copy());

		return newStack;
	}

	public static ItemStack cloneStack(ItemStack stack) {
		ItemStack clone = new ItemStack(stack.getItem(), stack.getCount());
		if (stack.getTag() != null)
			clone.setTag(stack.getTag().copy());

		return clone;
	}
}
