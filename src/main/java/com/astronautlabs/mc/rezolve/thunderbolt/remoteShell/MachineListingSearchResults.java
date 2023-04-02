package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class MachineListingSearchResults implements INBTSerializable<ListTag> {
    public List<MachineListing> machines = new ArrayList<>();

    @Override
    public ListTag serializeNBT() {
        var tag = new ListTag();
        for (var machine : machines)
            tag.add(machine.serializeNBT());

        return tag;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        machines = new ArrayList<>();
        for (int i = 0, max = nbt.size(); i < max; ++i) {
            var machine = new MachineListing();
            machine.deserializeNBT(nbt.getCompound(i));
            machines.add(machine);
        }
    }
}
