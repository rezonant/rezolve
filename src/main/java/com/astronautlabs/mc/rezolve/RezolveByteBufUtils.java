package com.astronautlabs.mc.rezolve;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;

public class RezolveByteBufUtils {
	public static void writeBlockPos(ByteBuf buf, BlockPos pos) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
	}
	
	public static BlockPos readBlockPos(ByteBuf buf) {
		int x, y, z;
		
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		
		return new BlockPos(x, y, z);
	}
}
