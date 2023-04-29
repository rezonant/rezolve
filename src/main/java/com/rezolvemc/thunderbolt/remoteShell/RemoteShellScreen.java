package com.rezolvemc.thunderbolt.remoteShell;

import com.rezolvemc.common.gui.EnergyMeter;
import net.minecraft.ChatFormatting;
import org.torchmc.layout.*;
import org.torchmc.widgets.*;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.util.RezolveItemUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public class RemoteShellScreen extends MachineScreen<RemoteShellMenu> {

	public RemoteShellScreen(RemoteShellMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 255, 212);
		enableInventoryLabel = false;
	}

	private Panel detailsLayout;
	private ListView listView;
	private Button securedBtn;
	private EditBox nameField;
	private EditBox searchField;
	private int slotWidth = 207;
	private int slotHeight = 22;
	private MachineListing selectedMachine = null;
	private Component selectedMachineName = null;
	private Label hintLbl;
	private Label infoLbl;

	@Override
	protected void setup() {
		super.setup();

		setPanel(new AxisLayoutPanel(Axis.Y), root -> {
			root.addChild(new AxisLayoutPanel(Axis.X), topLayout -> {
				topLayout.setGrowScale(1);
				topLayout.addChild(new AxisLayoutPanel(Axis.Y), storageLayout -> {
					storageLayout.setGrowScale(1);

					// Search label + field

					storageLayout.addChild(new AxisLayoutPanel(Axis.X), panel -> {
						panel.addChild(new Label(Component.translatable("screens.rezolve.search")), label -> {
							//label.setAlignment(Label.Alignment.CENTERED);
							label.setVerticalAlignment(Label.VerticalAlignment.CENTER);
						});
						panel.addChild(new EditBox(Component.translatable("screens.rezolve.search")), field -> {
							field.setMaxLength(23);
							field.setGrowScale(1);
							searchField = field;
						});
					});

					storageLayout.addChild(new ListView(Component.translatable("screens.rezolve.machine_list")), listView -> {
						listView.setGrowScale(1);
						listView.setItemPadding(2);

						this.listView = listView;
					});
				});

				topLayout.addChild(new EnergyMeter());
			});

			root.addChild(new Label("Right click a machine for info."), label -> {
				hintLbl = label;
			});

			root.addChild(new AxisLayoutPanel(Axis.Y), detailsLayout -> {
				detailsLayout.setVisible(false);
				this.detailsLayout = detailsLayout;

				detailsLayout.addChild(new EditBox(Component.translatable("screens.rezolve.name")), field -> {
					field.setMaxLength(23);
					field.setVisible(false);
					nameField = field;
				});

				detailsLayout.addChild(new AxisLayoutPanel(Axis.X), panel -> {
					panel.addChild(new Label(), label -> {
						label.setGrowScale(1);
						infoLbl = label;
					});

					panel.addChild(new AxisLayoutPanel(Axis.Y), buttons -> {
						buttons.addChild(new org.torchmc.widgets.Button(), button -> {
							button.setHandler(mouseButton -> {
								if (this.selectedMachineSecure) {
									this.securedBtn.setText(Component.translatable("screens.rezolve.secured"));
								} else {
									this.securedBtn.setText(Component.translatable("screens.rezolve.not_secured"));
								}
								this.selectedMachineSecure = !this.selectedMachineSecure;
							});
							button.setVisible(false);

							securedBtn = button;
						});
					});
				});
			});
		});
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
		hintLbl.setVisible(false);
		detailsLayout.setVisible(true);

		ItemStack stack = machine.getItem();

		if (stack == null) {
			clearSelectedMachine();
			return;
		}

		// Set UI properties based on this machine
		this.selectedMachineSecure = false;
		this.selectedMachineName = machine.getName() != null ? Component.literal(machine.getName()) : machine.getItem().getDisplayName();

		// Set fields

		if (selectedMachineName != null && !Objects.equals("", selectedMachineName))
			nameField.setValue(this.selectedMachineName.toString());
		else
			nameField.setValue("");

		this.nameField.setVisible(menu.hasDatabase);
		this.securedBtn.setText(Component.translatable(this.selectedMachineSecure ? "screens.rezolve.secured" : "screens.rezolve.not_secured"));

		BlockPos pos = machine.getBlockPos();

		String stackName = RezolveItemUtil.getName(stack);
		String position = String.format("Position: %d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
		infoLbl.setContent(
				Component.empty()
						.append(Component.literal(stackName).withStyle(ChatFormatting.BLACK))
						.append("\n")
						.append(Component.literal(position).withStyle(ChatFormatting.GRAY))
		);

		// TODO: need to add Security Server
		//this.securedBtn.visible = true;
	}

	private void clearSelectedMachine() {
		selectedMachine = null;
		detailsLayout.setVisible(false);
		hintLbl.setVisible(true);
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
	public void updateStateFromMenu() {
		if (currentResults != menu.searchResults) {
			loadResults();
		}
	}
}
