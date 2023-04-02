package com.astronautlabs.mc.rezolve.common.jei;

import com.astronautlabs.mc.rezolve.common.inventory.GhostSlot;
import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RezolveJeiGhostIngredientHandler implements IGhostIngredientHandler<MachineScreen> {

    @Override
    public <I> List<Target<I>> getTargets(MachineScreen gui, I ingredient, boolean doStart) {
        var targets = new ArrayList<Target<I>>();
        if (!(ingredient instanceof ItemStack))
            return targets;

        var stack = (ItemStack)ingredient;

        for (var slot : gui.getMenu().slots) {
            if (slot instanceof GhostSlot) {
                targets.add(new Target<I>() {
                    @Override
                    public Rect2i getArea() {
                        return new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
                    }

                    @Override
                    public void accept(I ingredient) {
                        slot.set(stack);
                        gui.getMenu().setRemoteSlot(slot.index, stack);
                    }
                });
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public boolean shouldHighlightTargets() {
        return IGhostIngredientHandler.super.shouldHighlightTargets();
    }
}
