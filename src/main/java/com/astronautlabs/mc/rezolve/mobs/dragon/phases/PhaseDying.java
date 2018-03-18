package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import javax.annotation.Nullable;

import com.astronautlabs.mc.rezolve.mobs.dragon.DragonPhaseList;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class PhaseDying extends PhaseBase
{
	private Vec3d targetLocation;
	private int animationTime = 0;

	public PhaseDying(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	/**
	 * Generates particle effects appropriate to the phase (or sometimes sounds).
	 * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
	 */
	@Override
	public void doClientRenderEffects()
	{
		if (this.animationTime == 0) {
			this.dragon.worldObj.playSound(this.dragon.posX, this.dragon.posY, this.dragon.posZ, SoundEvents.ENTITY_ENDERDRAGON_DEATH, this.dragon.getSoundCategory(), 5.0F, 0.8F + this.dragon.getRNG().nextFloat() * 0.3F, false);
		}

		if (this.animationTime++ % 10 == 0)
		{
			float f = (this.dragon.getRNG().nextFloat() - 0.5F) * 8.0F;
			float f1 = (this.dragon.getRNG().nextFloat() - 0.5F) * 4.0F;
			float f2 = (this.dragon.getRNG().nextFloat() - 0.5F) * 8.0F;
			this.dragon.worldObj.spawnParticle(EnumParticleTypes.LAVA, this.dragon.posX + (double)f, this.dragon.posY + 2.0D + (double)f1, this.dragon.posZ + (double)f2, 0.0D, 0.0D, 0.0D, new int[0]);
			this.dragon.worldObj.spawnParticle(EnumParticleTypes.FLAME, this.dragon.posX + (double)f, this.dragon.posY + 2.0D + (double)f1, this.dragon.posZ + (double)f2, 0.0D, 0.0D, 0.0D, new int[0]);

		}
	}

	int ticks = 0;

	@Override
	public boolean getIsDying() {
		return true;
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	@Override
	public void doLocalUpdate()
	{
		++this.ticks;
		++this.animationTime;

		// Have the dragon go up a bunch.

		if (this.targetLocation == null) {
			this.targetLocation = this.dragon.getPositionVector().add(new Vec3d(0, 32, 0));
			this.dragon.log("Dragon will die upwards to {}", this.targetLocation);
		}

		double distance = this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);

		if (this.ticks < 20*15) {
			this.dragon.setHealth(1.0F);
		} else {
			this.dragon.log("Dragon has died.");
			this.dragon.setHealth(0.0F);
		}
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase()
	{
		this.targetLocation = null;
		this.animationTime = 0;
	}

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	public float getMaxRiseOrFall()
	{
		return 32F;
	}

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vec3d getTargetLocation()
	{
		return this.targetLocation;
	}

	public DragonPhaseList<PhaseDying> getPhaseList()
	{
		return DragonPhaseList.DYING;
	}
}