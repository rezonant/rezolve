package com.astronautlabs.mc.rezolve.common;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class BaseScreen<MenuT extends MachineMenu> extends AbstractContainerScreen<MenuT> {
    protected BaseScreen(MenuT menu, Inventory playerInventory, Component pTitle, String guiBackgroundResource, int width, int height) {
        super(menu, playerInventory, pTitle);
        this.width = width;
        this.height = height;
        this.guiBackgroundResource = guiBackgroundResource;
    }
    protected String guiBackgroundResource;
    private ArrayList<GuiComponent> controls = new ArrayList<>();

    @Override
    protected void init() {
        super.init();
        this.controls.clear();
    }

    protected void drawSubWindows(PoseStack poseStack, int mouseX, int mouseY) {

    }

    protected void drawItem(PoseStack poseStack, ItemStack stack, int x, int y) {

        RenderSystem.setShaderColor(1, 1, 1, 1);

        poseStack.translate(0,0, 32);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();

        //RenderHelper.disableStandardItemLighting();
        //RenderHelper.enableGUIStandardItemLighting();

        this.setBlitOffset(200);
        Font font = this.font;
        this.itemRenderer.renderAndDecorateItem(stack, x, y);
        //this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, null);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;

        //RenderHelper.enableStandardItemLighting();
        //GlStateManager.enableLighting();
        RenderSystem.disableDepthTest();
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        Slot slot = this.getSlotUnderMouse();

        if (slot != null) {
            if (slot instanceof GhostSlot) {
                return true;
            }
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (GuiComponent control : this.controls) {
            if (control instanceof EditBox) {
                EditBox textField = (EditBox) control;
                textField.mouseClicked(pMouseX, pMouseY, pButton);
            }
        }


        Slot slot = this.getSlotUnderMouse();

        if (slot != null) {
            if (slot instanceof GhostSlot) {
                GhostSlot ghostSlot = (GhostSlot)slot;
                LocalPlayer player = Minecraft.getInstance().player;
                ItemStack stack = player.getInventory().getSelected();

                if (stack != null) {
                    stack = stack.copy();

                    if (ghostSlot.isSingleItemOnly())
                        stack.setCount(1);
                }

                slot.set(stack);
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        boolean textFocused = false;

        for (GuiComponent control : this.controls) {
            if (control instanceof EditBox) {
                EditBox textField = (EditBox) control;
                if (textField.isFocused()) {
                    textFocused = true;
                    textField.charTyped(pCodePoint, pModifiers);
                    break;
                }
            }
        }

        if (!textFocused || pCodePoint != 'e')
            return super.charTyped(pCodePoint, pModifiers);

        return false;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

//        for (GuiComponent control : this.controls) {
//            if (control instanceof GuiTextField) {
//                ((GuiTextField)control).drawTextBox();
//            } else if (control instanceof GuiButton) {
//                ((GuiButton)control).drawButton(this.mc, mouseX, mouseY);
//            }
//        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
//        RenderSystem.disableDepthTest();
//
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(this.guiLeft + 10, this.guiTop + 10, 0);
//        this.drawRect(0, 0, this.xSize - 20, this.ySize - 20, 0xFFB0B0B0);
//        GlStateManager.popMatrix();
//
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(this.guiLeft, this.guiTop, 0);
//        this.drawSubWindows(pPoseStack, mouseX - this.guiLeft, mouseY - this.guiTop);
//        GlStateManager.popMatrix();
//
//        RenderSystem.disableDepthTest();
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        this.mc.getTextureManager().bindTexture(new ResourceLocation(this.guiBackgroundResource));
//        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    protected void textureQuad(ResourceLocation texture, double x, double y, double width, double height) {
        RenderSystem.setShaderTexture(0, texture);
        this.drawQuad(x, y, width, height);
    }

    protected void colorQuad(int color, double x, double y, double width, double height) {
        float red = (color >> 24) / 255.0f;
        float green = (color >> 16) / 255.0f;
        float blue = (color >> 8) / 255.0f;
        float alpha = (color >> 8) / 255.0f;

        colorQuad(red, green, blue, alpha, x, y, width, height);
    }
    protected void colorQuad(float r, float g, float b, float a, double x, double y, double width, double height) {
        RenderSystem.setShaderColor(r, g, b, a);
        this.drawQuad(x, y, width, height);
    }

    protected void drawQuad(double x, double y, double width, double height) {

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(0.0D, (double)height, 0.0D)
                .uv(0.0F, (float)height / 32.0F)
                .color(64, 64, 64, 255)
                .endVertex();
        bufferbuilder.vertex((double)width, (double)height, 0.0D)
                .uv((float)width / 32.0F, (float)height / 32.0F)
                .color(64, 64, 64, 255)
                .endVertex();
        bufferbuilder.vertex((double)width, 0.0D, 0.0D)
                .uv((float)width / 32.0F, 0)
                .color(64, 64, 64, 255)
                .endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, 0.0D)
                .uv(0.0F, 0)
                .color(64, 64, 64, 255)
                .endVertex();
        tesselator.end();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundRendered(this, new PoseStack()));
    }
}
