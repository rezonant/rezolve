package com.astronautlabs.mc.rezolve.worlds.cities;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class CityMapGenRavine extends net.minecraft.world.gen.MapGenRavine {

	@Override
	public void generate(World worldIn, int x, int z, ChunkPrimer primer) {

		if (CityBiome.isCity(worldIn, new ChunkPos(x, z)))
			return;

		super.generate(worldIn, x, z, primer);
	}
}
