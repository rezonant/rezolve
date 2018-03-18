package com.astronautlabs.mc.rezolve.mobs.dragon;

import com.enderio.core.client.render.GlState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDragon extends RenderLiving<EntityDragon>
{
	private static final ResourceLocation DRAGON_EXPLODING_TEXTURES = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
	private static final ResourceLocation DRAGON_TEXTURES = new ResourceLocation("textures/entity/enderdragon/dragon.png");
	/** An instance of the dragon model in RenderDragon */
	protected ModelDragon modelDragon;

	public RenderDragon(RenderManager renderManagerIn)
	{
		super(renderManagerIn, new ModelDragon(0.0F), 0.5F);
		this.modelDragon = (ModelDragon)this.mainModel;
		this.addLayer(new LayerDragonEyes(this));
		this.addLayer(new LayerDragonDeath());
	}

	/**
	 * Renders the model in RenderLiving
	 */
	protected void renderModel(EntityDragon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor)
	{
		if (entitylivingbaseIn.deathTicks > 0)
		{
			float f = (float)entitylivingbaseIn.deathTicks / 200.0F;
			GlStateManager.depthFunc(515);
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(516, f);
			this.bindTexture(DRAGON_EXPLODING_TEXTURES);
			this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.depthFunc(514);
		}

		GlStateManager.rotate(180f, 0, 1, 0);

		this.bindEntityTexture(entitylivingbaseIn);
		this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

		if (entitylivingbaseIn.hurtTime > 0)
		{
			GlStateManager.depthFunc(514);
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1.0F, 0.0F, 0.0F, 0.5F);
			this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.depthFunc(515);
		}
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityDragon entity)
	{
		return DRAGON_TEXTURES;
	}
}