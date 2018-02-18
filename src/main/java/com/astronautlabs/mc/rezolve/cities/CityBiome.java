package com.astronautlabs.mc.rezolve.cities;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomePlains;
import net.minecraft.world.chunk.ChunkPrimer;

public class CityBiome extends BiomePlains {

	public CityBiome() {
		super(false, new BiomeProperties("City"));
		this.setRegistryName("city");

		this.topBlock = Blocks.STONE.getDefaultState();
		this.fillerBlock = Blocks.STONE.getDefaultState();
		this.spawnableCreatureList.clear();
		this.spawnableMonsterList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
		
	}

	@Override
	public void decorate(World worldIn, Random rand, BlockPos pos) {
		// Do nothing!
	}

	@Override
	public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimer, int chunkX, int chunkZ,
			double noiseVal) {
		super.genTerrainBlocks(worldIn, rand, chunkPrimer, chunkX, chunkZ, noiseVal);
	}

}
