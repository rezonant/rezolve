package com.astronautlabs.mc.rezolve.cities;

import java.util.Random;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class CityGenerator implements IWorldGenerator {
	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider) {

		boolean isCity = CityBiome.isCity(world, new ChunkPos(chunkX, chunkZ));

		if (!isCity)
			return;


		CityNode node = CityNode.nodeFor(world, chunkX * 16, 0, chunkZ * 16);

		if (node == null) {
			System.out.println("ERROR: NULL node for city chunk "+chunkX+", "+chunkZ);
		} else {
			node.drawChunk(world, new ChunkPos(chunkX, chunkZ));

		}

	}

}
