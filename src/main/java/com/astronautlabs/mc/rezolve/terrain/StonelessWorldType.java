package com.astronautlabs.mc.rezolve.terrain;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkGenerator;

public class StonelessWorldType extends WorldType {

	/**
	 * Creates a new world type, the ID is hidden and should not be referenced by modders.
	 * It will automatically expand the underlying worldType array if there are no IDs left.
	 *
	 * @param name
	 */
	public StonelessWorldType() {
		super("stoneless");
	}

	@Override
	public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {

		if (this == FLAT) return new net.minecraft.world.gen.ChunkProviderFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
		if (this == DEBUG_WORLD) return new net.minecraft.world.gen.ChunkProviderDebug(world);
		if (this == CUSTOMIZED) return new ChunkProviderStoneless(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);

		return new ChunkProviderStoneless(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
	}
}


