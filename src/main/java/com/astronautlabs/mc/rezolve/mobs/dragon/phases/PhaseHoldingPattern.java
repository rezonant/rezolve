/**
 * (C) Mojang AB
 * (C) 2018 William Lahti
 */

package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import javax.annotation.Nullable;

import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import com.astronautlabs.mc.rezolve.mobs.dragon.DragonPhaseList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PhaseHoldingPattern extends PhaseBase
{
	private Path currentPath;
	private Vec3d targetLocation;
	private boolean clockwise;

	public PhaseHoldingPattern(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	public DragonPhaseList<PhaseHoldingPattern> getPhaseList()
	{
		return DragonPhaseList.HOLDING_PATTERN;
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate()
	{
		double d0 = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);

		if (d0 < 100.0D || d0 > 22500.0D || this.dragon.isCollidedHorizontally || this.dragon.isCollidedVertically)
		{
			this.findNewTarget();
		}
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
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vec3d getTargetLocation()
	{
		return this.targetLocation;
	}

	private void findNewTarget()
	{
		if (this.currentPath != null && this.currentPath.isFinished())
		{
			// Dragon has a 1 in 3 chance to decide to land

			if (this.dragon.getRNG().nextInt(3) == 0) {
				this.dragon.getPhaseManager().setPhase(DragonPhaseList.LANDING_APPROACH);
				return;
			}

			double maxDistance = 64.0D;
			BlockPos dragonPos = new BlockPos(this.dragon.posX, this.dragon.posY, this.dragon.posZ);
			BlockPos groundAboveDragonPos = this.dragon.worldObj.getTopSolidOrLiquidBlock(dragonPos);
			EntityPlayer entityplayer = this.dragon.worldObj.getNearestAttackablePlayer(groundAboveDragonPos, maxDistance, maxDistance);

			if (entityplayer != null) {
				maxDistance = entityplayer.getDistanceSqToCenter(groundAboveDragonPos) / 512.0D;

				// A 50% chance to start strafing the player

				if ((this.dragon.getRNG().nextInt(MathHelper.abs_int((int) maxDistance) + 2) == 0)) {

					System.out.println("Dragon will strafe the player: "+entityplayer.toString());

					this.strafePlayer(entityplayer);
					return;
				}
			} else {
				// No target
				// A 10% chance of selecting a new haunt

				if (this.dragon.getRNG().nextInt(10) == 0) {
					this.dragon.getPhaseManager().setPhase(DragonPhaseList.FLYING);
//					float distance = 16f + this.dragon.getRNG().nextFloat() * 64f;
//
//					Vec3d haunt = new Vec3d(this.dragon.posX, 0, this.dragon.posZ).add((new Vec3d(1, 0, 0).rotateYaw(this.dragon.getRNG().nextFloat() * 360f).scale(distance)));
//					BlockPos hauntBlock = this.dragon.worldObj.getTopSolidOrLiquidBlock(new BlockPos(haunt.xCoord, haunt.yCoord, haunt.zCoord));
//
//					System.out.println("Dragon selected a new haunt: "+hauntBlock.toString());
//
//					this.dragon.initPathPoints(hauntBlock.getX(), hauntBlock.getY(), hauntBlock.getZ());
//					this.dragon.getPhaseManager().setPhase(DragonPhaseList.HOLDING_PATTERN);
					return;
				}
			}
		}

		// Fly around the focal point either clockwise or counter-clockwise (randomly).
		// Create a new path if we haven't done that yet, or our last one finished.

		if (this.currentPath == null || this.currentPath.isFinished()) {
			int ppCurrentIdx = this.dragon.initPathPoints();
			int ppNextIdx = ppCurrentIdx;

			// A 1 in 8 chance to turn around

			if (this.dragon.getRNG().nextInt(8) == 0) {
				this.clockwise = !this.clockwise;
				ppNextIdx = ppCurrentIdx + 6;
			}

			// Continue around the spiral of pathfinding points in the intended direction

			if (this.clockwise) {
				++ppNextIdx;
			} else {
				--ppNextIdx;
			}

			boolean innerRing = false;

			if (innerRing) {
				// circle the inner ring (12 to 19)

				ppNextIdx = ppNextIdx - 12;
				ppNextIdx = ppNextIdx & 7;
				ppNextIdx = ppNextIdx + 12;
			} else {
				// circle the outer ring (0 - 11)

				ppNextIdx = ppNextIdx % 12;
				if (ppNextIdx < 0)
					ppNextIdx += 12;
			}

			// Set up path finding to move from our current point to the selected point.
			// When it finishes, this branch will run again to select the next destination.

			this.currentPath = this.dragon.findPath(ppCurrentIdx, ppNextIdx);

			if (this.currentPath != null)
				this.currentPath.incrementPathIndex();
		}

		this.navigateToNextPathNode();
	}

	private void strafePlayer(EntityPlayer player)
	{
		this.dragon.getPhaseManager().setPhase(DragonPhaseList.STRAFE_PLAYER);
		((PhaseStrafePlayer)this.dragon.getPhaseManager().getPhase(DragonPhaseList.STRAFE_PLAYER)).setTarget(player);
	}

	private void navigateToNextPathNode()
	{
		if (this.currentPath == null || this.currentPath.isFinished())
			return;

		Vec3d vec3d = this.currentPath.getCurrentPos();
		this.currentPath.incrementPathIndex();
		double x = vec3d.xCoord;
		double y = vec3d.yCoord + (double)(this.dragon.getRNG().nextFloat() * 20.0F);
		double z = vec3d.zCoord;
		this.targetLocation = new Vec3d(x, y, z);
	}
}