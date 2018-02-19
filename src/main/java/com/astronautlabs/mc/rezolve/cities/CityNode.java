package com.astronautlabs.mc.rezolve.cities;

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class CityNode {
	public CityNode(CityNode parent, World world, int chunkX, int chunkY, int chunkZ, int spanX, int spanY, int spanZ) {
		this(world, chunkX, chunkY, chunkZ, spanX, spanY, spanZ);
		this.parent = parent;
	}

	public CityNode(World world, int chunkX, int chunkY, int chunkZ, int spanX, int spanY, int spanZ) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.spanX = spanX;
		this.spanZ = spanZ;
		this.spanY = spanY;
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

	private World world;
	private Feature feature = Feature.COMPOSITE;
	private int chunkX;
	private int chunkY;
	private int chunkZ;
	private int spanX;
	private int spanY;
	private int spanZ;
	private HashMap<String,Integer> params = new HashMap<String,Integer>();

	public static final int BUILDING_MIN_FLOORS = 1;
	public static final int BUILDING_MAX_FLOORS = 40;
	public static final int BUILDING_FLOOR_HEIGHT = 7;

	public int getParameter(String name, int defaultValue) {
		if (this.params.containsKey(name))
			return this.params.get(name);

		return defaultValue;
	}

	public int getParameter(String name) {
		return this.getParameter(name, 0);
	}

	public boolean containsChunk(int chunkX, int chunkZ) {
		return this.chunkX <= chunkX
			&& chunkX < this.chunkX + this.spanX
			&& this.chunkZ <= chunkZ
			&& chunkZ < this.chunkZ + this.spanZ;
	}

	public boolean connectsNorth(ChunkPos chunk) {
		return this.containsChunk(chunk.chunkXPos, chunk.chunkZPos - 1);
	}

	public boolean connectsSouth(ChunkPos chunk) {
		return this.containsChunk(chunk.chunkXPos, chunk.chunkZPos + 1);
	}

	public boolean connectsWest(ChunkPos chunk) {
		return this.containsChunk(chunk.chunkXPos - 1, chunk.chunkZPos);
	}

	public boolean connectsEast(ChunkPos chunk) {
		return this.containsChunk(chunk.chunkXPos + 1, chunk.chunkZPos);
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
						block = Blocks.AIR.getDefaultState();
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

		boolean connectsNorth = this.connectsNorth(chunk);
		boolean connectsSouth = this.connectsSouth(chunk);
		boolean connectsEast = this.connectsEast(chunk);
		boolean connectsWest = this.connectsWest(chunk);

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

		boolean connectsNorth = this.connectsNorth(chunk);
		boolean connectsSouth = this.connectsSouth(chunk);
		boolean connectsEast = this.connectsEast(chunk);
		boolean connectsWest = this.connectsWest(chunk);

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

		if (!connectsNorth)
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, Blocks.STONE_SLAB.getDefaultState());

		// South

		swZ = 12;
		if (!connectsSouth)
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, Blocks.STONE_SLAB.getDefaultState());


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
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, Blocks.STONE_SLAB.getDefaultState());

		// West

		swX = 12;
		if (!connectsEast)
			this.fill(world, chunk, swX, groundY + 1, swZ, swSX, 1, swSZ, Blocks.STONE_SLAB.getDefaultState());

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

		boolean connectsNorth = this.connectsNorth(chunk);
		boolean connectsSouth = this.connectsSouth(chunk);
		boolean connectsEast = this.connectsEast(chunk);
		boolean connectsWest = this.connectsWest(chunk);

		boolean roundedCorners = this.getParameter("building.roundedCorners") == 1;
		boolean tallWindows = this.getParameter("building.tallWindows") == 1;
		boolean flushWindows = this.getParameter("building.flushWindows") == 1;
		int floorHeight = 4;
		int towerHeight = floors * 4;

		roundedCorners = false;
		tallWindows = false;

		IBlockState buildingMaterial = BUILDING_MATERIALS[this.getParameter("building.wallMaterial")];
		IBlockState glassMaterial = GLASS_MATERIALS[this.getParameter("building.glassMaterial")];
		IBlockState floorMaterial = FLOOR_MATERIALS[this.getParameter("building.floorMaterial")];

		int sizeX = 16;
		int sizeZ = 16;

		int southWall = connectsSouth ? -1 : 4;
		int northWall = connectsNorth ? -1 : 4;
		int westWall = connectsWest ? -1 : 4;
		int eastWall = connectsEast ? -1 : 4;

		for (int x = 0; x < sizeX; ++x) {
			for (int z = 0; z < sizeZ; ++z) {

				if (z < northWall || z >= sizeZ - southWall)
					continue;

				if (x < westWall || x >= sizeX - eastWall)
					continue;

				int edgeX = (x > sizeX / 2) ? sizeX - 1 - x : x;
				int edgeZ = (z > sizeZ / 2) ? sizeZ - 1 - z : z;
				boolean xWall = edgeX == westWall || edgeX == eastWall;
				boolean zWall = edgeZ == northWall || edgeZ == southWall;
				boolean isWall = xWall || zWall;

				// Skip drawing the edges if we are the corners and rounded corners is on
				if (roundedCorners && xWall && zWall)
					xWall = zWall = false;

				if (xWall || zWall) {
					for (int y = 0; y < towerHeight; ++y) {

						IBlockState wallMaterial = buildingMaterial;
						boolean isWindow = false;

						int windowEdge = 1;
						if (roundedCorners)
							windowEdge = 2;

						boolean inWindowRange = xWall != zWall;

						if (tallWindows) {
							isWindow = inWindowRange && y % floorHeight != floorHeight - 1;
						} else {
							int floorBlock = y % floorHeight;
							isWindow = inWindowRange && floorBlock >= 1 && floorBlock < floorHeight - 2;
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

				// Handle floors


				if (false && tallWindows && (edgeX == 0 || edgeZ == 0)) {
					world.setBlockState(
							chunk.getBlock(x, groundY - 1, z),
							floorMaterial,
							2
					);
				}

				for (int y = -1; y < towerHeight; y += floorHeight) {
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
				this.prepareSpace(world, chunk, groundY, Blocks.STONE.getDefaultState());
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

	public enum Feature {
		COMPOSITE,
		PARK,
		BUILDING,
		MAX
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

	private ArrayList<CityNode> children = new ArrayList<CityNode>();
	private CityNode parent = null;

	public static final int CITY_BLOCK_SIZE = 8;
	private static HashMap<ChunkPos, SoftReference<CityNode>> generatedBlocks = new HashMap<ChunkPos, SoftReference<CityNode>>();

	public static CityNode cityBlockFor(World world, int x, int z) {
		ChunkPos cityBlockPos = cityBlockPosFor(x, z);

		if (generatedBlocks.containsKey(cityBlockPos)) {
			CityNode cachedNode = generatedBlocks.get(cityBlockPos).get();
			if (cachedNode != null)
				return cachedNode;
		}

		//System.out.println("Populating city block "+cityBlockPos.chunkXPos+", "+cityBlockPos.chunkZPos);
		CityNode blockNode = new CityNode(world, cityBlockPos.chunkXPos, 0, cityBlockPos.chunkZPos, CITY_BLOCK_SIZE, 16, CITY_BLOCK_SIZE);
		blockNode.populate();

		generatedBlocks.put(cityBlockPos, new SoftReference<CityNode>(blockNode));

		return blockNode;
	}

	public static CityNode nodeFor(World world, int x, int y, int z) {
		CityNode cityNode = cityBlockFor(world, x, z);
		return cityNode.getLeafFor(x, y, z);
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

	public static ChunkPos cityBlockPosFor(int x, int z) {
		int chunkX = (int)Math.floor(x / 16.0f);
		int chunkZ = (int)Math.floor(z / 16.0f);

		int blockX = (int)(Math.floor(chunkX / (float)CITY_BLOCK_SIZE) * CITY_BLOCK_SIZE);
		int blockZ = (int)(Math.floor(chunkZ / (float)CITY_BLOCK_SIZE) * CITY_BLOCK_SIZE);

		return new ChunkPos(blockX, blockZ);
	}

	public CityNode rootNode() {
		if (this.parent == null)
			return this;

		return this.parent.rootNode();
	}

	public void trace() {
		this.trace(0);
	}

	protected void trace(int depth) {
		indentPrint(depth, String.format("%s [%d, %d, %d]", this.getFeature().toString(), this.spanX, this.spanY, this.spanZ));
		for (CityNode child : children) {
			child.trace(depth + 1);
		}
	}

	private void indentPrint(int indent, String message) {
		String indentStr = "";
		for (int i = 0; i < indent; ++i)
			indentStr += "  ";

		System.out.println(indentStr+message);
	}

	public Random randomSource() {
		return new Random(world.getSeed() + chunkX * 13 + chunkY * 19 + chunkZ * 31 + spanX * 17 + spanY * 19 + spanZ * 37);
	}

	public CityNode[] getChildren() {
		return this.children.toArray(new CityNode[this.children.size()]);
	}

	public void populate() {

		Random random = this.randomSource();
		int subdivide = random.nextInt(10);

		if (subdivide < 4 && this.spanX > 1) {
			// Subdivide vertically into 2 features
			int node1Width = random.nextInt(this.spanX - 1) + 1;
			this.children.add(new CityNode(this, this.world, chunkX, chunkY, chunkZ, node1Width, this.spanY, this.spanZ));
			this.children.add(new CityNode(this, this.world, chunkX + node1Width, chunkY, chunkZ, this.spanX - node1Width, this.spanY, this.spanZ));

		} else if (subdivide < 7 && this.spanZ > 1) {
			// Subdivide horizontally
			int node1Width = random.nextInt(this.spanZ - 1) + 1;
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

	private void populateParameters(Random random) {
		switch (this.feature) {
			case BUILDING:

				this.params.put("building.floors", BUILDING_MIN_FLOORS + random.nextInt(BUILDING_MAX_FLOORS - BUILDING_MIN_FLOORS));
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
