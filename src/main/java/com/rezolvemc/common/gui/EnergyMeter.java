package com.rezolvemc.common.gui;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.MachineScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.torchmc.ui.widgets.Meter;

public class EnergyMeter extends Meter {
    public EnergyMeter() {
        super(
                Component.translatable("screens.rezolve.energy_meter"),
                Component.translatable("screens.rezolve.energy_unit"),
                new ResourceLocation("rezolve", "textures/gui/widgets/energy_meter.png")
        );

        if (screen instanceof MachineScreen<?> machineScreen) {
            machineMenu = machineScreen.getMachineMenu();
        }
    }

    MachineMenu machineMenu;

    @Override
    public void updateState() {
        if (machineMenu != null) {
            setValue(machineMenu.energyStored / (double)machineMenu.energyCapacity);
        }
    }
}
