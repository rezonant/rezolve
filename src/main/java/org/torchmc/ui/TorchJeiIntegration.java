package org.torchmc.ui;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class TorchJeiIntegration implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("torchmc", "core");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new TorchScreen.JeiHandler());
    }
}
