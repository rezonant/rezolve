package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import com.astronautlabs.mc.rezolve.mobs.dragon.DragonPhaseList;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import net.minecraft.init.SoundEvents;

public class PhaseSittingAttacking extends PhaseSittingBase
{
	private int attackingTicks;

	public PhaseSittingAttacking(EntityDragon dragonIn)
	{
		super(dragonIn);
	}

	/**
	 * Generates particle effects appropriate to the phase (or sometimes sounds).
	 * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
	 */
	public void doClientRenderEffects()
	{
		this.dragon.worldObj.playSound(this.dragon.posX, this.dragon.posY, this.dragon.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, this.dragon.getSoundCategory(), 2.5F, 0.8F + this.dragon.getRNG().nextFloat() * 0.3F, false);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate()
	{
		super.doLocalUpdate();
		if (this.attackingTicks++ >= 40)
		{
			this.dragon.getPhaseManager().setPhase(DragonPhaseList.SITTING_FLAMING);
		}
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase()
	{
		this.attackingTicks = 0;
	}

	public DragonPhaseList<PhaseSittingAttacking> getPhaseList()
	{
		return DragonPhaseList.SITTING_ATTACKING;
	}
}