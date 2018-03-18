package com.astronautlabs.mc.rezolve.mobs.dragon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DragonLookHelper
{
	private final EntityDragon entity;
	/** The amount of change that is made each update for an entity facing a direction. */
	private float deltaLookYaw;
	/** The amount of change that is made each update for an entity facing a direction. */
	private float deltaLookPitch;
	/** Whether or not the entity is trying to look at something. */
	private boolean isLooking;
	private double posX;
	private double posY;
	private double posZ;

	public DragonLookHelper(EntityDragon entitylivingIn)
	{
		this.entity = entitylivingIn;
	}

	/**
	 * Sets position to look at using entity
	 */
	public void setLookPositionWithEntity(Entity entityIn, float deltaYaw, float deltaPitch)
	{
		this.posX = entityIn.posX;

		if (entityIn instanceof EntityLivingBase)
		{
			this.posY = entityIn.posY + (double)entityIn.getEyeHeight();
		}
		else
		{
			this.posY = (entityIn.getEntityBoundingBox().minY + entityIn.getEntityBoundingBox().maxY) / 2.0D;
		}

		this.posZ = entityIn.posZ;
		this.deltaLookYaw = deltaYaw;
		this.deltaLookPitch = deltaPitch;
		this.isLooking = true;
	}

	/**
	 * Sets position to look at
	 */
	public void setLookPosition(double x, double y, double z, float deltaYaw, float deltaPitch)
	{
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.deltaLookYaw = deltaYaw;
		this.deltaLookPitch = deltaPitch;
		this.isLooking = true;
	}

	/**
	 * Updates look
	 */
	public void onUpdateLook()
	{
		this.entity.rotationHeadPitch = 0.0F;

		if (this.isLooking) {
			Vec3d headPos = this.entity.getHeadPos();

			this.isLooking = false;
			double dx = this.posX - headPos.xCoord;
			double dy = this.posY - headPos.yCoord;
			double dz = this.posZ - headPos.zCoord;
			double distance = (double)MathHelper.sqrt_double(dx * dx + dz * dz);
			float yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
			float pitch = (float)(-(MathHelper.atan2(dy, distance) * (180D / Math.PI)));
			this.entity.rotationHeadPitch = this.updateRotation(this.entity.rotationHeadPitch, pitch, this.deltaLookPitch);
			this.entity.rotationHeadYaw2 = this.updateRotation(this.entity.rotationHeadYaw2, yaw, this.deltaLookYaw);
		} else {
			this.entity.rotationHeadYaw2 = this.updateRotation(this.entity.rotationHeadYaw2, this.entity.renderYawOffset, 10.0F);
		}

		float f2 = MathHelper.wrapDegrees(this.entity.rotationHeadYaw2 - this.entity.renderYawOffset);

		if (!this.entity.getNavigator().noPath()) {
			if (f2 < -75.0F)
				this.entity.rotationHeadYaw2 = this.entity.renderYawOffset - 75.0F;

			if (f2 > 75.0F)
				this.entity.rotationHeadYaw2 = this.entity.renderYawOffset + 75.0F;
		}
	}

	private float updateRotation(float currentValue, float newValue, float maxDelta)
	{
		float f = MathHelper.wrapDegrees(newValue - currentValue);

		if (f > maxDelta)
		{
			f = maxDelta;
		}

		if (f < -maxDelta)
		{
			f = -maxDelta;
		}

		return currentValue + f;
	}

	public boolean getIsLooking()
	{
		return this.isLooking;
	}

	public double getLookPosX()
	{
		return this.posX;
	}

	public double getLookPosY()
	{
		return this.posY;
	}

	public double getLookPosZ()
	{
		return this.posZ;
	}
}