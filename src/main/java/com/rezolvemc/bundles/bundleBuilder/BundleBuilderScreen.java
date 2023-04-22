package com.rezolvemc.bundles.bundleBuilder;

import com.rezolvemc.common.machines.MachineScreen;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BundleBuilderScreen extends MachineScreen<BundleBuilderMenu> {

	public BundleBuilderScreen(BundleBuilderMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 218, 212);

		inventoryLabelX = 30;
		inventoryLabelY = 116;
	}

	@Override
	protected void init() {
		super.init();

		addEnergyMeter(leftPos + 191, topPos + 17, 88);
		//addProgressIndicator(leftPos + 117, topPos + 60, Component.literal(""), () -> 0.0);
		addSlotGrid(Component.translatable("screens.rezolve.items"), 3, 3, 9);
		addSlot(Component.translatable("screens.rezolve.in"), BundleBuilderEntity.PATTERN_INPUT_SLOT, true);
		addSlot(Component.translatable("screens.rezolve.out"), BundleBuilderEntity.PATTERN_OUTPUT_SLOT, true);
		addSlot(Component.translatable("screens.rezolve.dye"), BundleBuilderEntity.DYE_SLOT, true);

		addLabel(Component.translatable("screens.rezolve.name"), leftPos + 83, topPos + 41 - font.lineHeight - 4);

		nameField = new EditBox(font, leftPos + 83, topPos + 41, 88, 13, Component.translatable("screens.rezolve.name"));
		nameField.setMaxLength(23);
		addRenderableWidget(nameField);

		lockPositions = CycleButton
				.booleanBuilder(
						Component.translatable("screens.rezolve.locked"),
						Component.translatable("screens.rezolve.unlocked")
				)
				.displayOnlyValue()
				.create(
						leftPos + 10, topPos + 94, 54, 20,
						Component.translatable("screens.rezolve.positions"),
						(btn, value) -> menu.setLockPositions(value)
				)
		;

		this.addRenderableWidget(lockPositions);
	}

	private EditBox nameField;
	private CycleButton lockPositions;

	@Override
	public void updateStateFromMenu() {
		if ((boolean)lockPositions.getValue() != menu.lockPositions)
			lockPositions.setValue(menu.lockPositions);

		nameField.setValue(menu.patternName);
		super.updateStateFromMenu();
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		var result = super.charTyped(pCodePoint, pModifiers);
		if (result && this.nameField.isFocused())
			menu.setPatternName(this.nameField.getValue());

		return result;
	}
}
