package com.astronautlabs.mc.rezolve.worlds.ores;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

public class OreGenerator implements IWorldGenerator {

	public OreGenerator() {
		GameRegistry.registerWorldGenerator(this, 0);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimension() != 0)
			return;

		this.addOreSpawn(
			RezolveMod.METAL_ORE_BLOCK.getStateFor(Metal.COPPER),
			world, random, chunkX, chunkZ, 16, 16,
			10, 7, 20, 130,
			BlockMatcher.forBlock(Blocks.STONE)
		);

		this.addOreSpawn(
			RezolveMod.METAL_ORE_BLOCK.getStateFor(Metal.TIN),
			world, random, chunkX, chunkZ, 16, 16,
			10, 6, 20, 100,
			BlockMatcher.forBlock(Blocks.STONE)
		);

		this.addOreSpawn(
			RezolveMod.METAL_ORE_BLOCK.getStateFor(Metal.LEAD),
			world, random, chunkX, chunkZ, 16, 16,
			7, 5, 20, 85,
			BlockMatcher.forBlock(Blocks.STONE)
		);
	}

	private void addOreSpawn(IBlockState block, World world, Random random, int chunkXPos, int chunkZPos, int maxX, int maxZ, int maxVeinSize, int chance, int minY, int maxY, Predicate<IBlockState> blockToSpawnIn){
		int diffMinMaxY = maxY - minY;

		for (int x = 0; x < chance; x++) {
			int posX = chunkXPos * 16 + random.nextInt(maxX);
			int posY = minY + random.nextInt(diffMinMaxY);
			int posZ = chunkZPos * 16 + random.nextInt(maxZ);
			(new WorldGenMinable(block, maxVeinSize, blockToSpawnIn)).generate(world, random, new BlockPos(posX, posY, posZ));
		}
	}
}
