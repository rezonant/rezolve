package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import javax.annotation.Nullable;

import com.astronautlabs.mc.rezolve.mobs.dragon.DragonPhaseList;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import net.minecraft.util.math.Vec3d;

public class PhaseHover extends PhaseBase
{
	private Vec3d targetLocation;

	public PhaseHover(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate()
	{
		if (this.targetLocation == null)
		{
			this.targetLocation = new Vec3d(this.dragon.posX, this.dragon.posY, this.dragon.posZ);
		}
	}

	public boolean getIsStationary()
	{
		return true;
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase()
	{
		this.targetLocation = null;
	}

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	public float getMaxRiseOrFall()
	{
		return 1.0F;
	}

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vec3d getTargetLocation()
	{
		return this.targetLocation;
	}

	public DragonPhaseList<PhaseHover> getPhaseList()
	{
		return DragonPhaseList.HOVER;
	}
}