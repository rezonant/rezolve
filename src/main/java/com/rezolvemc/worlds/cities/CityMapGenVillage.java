//package com.rezolvemc.worlds.cities;
//
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.ChunkPos;
//import net.minecraft.world.World;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.chunk.ChunkPrimer;
//import net.minecraft.world.gen.structure.MapGenVillage;
//import net.minecraft.world.gen.structure.StructureStart;
//
//import java.util.Random;
//
//public class CityMapGenVillage extends net.minecraft.world.gen.structure.MapGenVillage {
//
//	@Override
//	public void generate(World worldIn, int x, int z, ChunkPrimer primer) {
//		if (CityBiome.isCity(worldIn, new ChunkPos(x,z)))
//			return;
//
//		super.generate(worldIn, x, z, primer);
//	}
//
//	protected StructureStart getStructureStart(int chunkX, int chunkZ)
//	{
//		int size = 0;
//
//		Biome biome = this.worldObj.getBiome(new BlockPos(chunkX * 16 + 8, 128, chunkZ * 16 + 8));
//
//		if (biome instanceof com.rezolvemc.worlds.cities.TownBiome)
//			size = 6;
//
//		return new MapGenVillage.Start(this.worldObj, this.rand, chunkX, chunkZ, size);
//	}
//
//	protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
//
//		int distance = 32;
//
//		Biome biome = this.worldObj.getBiome(new BlockPos(chunkX * 16 + 8, 128, chunkZ * 16 + 8));
//		boolean skipVariance = false;
//
//		if (biome instanceof com.rezolvemc.worlds.cities.TownBiome) {
//			distance = 8;
//			skipVariance = true;
//		}
//
//		int originalChunkX = chunkX;
//		int originalChunkZ = chunkZ;
//
//		if (chunkX < 0)
//			chunkX -= distance - 1;
//
//		if (chunkZ < 0)
//			chunkZ -= distance - 1;
//
//		int gridAlignedX = (chunkX / distance) * distance;
//		int gridAlignedZ = (chunkZ / distance) * distance;
//
//		Random random = this.worldObj.setRandomSeed(chunkX / distance, chunkZ / distance, 10387312);
//		if (!skipVariance) {
//			gridAlignedX += random.nextInt(distance - 8);
//			gridAlignedZ += random.nextInt(distance - 8);
//		}
//
//		if (originalChunkX == gridAlignedX && originalChunkZ == gridAlignedZ) {
//			return this.worldObj.getBiomeProvider().areBiomesViable(originalChunkX * 16 + 8, originalChunkZ * 16 + 8, 0, MapGenVillage.VILLAGE_SPAWN_BIOMES);
//		}
//
//		return false;
//	}
//}
