package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import javax.annotation.Nullable;

import com.astronautlabs.mc.rezolve.mobs.dragon.DragonPhaseList;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseTakeoff extends PhaseBase
{
	private boolean firstTick;
	private Path currentPath;
	private Vec3d targetLocation;

	public PhaseTakeoff(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	@Override
	public float getMaxRiseOrFall() {
		return 0.3f;
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate()
	{
		if (this.firstTick) {
			this.firstTick = false;
			this.findNewTarget();
		} else {
			double distance = Math.sqrt(this.dragon.getDistanceSqToCenter(new BlockPos((int)this.targetLocation.xCoord, (int)this.targetLocation.yCoord, (int)this.targetLocation.zCoord)));
			//this.dragon.log("At {}, {} blocks away from takeoff destination {}", this.dragon.getPositionVector(), distance, this.targetLocation);

			if (distance < 10D) {
				this.dragon.log("Reached destination.");
				if (this.dragon.getRNG().nextInt(3) == 0) {

					this.dragon.log("Holders gonna hold.");
					this.dragon.getPhaseManager().setPhase(DragonPhaseList.HOLDING_PATTERN);
				} else {
					this.dragon.log("Now fly away!");
					this.dragon.getPhaseManager().setPhase(DragonPhaseList.FLYING);
				}
			}
		}
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase()
	{
		this.firstTick = true;
		this.currentPath = null;
		this.targetLocation = null;
	}

	private void findNewTarget()
	{
		int currentIdx = this.dragon.initPathPoints((int)this.dragon.posX, (int)this.dragon.posY, (int)this.dragon.posZ);

		Vec3d vec3d = this.dragon.getHeadLookVec(1.0F);
		int nextIdx = this.dragon.getNearestPpIdx(-vec3d.xCoord * 40.0D, 105.0D, -vec3d.zCoord * 40.0D);

		boolean innerRing = false;

		if (innerRing) {
			// inner ring
			nextIdx = nextIdx - 12;
			nextIdx = nextIdx & 7;
			nextIdx = nextIdx + 12;
		} else {
			// outer ring
			nextIdx = nextIdx % 12;

			if (nextIdx < 0)
			{
				nextIdx += 12;
			}
		}

		this.currentPath = this.dragon.findPath(currentIdx, nextIdx);

		if (this.currentPath != null) {
			this.currentPath.incrementPathIndex();
			if (this.currentPath.isFinished()) {
				this.currentPath.setCurrentPathIndex(0);
			}
			this.navigateToNextPathNode();
		}
	}

	private void navigateToNextPathNode()
	{
		Vec3d vec3d = this.currentPath.getCurrentPos();
		this.currentPath.incrementPathIndex();

		double y = vec3d.yCoord + (double)(this.dragon.getRNG().nextFloat() * 20.0F);
		this.targetLocation = new Vec3d(vec3d.xCoord, y, vec3d.zCoord);

		double distance = this.targetLocation.distanceTo(this.dragon.getPositionVector());

		this.dragon.log("Dragon selected takeoff target location of {} ({} units away)", this.targetLocation, Math.sqrt(distance));
	}

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vec3d getTargetLocation()
	{
		return this.targetLocation;
	}

	public DragonPhaseList<PhaseTakeoff> getPhaseList()
	{
		return DragonPhaseList.TAKEOFF;
	}
}