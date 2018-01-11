package com.astronautlabs.mc.rezolve.cities;

import java.util.ArrayList;

import net.minecraft.world.World;
import scala.util.Random;

/**
 * Randomly produces non-overlapping features of a set of defined shapes within the world space by using 
 * predictable randomness. Can be called per chunk to produce coordinated structures of any shape or size 
 * without incurring performance penalties for checking neighbors.
 * 
 * Note that features are only guaranteed to not overlap when all chunks generated use the same FeatureGenerator
 * instance. This also improves performance as the rule sets for making shapes do not need to be constructed 
 * for each chunk, and instead can be shared (see ShapeGenerator)
 *   
 * @author Liam
 *
 */
public class FeatureGenerator {
	public FeatureGenerator(int maxSizeX, int maxSizeZ, float featurePresenceThreshold) {
		this.maxSizeX = maxSizeX;
		this.maxSizeZ = maxSizeZ;
		this.featurePresenceThreshold = featurePresenceThreshold;
	}
	
	public int maxSizeX = 3;
	public int maxSizeZ = 3;
	public float featurePresenceThreshold = 1f;
	private ShapeGenerator[] shapeGenerators = null;
	
	/**
	 * Return the available set of shapes for this feature generator.
	 * @return
	 */
	public ShapeGenerator[] getGenerators() {
		if (this.shapeGenerators != null)
			return this.shapeGenerators;
		
		ArrayList<ShapeGenerator> generators = new ArrayList<ShapeGenerator>();
		
		for (int sizeX = 1; sizeX <= this.maxSizeX; ++sizeX) {
			for (int sizeZ = 1; sizeZ <= this.maxSizeZ; ++sizeZ) {
				for (int x = 0; x < this.maxSizeX; ++x) {
					for (int z = 0; z < this.maxSizeZ; ++z) {
						generators.add(new ShapeGenerator(new Shape(-x, -z, sizeX, sizeZ)));		
					}
				}
			}
		}
		
		this.shapeGenerators = generators.toArray(new ShapeGenerator[generators.size()]);
		return this.shapeGenerators;
	}
	
	/**
	 * Represents a multi-chunk shape plan that could appear on the landscape
	 * @author Liam
	 *
	 */
	public static class Shape {
		public Shape(int originX, int originZ, int sizeX, int sizeZ) {
			this.originX = originX;
			this.originZ = originZ;
			this.sizeX = sizeX;
			this.sizeZ = sizeZ;
		}
		public int originX;
		public int originZ;
		public int sizeX;
		public int sizeZ;
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Shape))
				return false;
			
			if (obj == null)
				return false;
			
			Shape shape = (Shape)obj;
			
			return (
				this.originX == shape.originX 
				&& this.originZ == shape.originZ 
				&& this.sizeX == shape.sizeX 
				&& this.sizeZ == shape.sizeZ
			);
		}
		
		/**
		 * Determine if the given chunk position is part of this shape.
		 * Default implementation is square (originX, originZ, sizeX, sizeZ),
		 * but subclasses can override this for different shapes.
		 * 
		 * @param chunkX
		 * @param chunkZ
		 * @return
		 */
		public boolean collides(int chunkX, int chunkZ) {
			return 	   chunkX >= this.originX 
					&& chunkX <  this.originX + this.sizeX 
					&& chunkZ >= this.originZ 
					&& chunkZ <  this.originZ + this.sizeZ
			;
		}
		
		public int[] getOffset(int chunkX, int chunkZ) {
			return new int[] { chunkX - this.originX, chunkZ - this.originZ };
		}
		
		public static Random seedRandom(World world, int originX, int originZ, int sizeX, int sizeZ) {
			return new Random(world.getSeed() + originX*41 + originZ*77 + sizeX*111 + sizeZ*1337);
		}
		
		public static int scoreOrigin(World world, int originX, int originZ, int sizeX, int sizeZ) {
			Random random = seedRandom(world, originX, originZ, sizeX, sizeZ);
			return random.nextInt();
		}

		public Random seed(World world, int subtype) {
			Random random = seedRandom(world, this.originX, this.originZ, this.sizeX, this.sizeZ);
			return new Random(random.nextLong() + subtype);
		}
		
		public Random seed(World world) {
			return seedRandom(world, this.originX, this.originZ, this.sizeX, this.sizeZ);
		}
		
		public int score(World world) {
			return scoreOrigin(world, originX, originZ, sizeX, sizeZ);
		}
		
		/**
		 * Get all chunk coordinates which are a part of this shape.
		 * Override to implement custom shapes.
		 * @return
		 */
		public int[][] getPieces() {
			int[][] array = new int[this.sizeX * this.sizeZ][];
			
			for (int x = 0; x < this.sizeX; ++x) {
				for (int z = 0; z < this.sizeZ; ++z) {
					array[x * this.sizeX + z] = new int[] { this.originX + x, this.originZ + z };
				}
			}
			
			return array;
		}
	}
	
	/**
	 * Represents a possible plan used for efficiently choosing a Shape for any given chunk coordinates.
	 * @author Liam
	 */
	public static class ShapeGenerator {
		public ShapeGenerator(FeatureGenerator generator, Shape prototype) {
			this.featureGenerator = generator;
			this.prototype = prototype;
		}
		
		FeatureGenerator featureGenerator;
		Shape prototype;
		
		public Shape shapeAt(int chunkX, int chunkZ) {
			return new Shape(this.prototype.originX + chunkX, this.prototype.originZ + chunkZ, this.prototype.sizeX, this.prototype.sizeZ);
		}
		
		public int score(World world, int originX, int originZ) {
			return Shape.scoreOrigin(world, originX + this.prototype.originX, this.prototype.originZ, this.prototype.sizeX, this.prototype.sizeZ);
		}
		
		public boolean isViable(World world, int originX, int originZ) {
			int thisScore = this.score(world, originX, originZ);
			Shape shape = this.shapeAt(originX, originZ);
			
			for (int[] piece : this.prototype.getPieces()) {
				if (piece[0] == originX && piece[1] == originZ)
					continue;
				
				Shape pieceShape = this.featureGenerator.getShape(world, piece[0], piece[1]);
				
				if (!pieceShape.equals(shape)) {
					return false;
				}
			}
			
			return false;
		}
	}
	
	public Shape getShape(final World world, int chunkX, int chunkZ) {
		int maxScore = Integer.MIN_VALUE;
		ShapeGenerator chosen = null;
		
		for (ShapeGenerator gen : this.getGenerators()) {
			int score = gen.score(world, chunkX, chunkZ);
			boolean viable = gen.isViable(world, chunkX, chunkZ);
			
			if (viable && score > (int)(Integer.MAX_VALUE * this.featurePresenceThreshold) && score > maxScore) {
				maxScore = score;
				chosen = gen;
			}
		}
		
		if (chosen == null)
			return null;
		
		return chosen.shapeAt(chunkX, chunkZ);
	}
}
