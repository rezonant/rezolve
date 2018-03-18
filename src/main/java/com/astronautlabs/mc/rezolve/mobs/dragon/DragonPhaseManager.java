package com.astronautlabs.mc.rezolve.mobs.dragon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DragonPhaseManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	private final EntityDragon dragon;
	private final IPhase[] phases = new IPhase[DragonPhaseList.getTotalPhases()];
	private IPhase phase;

	public DragonPhaseManager(EntityDragon dragonIn)
	{
		this.dragon = dragonIn;
		this.setPhase(DragonPhaseList.HOLDING_PATTERN);
	}

	public void setPhase(DragonPhaseList<?> phaseIn)
	{
		phaseIn = DragonPhaseList.SITTING_SCANNING;

		if (this.phase == null || phaseIn != this.phase.getPhaseList()) {
			if (this.phase != null) {
				this.phase.removeAreaEffect();
			}

			this.phase = this.getPhase(phaseIn);

			if (!this.dragon.worldObj.isRemote) {
				this.dragon.getDataManager().set(EntityDragon.PHASE, Integer.valueOf(phaseIn.getId()));
			}

			this.dragon.log("** Switching to phase {} on the {}", phaseIn, this.dragon.worldObj.isRemote ? "client" : "server");
			this.phase.initPhase();
		}
	}

	public IPhase getCurrentPhase()
	{
		return this.phase;
	}

	public <T extends IPhase> T getPhase(DragonPhaseList<T> phaseIn)
	{
		int i = phaseIn.getId();

		if (this.phases[i] == null)
		{
			this.phases[i] = phaseIn.createPhase(this.dragon);
		}

		return (T)this.phases[i];
	}
}