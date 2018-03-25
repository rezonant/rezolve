package com.astronautlabs.mc.rezolve.jei;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.storage.gui.StorageShellRecipeTransferHandler;
import mezz.jei.api.*;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

@JEIPlugin
public class RezolveJeiPlugin extends BlankModPlugin implements IModPlugin {

	public RezolveJeiPlugin() {

	}

	@Override
	public void register(IModRegistry registry) {
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new StorageShellRecipeTransferHandler<ContainerBase>(registry), VanillaRecipeCategoryUid.CRAFTING);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		RezolveMod.instance().setJeiRuntime(jeiRuntime);
	}
}
