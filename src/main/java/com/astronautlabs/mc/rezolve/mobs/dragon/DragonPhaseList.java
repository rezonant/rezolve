package com.astronautlabs.mc.rezolve.mobs.dragon;

import com.astronautlabs.mc.rezolve.mobs.dragon.phases.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class DragonPhaseList<T extends IPhase>
{
	private static DragonPhaseList<?>[] phases = new DragonPhaseList[0];
	public static final DragonPhaseList<PhaseHoldingPattern> HOLDING_PATTERN = create(PhaseHoldingPattern.class, "HoldingPattern");
	public static final DragonPhaseList<PhaseDying> DYING = create(PhaseDying.class, "Dying");
	public static final DragonPhaseList<PhaseSittingFlaming> SITTING_FLAMING = create(PhaseSittingFlaming.class, "SittingFlaming");
	public static final DragonPhaseList<PhaseSittingScanning> SITTING_SCANNING = create(PhaseSittingScanning.class, "SittingScanning");
	public static final DragonPhaseList<PhaseSittingAttacking> SITTING_ATTACKING = create(PhaseSittingAttacking.class, "SittingAttacking");

	public static final DragonPhaseList<PhaseStrafePlayer> STRAFE_PLAYER = create(PhaseStrafePlayer.class, "StrafePlayer");
	public static final DragonPhaseList<PhaseLandingApproach> LANDING_APPROACH = create(PhaseLandingApproach.class, "LandingApproach");
	public static final DragonPhaseList<PhaseLanding> LANDING = create(PhaseLanding.class, "Landing");
	public static final DragonPhaseList<PhaseTakeoff> TAKEOFF = create(PhaseTakeoff.class, "Takeoff");
	public static final DragonPhaseList<PhaseFlying> FLYING = create(PhaseFlying.class, "Flying");


	public static final DragonPhaseList<PhaseChargingPlayer> CHARGING_PLAYER = create(PhaseChargingPlayer.class, "ChargingPlayer");
	public static final DragonPhaseList<PhaseHover> HOVER = create(PhaseHover.class, "Hover");

	private final Class <? extends IPhase > clazz;
	private final int id;
	private final String name;

	private DragonPhaseList(int idIn, Class <? extends IPhase > clazzIn, String nameIn)
	{
		this.id = idIn;
		this.clazz = clazzIn;
		this.name = nameIn;
	}

	public IPhase createPhase(EntityDragon dragon)
	{
		try
		{
			Constructor <? extends IPhase > constructor = this.getConstructor();
			return (IPhase)constructor.newInstance(new Object[] {dragon});
		}
		catch (Exception exception)
		{
			throw new Error(exception);
		}
	}

	protected Constructor <? extends IPhase > getConstructor() throws NoSuchMethodException
	{
		return this.clazz.getConstructor(new Class[] {EntityDragon.class});
	}

	public int getId()
	{
		return this.id;
	}

	public String toString()
	{
		return this.name + " (#" + this.id + ")";
	}

	public static DragonPhaseList<?> getById(int p_188738_0_)
	{
		return p_188738_0_ >= 0 && p_188738_0_ < phases.length ? phases[p_188738_0_] : HOLDING_PATTERN;
	}

	public static int getTotalPhases()
	{
		return phases.length;
	}

	private static <T extends IPhase> DragonPhaseList<T> create(Class<T> phaseIn, String nameIn)
	{
		DragonPhaseList<T> phaselist = new DragonPhaseList(phases.length, phaseIn, nameIn);
		phases = (DragonPhaseList[])Arrays.copyOf(phases, phases.length + 1);
		phases[phaselist.getId()] = phaselist;
		return phaselist;
	}
}