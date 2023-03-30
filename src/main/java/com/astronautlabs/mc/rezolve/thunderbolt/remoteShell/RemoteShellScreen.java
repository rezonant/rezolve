//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import java.io.IOException;
//
//import com.astronautlabs.mc.rezolve.common.BaseScreen;
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.EditBox;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.Container;
//import net.minecraft.world.entity.player.Inventory;
//
//import com.astronautlabs.mc.rezolve.RezolveMod;
//import com.astronautlabs.mc.rezolve.databaseServer.DatabaseServerEntity;
//
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Gui;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.core.Direction;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.BlockHitResult;
//
//public class RemoteShellScreen extends BaseScreen {
//
//	public RemoteShellScreen(RemoteShellMenu menu, Inventory playerInv) {
//		super(menu, playerInv, Component.literal("Remote Shell"), "rezolve:textures/gui/container/remote_shell.png", 255, 212);
//	}
//
//	@Override
//	public boolean charTyped(char pCodePoint, int pModifiers) {
////		if (this.nameField.getVisible() && this.nameField.isFocused()) {
////			if (this.selectedMachine != null && !this.nameField.getText().equals(this.selectedMachineName)) {
////				this.selectedMachineName = this.nameField.getText();
////				this.entity.renameMachine(this.selectedMachine, this.selectedMachineName);
////			}
////		}
//
//		return super.charTyped(pCodePoint, pModifiers);
//	}
//
//	@Override
//	protected void init() {
//		super.init();
//
//		this.nameField = new EditBox(this.font, this.leftPos + 11, this.topPos + 120, 211, 18, Component.literal("Name"));
//		this.nameField.setMaxLength(23);
//		this.nameField.setValue("");
//		this.nameField.changeFocus(false);
//		this.nameField.setVisible(false);
//		this.addRenderableWidget(this.nameField);
//
//		this.searchField = new EditBox(this.font, this.leftPos + 11, this.topPos + 5, 211, 18, Component.literal("Search"));
//		this.searchField.setMaxLength(23);
//		this.searchField.setValue("");
//		this.searchField.changeFocus(true);
//		this.searchField.setVisible(true);
//		this.addRenderableWidget(this.searchField);
//
//		this.securedBtn = new Button(this.leftPos + 50, this.topPos + 167, 100, 18, Component.literal("Not Secured"), (button) -> {
//
//			if (this.selectedMachineSecure) {
//				this.securedBtn.setMessage(Component.literal("Not Secured"));
//			} else {
//				this.securedBtn.setMessage(Component.literal("Secured"));
//			}
//			this.selectedMachineSecure = !this.selectedMachineSecure;
//		});
//		this.securedBtn.visible = false;
//		this.addRenderableWidget(this.securedBtn);
//	}
//
//	private static final int NAME_FIELD = 0;
//	private static final int SEARCH_FIELD = 1;
//	private static final int SECURED_BUTTON = 2;
//
//	private Button securedBtn;
//	private EditBox nameField;
//	private EditBox searchField;
//
//	private Container playerInv;
//	private RemoteShellEntity entity;
//
//	private int listX = 13;
//	private int listY = 33;
//	private int listHeight = 85;
//	private int slotWidth = 207;
//	private int slotHeight = 22;
//
////	@Override
////	public void handleMouseInput() throws IOException {
////		super.handleMouseInput();
////
////		BlockPos[] machines = this.entity.getConnectedMachines();
////
////		this.listScrollPosition += Mouse.getEventDWheel() / 10;
////		this.listScrollPosition = Math.min(0, this.listScrollPosition);
////		this.listScrollPosition = Math.max(Math.min(0, machines.length * this.slotHeight * -1 + this.listHeight), this.listScrollPosition);
////	}
//
//	private BlockPos selectedMachine = null;
//	private String selectedMachineName = null;
//
//	private void selectMachine(BlockPos pos) {
//		if (pos == null) {
//			this.clearSelectedMachine();
//			return;
//		}
//
//		this.selectedMachine = pos;
//
//	    DatabaseServerEntity db = this.entity.getDatabase();
//
//
//		ItemStack stack = getItemFromBlock(this.entity, this.selectedMachine);
//
//    	if (stack != null) {
//
//    		// Set UI properties based on this machine
//    		this.selectedMachineSecure = false;
//
//    		if (db != null)
//    			this.selectedMachineName = db.getMachineName(pos);
//
//    		// Set fields
//
//    		if (this.selectedMachineName != null)
//    			this.nameField.setValue(this.selectedMachineName);
//    		else
//    			this.nameField.setValue("");
//
//    		this.nameField.setVisible(db != null);
//    		this.securedBtn.setMessage(Component.literal(this.selectedMachineSecure ? "Secured" : "Not Secured"));
//
//    		// TODO: need to add Security Server
//    		//this.securedBtn.visible = true;
//
//    	} else {
//    		this.clearSelectedMachine();
//    	}
//	}
//
//	private void clearSelectedMachine() {
//		this.selectedMachine = null;
//    	this.nameField.setVisible(false);
//		this.securedBtn.visible = false;
//	}
//
//	boolean selectedMachineSecure = false;
//
//	@Override
//	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
//
//		if (mouseButton == 1) {
//			// Right click
//
//		    BlockPos[] machines = this.entity.getConnectedMachines();
//		    int x = listX + leftPos;
//		    int y = listY + topPos + this.listScrollPosition;
//
//		    for (BlockPos pos : machines) {
//		    	ItemStack stack = getItemFromBlock(this.entity, pos);
//		    	if (stack == null)
//		    		continue;
//		    	if (!this.matchesSearch(pos))
//		    		continue;
//
//		    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight && mouseY > listY + topPos && mouseY < listY + listHeight + topPos) {
//		    		this.selectMachine(pos);
//		    		return true;
//		    	}
//
//		    	y += slotHeight;
//		    }
//
//
//		} else if (mouseButton == 0) {
//			// Left click
//
//		    BlockPos[] machines = this.entity.getConnectedMachines();
//		    int x = listX + leftPos;
//		    int y = listY + topPos + this.listScrollPosition;
//
//		    for (BlockPos pos : machines) {
//		    	ItemStack stack = getItemFromBlock(this.entity, pos);
//		    	if (stack == null)
//		    		continue;
//		    	if (!this.matchesSearch(pos))
//		    		continue;
//
//		    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight && mouseY > listY + topPos && mouseY < listY + listHeight + topPos) {
//		    		this.activateMachine(pos);
//		    		return true;
//		    	}
//
//		    	y += slotHeight;
//		    }
//
//		}
//
//		return super.mouseClicked(mouseX, mouseY, mouseButton);
//	}
//
//	private void activateMachine(BlockPos pos) {
//		Level world = this.entity.getLevel();
//
//		if (!world.isClientSide)
//			return;
//
//		this.entity.activate(pos, Minecraft.getInstance().player);
//	}
//
//    int iconWidth = 18;
//    int iconHeight = 18;
//
//    int listScrollPosition = 0;
//
//    public static ItemStack getItemFromBlock(RemoteShellEntity entity, BlockPos pos) {
//		// TODO
////
////    	if (pos.equals(entity.getPos()))
////    		return null;
////
////    	Player player = Minecraft.getInstance().thePlayer;
////    	BlockState state = entity.getWorld().getBlockState(pos);
////
////    	if (state == null || state.getBlock() == null)
////    		return null;
////
////    	ItemStack stack = state.getBlock().getPickBlock(
////    			state, new BlockHitResult(player),
////    			entity.getWorld(), pos, player);
////
////    	// Sometimes that doesn't work...
////    	if (stack == null) {
////    		int meta = state.getBlock().getMetaFromState(state);
////    		stack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, meta);
////    	}
////
////    	if (stack == null || stack.getItem() == null)
////    		return null;
////
////    	return stack;
//		return null;
//    }
//
//    private boolean matchesSearch(BlockPos pos) {
//    	if ("".equals(this.searchField.getValue()))
//    		return true;
//
//    	BlockState state = this.entity.getLevel().getBlockState(pos);
//
//    	if (state.getBlock() == RezolveMod.REMOTE_SHELL_BLOCK)
//    		return false;
//
//    	String searchString = this.searchField.getValue();
//    	ItemStack stack = getItemFromBlock(this.entity, pos);
//
//    	if (stack == null)
//    		return false;
//
//    	String name = stack.getDisplayName().getString();
//    	String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();
//
//    	if (name.toLowerCase().contains(searchString.toLowerCase()) || subName.toLowerCase().contains(searchString.toLowerCase()))
//    		return true;
//
//	    DatabaseServerEntity db = this.entity.getDatabase();
//    	if (db != null) {
//    		String customName = db.getMachineName(pos);
//    		if (customName != null && !"".equals(customName)) {
//    			if (customName.toLowerCase().contains(searchString.toLowerCase()))
//    				return true;
//    		}
//    	}
//
//    	return false;
//    }
//
//    @Override
//    protected void drawSubWindows(PoseStack pPoseStack, int mouseX, int mouseY) {
//
//		int scrollOffset = this.listScrollPosition;
//	    BlockPos[] machines = this.entity.getConnectedMachines();
//
//	    int x = listX;
//	    int y = listY + scrollOffset;
//
//	    for (BlockPos pos : machines) {
//	    	ItemStack stack = getItemFromBlock(this.entity, pos);
//	    	if (stack == null)
//	    		continue;
//	    	if (!this.matchesSearch(pos))
//	    		continue;
//
//	    	boolean highlighted =
//	    		x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight
//	    		&& y > 0 && y < this.listY + this.listHeight
//	    		&& mouseY < this.listY + this.listHeight
//	    	;
//
//	    	if (highlighted) {
//				colorQuad(0x44FFFFFF, x, y, x + slotWidth, y + slotHeight);
//	    	}
//
//	    	y += slotHeight;
//	    }
//
//	    x = listX;
//	    y = listY + scrollOffset;
//
//	    DatabaseServerEntity db = this.entity.getDatabase();
//
//	    for (BlockPos pos : machines) {
//
//	    	ItemStack stack = getItemFromBlock(this.entity, pos);
//	    	if (stack == null)
//	    		continue;
//
//	    	if (!this.matchesSearch(pos))
//	    		continue;
//
//	    	if (y > 0 && y + slotHeight < height) {
//		    	this.drawItem(pPoseStack, stack, x, y);
//
//		    	String name = stack.getDisplayName().getString();
//		    	String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();
//
//		    	if (db != null) {
//		    		String customName = db.getMachineName(pos);
//		    		if (customName != null && !"".equals(customName)) {
//		    			subName = name+" ["+subName+"]";
//		    			name = customName;
//		    		}
//		    	}
//
//		    	this.font.draw(
//						pPoseStack,
//		    		name,
//		    		x + this.iconWidth + 2,
//		    		y + 1,
//		    		0xFF000000
//		    	);
//
//		    	this.font.draw(
//						pPoseStack,
//		    		subName,
//		    		x + this.iconWidth + 2,
//		    		y + 10,
//		    		0xFF666666
//		    	);
//	    	}
//
//	    	y += slotHeight;
//
//	    }
//    }
//
//	@Override
//	public void render(PoseStack pPoseStack, int mouseX, int mouseY, float pPartialTick) {
//		super.render(pPoseStack, mouseX, mouseY, pPartialTick);
//
//	    int rfBarX = 231;
//	    int rfBarY = 20;
//	    int rfBarHeight = 88;
//	    int rfBarWidth = 14;
//
//	    int usedHeight = 0; // TODO rfBarHeight - (int)(this.entity.getEnergyStored(Direction.DOWN) / (double)this.entity.getMaxEnergyStored(Direction.DOWN) * rfBarHeight);
//	    //Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + usedHeight, 0xFF000000);
//
//	    DatabaseServerEntity db = this.entity.getDatabase();
//		this.nameField.setVisible(this.selectedMachine != null && db != null);
//
//	    if (this.selectedMachine != null) {
//	    	BlockPos pos = this.selectedMachine;
//	    	ItemStack stack = getItemFromBlock(this.entity, pos);
//
//	    	if (stack != null) {
//		    	String stackName = stack.getDisplayName().getString();
//		    	String position = pos.getX()+", "+pos.getY()+", "+pos.getZ();
//
//		    	if (!this.nameField.isVisible()) {
//		    		this.font.draw(pPoseStack, stackName, 10, 126, 0xFF000000);
//		    		this.font.draw(pPoseStack, position, 10, 141, 0xFF666666);
//		    	} else {
//		    		this.font.draw(pPoseStack, stackName, 10, 141, 0xFF666666);
//		    		this.font.draw(pPoseStack, position, 10, 153, 0xFF666666);
//		    	}
//	    	}
//	    } else {
//
//    		this.font.draw(pPoseStack, "Right click a machine for info.", 10, 126, 0xFF666666);
//	    }
//	}
//}
