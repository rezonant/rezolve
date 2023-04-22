package com.rezolvemc.thunderbolt.remoteShell;

import org.torchmc.widgets.ListView;
import org.torchmc.widgets.ListViewItem;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.util.RezolveItemUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public class RemoteShellScreen extends MachineScreen<RemoteShellMenu> {
	public RemoteShellScreen(RemoteShellMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 255, 212);
		twoToneHeight = 0;
		enableInventoryLabel = false;
	}

	private ListView listView;
	private Button securedBtn;
	private EditBox nameField;
	private EditBox searchField;
	private int slotWidth = 207;
	private int slotHeight = 22;
	private MachineListing selectedMachine = null;
	private String selectedMachineName = null;

	@Override
	protected void setup() {
		super.setup();

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

		addEnergyMeter(leftPos + 230, topPos + 43, 95);

		listView = addRenderableWidget(new ListView(
				Component.translatable("screens.rezolve.machine_list"),
				leftPos + 13, topPos + 47, slotWidth, 81
		));

		listView.setItemPadding(2);
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

	private void activateMachine(MachineListing machine) {
		menu.activate(machine, minecraft.player);
	}

    int iconWidth = 18;
    int iconHeight = 18;

    private boolean matchesSearch(BlockPos pos) {
//    	if ("".equals(this.searchField.getValue()))
//    		return true;
//
//    	BlockState state = this.entity.getLevel().getBlockState(pos);
//
//    	if (state.getBlock() == RezolveRegistry.block(RemoteShellBlock.class))
//    		return false;
//
//    	String searchString = this.searchField.getValue();
//    	ItemStack stack = null; // should be the machine item
//
//    	if (stack == null)
//    		return false;
//
//    	String name = stack.getDisplayName().getString();
//    	String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();
//
//    	if (name.toLowerCase(Locale.ROOT).contains(searchString.toLowerCase()) || subName.toLowerCase().contains(searchString.toLowerCase()))
//    		return true;
//
//	    DatabaseServerEntity db = this.entity.getDatabase();
//    	if (db != null) {
//    		String customName = db.getMachineName(pos);
//    		if (customName != null && !"".equals(customName)) {
//    			if (customName.toLowerCase(Locale.ROOT).contains(searchString.toLowerCase()))
//    				return true;
//    		}
//    	}

    	return false;
    }

    @Override
    protected void renderSubWindows(PoseStack pPoseStack, double mouseX, double mouseY) {
		super.renderSubWindows(pPoseStack, mouseX, mouseY);
    }

	MachineListingSearchResults currentResults;

	private void loadResults() {
		listView.clearItems();
		currentResults = menu.searchResults;
		for (var machine : currentResults.machines) {
			listView.addItem(new MachineListViewItem(machine));
		}
	}

	public class MachineListViewItem implements ListViewItem {
		public MachineListViewItem(MachineListing listing) {
			this.machine = listing;
		}

		private MachineListing machine;

		public MachineListing getMachine() {
			return machine;
		}

		@Override
		public void render(PoseStack pPoseStack, int width, int mouseX, int mouseY, float partialTicks) {
			var stack = machine.getItem();
			var pos = machine.getBlockPos();

			drawItem(pPoseStack, stack, 2, 1);

			String name = stack.getDisplayName().getString();
			String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();

			String customName = machine.getName();
			if (!Objects.equals(name, customName)) {
				subName = name+" ["+subName+"]";
				name = customName;
			}

			font.draw(
					pPoseStack,
					name,
					iconWidth + 2,
					1,
					0xFF000000
			);

			font.draw(
					pPoseStack,
					subName,
					iconWidth + 2,
					10,
					0xFF666666
			);
		}

		@Override
		public void mouseClicked(int button) {
			if (button == 0) {
				activateMachine(machine);
			} else if (button == 1) {
				selectMachine(machine);
			}
		}

		@Override
		public int getHeight() {
			return slotHeight;
		}
	}

    @Override
    protected void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float pPartialTick) {
		if (currentResults != menu.searchResults) {
			loadResults();
		}

        super.renderContents(pPoseStack, mouseX, mouseY, pPartialTick);

		minecraft.font.draw(pPoseStack, "Search: ", searchField.x - leftPos - 45, searchField.y - topPos + 5, 0xFF000000);

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
