package com.astronautlabs.mc.rezolve.mapgen;

import com.astronautlabs.mc.rezolve.cities.CityBiome;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenRavine extends net.minecraft.world.gen.MapGenRavine {

	@Override
	public void generate(World worldIn, int x, int z, ChunkPrimer primer) {

		if (CityBiome.isCity(worldIn, new ChunkPos(x, z)))
			return;

		super.generate(worldIn, x, z, primer);
	}
}
