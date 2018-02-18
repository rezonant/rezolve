package com.astronautlabs.mc.rezolve.cities;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.google.common.collect.Lists;

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.lwjgl.util.vector.Vector2f;

public class CityGenerator implements IWorldGenerator {

	public Chunk[] getNeighborCities(World world, int chunkX, int chunkZ) {
		ArrayList<Chunk> neighborCities = new ArrayList<Chunk>();
		Chunk[] neighbors = new Chunk[] {
			world.getChunkProvider().getLoadedChunk(chunkX + 1, chunkZ),
			world.getChunkProvider().getLoadedChunk(chunkX + 2, chunkZ),
			world.getChunkProvider().getLoadedChunk(chunkX - 1, chunkZ),
			world.getChunkProvider().getLoadedChunk(chunkX - 2, chunkZ),
			world.getChunkProvider().getLoadedChunk(chunkX, chunkZ + 1),
			world.getChunkProvider().getLoadedChunk(chunkX, chunkZ + 2),
			world.getChunkProvider().getLoadedChunk(chunkX, chunkZ - 1),
			world.getChunkProvider().getLoadedChunk(chunkX, chunkZ - 2),
			world.getChunkProvider().getLoadedChunk(chunkX + 1, chunkZ + 1),
			world.getChunkProvider().getLoadedChunk(chunkX + 2, chunkZ + 2),
			world.getChunkProvider().getLoadedChunk(chunkX - 1, chunkZ - 1),
			world.getChunkProvider().getLoadedChunk(chunkX - 2, chunkZ - 2),
			world.getChunkProvider().getLoadedChunk(chunkX + 1, chunkZ - 1),
			world.getChunkProvider().getLoadedChunk(chunkX + 2, chunkZ - 2),
			world.getChunkProvider().getLoadedChunk(chunkX - 1, chunkZ + 1),
			world.getChunkProvider().getLoadedChunk(chunkX - 2, chunkZ + 2),
		};
		
		for (Chunk neighbor : neighbors) {
			if (neighbor == null)
				continue;
			
			Biome biome = world.getBiome(new BlockPos(neighbor.xPosition * 16 + 8, 128, neighbor.zPosition * 16 + 8));
			if (biome == RezolveMod.CITY_BIOME)
				neighborCities.add(neighbor);
		}
		
		return neighborCities.toArray(new Chunk[neighborCities.size()]);
	}

	public static IBlockState[] BUILDING_MATERIALS = new IBlockState[] {
		Blocks.QUARTZ_BLOCK.getDefaultState(),		// quartz
		Blocks.QUARTZ_BLOCK.getStateFromMeta(2),	// quartz pillar
		Blocks.QUARTZ_BLOCK.getStateFromMeta(1),	// chiseled quartz
		Blocks.STONE.getStateFromMeta(6),			// polished andesite
		Blocks.STONE.getStateFromMeta(4),			// polished diorite
		Blocks.STONE.getStateFromMeta(2),			// polished granite
		Blocks.STONE.getStateFromMeta(1),			// granite
		Blocks.BRICK_BLOCK.getDefaultState(),		// stone bricks
		Blocks.BONE_BLOCK.getDefaultState(),		// bone block
	};

	public static IBlockState[] FLUSH_GLASS_MATERIALS = new IBlockState[] {
		Blocks.GLASS.getDefaultState()
	};
	
	public static IBlockState[] GLASS_PANE_MATERIALS = new IBlockState[] {
		Blocks.GLASS_PANE.getDefaultState()
	};
	
	public static IBlockState[] FLOOR_MATERIALS = new IBlockState[] {
		Blocks.PLANKS.getDefaultState(),
		Blocks.PLANKS.getStateFromMeta(1),
		Blocks.PLANKS.getStateFromMeta(2),
		Blocks.PLANKS.getStateFromMeta(3),
		Blocks.PLANKS.getStateFromMeta(4),
		Blocks.PLANKS.getStateFromMeta(5),
			
		Blocks.WOOL.getDefaultState(),
		Blocks.WOOL.getStateFromMeta(1),
		Blocks.WOOL.getStateFromMeta(2),
		Blocks.WOOL.getStateFromMeta(3),
		Blocks.WOOL.getStateFromMeta(4),
		Blocks.WOOL.getStateFromMeta(5),
		Blocks.WOOL.getStateFromMeta(6),
		Blocks.WOOL.getStateFromMeta(7),	
		Blocks.WOOL.getStateFromMeta(8),
		Blocks.WOOL.getStateFromMeta(9),
		Blocks.WOOL.getStateFromMeta(10),
		Blocks.WOOL.getStateFromMeta(11),
		Blocks.WOOL.getStateFromMeta(12),
		Blocks.WOOL.getStateFromMeta(13),
		Blocks.WOOL.getStateFromMeta(14),
		Blocks.WOOL.getStateFromMeta(15),

		Blocks.STONE.getStateFromMeta(2), // polished granite
		Blocks.STONE.getStateFromMeta(4), // polished diorite
		Blocks.STONE.getStateFromMeta(6), // polished andesite
	};
	
	public void generateTower(Random random, World world, FeatureGenerator.Shape shape, ChunkPos chunk, int groundY, int floors) {

		boolean roundedCorners = random.nextBoolean();
		boolean tallWindows = random.nextBoolean();
		boolean flushWindows = random.nextBoolean();
		
		int floorHeight = 4;
		int towerHeight = floors * 4;

		IBlockState buildingMaterial = BUILDING_MATERIALS[random.nextInt(BUILDING_MATERIALS.length)];
		IBlockState glassMaterial;
		IBlockState floorMaterial = FLOOR_MATERIALS[random.nextInt(FLOOR_MATERIALS.length)];
		
		if (flushWindows) {
			glassMaterial = FLUSH_GLASS_MATERIALS[random.nextInt(FLUSH_GLASS_MATERIALS.length)];
		} else {
			glassMaterial = GLASS_PANE_MATERIALS[random.nextInt(GLASS_PANE_MATERIALS.length)];
		}

		ChunkPos globalChunk = shape.localToGlobal(chunk);
		int sizeX = 16;
		int sizeZ = 16;
		boolean connectsNorth = shape.connectsNorth(chunk);
		boolean connectsSouth = shape.connectsSouth(chunk);
		boolean connectsEast = shape.connectsEast(chunk);
		boolean connectsWest = shape.connectsWest(chunk);

		for (int x = 0; x < sizeX; ++x) {
			for (int z = 0; z < sizeZ; ++z) {

				int edgeX = (x > sizeX / 2) ? sizeX - 1 - x : x;
				int edgeZ = (z > sizeZ / 2) ? sizeZ - 1 - z : z;
				boolean xWall = edgeX == 4;
				boolean zWall = edgeZ == 4;

				// Skip drawing the edges if we are the corners and rounded corners is on
				if (roundedCorners && xWall && zWall)
					xWall = zWall = false;

				if (z < 8 && connectsNorth)
					zWall = false;

				if (z > 8 && connectsSouth)
					zWall = false;

				if (x < 8 && connectsWest)
					xWall = false;

				if (x > 8 && connectsEast)
					xWall = false;

				if (xWall || zWall) {
					for (int y = 0; y < towerHeight; ++y) {
						
						IBlockState wallMaterial = buildingMaterial;
						boolean isWindow = false;
						
						int windowEdge = 1;
						if (roundedCorners)
							windowEdge = 2;
						
						if (tallWindows) {
							isWindow = ((edgeX >= windowEdge || edgeZ >= windowEdge) && y % floorHeight != floorHeight - 1);
						} else {
							int floorBlock = y % floorHeight;
							isWindow = ((edgeX >= windowEdge || edgeZ >= windowEdge) && floorBlock >= 1 && floorBlock < floorHeight - 2);
						}
						
						if (isWindow) {
							// Window!
							wallMaterial = glassMaterial;
						}
						
						world.setBlockState(
							globalChunk.getBlock(x, y, z),
							wallMaterial,
							2
						);
					}
				}
				
				// Handle floors 

				
				if (tallWindows && (edgeX == 0 || edgeZ == 0)) {
					world.setBlockState(
						globalChunk.getBlock(x, groundY - 1, z),
						floorMaterial,
						2
					);
				} 
				
				if (edgeX > 0 && edgeZ > 0) {
					for (int y = -1; y < towerHeight; y += floorHeight) {
						world.setBlockState(
							globalChunk.getBlock(x, groundY + y, z),
							floorMaterial,
							2
						);
					}
				}
				
			}
		}
	}
	
	public boolean spaceForMegaStructureAt(World world, int chunkX, int chunkZ, int chunkSizeX, int chunkSizeZ) {
		for (int x = chunkX, maxX = chunkX + chunkSizeX; x < maxX; ++x) {
			for (int z = chunkZ, maxZ = chunkZ + chunkSizeZ; z < maxZ; ++z) {
				if (this.isSpaceUsed(world, x, z))
					return false;
			}
		}
		
		return true;
	}
	
	public int[] findSpaceForMegaStructure(World world, int chunkX, int chunkZ, int chunkSizeX, int chunkSizeZ) {
		int[][] possibilities = new int[][] {
			new int[] { chunkX, chunkZ },
			new int[] { chunkX + 1, chunkZ },
			new int[] { chunkX, chunkZ + 1 },
			new int[] { chunkX - 1, chunkZ },
			new int[] { chunkX, chunkZ - 1 },
			new int[] { chunkX + 1, chunkZ + 1 },
			new int[] { chunkX - 1, chunkZ - 1 },
			new int[] { chunkX, chunkZ - 2 },
			new int[] { chunkX - 2, chunkZ },
			new int[] { chunkX - 2, chunkZ - 2 },
			new int[] { chunkX, chunkZ - 3 },
			new int[] { chunkX - 3, chunkZ },
			new int[] { chunkX - 3, chunkZ - 3 },
		};
		
		for (int[] possibility : possibilities) {
			int potentialChunkX = possibility[0];
			int potentialChunkZ = possibility[1];
			
			this.spaceForMegaStructureAt(world, potentialChunkX, potentialChunkZ, chunkSizeX, chunkSizeZ);
			if (this.spaceForMegaStructureAt(world, potentialChunkX, potentialChunkZ, chunkSizeX, chunkSizeZ))
				return possibility;
		}
		
		return null;
	}

	public void markSpaceUsed(World world, int chunkX, int chunkZ, int chunkCount) {
		markSpaceUsed(world, chunkX, chunkZ, chunkCount, chunkCount);
	}
	
	public void markSpaceUsed(World world, int chunkX, int chunkZ, int chunkSizeX, int chunkSizeZ) {
		for (int x = chunkX, maxX = chunkX + chunkSizeX; x < maxX; ++x) {
			for (int z = chunkZ, maxZ = chunkZ + chunkSizeZ; z < maxZ; ++z) {
				this.markSpaceUsed(world, x, z);
			}
		}
	}
	
	public void markSpaceUsed(World world, int chunkX, int chunkZ) {
		world.setBlockState(
			new BlockPos(chunkX * 16, 0, chunkZ * 16), 
			Blocks.REDSTONE_BLOCK.getDefaultState(),
			2
		);
	}

	public boolean isSpaceUsed(World world, int chunkX, int chunkZ) {

		BlockPos indicatorBlockPos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
		IBlockState indicatorBlock = world.getBlockState(indicatorBlockPos);
		if (indicatorBlock.getBlock() == Blocks.REDSTONE_BLOCK)
			return true;
		
		return false;
	}
	
	public class GeneratedItem extends WeightedRandom.Item {

		public GeneratedItem(int itemWeightIn, int key) {
			super(itemWeightIn);
			this.key = key;
		}
		
		public int key;
		
	}
	
	private void drawStandardRoads(World world, FeatureGenerator.Shape shape, ChunkPos chunk, int groundY) {

		// Draw roads

		ChunkPos globalChunk = shape.localToGlobal(chunk);
		boolean connectNorth = shape.connectsNorth(chunk);
		boolean connectSouth = shape.connectsSouth(chunk);
		boolean connectEast = shape.connectsEast(chunk);
		boolean connectWest = shape.connectsWest(chunk);

		for (int x = 0, maxX = 16; x < maxX; ++x) {
			for (int z = 0, maxZ = 16; z < maxZ; ++z) {
				int edgeX = (x > maxX / 2) ? maxX - 1 - x : x;
				int edgeZ = (z > maxZ / 2) ? maxZ - 1 - z : z;

				boolean xRoads = edgeX < 2;
				boolean zRoads = edgeZ < 2;

				if (z < 2 && connectNorth)
					zRoads = false;

				if (z >= maxZ - 2 && connectSouth)
					zRoads = false;

				if (x >= maxX - 2 && connectEast)
					xRoads = false;

				if (x < 2 && connectWest)
					xRoads = false;

				if (xRoads || zRoads) {
					world.setBlockState(
						globalChunk.getBlock(x, groundY, z),
						Blocks.STONE.getStateFromMeta(4),
						2
					);
				}
			}
		}
	}
	
	private void drawStandardSidewalks(World world, FeatureGenerator.Shape shape, ChunkPos chunk, int groundY) {

		// Draw the sidewalks

		ChunkPos globalChunk = shape.localToGlobal(chunk);
		boolean connectNorth = shape.connectsNorth(chunk);
		boolean connectSouth = shape.connectsSouth(chunk);
		boolean connectEast = shape.connectsEast(chunk);
		boolean connectWest = shape.connectsWest(chunk);

		for (int x = 0, maxX = 16; x < maxX; ++x) {
			for (int z = 0, maxZ = 16; z < maxZ; ++z) {
				int edgeX = x > 8 ? 16 - 1 - x : x;
				int edgeZ = z > 8 ? 16 - 1 - z : z;

				boolean xSidewalks = edgeX >= 2 && edgeX < 4;
				boolean zSidewalks = edgeZ >= 2 && edgeZ < 4;
				if (edgeX < 2 || edgeZ < 2)
					continue;

				if (edgeX >= 4 && edgeZ >= 4)
					continue;

				if (z < 5 && connectNorth)
					zSidewalks = false;

				if (z >= maxZ - 5 && connectSouth)
					zSidewalks = false;

				if (x >= maxX - 5 && connectEast)
					xSidewalks = false;

				if (x < 5 && connectWest)
					xSidewalks = false;

				if (xSidewalks || zSidewalks) {
					world.setBlockState(
						globalChunk.getBlock(x, groundY + 1, z),
						Blocks.STONE_SLAB.getDefaultState(),
						2
					);
				}
			}
		}

	}

	private void prepareSpace(World world, ChunkPos chunk, int groundY, IBlockState groundBlock) {
		this.prepareSpace(world, chunk, groundY, groundBlock, Blocks.STONE.getDefaultState());
	}
	
	private void prepareSpace(World world, ChunkPos chunk, int groundY, IBlockState groundBlock, IBlockState undergroundBlock) {
		// Flatten that shit.

		int chunkSize = 16;

		for (int x = 0, maxX = chunkSize; x < maxX; ++x) {
			for (int z = 0, maxZ = chunkSize; z < maxZ; ++z) {
				for (int y = 0, yMax = 255; y < yMax; ++y) {
					IBlockState block = null;

					if (y > groundY)
						block = Blocks.AIR.getDefaultState();
					else if (y == groundY) 
						block = groundBlock;
					else if (y > groundY - 4 && y > 1)
						block = undergroundBlock;

					if (block != null)
						world.setBlockState(chunk.getBlock(x, y, z), undergroundBlock, 2);
				}
			}
		}
	}

	private void generatePark(Random rand, FeatureGenerator.Shape shape, ChunkPos chunk, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider, int groundY) {

		//int groundY = world.getSeaLevel() + 2;
		int chunkSize = 16;

		this.prepareSpace(world,
			shape.localToGlobal(chunk),
			groundY,
			Blocks.GRASS.getDefaultState()
		);

		this.drawStandardRoads(world, shape, chunk, groundY);
		this.drawStandardSidewalks(world, shape, chunk, groundY);
		
		// If we're 1x1 chunk...

		ChunkPos globalChunk = shape.localToGlobal(chunk);

		GeneratedItem chosenItem = WeightedRandom.getRandomItem(rand, PARK_FEATURES);
		if (chosenItem.key == TREE_FEATURE ) {

			int centerBlockX = chunkSize / 2;
			int centerBlockZ = chunkSize / 2;
			BlockPos saplingPos = globalChunk.getBlock(centerBlockX, groundY + 1, centerBlockZ);
			IBlockState sapling = Blocks.SAPLING.getStateFromMeta(rand.nextInt(6));
			world.setBlockState(
				saplingPos,
				sapling,
				2
			);

			int meta = Blocks.SAPLING.getMetaFromState(sapling);
			if (sapling.getBlock() == Blocks.SAPLING && meta == 5) {
				// Dark oak needs 4 saplings
				world.setBlockState(new BlockPos(saplingPos.getX() + 1, saplingPos.getY(), saplingPos.getZ()), sapling, 2);
				world.setBlockState(new BlockPos(saplingPos.getX(), saplingPos.getY(), saplingPos.getZ() + 1), sapling, 2);
				world.setBlockState(new BlockPos(saplingPos.getX() + 1, saplingPos.getY(), saplingPos.getZ() + 1), sapling, 2);
			}

			IGrowable growable = (IGrowable)sapling.getBlock();
			growable.grow(world, rand, saplingPos, sapling);
			growable.grow(world, rand, saplingPos, world.getBlockState(saplingPos));
			//growable.grow(world, rand, saplingPos, world.getBlockState(saplingPos));

		} else if ("pumpkins".equals(chosenItem.key)) {

			int centerBlockX = chunkSize / 2;
			int centerBlockZ = chunkSize / 2;
			BlockPos saplingPos = globalChunk.getBlock(centerBlockX, groundY, centerBlockZ);
			IBlockState sapling = Blocks.SAPLING.getStateFromMeta(rand.nextInt(6));
			world.setBlockState(
				saplingPos,
				sapling,
				2
			);

			IGrowable growable = (IGrowable)sapling.getBlock();
			growable.grow(world, rand, saplingPos, sapling);
		}
		
	}
	
	private void generateBuilding(
		Random rand, FeatureGenerator.Shape shape, ChunkPos chunk, int groundY, int towerFloors,
		World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) 
	{

		ChunkPos globalChunk = shape.localToGlobal(chunk);

		this.prepareSpace(world, globalChunk, groundY, Blocks.AIR.getDefaultState());
		this.drawStandardRoads(world, shape, chunk, groundY);
		this.drawStandardSidewalks(world, shape, chunk, groundY);
	
		// Draw the tower	
		this.generateTower(
			rand, world,
			shape, chunk,
			groundY + 1,
			towerFloors
		);
		
	}

	private static final int PARK_FEATURE = 0;
	private static final int BUILDING_FEATURE = 1;
	
	// Park Features
	private static final int TREE_FEATURE = 2; 
	
	private final ArrayList<GeneratedItem> FEATURES = Lists.newArrayList(new GeneratedItem[] {
		new GeneratedItem(3, PARK_FEATURE),
		new GeneratedItem(10, BUILDING_FEATURE)			
	});
	
	private final ArrayList<GeneratedItem> PARK_FEATURES = Lists.newArrayList(new GeneratedItem[] {
		//new GeneratedItem(10, "pumpkins"),
		new GeneratedItem(10, TREE_FEATURE)
	});
	
	private final static Object GENERATION_LOCK = new Object();

	private final static FeatureGenerator FEATURE_GENERATOR = new FeatureGenerator(3, 3, 1);

	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider) {

		//if (this.isSpaceUsed(world, chunkX, chunkZ))
		//	return;

		boolean isCity = false;

		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunkX * 16 + 8, 128, chunkZ * 16 + 8)))
			isCity = true;

		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunkX * 16 + 1, 128, chunkZ * 16 + 1)))
			isCity = true;
		
		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunkX * 16 + 15, 128, chunkZ * 16 + 15)))
			isCity = true;
		
		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunkX * 16 + 15, 128, chunkZ * 16 + 1)))
			isCity = true;
		
		if (!isCity && RezolveMod.CITY_BIOME == world.getBiome(new BlockPos(chunkX * 16 + 1, 128, chunkZ * 16 + 15)))
			isCity = true;
		
		if (!isCity)
			return;


		FeatureGenerator.Shape shape = FEATURE_GENERATOR.getShape(world, chunkX, chunkZ);
		int groundY = world.getSeaLevel() + 2;
		
		int chunkSize = 16;
		int minFloors = 3;
		int maxFloors = 8;

		int[] superposition = null;
		
		if (shape.feature == PARK_FEATURE) {
			
			if (rand.nextInt(100) > 60) {
				superposition = this.findSpaceForMegaStructure(world, chunkX, chunkZ, 2, 2);
			}

		} else if (shape.feature == BUILDING_FEATURE) {
			Chunk[] neighborCities = this.getNeighborCities(world, chunkX, chunkZ);
			int megaBuildingChance = 30;
			
			if (neighborCities.length <= 5) {
				minFloors = 2;
				maxFloors = 4;
			} else if (neighborCities.length <= 8) {
				minFloors = 4;
				maxFloors = 7;
				megaBuildingChance = 40;
			} else if (neighborCities.length <= 11) {
				minFloors = 8;
				maxFloors = 12;
				megaBuildingChance = 50;
			} else {
				minFloors = 9;
				maxFloors = 15;
				megaBuildingChance = 70;
			}

			if (rand.nextInt(100) > megaBuildingChance) {
				superposition = this.findSpaceForMegaStructure(world, chunkX, chunkZ, 2, 2);
			}
		}

		synchronized (GENERATION_LOCK) {
			if (superposition != null) {
				if (this.spaceForMegaStructureAt(world, superposition[0], superposition[1], 2, 2)) {
					chunkX = superposition[0];
					chunkZ = superposition[1];
					minFloors = 15;
					maxFloors = 20;
					chunkSize = 32;	
				} else {
					superposition = null;
				}
			}
			
			if (superposition == null) {
				if (this.isSpaceUsed(world, chunkX, chunkZ))
					return;
			}
			
			this.markSpaceUsed(world, chunkX, chunkZ, chunkSize / 16);
		}
		
		if (shape.feature == PARK_FEATURE) {
			this.generatePark(rand, shape, chunk, world, chunkGenerator, chunkProvider);
		} else if (shape.feature == BUILDING_FEATURE) {
			int towerFloors = minFloors + rand.nextInt(maxFloors - minFloors);
			this.generateBuilding(rand, shape, chunk, groundY, towerFloors, world, chunkGenerator, chunkProvider);
		}
	}

}
