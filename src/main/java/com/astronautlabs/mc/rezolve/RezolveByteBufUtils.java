package com.astronautlabs.mc.rezolve;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class RezolveByteBufUtils {
	@Deprecated
	public static void writeBlockPos(FriendlyByteBuf buf, BlockPos pos) {
		buf.writeBlockPos(pos);
	}

	@Deprecated
	public static BlockPos readBlockPos(FriendlyByteBuf buf) {
		return buf.readBlockPos();
	}
}
