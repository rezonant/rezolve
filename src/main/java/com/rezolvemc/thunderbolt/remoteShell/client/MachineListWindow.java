package com.rezolvemc.thunderbolt.remoteShell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.Rezolve;
import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.util.RezolveItemUtil;
import com.rezolvemc.thunderbolt.remoteShell.common.MachineListing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.torchmc.ui.Window;
import org.torchmc.ui.layout.*;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.widgets.*;
import org.torchmc.ui.util.TorchUtil;
import org.torchmc.util.Values;

import java.util.Objects;

public class MachineListWindow extends Window {
    public MachineListWindow() {
        super(Component.empty());
    }

    private Panel detailsLayout;
    private ListView listView;
    private Button securedBtn;
    private EditBox nameField;
    private EditBox searchField;
    private MachineListing selectedMachine = null;
    private Component selectedMachineName = null;
    private Label hintLbl;
    private Label infoLbl;

    @Override
    protected void setup() {
        super.setup();

        removeWhenDisposed(getSession().addEventListener(RemoteAccessClientSession.RESULTS_RECEIVED, event -> loadResults()));
        setTitle(Rezolve.str("machines"));

        setPanel(new AxisLayoutPanel(Axis.Y), root -> {
            root.addChild(new HorizontalLayoutPanel(), topLayout -> {
                topLayout.setExpansionFactor(1);
                topLayout.setAlignment(AxisAlignment.CENTER);
                topLayout.addChild(new VerticalLayoutPanel(), storageLayout -> {
                    storageLayout.setExpansionFactor(1);

                    // Search label + field

                    storageLayout.addChild(new HorizontalLayoutPanel(), panel -> {
                        panel.addChild(new Label(Component.translatable("screens.rezolve.search")), label -> {
                            //label.setAlignment(Label.Alignment.CENTERED);
                            label.setVerticalAlignment(Label.VerticalAlignment.CENTER);
                        });
                        panel.addChild(new EditBox(Component.translatable("screens.rezolve.search")), field -> {
                            field.setMaxLength(23);
                            field.setExpansionFactor(1);
                            field.addEventListener(EditBox.VALUE_CHANGED, e -> updateMachineList());
                            searchField = field;
                        });
                    });

                    storageLayout.addChild(new ListView(Component.translatable("screens.rezolve.machine_list")), listView -> {
                        listView.setExpansionFactor(1);
                        listView.setItemPadding(2);

                        this.listView = listView;
                    });
                });

                topLayout.addChild(new EnergyMeter());
            });

            root.addChild(new Label("Right click a machine for info."), label -> {
                hintLbl = label;
            });

            root.addChild(new VerticalLayoutPanel(), detailsLayout -> {
                detailsLayout.setVisible(false);
                this.detailsLayout = detailsLayout;

                detailsLayout.addChild(new EditBox(Component.translatable("screens.rezolve.name")), field -> {
                    field.setMaxLength(23);
                    field.setVisible(false);
                    field.addEventListener(field.ACTIVATED, e -> setName(selectedMachine, e.value));

                    nameField = field;
                });

                detailsLayout.addChild(new AxisLayoutPanel(Axis.X), panel -> {
                    panel.addChild(new Label(), label -> {
                        label.setExpansionFactor(1);
                        infoLbl = label;
                    });

                    panel.addChild(new AxisLayoutPanel(Axis.Y), buttons -> {
                        buttons.addChild(new Button(), button -> {
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

        updateMachineList();
    }

    private void updateMachineList() {
        getSession().updateMachineList(this.searchField.getValue(), 0, 0);
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
        this.selectedMachineName = machine.getName() != null ? Component.literal(machine.getName()) : machine.getItem().getHoverName();

        // Set fields

        if (selectedMachineName != null && !Objects.equals("", selectedMachineName))
            nameField.setValue(this.selectedMachineName.getString());
        else
            nameField.setValue("");

        this.nameField.setVisible(getSession().hasDatabase);
        this.securedBtn.setText(Component.translatable(this.selectedMachineSecure ? "screens.rezolve.secured" : "screens.rezolve.not_secured"));

        BlockPos pos = machine.getBlockPos();

        var label = Component.empty();

        String stackName = RezolveItemUtil.getName(stack);
        String position = String.format("Position: %d, %d, %d", pos.getX(), pos.getY(), pos.getZ());

        if (!nameField.isVisible())
            label.append(Component.literal(stackName).withStyle(ChatFormatting.BLACK)).append("\n");

        label.append(Component.literal(position).withStyle(ChatFormatting.DARK_GRAY));

        infoLbl.setContent(label);

        // TODO: need to add Security Server
        //this.securedBtn.visible = true;
    }

    private void setName(MachineListing machine, String name) {
        getSession().setMachineName(machine, name);
        updateMachineList();
    }

    private void clearSelectedMachine() {
        selectedMachine = null;
        detailsLayout.setVisible(false);
        hintLbl.setVisible(true);
    }

    boolean selectedMachineSecure = false;

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

    private RemoteAccessClientSession getSession() {
        return RemoteAccessClientSession.INSTANCE;
    }

    private void loadResults() {
        listView.clearItems();
        for (var machine : getSession().machines) {
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
        public void render(GuiGraphics gfx, int width, int mouseX, int mouseY, float partialTicks) {
            var stack = machine.getItem();
            var pos = machine.getBlockPos();

            TorchUtil.drawItem(gfx, stack, 2, 2);

            int distance = (int)minecraft.player.position().distanceToSqr(Vec3.atCenterOf(pos));
            String name = stack.getHoverName().getString();
            String subName = String.format("%dm away", distance);

            String customName = machine.getName();
            if (!Values.isNullOrEmpty(customName) && !Objects.equals(name, customName)) {
                subName = subName + " [" + name + "]";
                name = customName;
            }

            int textY = 2;
            gfx.drawString(font, name, iconWidth + 2, textY, 0xFF000000, false);
            gfx.drawString(font, subName, iconWidth + 2, textY + font.lineHeight + 1, 0xFF666666, false);
        }

        @Override
        public void mouseClicked(int button) {
            if (button == 0) {
                getSession().connectToMachine(machine);
            } else if (button == 1) {
                selectMachine(machine);
            }
        }

        @Override
        public int getHeight() {
            return 22;
        }
    }

}
