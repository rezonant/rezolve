//package com.astronautlabs.mc.rezolve.worlds.cities;
//
//import java.util.Random;
//
//import com.astronautlabs.mc.rezolve.RezolveMod;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.init.Blocks;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.ChunkPos;
//import net.minecraft.world.World;
//import net.minecraft.world.biome.BiomePlains;
//import net.minecraft.world.chunk.ChunkPrimer;
//import net.minecraft.world.gen.structure.MapGenVillage;
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
//		this.theBiomeDecorator.generateLakes = false;
//		this.theBiomeDecorator.treesPerChunk = 0;
//		this.theBiomeDecorator.bigMushroomsPerChunk = 0;
//		this.theBiomeDecorator.cactiPerChunk = 0;
//		this.theBiomeDecorator.clayPerChunk = 0;
//		this.theBiomeDecorator.deadBushPerChunk = 0;
//		this.theBiomeDecorator.flowersPerChunk = 0;
//		this.theBiomeDecorator.grassPerChunk = 0;
//		this.theBiomeDecorator.mushroomsPerChunk = 0;
//		this.theBiomeDecorator.sandPerChunk = 0;
//		this.theBiomeDecorator.waterlilyPerChunk = 0;
//		this.theBiomeDecorator.reedsPerChunk = 0;
//		this.theBiomeDecorator.extraTreeChance = 0;
//		this.theBiomeDecorator.sandPerChunk2 = 0;
//
//	}
//
//	public static boolean isCity(World world, ChunkPos chunk) {
//
//		// Debugging
//
//		if (Math.abs(chunk.chunkXPos) < 5 && Math.abs(chunk.chunkZPos) < 5)
//			return true;
//
//		// Standard
//
//		boolean isCity = false;
//
//		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunk.chunkXPos * 16 + 8, 128, chunk.chunkZPos * 16 + 8)))
//			isCity = true;
//
//		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunk.chunkXPos * 16 + 1, 128, chunk.chunkZPos * 16 + 1)))
//			isCity = true;
//
//		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunk.chunkXPos * 16 + 15, 128, chunk.chunkZPos * 16 + 15)))
//			isCity = true;
//
//		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunk.chunkXPos * 16 + 15, 128, chunk.chunkZPos * 16 + 1)))
//			isCity = true;
//
//		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunk.chunkXPos * 16 + 1, 128, chunk.chunkZPos * 16 + 15)))
//			isCity = true;
//
//		return isCity;
//	}
//	@Override
//	public void decorate(World worldIn, Random rand, BlockPos pos) {
//		// Do nothing!
//	}
//
//	@Override
//	public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimer, int chunkX, int chunkZ,
//			double noiseVal) {
//
//		int groundY = worldIn.getSeaLevel() + 2;
//
//		IBlockState stoneBlock = Blocks.STONE.getDefaultState();
//		IBlockState airBlock = Blocks.AIR.getDefaultState();
//
//		for (int x = 0, maxX = 16; x < maxX; ++x) {
//			for (int z = 0, maxZ = 16; z < maxZ; ++z) {
//
//				chunkPrimer.setBlockState(x, groundY, z, Blocks.STONE.getDefaultState());
//
//				for (int y = 0, maxY = 256; y < maxY; ++y) {
//
//					IBlockState currentBlock = chunkPrimer.getBlockState(x, y, z);
//					IBlockState newBlock = null;
//
//					if (y < groundY && currentBlock.getBlock() == Blocks.AIR) {
//						// fill it
//						newBlock = stoneBlock;
//					} else if (y > groundY && currentBlock.getBlock() != Blocks.AIR) {
//						newBlock = airBlock;
//					}
//
//
//					if (newBlock != null)
//						chunkPrimer.setBlockState(x, y, z, newBlock);
//				}
//			}
//		}
//
//		//super.genTerrainBlocks(worldIn, rand, chunkPrimer, chunkX, chunkZ, noiseVal);
//	}
//}
