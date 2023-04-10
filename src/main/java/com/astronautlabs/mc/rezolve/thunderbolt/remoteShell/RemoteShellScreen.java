package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.common.util.RezolveItemUtil;
import com.astronautlabs.mc.rezolve.thunderbolt.databaseServer.DatabaseServerEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

import java.util.Locale;
import java.util.Objects;

public class RemoteShellScreen extends MachineScreen<RemoteShellMenu> {
	public RemoteShellScreen(RemoteShellMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, "rezolve:textures/gui/container/remote_shell.png", 255, 212);

		titleLabelX = 12;
		titleLabelY = 8;
	}

	@Override
	protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
		this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
//		if (this.nameField.getVisible() && this.nameField.isFocused()) {
//			if (this.selectedMachine != null && !this.nameField.getText().equals(this.selectedMachineName)) {
//				this.selectedMachineName = this.nameField.getText();
//				this.entity.renameMachine(this.selectedMachine, this.selectedMachineName);
//			}
//		}

		return super.charTyped(pCodePoint, pModifiers);
	}

	@Override
	protected void init() {
		super.init();

		this.nameField = new EditBox(this.font, this.leftPos + 11, this.topPos + 120, 211, 18, Component.translatable("screens.rezolve.name"));
		this.nameField.setMaxLength(23);
		this.nameField.setValue("");
		this.nameField.changeFocus(false);
		this.nameField.setVisible(false);
		this.addRenderableWidget(this.nameField);

		this.searchField = new EditBox(this.font, this.leftPos + 56, this.topPos + 21, 165, 18, Component.translatable("screens.rezolve.search"));
		this.searchField.setMaxLength(23);
		this.searchField.setValue("");
		this.searchField.changeFocus(true);
		this.searchField.setVisible(true);
		this.addRenderableWidget(this.searchField);

		this.securedBtn = new Button(this.leftPos + 50, this.topPos + 167, 100, 18, Component.translatable("screens.rezolve.not_secured"), (button) -> {

			if (this.selectedMachineSecure) {
				this.securedBtn.setMessage(Component.translatable("screens.rezolve.secured"));
			} else {
				this.securedBtn.setMessage(Component.translatable("screens.rezolve.not_secured"));
			}
			this.selectedMachineSecure = !this.selectedMachineSecure;
		});
		this.securedBtn.visible = false;
		this.addRenderableWidget(this.securedBtn);
	}

	private static final int NAME_FIELD = 0;
	private static final int SEARCH_FIELD = 1;
	private static final int SECURED_BUTTON = 2;

	private Button securedBtn;
	private EditBox nameField;
	private EditBox searchField;

	private Container playerInv;
	private RemoteShellEntity entity;

	// Dimensions of each item within the remote machine list
	private int slotWidth = 207;
	private int slotHeight = 22;

	// Dimensions of the machine list
	private int listX = 13;
	private int listY = 47;
	private int listHeight = 85;
	private int listWidth = slotWidth;

//	@Override
//	public void handleMouseInput() throws IOException {
//		super.handleMouseInput();
//
//		BlockPos[] machines = this.entity.getConnectedMachines();
//
//		this.listScrollPosition += Mouse.getEventDWheel() / 10;
//		this.listScrollPosition = Math.min(0, this.listScrollPosition);
//		this.listScrollPosition = Math.max(Math.min(0, machines.length * this.slotHeight * -1 + this.listHeight), this.listScrollPosition);
//	}


	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (intersectsList(pMouseX, pMouseY)) {
			int max = 0;
			if (menu.searchResults != null)
				max = menu.searchResults.machines.size() * slotHeight - listHeight;

			listScrollPosition = Math.min(0, Math.max(-max, (int)(listScrollPosition + pDelta * 4)));
			return true;
		}

		return super.mouseScrolled(pMouseX, pMouseY, pDelta);

	}

	private MachineListing selectedMachine = null;
	private String selectedMachineName = null;

	private void selectMachine(MachineListing machine) {
		if (machine == null) {
			this.clearSelectedMachine();
			return;
		}

		this.selectedMachine = machine;

		ItemStack stack = machine.getItem();

    	if (stack != null) {

    		// Set UI properties based on this machine
    		this.selectedMachineSecure = false;
    		this.selectedMachineName = machine.getName();

    		// Set fields

    		if (selectedMachineName != null && !Objects.equals("", selectedMachineName))
    			nameField.setValue(this.selectedMachineName);
    		else
    			nameField.setValue("");

    		this.nameField.setVisible(menu.hasDatabase);
    		this.securedBtn.setMessage(Component.translatable(this.selectedMachineSecure ? "screens.rezolve.secured" : "screens.rezolve.not_secured"));

    		// TODO: need to add Security Server
    		//this.securedBtn.visible = true;

    	} else {
    		this.clearSelectedMachine();
    	}
	}

	private void clearSelectedMachine() {
		this.selectedMachine = null;
    	this.nameField.setVisible(false);
		this.securedBtn.visible = false;
	}

	boolean selectedMachineSecure = false;

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

		if (mouseButton == 1) {
			// Right click

		    int x = listX + leftPos;
		    int y = listY + topPos + this.listScrollPosition;

		    for (var machine : menu.searchResults.machines) {
				BlockPos pos = machine.getBlockPos();
		    	ItemStack stack = machine.getItem();
		    	if (stack == null)
		    		continue;
//		    	if (!this.matchesSearch(pos))
//		    		continue;

		    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight && mouseY > listY + topPos && mouseY < listY + listHeight + topPos) {
		    		this.selectMachine(machine);
		    		return true;
		    	}

		    	y += slotHeight;
		    }


		} else if (mouseButton == 0) {
			// Left click

		    int x = listX + leftPos;
		    int y = listY + topPos + this.listScrollPosition;

		    for (var machine : menu.searchResults.machines) {
				BlockPos pos = machine.getBlockPos();
		    	ItemStack stack = machine.getItem();

		    	if (stack == null)
		    		continue;
//		    	if (!this.matchesSearch(pos))
//		    		continue;

		    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight && mouseY > listY + topPos && mouseY < listY + listHeight + topPos) {
		    		this.activateMachine(machine);
		    		return true;
		    	}

		    	y += slotHeight;
		    }

		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void activateMachine(MachineListing machine) {
		menu.activate(machine, minecraft.player);
	}

    int iconWidth = 18;
    int iconHeight = 18;

    int listScrollPosition = 0;

    private boolean matchesSearch(BlockPos pos) {
    	if ("".equals(this.searchField.getValue()))
    		return true;

    	BlockState state = this.entity.getLevel().getBlockState(pos);

    	if (state.getBlock() == RezolveRegistry.block(RemoteShellBlock.class))
    		return false;

    	String searchString = this.searchField.getValue();
    	ItemStack stack = null; // should be the machine item

    	if (stack == null)
    		return false;

    	String name = stack.getDisplayName().getString();
    	String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();

    	if (name.toLowerCase(Locale.ROOT).contains(searchString.toLowerCase()) || subName.toLowerCase().contains(searchString.toLowerCase()))
    		return true;

	    DatabaseServerEntity db = this.entity.getDatabase();
    	if (db != null) {
    		String customName = db.getMachineName(pos);
    		if (customName != null && !"".equals(customName)) {
    			if (customName.toLowerCase(Locale.ROOT).contains(searchString.toLowerCase()))
    				return true;
    		}
    	}

    	return false;
    }

	private boolean intersectsList(double mouseX, double mouseY) {
		return leftPos + listX < mouseX && leftPos + listX + listWidth > mouseX && topPos + listY < mouseY && topPos + listY + listHeight > mouseY;
	}

    @Override
    protected void renderSubWindows(PoseStack pPoseStack, double mouseX, double mouseY) {
		enableScissor(leftPos + listX, topPos + listY, leftPos + listX + listWidth + 1, topPos + listY + listHeight);
		colorQuad(pPoseStack, 0xFFCCCCCC, listX - 10, listY - 10, listWidth + 20, listHeight + 20);

		int scrollOffset = this.listScrollPosition;
	    int x = listX;
	    int y = listY + scrollOffset;

	    for (var machine : this.menu.searchResults.machines) {
	    	ItemStack stack = machine.getItem();
	    	if (stack == null)
	    		continue;
//	    	if (!this.matchesSearch(pos))
//	    		continue;

	    	boolean highlighted =
	    		x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight
	    		&& y > 0 && y < this.listY + this.listHeight
	    		&& mouseY < this.listY + this.listHeight
	    	;

	    	if (highlighted) {
				colorQuad(pPoseStack, 0xFF44FFFF, x, y, slotWidth, slotHeight);
	    	}

	    	y += slotHeight;
	    }

	    x = listX;
	    y = listY + scrollOffset;

	    for (var machine : menu.searchResults.machines) {

			BlockPos pos = machine.getBlockPos();
	    	ItemStack stack = machine.getItem();
	    	if (stack == null)
	    		continue;

//	    	if (!this.matchesSearch(pos))
//	    		continue;

	    	if (y > 0 && y + slotHeight < height) {
		    	this.drawItem(pPoseStack, stack, x, y);

		    	String name = stack.getDisplayName().getString();
		    	String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();

				String customName = machine.getName();
				if (!Objects.equals(name, customName)) {
					subName = name+" ["+subName+"]";
					name = customName;
				}

		    	this.font.draw(
						pPoseStack,
		    		name,
		    		x + this.iconWidth + 2,
		    		y + 1,
		    		0xFF000000
		    	);

		    	this.font.draw(
						pPoseStack,
		    		subName,
		    		x + this.iconWidth + 2,
		    		y + 10,
		    		0xFF666666
		    	);
	    	}

	    	y += slotHeight;

	    }

		disableScissor();
    }

    @Override
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);

		minecraft.font.draw(pPoseStack, "Search: ", searchField.x - leftPos - 45, searchField.y - topPos + 5, 0xFF000000);

        int rfBarX = 231;
        int rfBarY = 40;
        int rfBarHeight = 88;
        int rfBarWidth = 14;

		double usedHeight = menu.energyStored / (double)menu.energyCapacity * rfBarHeight;
		colorQuad(pPoseStack, 0, 0, 0, 1, rfBarX, rfBarY, rfBarWidth, rfBarHeight - usedHeight);

        this.nameField.setVisible(this.selectedMachine != null && menu.hasDatabase);

		int infoSectionX = 10;
		int infoSectionY = 135;
		int lineHeight = 15;

        if (this.selectedMachine != null) {
			var machine = this.selectedMachine;
			BlockPos pos = machine.getBlockPos();
            ItemStack stack = machine.getItem();

            if (stack != null) {
                String stackName = RezolveItemUtil.getName(stack);
                String position = String.format("Position: %d, %d, %d", pos.getX(), pos.getY(), pos.getZ());

                if (!this.nameField.isVisible()) {
                    this.font.draw(pPoseStack, stackName, infoSectionX, infoSectionY + lineHeight*0, 0xFF000000);
                    this.font.draw(pPoseStack, position, infoSectionX, infoSectionY + lineHeight*1, 0xFF666666);
                } else {
                    this.font.draw(pPoseStack, stackName, infoSectionX, infoSectionY + lineHeight*0, 0xFF666666);
                    this.font.draw(pPoseStack, position, infoSectionX, infoSectionY + lineHeight*1, 0xFF666666);
                }
            }
        } else {

            this.font.draw(pPoseStack, "Right click a machine for info.", infoSectionX, infoSectionY, 0xFF666666);
        }
    }
}
