//package com.astronautlabs.mc.rezolve.cities;
//
//import java.util.ArrayList;
//import java.util.Random;
//
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.biome.BiomePlains;
//import net.minecraft.world.chunk.Chunk;
//import net.minecraft.world.chunk.ChunkPrimer;
//
//public class CityBiome extends BiomePlains {
//
//	public CityBiome() {
//		super(false, new BiomeProperties("City"));
//		this.setRegistryName("city");
//
//		this.topBlock = Blocks.STONE.getDefaultState();
//		this.fillerBlock = Blocks.STONE.getDefaultState();
//		this.spawnableCreatureList.clear();
//		this.spawnableMonsterList.clear();
//		this.spawnableWaterCreatureList.clear();
//		this.spawnableCaveCreatureList.clear();
//
//	}
//
//	@Override
//	public void decorate(Level worldIn, Random rand, BlockPos pos) {
//		// Do nothing!
//	}
//
//	@Override
//	public void genTerrainBlocks(Level worldIn, Random rand, ChunkPrimer chunkPrimer, int chunkX, int chunkZ,
//			double noiseVal) {
//		super.genTerrainBlocks(worldIn, rand, chunkPrimer, chunkX, chunkZ, noiseVal);
//	}
//
//}
