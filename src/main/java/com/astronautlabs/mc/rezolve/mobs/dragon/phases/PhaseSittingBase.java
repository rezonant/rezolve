package com.astronautlabs.mc.rezolve.mobs.dragon.phases;

import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;

public abstract class PhaseSittingBase extends PhaseBase
{
	public PhaseSittingBase(EntityDragon p_i46794_1_)
	{
		super(p_i46794_1_);
	}

	public boolean getIsStationary()
	{
		return true;
	}

	/**
	 * Normally, just returns damage. If dragon is sitting and src is an arrow, arrow is enflamed and zero damage
	 * returned.
	 */
	public float getAdjustedDamage(EntityDragonPart pt, DamageSource src, float damage)
	{
		if (src.getSourceOfDamage() instanceof EntityArrow)
		{
			src.getSourceOfDamage().setFire(1);
			return 0.0F;
		}
		else
		{
			return super.getAdjustedDamage(pt, src, damage);
		}
	}

	@Override
	public void doLocalUpdate() {
		//EntityPlayer entityplayer = this.dragon.worldObj.getNearestAttackablePlayer(this.dragon.getPosition(), 128.0D, 128.0D);
		//if (entityplayer != null) {
		//	this.dragon.rotationYaw = 180f + entityplayer.rotationYaw;
		//}

	}
}