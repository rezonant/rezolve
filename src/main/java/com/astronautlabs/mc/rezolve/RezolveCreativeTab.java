package com.astronautlabs.mc.rezolve;

import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.RemoteShellBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class RezolveCreativeTab extends CreativeModeTab {
    RezolveCreativeTab() {
        super("rezolve");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(RezolveRegistry.block(RemoteShellBlock.class).asItem(), 1);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("rezolve");
    }
}
