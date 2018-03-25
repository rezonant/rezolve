package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.BuildableContainer;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.storage.machines.storageShell.StorageShellEntity;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageShellRecipeTransferHandler<C extends Container> implements IRecipeTransferHandler<C> {

	public StorageShellRecipeTransferHandler(IModRegistry registry) {
		this.registry = registry;
	}

	IModRegistry registry;

	@Override
	public Class getContainerClass() {
		return BuildableContainer.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(Container container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {

		if (doTransfer) {
			StorageViewRecipeRequestMessage recipeRequest = new StorageViewRecipeRequestMessage(player, recipeLayout);
			RezolvePacketHandler.INSTANCE.sendToServer(recipeRequest);
		}

		return null;
	}
}
