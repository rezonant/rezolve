package com.astronautlabs.mc.rezolve.machines.diskBay;

import com.astronautlabs.mc.rezolve.RezolveMod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.model.ModelDragon;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;

public class DiskAccessor {
	public DiskAccessor(ItemStack disk) {
		this.nbt = disk.getTagCompound();
		this.size = disk.getMetadata();
	}

	public static final String PROP_COUNT = "disk.count";

	public int size;
	public NBTTagCompound nbt;
	public HashMap<String, TypeContents> types;

	public static class TypeContents {
		public ItemStack template;
		public int count;

		public HashMap<String, VariantContents> contents;

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("disk.c", count);


			//nbt.setString("disk.h", );


			NBTTagList variants = new NBTTagList();

			for (String key : contents.keySet()) {
				VariantContents variant = contents.get(key);

				NBTTagCompound tag = new NBTTagCompound();
				variant.writeToNBT(tag);
				tag.setString("Disk.h", hashOfStack(variant.template));
				variants.appendTag(tag);
			}

			nbt.setTag("Items", variants);
		}

		public static TypeContents readFromNBT(NBTTagCompound nbt) {
			TypeContents type = new TypeContents();
			type.count = nbt.getInteger("Count");
			return null; // TODO
		}
	}

	public static class VariantContents {
		public ItemStack template;
		public long count;

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setLong("Count", this.count);

			NBTTagCompound stackTag = new NBTTagCompound();
			template.writeToNBT(stackTag);
			nbt.setTag("Template", stackTag);
		}
	}

	public void add(ItemStack stack) {

	}

	public void remove(ItemStack stack) {

	}

	public void clear() {
		nbt.removeTag("Items");
	}

	public static String sha1(byte[] data) {

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

	public int count(ItemStack stack) {
		String hash = this.hashOfStack(stack);

		return 0; // TODO
	}

	public int count() {
		return this.nbt.getInteger(PROP_COUNT);
	}

	public ItemStack getItemStack() {
		ItemStack stack = new ItemStack(RezolveMod.DISK_ITEM, 1, this.size);
		stack.setTagCompound(this.nbt);
		return null; // TODO
	}

	public NBTTagCompound getShareTag() {
		return null; // TODO
	}
}
