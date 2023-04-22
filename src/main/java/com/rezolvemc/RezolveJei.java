package com.rezolvemc;

import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.registry.RezolveRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class RezolveJei implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return Rezolve.loc("core");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        for (var screen : RezolveRegistry.screenClasses()) {
            if (!MachineScreen.class.isAssignableFrom(screen))
                continue;

            registration.addGenericGuiContainerHandler((Class<MachineScreen>)screen, new MachineScreen.Jei());
        }
    }
}
