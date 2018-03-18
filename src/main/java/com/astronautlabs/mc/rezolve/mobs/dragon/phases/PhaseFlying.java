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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PhaseFlying extends PhaseBase
{
	private Vec3d targetLocation;

	public PhaseFlying(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	public DragonPhaseList<PhaseFlying> getPhaseList()
	{
		return DragonPhaseList.FLYING;
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
		if (this.targetLocation == null) {
			this.targetLocation =
				new Vec3d(1, 0, 0)
					.rotateYaw(this.dragon.getRNG().nextFloat() * 360f)
					.scale(16 + Math.pow(this.dragon.getRNG().nextFloat(), 2) * 250f)
					.add(this.dragon.getPositionVector());
		} else {
			this.dragon.initPathPoints((int)this.dragon.posX, (int)this.dragon.posY, (int)this.dragon.posZ);
			this.dragon.getPhaseManager().setPhase(DragonPhaseList.HOLDING_PATTERN);
		}
	}
}