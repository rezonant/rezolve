package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class MachineMenu<MachineT extends MachineEntity> extends AbstractContainerMenu {
	protected MachineMenu(MenuType<?> menuType, int pContainerId, Inventory playerInventory, MachineT machine) {
		super(menuType, pContainerId);

		this.playerInventory = playerInventory;
		this.machine = machine;
	}

	protected MachineMenu(MenuType<MachineMenu> menuType, int pContainerId, Inventory playerInventory) {
		super(menuType, pContainerId);

		this.playerInventory = playerInventory;
		this.machine = null;
	}

	protected Inventory playerInventory;
	protected MachineT machine;

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return null;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return false;
	}

	public static final int SLOT_PIXEL_SIZE = 18;

	protected void addPlayerSlots(int offsetX, int offsetY) {
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				this.addSlot(new Slot(playerInventory, 9 + x + y * 9, offsetX + x * SLOT_PIXEL_SIZE, offsetY + y * SLOT_PIXEL_SIZE));
			}
		}

		int playerHotbarOffsetX = offsetX;
		int playerHotbarOffsetY = offsetY + 58;

		// Player Hotbar, slots 0-8

		for (int x = 0; x < 9; ++x) {
			this.addSlot(new Slot(playerInventory, x, playerHotbarOffsetX + x * 18, playerHotbarOffsetY));
		}
	}
}