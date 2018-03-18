package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import javax.annotation.Nullable;

import com.astronautlabs.mc.rezolve.mobs.dragon.DragonPhaseList;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseLandingApproach extends PhaseBase
{
	private Path currentPath;
	private Vec3d targetLocation;

	public PhaseLandingApproach(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	public DragonPhaseList<PhaseLandingApproach> getPhaseList()
	{
		return DragonPhaseList.LANDING_APPROACH;
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase()
	{
		this.currentPath = null;
		this.targetLocation = null;
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate()
	{
		double distanceToTarget = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);

		if (distanceToTarget < 100.0D || distanceToTarget > 22500.0D || this.dragon.isCollidedHorizontally || this.dragon.isCollidedVertically)
		{
			this.findNewTarget();
		}
	}

	@Override
	public float getMaxRiseOrFall() {
		return 1.8f;
	}

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vec3d getTargetLocation()
	{
		return this.targetLocation;
	}

	private void findNewTarget()
	{
		if (this.targetLocation != null) {
			System.out.println("Reached landing approach destination...");
			this.dragon.getPhaseManager().setPhase(DragonPhaseList.LANDING);
			return;
		}

		int currentPointIdx = this.dragon.initPathPoints();
		BlockPos blockpos = new BlockPos(this.dragon.posX, this.dragon.posY, this.dragon.posZ);
		EntityPlayer entityplayer = this.dragon.worldObj.getNearestAttackablePlayer(blockpos, 128.0D, 128.0D);
		int destPointIdx;

		PathPoint pathpoint;
		Vec3d destPoint;

		BlockPos landingPos = new BlockPos((int)this.dragon.posX, 0, (int)this.dragon.posZ);
		this.targetLocation = new Vec3d(this.dragon.worldObj.getTopSolidOrLiquidBlock(landingPos));

		if (entityplayer != null) {
			System.out.println("#### Dragon is going to land on player. Suck it player!");
			Vec3d playerPos = (new Vec3d(entityplayer.posX, 0.0D, entityplayer.posZ)).normalize();
			destPointIdx = this.dragon.getNearestPpIdx(-playerPos.xCoord * 40.0D, 105.0D, -playerPos.zCoord * 40.0D);
			destPoint = entityplayer.getPositionVector();
			pathpoint = new PathPoint((int)entityplayer.posX, (int)entityplayer.posY, (int)entityplayer.posZ);
		} else {
			destPointIdx = this.dragon.getNearestPpIdx(40.0D, (double)blockpos.getY(), 0.0D);
			Vec3d pos = new Vec3d(this.dragon.posX, this.dragon.posY, this.dragon.posZ).add(new Vec3d(20, 0, 0).rotateYaw(this.dragon.getRNG().nextFloat() * 360f));
			destPoint = pos;
			pathpoint = new PathPoint((int)pos.xCoord, (int)pos.yCoord, (int)pos.zCoord);

			System.out.println("Dragon is going to land nearby. Current position: "+blockpos.toString()+", destination: "+pathpoint.toString());
		}

		destPoint = new Vec3d(this.dragon.worldObj.getTopSolidOrLiquidBlock(new BlockPos(destPoint)));

		// Navigate from our current point to the destination point, then to the landing location

		this.targetLocation = destPoint;
		//this.currentPath = this.dragon.findPath(currentPointIdx, destPointIdx, pathpoint);

		//if (this.currentPath != null) {
		//	this.currentPath.incrementPathIndex();
		//}
	}

	private void navigateToNextPathNode()
	{
		if (this.currentPath == null || this.currentPath.isFinished())
			return;

		Vec3d vec3d = this.currentPath.getCurrentPos();
		this.currentPath.incrementPathIndex();

		// make the dragon randomly fly up to 20 units above the point?
		// vec3d.yCoord + (double)(this.dragon.getRNG().nextFloat() * 20.0F);

		this.targetLocation = vec3d;
	}
}