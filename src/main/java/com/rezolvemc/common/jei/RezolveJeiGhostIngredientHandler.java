package com.rezolvemc.common.jei;

import com.rezolvemc.common.inventory.IngredientSlot;
import com.rezolvemc.common.machines.MachineScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RezolveJeiGhostIngredientHandler implements IGhostIngredientHandler<MachineScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(MachineScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        var targets = new ArrayList<Target<I>>();

        if (!(ingredient.getIngredient() instanceof ItemStack stack))
            return targets;

        for (var slot : gui.getMenu().slots) {
            if (slot instanceof IngredientSlot) {
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
