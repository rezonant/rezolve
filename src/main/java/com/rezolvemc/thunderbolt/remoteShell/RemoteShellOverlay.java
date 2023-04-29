package com.rezolvemc.thunderbolt.remoteShell;

import com.rezolvemc.Rezolve;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellEntityReturnPacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStatePacket;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.torchmc.TorchScreen;
import org.torchmc.Window;
import org.torchmc.layout.*;
import org.torchmc.widgets.Button;
import org.torchmc.widgets.Label;
import org.torchmc.widgets.Meter;

@Mod.EventBusSubscriber(modid = Rezolve.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RemoteShellOverlay implements IGuiOverlay {
	public static RemoteShellOverlay INSTANCE = new RemoteShellOverlay();
	private static Logger LOGGER = Rezolve.logger(RemoteShellOverlay.class);

	@SubscribeEvent
	public static void registerWindow(ScreenEvent.Init.Post event) {
		var screen = event.getScreen();

		var overlay = new Window("Remote Shell");

		overlay.setClosable(false);

		overlay.setOnTick(() -> {
			var state = RemoteShellOverlay.INSTANCE.state;
			overlay.setVisible(state != null && state.active);
		});

		overlay.setPanel(new HorizontalLayoutPanel(), status -> {
			status.setAlignment(AxisAlignment.CENTER);
			status.addChild(new Label(""), label -> {
				label.setGrowScale(1);
				label.setOnTick(() -> {
					var state = RemoteShellOverlay.INSTANCE.state;
					if (state != null && state.activeMachine != null) {
						var itemStack = state.activeMachine.getItem();
						var itemName = itemStack.getItem().getName(itemStack).plainCopy();

						if (state.activeMachine.getName() != null) {
							label.setContent(
									Component.empty()
											.append(Component.literal(state.activeMachine.getName()))
											.append(
													Component.empty()
															.withStyle(ChatFormatting.GRAY)
															.append(" (")
															.append(itemName)
															.append(")")
											)
							);
						} else {
							label.setContent(itemName);
						}
					}
				});
			});
			status.addChild(new Meter(Component.literal("Energy"), Component.literal("FE"), Rezolve.tex("gui/widgets/energy_meter.png")), meter -> {
				meter.setOrientation(Axis.X);
				meter.setWidthConstraint(AxisConstraint.atLeast(40));
				meter.setOnTick(() -> {
					var state = RemoteShellOverlay.INSTANCE.state;
					if (state == null)
						return;

					meter.setMax(state.remoteShellEnergyCapacity);
					meter.setValue(state.remoteShellEnergy);
				});
			});
			status.addChild(new Button("Return"), button -> {
				button.setHandler(btn -> {
					var state = RemoteShellOverlay.INSTANCE.state;

					if (state == null)
						return;

					System.out.println("You tried to return to the remote shell!");
					var returnMessage = new RemoteShellEntityReturnPacket();
					returnMessage.dimension = state.remoteShellDimension;
					returnMessage.blockPos = state.remoteShellPosition;
					returnMessage.sendToServer();
				});
			});
		});

		if (event.getScreen() instanceof TorchScreen<?> torchScreen) {
			torchScreen.addWindow(overlay);
		} else {
			event.addListener(overlay);
		}

		int xPadding = 6;
		overlay.move(xPadding, 3, screen.width - xPadding*2, 45);
		overlay.setTitleBarVisible(false);
	}

	public RemoteShellStatePacket state;
	public void updateState(RemoteShellStatePacket state) {
		this.state = state;
	}

	public RemoteShellOverlay() {
		this.mc = Minecraft.getInstance();
	}

	Minecraft mc;

	@Override
	public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
		if (state == null || !state.active || state.activeMachine == null)
			return;

		this.onMouseEvent(mc.mouseHandler.xpos(), mc.mouseHandler.ypos(), mc.mouseHandler.isLeftPressed());


		Lighting.setupForFlatItems();

		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();

		MachineListing machine = state.activeMachine;
    	BlockPos pos = machine.getBlockPos();
    	ItemStack stack = machine.getItem();

    	int x = 4;
    	int y = 4;
		if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
			y = screen.getGuiTop() - 15 * 2;
			x = screen.getGuiLeft();

			var menu = screen.getMenu();
			if (menu != null) {
				menu.addSlotListener(new ContainerListener() {
					@Override
					public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
						LOGGER.info("Slot changed: " + pDataSlotIndex + ", item: " + pStack.toString());
					}

					@Override
					public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
						LOGGER.info("Data changed: " + pDataSlotIndex + ", value: " + pValue);
					}
				});
			}
		}

    	if (stack == null) {
    		x += drawText(poseStack, "No activated machine.  ", x, y, 0xFFAA00);
    	} else {
    		x += drawText(poseStack, stack.getDisplayName(), x, y, 0xFFAA00);
    	}

    	this.remoteShellX = x;
    	this.remoteShellY = y;
    	x += (this.remoteShellWidth = drawText(poseStack, "Remote Shell ", x, 4, this.remoteShellColor));

		var energy = 0; // this.entity.getEnergyStored(Direction.UP) // TODO
    	x += drawText(poseStack, energy+" FE ", x, y, 0xff4f63);

	}

	private int remoteShellX;
	private int remoteShellY;
	private int remoteShellWidth;
	private int fontHeight = 16;
	private int remoteShellColor = 0xbc7100;
	private int remoteShellColorHover = 0xffc300;
	private int remoteShellColorNormal = 0xbc7100;

	private int drawText(PoseStack poseStack, String text, int x, int y, int color) {
		return drawText(poseStack, Component.literal(text), x, y, color);
	}

	private int drawText(PoseStack poseStack, Component text, int x, int y, int color) {
		mc.font.draw(poseStack, text, x, y, color);
		return mc.font.width(text);
	}

	private boolean mouseDown = false;

	private void onMouseEvent(double x, double y, boolean clicking) {
		float scaleFactor = (float)mc.getWindow().getGuiScale();
		int mx = (int)(x * scaleFactor);
		int my = (int)((this.mc.getWindow().getHeight() - y) * scaleFactor);

		boolean hitRemoteShell =
			mx > remoteShellX && mx < remoteShellX + remoteShellWidth
			&& my > remoteShellY && my < remoteShellY + fontHeight
		;

		if (!this.mouseDown && clicking) {
			this.mouseDown = true;
			if (hitRemoteShell) {
				System.out.println("You tried to return to the remote shell!");
				var returnMessage = new RemoteShellEntityReturnPacket();
				returnMessage.dimension = state.remoteShellDimension;
				returnMessage.blockPos = state.remoteShellPosition;
				returnMessage.sendToServer();
			}
		} else {
			this.mouseDown = false;
			this.remoteShellColor = hitRemoteShell ? this.remoteShellColorHover : this.remoteShellColorNormal;
		}
	}

}
