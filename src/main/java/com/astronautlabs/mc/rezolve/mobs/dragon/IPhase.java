package com.astronautlabs.mc.rezolve.mobs.dragon;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface IPhase
{
	boolean getIsStationary();
	boolean getIsDying();

	/**
	 * Generates particle effects appropriate to the phase (or sometimes sounds).
	 * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
	 */
	void doClientRenderEffects();

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	void doLocalUpdate();

	/**
	 * Called when this phase is set to active
	 */
	void initPhase();

	void removeAreaEffect();

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	float getMaxRiseOrFall();

	float getYawFactor();

	DragonPhaseList <? extends IPhase > getPhaseList();

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	Vec3d getTargetLocation();

	/**
	 * Normally, just returns damage. If dragon is sitting and src is an arrow, arrow is enflamed and zero damage
	 * returned.
	 */
	float getAdjustedDamage(EntityDragonPart pt, DamageSource src, float damage);
}