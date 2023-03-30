package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.common.inventory.VirtualInventory;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacketReceiver;
import com.astronautlabs.mc.rezolve.common.network.WithPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@WithPacket(MachineMenuStatePacket.class)
public class MachineMenu<MachineT extends MachineEntity> extends AbstractContainerMenu implements RezolvePacketReceiver {
	protected MachineMenu(MenuType<?> menuType, int pContainerId, Inventory playerInventory, MachineT machine) {
		super(menuType, pContainerId);

		this.playerInventory = playerInventory;
		this.machine = machine;
		this.container = machine != null ? machine : new VirtualInventory();
	}

	public Container container;

	protected MachineMenu(MenuType<MachineMenu> menuType, int pContainerId, Inventory playerInventory) {
		super(menuType, pContainerId);

		this.playerInventory = playerInventory;
		this.machine = null;
	}

	protected Inventory playerInventory;
	protected MachineT machine;

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		sendMachineState();
	}

	@Override
	public void broadcastFullState() {
		super.broadcastFullState();
		sendMachineState();
	}

	public int energyCapacity;
	public int energyStored;
	public float progress;
	public Operation currentOperation;

	void sendMachineState() {
		var state = new MachineMenuStatePacket();
		state.setMenu(this);
		state.energyCapacity = this.machine.maxEnergyStored;
		state.energyStored = this.machine.getStoredEnergy();
		state.progress = this.machine.getProgress();
		state.operation = this.machine.getCurrentOperation();
		state.sendToPlayer((ServerPlayer) this.playerInventory.player);
	}

	@Override
	public void receivePacketOnClient(RezolvePacket rezolvePacket) {
		if (rezolvePacket instanceof MachineMenuStatePacket state) {
			energyCapacity = state.energyCapacity;
			energyStored = state.energyStored;
			progress = state.progress;
			currentOperation = state.operation;
		}
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return null;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return this.machine != null ? this.machine.stillValid(pPlayer) : true;
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