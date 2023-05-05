package com.rezolvemc.common.machines;

import com.rezolvemc.Rezolve;
import org.torchmc.ui.widgets.ProgressIndicator;

public class MachineProgressIndicator extends ProgressIndicator {
    public MachineProgressIndicator() {
        super(Rezolve.str("progress"));
    }

    @Override
    protected void updateState() {
        if (screen instanceof MachineScreen machineScreen) {
            setValue(machineScreen.getMachineMenu().progress);
        }
    }
}
