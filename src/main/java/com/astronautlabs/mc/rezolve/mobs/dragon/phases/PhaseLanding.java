package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import javax.annotation.Nullable;

import com.astronautlabs.mc.rezolve.mobs.dragon.DragonPhaseList;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseLanding extends PhaseBase
{
	private Vec3d targetLocation;

	public PhaseLanding(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	/**
	 * Generates particle effects appropriate to the phase (or sometimes sounds).
	 * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
	 */
	public void doClientRenderEffects()
	{
		Vec3d vec3d = this.dragon.getHeadLookVec(1.0F).normalize();
		vec3d.rotateYaw(-((float)Math.PI / 4F));
		double d0 = this.dragon.dragonPartHead.posX;
		double d1 = this.dragon.dragonPartHead.posY + (double)(this.dragon.dragonPartHead.height / 2.0F);
		double d2 = this.dragon.dragonPartHead.posZ;

		for (int i = 0; i < 8; ++i)
		{
			double d3 = d0 + this.dragon.getRNG().nextGaussian() / 2.0D;
			double d4 = d1 + this.dragon.getRNG().nextGaussian() / 2.0D;
			double d5 = d2 + this.dragon.getRNG().nextGaussian() / 2.0D;
			this.dragon.worldObj.spawnParticle(EnumParticleTypes.DRAGON_BREATH, d3, d4, d5, -vec3d.xCoord * 0.07999999821186066D + this.dragon.motionX, -vec3d.yCoord * 0.30000001192092896D + this.dragon.motionY, -vec3d.zCoord * 0.07999999821186066D + this.dragon.motionZ, new int[0]);
			this.dragon.worldObj.spawnParticle(EnumParticleTypes.LAVA, d3, d4, d5, -vec3d.xCoord * 0.07999999821186066D + this.dragon.motionX, -vec3d.yCoord * 0.30000001192092896D + this.dragon.motionY, -vec3d.zCoord * 0.07999999821186066D + this.dragon.motionZ, new int[0]);


			vec3d.rotateYaw(0.19634955F);
		}
	}

	int ticks = 0;

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate()
	{
		this.ticks += 1;

		if (this.targetLocation == null) {
			BlockPos landingPos = new BlockPos((int)this.dragon.posX, 0, (int)this.dragon.posZ);
			this.targetLocation = new Vec3d(this.dragon.worldObj.getTopSolidOrLiquidBlock(landingPos));
		}

		if (this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ) < 2.0D) {
			((PhaseSittingFlaming)this.dragon.getPhaseManager().getPhase(DragonPhaseList.SITTING_FLAMING)).resetFlameCount();
			this.dragon.getPhaseManager().setPhase(DragonPhaseList.SITTING_SCANNING);
		} else if (ticks > 20*6) {
			this.dragon.getPhaseManager().setPhase(DragonPhaseList.TAKEOFF);
		}
	}

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	public float getMaxRiseOrFall()
	{
		return 1.5F;
	}

	public float getYawFactor()
	{
		float f = MathHelper.sqrt_double(this.dragon.motionX * this.dragon.motionX + this.dragon.motionZ * this.dragon.motionZ) + 1.0F;
		float f1 = Math.min(f, 40.0F);
		return f1 / f;
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase()
	{
		this.targetLocation = null;
		this.ticks = 0;
	}

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vec3d getTargetLocation()
	{
		return this.targetLocation;
	}

	public DragonPhaseList<PhaseLanding> getPhaseList()
	{
		return DragonPhaseList.LANDING;
	}
}