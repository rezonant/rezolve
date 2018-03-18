package com.astronautlabs.mc.rezolve.cities;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CityNode {

	public enum Feature {
		COMPOSITE,
		PARK,
		BUILDING,
		MAX,

		RESERVED
	}

	public enum Material {
		QUARTZ,
		STONE,
		STONE_BRICKS,

		MAX
	}

	public enum ChunkSide {
		NORTH,
		SOUTH,
		EAST,
		WEST
	}

	public static final int CITY_BLOCK_SIZE = 8;
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

	public static IBlockState[] GLASS_MATERIALS = new IBlockState[] {
			Blocks.GLASS.getDefaultState(),
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

	/**
	 * Holds a cache of the city blocks generated
	 */
	private static HashMap<ChunkPos, SoftReference<CityNode>> generatedBlocks = new HashMap<ChunkPos, SoftReference<CityNode>>();

	/**
	 * Retrieve the top-level city block for the given chunk.
	 * @param world
	 * @param x Block X position
	 * @param z Block Z position
	 * @return
	 */
	public static CityNode cityBlockFor(World world, int x, int z) {
		ChunkPos cityBlockPos = cityBlockPosFor(x, z);

		if (generatedBlocks.containsKey(cityBlockPos)) {
			CityNode cachedNode = generatedBlocks.get(cityBlockPos).get();
			if (cachedNode != null)
				return cachedNode;
		}

		long startedAt = System.currentTimeMillis();

		CityNode blockNode = new CityNode(world, cityBlockPos.chunkXPos, 0, cityBlockPos.chunkZPos, CITY_BLOCK_SIZE, 16, CITY_BLOCK_SIZE);
		blockNode.populate();

		System.out.println("("+x+", "+z+") Generated city block in "+(System.currentTimeMillis() - startedAt)+"ms");
		generatedBlocks.put(cityBlockPos, new SoftReference<CityNode>(blockNode));

		return blockNode;
	}

	/**
	 * Retrieve the leaf node for the given block position.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static CityNode nodeFor(World world, int x, int y, int z) {
		CityNode cityNode = cityBlockFor(world, x, z);
		return cityNode.getLeafFor(x, y, z);
	}

	/**
	 * Construct a new City node with the given parent
	 * @param parent
	 * @param world
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @param spanX
	 * @param spanY
	 * @param spanZ
	 */
	public CityNode(CityNode parent, World world, int chunkX, int chunkY, int chunkZ, int spanX, int spanY, int spanZ) {
		this(world, chunkX, chunkY, chunkZ, spanX, spanY, spanZ);
		this.parent = parent;
	}

	/**
	 * Construct a new City node
	 *
	 * @param world
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @param spanX
	 * @param spanY
	 * @param spanZ
	 */
	public CityNode(World world, int chunkX, int chunkY, int chunkZ, int spanX, int spanY, int spanZ) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.spanX = spanX;
		this.spanZ = spanZ;
		this.spanY = spanY;
	}

	private World world;
	private Feature feature = Feature.COMPOSITE;
	private int chunkX;
	private int chunkY;
	private int chunkZ;
	private int spanX;
	private int spanY;
	private int spanZ;
	private HashMap<String,Integer> params = new HashMap<String,Integer>();
	private ArrayList<CityNode> children = new ArrayList<CityNode>();
	private CityNode parent = null;

	public static final int BUILDING_MIN_FLOORS = 1;
	public static final int BUILDING_MAX_FLOORS = 30;
	public static final int BUILDING_FLOOR_HEIGHT = 7;

	public int getParameter(String name, int defaultValue) {
		if (this.params.containsKey(name))
			return this.params.get(name);

		return defaultValue;
	}

	public int getParameter(String name) {
		return this.getParameter(name, 0);
	}

	public boolean containsChunk(World world, int chunkX, int chunkZ) {

		if (!CityBiome.isCity(world, new ChunkPos(chunkX, chunkZ)))
			return false;
		return this.chunkX <= chunkX
			&& chunkX < this.chunkX + this.spanX
			&& this.chunkZ <= chunkZ
			&& chunkZ < this.chunkZ + this.spanZ;
	}

	public boolean connectsNorth(World world, ChunkPos chunk) {
		return this.containsChunk(world, chunk.chunkXPos, chunk.chunkZPos - 1);
	}

	public boolean connectsNorthWest(World world, ChunkPos chunk) {
		return this.containsChunk(world, chunk.chunkXPos - 1, chunk.chunkZPos - 1);
	}

	public boolean connectsNorthEast(World world, ChunkPos chunk) {
		return this.containsChunk(world, chunk.chunkXPos + 1, chunk.chunkZPos - 1);
	}

	public boolean connectsSouth(World world, ChunkPos chunk) {
		return this.containsChunk(world, chunk.chunkXPos, chunk.chunkZPos + 1);
	}
	public boolean connectsSouthEast(World world, ChunkPos chunk) {
		return this.containsChunk(world, chunk.chunkXPos + 1, chunk.chunkZPos + 1);
	}
	public boolean connectsSouthWest(World world, ChunkPos chunk) {
		return this.containsChunk(world, chunk.chunkXPos - 1, chunk.chunkZPos + 1);
	}

	public boolean connectsWest(World world, ChunkPos chunk) {
		return this.containsChunk(world,chunk.chunkXPos - 1, chunk.chunkZPos);
	}

	public boolean connectsEast(World world, ChunkPos chunk) {
		return this.containsChunk(world,chunk.chunkXPos + 1, chunk.chunkZPos);
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

					if (y > groundY) {
						//block = Blocks.AIR.getDefaultState();
					} else if (y == groundY)
						block = groundBlock;

					if (block == null)
						continue;

					BlockPos blockPos = chunk.getBlock(x, y, z);
					IBlockState currentBlock = world.getBlockState(blockPos);
					if (currentBlock.getBlock() == block)
						return;

					world.setBlockState(blockPos, block, 2);
				}
			}
		}
	}

	private void drawRoad(World world, ChunkPos chunk, int groundY) {

		boolean connectsNorth = this.connectsNorth(world, chunk);
		boolean connectsSouth = this.connectsSouth(world, chunk);
		boolean connectsEast = this.connectsEast(world, chunk);
		boolean connectsWest = this.connectsWest(world, chunk);
		boolean connectsNorthEast = this.connectsNorthEast(world, chunk);
		boolean connectsNorthWest = this.connectsNorthWest(world, chunk);
		boolean connectsSouthEast = this.connectsSouthEast(world, chunk);
		boolean connectsSouthWest = this.connectsSouthWest(world, chunk);

		for (int x = 0, maxX = 16; x < maxX; ++x) {
			for (int z = 0, maxZ = 16; z < maxZ; ++z) {
				int edgeX = (x > maxX / 2) ? maxX - 1 - x : x;
				int edgeZ = (z > maxZ / 2) ? maxZ - 1 - z : z;

				boolean xRoads = edgeX < 2;
				boolean zRoads = edgeZ < 2;

				if (connectsNorth && z < 2)
					zRoads = false;

				if (connectsSouth && z >= maxZ - 2)
					zRoads = false;

				if (connectsEast && x >= maxX - 2)
					xRoads = false;

				if (connectsWest && x < 2)
					xRoads = false;

				if (connectsNorth && connectsWest && !connectsNorthWest) {
					if (x < 2 && z < 2)
						zRoads = true;
				}
				if (connectsNorth && connectsEast && !connectsNorthEast) {
					if (x > 13 && z < 2)
						zRoads = true;
				}

				if (connectsSouth && connectsWest && !connectsSouthWest) {
					if (x < 2 && z > 13)
						zRoads = true;
				}
				if (connectsSouth && connectsEast && !connectsSouthEast) {
					if (x > 13 && z > 13)
						zRoads = true;
				}

				if (xRoads || zRoads) {
					world.setBlockState(
							chunk.getBlock(x, groundY, z),
							Blocks.STONE.getStateFromMeta(4),
							2
					);
				}
			}
		}
	}

	private void fill(World world, ChunkPos chunk, int x, int y, int z, int sx, int sy, int sz, IBlockState block) {
		for (int ix = x; ix < x + sx; ++ix) {
			for (int iz = z; iz < z + sz; ++iz) {
				for (int iy = y; iy < y + sy; ++iy) {
					world.setBlockState(chunk.getBlock(ix, iy, iz), block, 2);
				}
			}
		}
	}

	private void drawSidewalk(World world, ChunkPos chunk, int groundY) {

		boolean connectsNorth = this.connectsNorth(world, chunk);
		boolean connectsSouth = this.connectsSouth(world, chunk);
		boolean connectsEast = this.connectsEast(world, chunk);
		boolean connectsWest = this.connectsWest(world, chunk);

		boolean connectsNorthWest = this.connectsNorthWest(world, chunk);
		boolean connectsNorthEast = this.connectsNorthEast(world, chunk);
		boolean connectsSouthWest = this.connectsSouthWest(world, chunk);
		boolean connectsSouthEast = this.connectsSouthEast(world, chunk);

		// Draw the north/south sidewalks

		int swX = 2;
		int swZ = 2;
		int swSX = 12;
		int swSZ = 2;

		if (connectsWest) {
			swX = 0;
			swSX += 2;
		}

		if (connectsEast) {
			swSX += 2;
		}

		IBlockState slabBlock = Blocks.STONE_SLAB.getDefaultState();
		if (!connectsNorth)
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, slabBlock);

		// South

		swZ = 12;
		if (!connectsSouth)
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, slabBlock);


		// West
		swX = 2;
		swZ = 2;
		swSX = 2;
		swSZ = 12;

		if (connectsNorth) {
			swZ = 0;
			swSZ += 2;
		}

		if (connectsSouth) {
			swSZ += 2;
		}

		if (!connectsWest)
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, slabBlock);

		// West

		swX = 12;
		if (!connectsEast)
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, slabBlock);

		// Corners

		if (connectsNorth && connectsWest && !connectsNorthWest) {
			this.fill(world, chunk, 2, groundY + 1, 0, 2, 1, 4, slabBlock);
			this.fill(world, chunk, 0, groundY + 1, 2, 2, 1, 2, slabBlock);
		}

		if (connectsNorth && connectsEast && !connectsNorthEast) {
			this.fill(world, chunk, 12, groundY + 1, 0, 2, 1, 4, slabBlock);
			this.fill(world, chunk, 14, groundY + 1, 2, 2, 1, 2, slabBlock);
		}

		if (connectsSouth && connectsWest && !connectsSouthWest) {
			this.fill(world, chunk, 2, groundY + 1, 12, 2, 1, 4, slabBlock);
			this.fill(world, chunk, 0, groundY + 1, 12, 2, 1, 2, slabBlock);
		}

		if (connectsSouth && connectsEast && !connectsSouthEast) {
			this.fill(world, chunk, 12, groundY + 1, 12, 2, 1, 4, slabBlock);
			this.fill(world, chunk, 14, groundY + 1, 12, 2, 1, 2, slabBlock);
		}
	}

	private void drawPark(World world, ChunkPos chunk, int groundY) {
		int chunkSize = 16;
		int centerBlockX = chunkSize / 2;
		int centerBlockZ = chunkSize / 2;
		BlockPos saplingPos = chunk.getBlock(centerBlockX, groundY + 1, centerBlockZ);
		IBlockState sapling = Blocks.SAPLING.getStateFromMeta(this.getParameter("park.sapling"));
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

		Random rand = new Random();
		IGrowable growable = (IGrowable)sapling.getBlock();
		growable.grow(world, rand, saplingPos, sapling);
		growable.grow(world, rand, saplingPos, world.getBlockState(saplingPos));
	}

	public void drawBuilding(World world, ChunkPos chunk, int groundY) {

		int floors = this.getParameter("building.floors");

		boolean connectsNorth = this.connectsNorth(world, chunk);
		boolean connectsSouth = this.connectsSouth(world, chunk);
		boolean connectsEast = this.connectsEast(world, chunk);
		boolean connectsWest = this.connectsWest(world, chunk);
		boolean connectsNorthWest = this.connectsNorthWest(world, chunk);
		boolean connectsNorthEast = this.connectsNorthEast(world, chunk);
		boolean connectsSouthWest = this.connectsSouthWest(world, chunk);
		boolean connectsSouthEast = this.connectsSouthEast(world, chunk);

		boolean roundedCorners = this.getParameter("building.roundedCorners") == 1;
		boolean tallWindows = this.getParameter("building.tallWindows") == 1;
		boolean flushWindows = this.getParameter("building.flushWindows") == 1;
		int floorHeight = BUILDING_FLOOR_HEIGHT;
		int towerHeight = floors * floorHeight;

		roundedCorners = false;
		tallWindows = false;

		IBlockState buildingMaterial = BUILDING_MATERIALS[this.getParameter("building.wallMaterial")];
		IBlockState glassMaterial = GLASS_MATERIALS[this.getParameter("building.glassMaterial")];
		IBlockState floorMaterial = FLOOR_MATERIALS[this.getParameter("building.floorMaterial")];

		int sizeX = 16;
		int sizeZ = 16;

		int northWall = connectsNorth ? -1 : 4;
		int southWall = connectsSouth ? -1 : sizeZ - 1 - 4;
		int westWall = connectsWest ? -1 : 4;
		int eastWall = connectsEast ? -1 : sizeX - 1 - 4 ;

		System.out.println("World seed: "+world.getSeed());

		for (int x = 0; x < sizeX; ++x) {
			for (int z = 0; z < sizeZ; ++z) {

				if (z < northWall || (southWall >= 0 && z > southWall))
					continue;

				if (x < westWall || (eastWall >= 0 && x > eastWall))
					continue;

				int edgeX = (x > sizeX / 2) ? sizeX - 1 - x : x;
				int edgeZ = (z > sizeZ / 2) ? sizeZ - 1 - z : z;
				boolean xWall = x == westWall || x == eastWall;
				boolean zWall = z == northWall || z == southWall;

				if (connectsNorth && connectsWest && !connectsNorthWest) {
					if (x == 4 && z <= 4)
						xWall = true;
					if (z == 4 && x <= 4)
						zWall = true;

					if (z < 4 && x < 4)
						continue;
				}

				if (connectsNorth && connectsEast && !connectsNorthEast) {
					if (x == 11 && z <= 4)
						xWall = true;
					if (z == 4 && x >= 11)
						zWall = true;

					if (x > 11 && z < 4)
						continue;
				}

				if (connectsSouth && connectsWest && !connectsSouthWest) {
					if (x == 4 && z >= 11)
						xWall = true;
					if (z == 11 && x <= 4)
						zWall = true;

					if (z > 11 && x < 4)
						continue;
				}

				if (connectsSouth && connectsEast && !connectsSouthEast) {
					if (x == 11 && z >= 11)
						xWall = true;
					if (z == 11 && x >= 11)
						zWall = true;

					if (z > 11 && x > 11)
						continue;
				}

				boolean isWall = xWall || zWall;

				// Skip drawing the edges if we are the corners and rounded corners is on
				if (roundedCorners && xWall && zWall)
					xWall = zWall = false;

				// Doors

				boolean door = false;
				boolean doorFrame = false;
				boolean leftDoor = false;

				if (xWall) {
					int middle = sizeZ / 2;

					door = (middle - 1 == z || z == middle);
					leftDoor = x < 8 ? (middle - 1 == z) : middle == z;
					doorFrame = middle - 2 == z || z == middle + 1;

				} else if (zWall) {
					int middle = sizeX / 2;

					door = (middle - 1 == x || x == middle);
					doorFrame = middle - 2 == x || x == middle + 1;
					leftDoor = z < 8 ? (middle == x) : middle - 1 == x;

				}

				// Walls

				if (xWall || zWall) {
					for (int y = 0; y < towerHeight; ++y) {

						if (y >= 1 && y < 4) {
							if (door) {
								Rotation doorRot = xWall ? Rotation.CLOCKWISE_90 : Rotation.CLOCKWISE_180;

								if (xWall && x > 8) {
									doorRot = Rotation.COUNTERCLOCKWISE_90;
								} else if (xWall) {
									//leftDoor = !leftDoor;
								}

								if (zWall && z > 8) {
									doorRot = Rotation.NONE;
									// leftDoor = !leftDoor; // ?
								} else if (zWall) {
								}

								// -6042638055931638963
								IBlockState doorBlock = Blocks.DARK_OAK_DOOR
									.getDefaultState()
									.withRotation(doorRot)
									.withProperty(BlockDoor.OPEN, false)
									.withProperty(BlockDoor.HINGE,
											leftDoor ? BlockDoor.EnumHingePosition.LEFT
													: BlockDoor.EnumHingePosition.RIGHT)
								;

								if (y == 1) {
									// Block below the door
									world.setBlockState(
											chunk.getBlock(x, groundY + y, z),
											buildingMaterial,
											2
									);
								}

								if (y == 2) {
									// Lower door
									world.setBlockState(
											chunk.getBlock(x, groundY + y, z),
											doorBlock,
											2
									);
								}

								if (y == 3) {
									// Upper door
									world.setBlockState(
											chunk.getBlock(x, groundY + y, z),
											doorBlock.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER),
											2
									);
								}
								continue;
							}

							if (doorFrame) {
								world.setBlockState(
									chunk.getBlock(x, groundY + y, z),
									buildingMaterial,
									2
								);
								continue;
							}
						}

						IBlockState wallMaterial = buildingMaterial;
						boolean isWindow = false;

						int windowEdge = 1;
						if (roundedCorners)
							windowEdge = 2;

						boolean inWindowRange = xWall != zWall;

						if (tallWindows) {
							isWindow = inWindowRange && y % floorHeight != 1;
						} else {
							int floorBlock = y % floorHeight;
							isWindow = inWindowRange && floorBlock >= 2 && floorBlock < floorHeight - 1;
						}

						if (isWindow) {
							// Window!
							wallMaterial = glassMaterial;
						}

						world.setBlockState(
								chunk.getBlock(x, groundY + y, z),
								wallMaterial,
								2
						);
					}
				}

				if (isWall)
					continue;

				// Everything below is building interior.
				// We've already established that this X/Z coordinate is part of the
				// building after considering connections with other chunks, so draw
				// away.

				// Handle floors

				if (false && tallWindows && (edgeX == 0 || edgeZ == 0)) {
					world.setBlockState(
							chunk.getBlock(x, groundY - 1, z),
							floorMaterial,
							2
					);
				}

				int floor = 1;

				for (int y = 0; y < towerHeight; y += floorHeight) {
					world.setBlockState(
							chunk.getBlock(x, groundY + y, z),
							floorMaterial,
							2
					);
				}
			}
		}
	}

	public void drawChunk(World world, ChunkPos chunk) {

		int groundY = world.getSeaLevel() + 2;


		switch (feature) {
			case BUILDING:
				this.drawRoad(world, chunk, groundY);
				this.drawSidewalk(world, chunk, groundY);
				this.drawBuilding(world, chunk, groundY);
				break;
			case PARK:
				this.prepareSpace(world, chunk, groundY, Blocks.GRASS.getDefaultState());
				this.drawRoad(world, chunk, groundY);
				this.drawSidewalk(world, chunk, groundY);
				this.drawPark(world, chunk, groundY);
				break;
			default:
				this.prepareSpace(world, chunk, groundY, Blocks.GRASS.getDefaultState());
				System.out.println("ERROR: Cannot draw chunk for feature "+feature);
		}

	}

	public HashMap<String,Integer> getParameters() {
		return (HashMap<String,Integer>)this.params.clone();
	}

	public int getOriginBlockX() {
		return this.getOriginX()*16;
	}

	public int getOriginBlockY() {
		return this.getOriginY()*16;
	}

	public int getOriginBlockZ() {
		return this.getOriginZ()*16;
	}

	public int getSpanBlockX() {
		return this.getSpanX()*16;
	}

	public int getSpanBlockY() {
		return this.getSpanY()*16;
	}

	public int getSpanBlockZ() {
		return this.getSpanZ()*16;
	}

	public boolean containsBlock(int x, int y, int z) {
		return this.getOriginBlockX() <= x
				&& x < this.getOriginBlockX() + this.getSpanBlockX()
				&& this.getOriginBlockZ() <= z
				&& z < this.getOriginBlockZ() + this.getSpanBlockZ();
	}

	public CityNode getChildFor(int x, int y, int z) {
		if (!this.containsBlock(x, y, z))
			return null;

		for (CityNode n : this.children) {
			if (n.containsBlock(x, y, z))
				return n;
		}

		return null;
	}

	public CityNode getLeafFor(int x, int y, int z) {
		if (!this.containsBlock(x, y, z))
			return null;

		if (this.children.size() == 0)
			return this;

		CityNode child = this.getChildFor(x, y, z);

		if (child == null) {
			System.out.println("ERROR: All inner space should be accounted for in this node, but it is not.");
			this.trace();
		}
		return child.getLeafFor(x, y, z);
	}

	/**
	 * Convert block X/Z position to a chunk position which represents the top-level city block
	 * (usually 8x8 chunks).
	 *
	 * @param x
	 * @param z
	 * @return
	 */
	public static ChunkPos cityBlockPosFor(int x, int z) {
		int chunkX = (int)Math.floor(x / 16.0f);
		int chunkZ = (int)Math.floor(z / 16.0f);

		int blockX = (int)(Math.floor(chunkX / (float)CITY_BLOCK_SIZE) * CITY_BLOCK_SIZE);
		int blockZ = (int)(Math.floor(chunkZ / (float)CITY_BLOCK_SIZE) * CITY_BLOCK_SIZE);

		return new ChunkPos(blockX, blockZ);
	}

	/**
	 * Retrieve the top-level city block for this node.
	 * @return
	 */
	public CityNode rootNode() {
		if (this.parent == null)
			return this;

		return this.parent.rootNode();
	}

	/**
	 * Put debug information about this node into the given string list.
	 * @param output
	 */
	public void trace(List<String> output) {
		this.trace(0, output);
	}

	/**
	 * Trace debug information about this node to System.out.
	 */
	public void trace() {
		ArrayList<String> output = new ArrayList<String>();

		this.trace(output);

		for (String line : output)
			System.out.println(line);
	}

	/**
	 * Trace debug information about this node to the given output list,
	 * given that this node is the given amount of depth levels into a larger
	 * trace (controls indent).
	 *
	 * @param depth
	 * @param output
	 */
	protected void trace(int depth, List<String> output) {
		indentPrint(depth, output, String.format("%s [%d, %d, %d]", this.getFeature().toString(), this.spanX, this.spanY, this.spanZ));
		for (CityNode child : children) {
			child.trace(depth + 1, output);
		}
	}

	/**
	 * Print an indented line into the given string list.
	 * @param indent
	 * @param output
	 * @param message
	 */
	private void indentPrint(int indent, List<String> output, String message) {
		String indentStr = "";
		for (int i = 0; i < indent; ++i)
			indentStr += "  ";

		output.add(indentStr+message);
	}

	/**
	 * Get a random source that is seeded specific to this node
	 * @return
	 */
	public Random randomSource() {
		return new Random(world.getSeed() + chunkX * 13 + chunkY * 19 + chunkZ * 31 + spanX * 17 + spanY * 19 + spanZ * 37);
	}

	/**
	 * Return an array of the child nodes for this node.
	 * @return
	 */
	public CityNode[] getChildren() {
		return this.children.toArray(new CityNode[this.children.size()]);
	}

	/**
	 * Populate this node with child nodes or a feature identity.
	 */
	public void populate() {

		Random random = this.randomSource();
		int subdivide = random.nextInt(100);

		float spanFactor = (spanX + spanZ) / 16.0f;
		float subdivideWeight = spanFactor * 0.9f;

		boolean subdividePrimary = subdivide < 80 * subdivideWeight;
		boolean subdivideSecondary = subdivide >= 80 * subdivideWeight && subdivide < 100 * subdivideWeight;
		boolean subdivideX;
		boolean subdivideZ;

		if (spanZ > spanX) {
			subdivideZ = subdividePrimary;
			subdivideX = subdivideSecondary;
		} else if (spanX > spanZ) {
			subdivideX = subdividePrimary;
			subdivideZ = subdivideSecondary;
		} else {
			subdivideX = subdivide < 50 * subdivideWeight;
			subdivideZ = subdivide >= 50 * subdivideWeight && subdivide < 100 * subdivideWeight;
		}

		if (subdivideX && this.spanX > 1) {
			int minSplit = Math.max(1, this.spanX / 2 - 1);
			int maxSplit = Math.min(this.spanX - 1, this.spanX / 2 + 1);

			// Subdivide vertically into 2 features
			int node1Width = minSplit + (maxSplit - minSplit <= 0 ? 0 : random.nextInt(maxSplit - minSplit));
			this.children.add(new CityNode(this, this.world, chunkX, chunkY, chunkZ, node1Width, this.spanY, this.spanZ));
			this.children.add(new CityNode(this, this.world, chunkX + node1Width, chunkY, chunkZ, this.spanX - node1Width, this.spanY, this.spanZ));

		} else if (subdivideZ && this.spanZ > 1) {
			int minSplit = Math.max(1, this.spanZ / 2 - 1);
			int maxSplit = Math.min(this.spanZ - 1, this.spanZ / 2 + 1);

			// Subdivide horizontally
			int node1Width = minSplit + (maxSplit - minSplit <= 0 ? 0 : random.nextInt(maxSplit - minSplit));
			this.children.add(new CityNode(this, this.world, chunkX, chunkY, chunkZ, this.spanX, this.spanY, node1Width));
			this.children.add(new CityNode(this, this.world, chunkX, chunkY, chunkZ + node1Width, this.spanX, this.spanY, this.spanZ - node1Width));
		} else {
			// Can't subdivide or chose not to. Assign this node a real feature

			this.feature = Feature.values()[1 + random.nextInt(Feature.MAX.ordinal() - 1)];
			this.populateParameters(random);
		}

		for (CityNode child : children)
			child.populate();

	}

	/**
	 * Populate the parameters of this node based on it's feature and the given seeded random source.
	 * @param random
	 */
	private void populateParameters(Random random) {
		switch (this.feature) {
			case BUILDING:

				int minFloors = BUILDING_MIN_FLOORS;
				int maxFloors = BUILDING_MAX_FLOORS;

				int area = this.spanX * this.spanZ;

				if (area == 1) {
					minFloors = 1;
					maxFloors = 4;
				} else if (area <= 4) {
					minFloors = 2;
					maxFloors = 6;
				} else if (area < 9) {
					minFloors = 4;
					maxFloors = 10;
				} else {
					minFloors = 6;
				}

				this.params.put("building.floors", minFloors + random.nextInt(maxFloors - minFloors));
				this.params.put("building.floorMaterial", random.nextInt(FLOOR_MATERIALS.length));
				this.params.put("building.wallMaterial", random.nextInt(BUILDING_MATERIALS.length));
				this.params.put("building.glassMaterial", random.nextInt(GLASS_MATERIALS.length));

				this.params.put("building.roundedCorners", random.nextBoolean() ? 1 : 0);
				this.params.put("building.tallWindows", random.nextBoolean() ? 1 : 0);
				this.params.put("building.flushWindows", random.nextBoolean() ? 1 : 0);

				break;
			case PARK:
				this.params.put("park.sapling", random.nextInt(6));

				break;
		}
	}

	public int getOriginX() {
		return this.chunkX;
	}

	public int getOriginY() {
		return this.chunkY;
	}

	public int getOriginZ() {
		return this.chunkZ;
	}

	public int getSpanX() {
		return this.spanX;
	}

	public int getSpanY() {
		return this.spanY;
	}

	public int getSpanZ() {
		return this.spanZ;
	}

	public Feature getFeature() {
		return this.feature;
	}
}
