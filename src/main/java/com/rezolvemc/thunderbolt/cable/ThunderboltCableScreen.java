package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.common.util.RezolveDirectionUtil;
import net.minecraft.ChatFormatting;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.layout.Panel;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.util.Size;
import org.torchmc.widgets.Button;
import org.torchmc.widgets.Label;
import com.rezolvemc.common.machines.MachineScreen;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ThunderboltCableScreen extends MachineScreen<ThunderboltCableMenu> {
    private static final Component SELECT_A_SIDE = Component.translatable("rezolve.thunderbolt.select_a_side");

    public ThunderboltCableScreen(ThunderboltCableMenu menu, Inventory playerInventory, Component pTitle) {
        super(menu, playerInventory, pTitle, 256, 256);

        enableInventoryLabel = false;
    }

    private static int transmissionTypesX = 150;
    private static int transmissionTypesY = 145;
    private static int transmissionTypeHeight = 20;
    private static int transmissionTypeWidth = 100;
    private static int transmissionTypeGap = 4;
    private static BakedModel PLATE_PREVIEW;

    private int itemX = 128;
    private int itemY = 80;
    private int nextTransmissionTypeY = transmissionTypesY;
    private boolean rotationInitialized = false;

    private Label selectASideLbl;
    private Panel sideConfigPanel;
    private Label sideInfoLbl;
    private Map<TransmissionType, Button> transmissionTypeButtons = new HashMap<>();
    private int blockViewHeight = 150;
    private ThunderboltEndpointConfigurator configurator;

    @Override
    protected void setup() {
        super.setup();

        setPanel(new VerticalLayoutPanel(), root -> {
            root.setSpace(4);

            root.addChild(new Configurator(), configurator -> {
                configurator.setGrowScale(1);
                configurator.setVisible(menu != null); // until we have a menu

                if (this.configurator != null) {
                    configurator.setSelectedSide(this.configurator.getSelectedSide());
                    configurator.setBlockState(this.configurator.getBlockState());
                }

                this.configurator = configurator;
            });

            root.addChild(new Label(SELECT_A_SIDE), label -> {
                label.setVisible(configurator.getSelectedSide() == null);
                selectASideLbl = label;
            });

            root.addChild(new HorizontalLayoutPanel(), panel -> {
                panel.setVisible(configurator.getSelectedSide() != null);
                panel.setSpace(4);

                sideConfigPanel = panel;

                panel.addChild(new Label(), label -> {
                    label.setGrowScale(1);

                    if (sideInfoLbl != null) {
                        label.setContent(sideInfoLbl.getContent());
                    }
                    sideInfoLbl = label;
                });
                panel.addChild(new VerticalLayoutPanel(), buttons -> {
                    nextTransmissionTypeY = transmissionTypesY;
                    transmissionTypeButtons.clear();

                    for (var type : TransmissionType.values()) {
                        buttons.addChild(new Button(), button -> {
                            button.setHandler(btn ->
                                    menu.cycleTransmissionMode(configurator.getSelectedSide(), type, btn == 0 ? 1 : -1)
                            );
                            button.setDesiredSize(new Size(transmissionTypeWidth, transmissionTypeHeight));
                            transmissionTypeButtons.put(type, button);
                        });
                    }

                });
            });
        });
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        configurator.setBlockState(Block.stateById(menu.targetBlockId));
        configurator.setVisible(true); // now that we have a menu

        if (!rotationInitialized && menu.position != null) {
            var vec = minecraft.player.position().subtract(Vec3.atCenterOf(menu.position)).multiply(1, -1, 1);
            var xRot = -angle2d(new Vec2((float)vec.x, (float)vec.z), Vec2.UNIT_X) + 90;
            //var yRot = angle2d(new Vec2((float)vec.x, (float)vec.y), Vec2.UNIT_X) + 90;

            configurator.setRotation((float)xRot, configurator.getRotationY());
            rotationInitialized = true;
        }

        if (configurator.getSelectedSide() != null) {
            for (var entry : transmissionTypeButtons.entrySet()) {
                var faceConfig = menu.configuration.getFace(configurator.getSelectedSide());
                var transmitConfig = faceConfig.getTransmissionConfiguration(entry.getKey());

                entry.getValue().setActive(transmitConfig.isSupported());
                entry.getValue().setAlpha(transmitConfig.isSupported() ? 1 : 0.5f);
                entry.getValue().setText(
                        Component.empty()
                                .append(transmitConfig.getType().translation())
                                .append(": ")
                                .append(transmitConfig.getMode().translation())
                );
            }
        }
    }

    private double angle2d(Vec2 a, Vec2 b) {
        var det = a.x * b.y - a.y * b.x;
        return Math.atan2(det, a.dot(Vec2.UNIT_X)) * (180 / Math.PI);
    }

    public class Configurator extends ThunderboltEndpointConfigurator {
        public Configurator() {
            super((dir) -> menu.configuration.getFace(dir));
        }

        @Override
        protected void selectedSideDidChange(Direction selectedSide) {
            selectASideLbl.setVisible(selectedSide == null);
            sideConfigPanel.setVisible(selectedSide != null);

            if (selectedSide != null) {
                // TODO
                sideInfoLbl.setContent(
                        Component
                                .empty()
                                .append(RezolveDirectionUtil.friendly(selectedSide)).append("\n")
                                .append("\n")
                                .append("Here's some ")
                                .append(Component.literal("cool").withStyle(ChatFormatting.ITALIC))
                                .append(" stuff. Bacon ipsum dolor amet alcatra bacon cupim tail. Meatball shoulder kielbasa leberkas, pork shank pork belly pork loin jerky sausage chislic cupim pork chop prosciutto.")
                );
            }
        }
    }
}
