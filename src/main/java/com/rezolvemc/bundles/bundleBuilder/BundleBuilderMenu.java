package com.rezolvemc.bundles.bundleBuilder;

import com.rezolvemc.common.inventory.DyeSlot;
import com.rezolvemc.common.inventory.IngredientSlot;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.MachineOutputSlot;
import com.rezolvemc.common.machines.Sync;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.network.WithPacket;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.registry.WithScreen;
import net.minecraft.world.entity.player.Inventory;

@WithScreen(BundleBuilderScreen.class)
@WithPacket(SetPatternSettingsPacket.class)
public class BundleBuilderMenu extends MachineMenu<BundleBuilderEntity> {
	public BundleBuilderMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, null);
	}

	public BundleBuilderMenu(int containerId, Inventory playerInventory, BundleBuilderEntity te) {
		super(RezolveRegistry.menuType(BundleBuilderMenu.class), containerId, playerInventory, te);
		int invSlotSize = 18;

		// Pattern/Dye Slots

        this.addSlot(new BundlePatternSlot(container, BundleBuilderEntity.PATTERN_INPUT_SLOT, 101, 59));		// Pattern slot
        this.addSlot(new MachineOutputSlot(container, BundleBuilderEntity.PATTERN_OUTPUT_SLOT, 137, 59));		// Pattern slot
        this.addSlot(new DyeSlot(container, BundleBuilderEntity.DYE_SLOT, 119, 77));					// Dye slot
        
	    // Bundle Items Slots
		
		int invOffsetX = 11;
		int invOffsetY = 41;
		int invWidth = 3;
		int invHeight = 3;
		
	    for (int y = 0; y < invHeight; ++y) {
	        for (int x = 0; x < invWidth; ++x) {
	            this.addSlot(new IngredientSlot(container, 3 + x + y * invWidth, invOffsetX + x * invSlotSize, invOffsetY + y * invSlotSize, false));
	        }
	    }

		addPlayerSlots(29, 131);
	}

	@Sync
    public boolean lockPositions;
	@Sync public String patternName;

	@Override
	protected void updateState() {
		super.updateState();
		patternName = machine.getPatternName();
		lockPositions = machine.arePositionsLocked();
	}

	public void setPatternName(String name) {
		patternName = name;
		sendPatternSettings();
	}

	public void setLockPositions(boolean value) {
		lockPositions = value;
		sendPatternSettings();
	}

	void sendPatternSettings() {
		var settings = new SetPatternSettingsPacket();
		settings.setMenu(this);
		settings.name = patternName;
		settings.lockPositions = lockPositions;
		settings.sendToServer();
	}

	@Override
	public void receivePacketOnServer(RezolvePacket rezolvePacket) {
		if (rezolvePacket instanceof SetPatternSettingsPacket settings) {
			machine.setPatternName(settings.name);
			machine.setLockedPositions(settings.lockPositions);
		} else {
			super.receivePacketOnServer(rezolvePacket);
		}
	}
}
