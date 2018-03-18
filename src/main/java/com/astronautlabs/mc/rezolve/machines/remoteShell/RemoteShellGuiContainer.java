package com.astronautlabs.mc.rezolve.machines.remoteShell;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.machines.databaseServer.DatabaseServerEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RemoteShellGuiContainer extends GuiContainerBase {

	public RemoteShellGuiContainer(IInventory playerInv, RemoteShellEntity entity) {
		super(new RemoteShellContainer(playerInv, entity), "rezolve:textures/gui/container/remote_shell.png", 255, 212);

		this.playerInv = playerInv;
		this.entity = entity;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (this.nameField.getVisible() && this.nameField.isFocused()) {
			if (this.selectedMachine != null && !this.nameField.getText().equals(this.selectedMachineName)) {
				this.selectedMachineName = this.nameField.getText();
				this.entity.renameMachine(this.selectedMachine, this.selectedMachineName);
			}
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();

		this.nameField = new GuiTextField(NAME_FIELD, this.fontRendererObj, this.guiLeft + 11, this.guiTop + 120, 211, 18);
		this.nameField.setMaxStringLength(23);
		this.nameField.setText("");
		this.nameField.setFocused(false);
		this.nameField.setVisible(false);
		this.addControl(this.nameField);
		
		this.searchField = new GuiTextField(SEARCH_FIELD, this.fontRendererObj, this.guiLeft + 11, this.guiTop + 5, 211, 18);
		this.searchField.setMaxStringLength(23);
		this.searchField.setText("");
		this.searchField.setFocused(true);
		this.searchField.setVisible(true);
		this.addControl(this.searchField);
		
		this.securedBtn = new GuiButton(SECURED_BUTTON, this.guiLeft + 50, this.guiTop + 167, 100, 18, "Not Secured");
		this.securedBtn.visible = false;
		this.addControl(this.securedBtn);
	}

	private static final int NAME_FIELD = 0;
	private static final int SEARCH_FIELD = 1;
	private static final int SECURED_BUTTON = 2;
	
	private GuiButton securedBtn;
	private GuiTextField nameField;
	private GuiTextField searchField;
	
	private IInventory playerInv;
	private RemoteShellEntity entity;
	
	private int listX = 13;
	private int listY = 33;
	private int listHeight = 85;
	private int slotWidth = 207;
	private int slotHeight = 22; 
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		
		BlockPos[] machines = this.entity.getConnectedMachines();
		
		this.listScrollPosition += Mouse.getEventDWheel() / 10;
		this.listScrollPosition = Math.min(0, this.listScrollPosition);
		this.listScrollPosition = Math.max(Math.min(0, machines.length * this.slotHeight * -1 + this.listHeight), this.listScrollPosition);
	}
	
	private BlockPos selectedMachine = null;
	private String selectedMachineName = null;
	
	private void selectMachine(BlockPos pos) {
		if (pos == null) {
			this.clearSelectedMachine();
			return;
		}
			
		this.selectedMachine = pos;

	    DatabaseServerEntity db = this.entity.getDatabase();
	    

		ItemStack stack = getItemFromBlock(this.entity, this.selectedMachine);
    	
    	if (stack != null) {

    		// Set UI properties based on this machine
    		this.selectedMachineSecure = false;
    		
    		if (db != null)
    			this.selectedMachineName = db.getMachineName(pos);
    		
    		// Set fields 
    		
    		if (this.selectedMachineName != null)
    			this.nameField.setText(this.selectedMachineName);
    		else
    			this.nameField.setText("");

    		this.nameField.setVisible(db != null);
    		this.securedBtn.displayString = this.selectedMachineSecure ? "Secured" : "Not Secured";
    		
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
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		if (this.securedBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
			
			if (this.selectedMachineSecure) {
				this.securedBtn.displayString = "Not Secured";
			} else {
				this.securedBtn.displayString = "Secured";
			}
			this.selectedMachineSecure = !this.selectedMachineSecure;
			return;
		}
		
		if (mouseButton == 1) {
			// Right click
			
		    BlockPos[] machines = this.entity.getConnectedMachines();
		    int x = listX + guiLeft;
		    int y = listY + guiTop + this.listScrollPosition;
	    	
		    for (BlockPos pos : machines) {
		    	ItemStack stack = getItemFromBlock(this.entity, pos);
		    	if (stack == null)
		    		continue;
		    	if (!this.matchesSearch(pos))
		    		continue;
		    	
		    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight && mouseY > listY + guiTop && mouseY < listY + listHeight + guiTop) {
		    		this.selectMachine(pos);
		    		return;
		    	}
		    	
		    	y += slotHeight;
		    }
			
			
		} else if (mouseButton == 0) {
			// Left click

		    BlockPos[] machines = this.entity.getConnectedMachines();
		    int x = listX + guiLeft;
		    int y = listY + guiTop + this.listScrollPosition;
	    	
		    for (BlockPos pos : machines) {
		    	ItemStack stack = getItemFromBlock(this.entity, pos);
		    	if (stack == null)
		    		continue;
		    	if (!this.matchesSearch(pos))
		    		continue;
		    	
		    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight && mouseY > listY + guiTop && mouseY < listY + listHeight + guiTop) {
		    		this.activateMachine(pos);
		    		return;
		    	}
		    	
		    	y += slotHeight;
		    }
			
		}
		
		// TODO Auto-generated method stub
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	private void activateMachine(BlockPos pos) {
		World world = this.entity.getWorld();
		
		if (!world.isRemote)
			return;
		
		this.entity.activate(pos, Minecraft.getMinecraft().thePlayer);
	}

    int iconWidth = 18;
    int iconHeight = 18;
	
    int listScrollPosition = 0;
    
    public static ItemStack getItemFromBlock(RemoteShellEntity entity, BlockPos pos) {
    	
    	if (pos.equals(entity.getPos()))
    		return null;
    	
    	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    	IBlockState state = entity.getWorld().getBlockState(pos);
    	
    	if (state == null || state.getBlock() == null)
    		return null;
    	
    	ItemStack stack = state.getBlock().getPickBlock(
    			state, new RayTraceResult(player), 
    			entity.getWorld(), pos, player);
    	
    	// Sometimes that doesn't work...
    	if (stack == null) {
    		int meta = state.getBlock().getMetaFromState(state);
    		stack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, meta);
    	}
    	
    	if (stack == null || stack.getItem() == null)
    		return null;
    	
    	return stack;
    }

    private boolean matchesSearch(BlockPos pos) {
    	if ("".equals(this.searchField.getText()))
    		return true;

    	IBlockState state = this.entity.getWorld().getBlockState(pos);
    	
    	if (state.getBlock() == RezolveMod.REMOTE_SHELL_BLOCK)
    		return false;
    	
    	String searchString = this.searchField.getText();
    	ItemStack stack = getItemFromBlock(this.entity, pos);
    	
    	if (stack == null)
    		return false;

    	String name = stack.getDisplayName();
    	String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();
    	
    	if (name.toLowerCase().contains(searchString.toLowerCase()) || subName.toLowerCase().contains(searchString.toLowerCase()))
    		return true;
    	
	    DatabaseServerEntity db = this.entity.getDatabase();    	
    	if (db != null) {
    		String customName = db.getMachineName(pos);
    		if (customName != null && !"".equals(customName)) {
    			if (customName.toLowerCase().contains(searchString.toLowerCase()))
    				return true;
    		}
    	}
    	
    	return false;
    }
    
    @Override
    protected void drawSubWindows(int mouseX, int mouseY) {
    	
		int scrollOffset = this.listScrollPosition;
	    BlockPos[] machines = this.entity.getConnectedMachines();
	    
	    int x = listX;
	    int y = listY + scrollOffset;

	    for (BlockPos pos : machines) {
	    	ItemStack stack = getItemFromBlock(this.entity, pos);
	    	if (stack == null)
	    		continue;
	    	if (!this.matchesSearch(pos))
	    		continue;
	    	
	    	boolean highlighted = 
	    		x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight 
	    		&& y > 0 && y < this.listY + this.listHeight
	    		&& mouseY < this.listY + this.listHeight
	    	;
	    		
	    	if (highlighted) {
	    		drawRect(x, y, x + slotWidth, y + slotHeight, 0x44FFFFFF);
	    	}
	    	
	    	y += slotHeight;
	    }
	    
	    x = listX;
	    y = listY + scrollOffset;
    	
	    DatabaseServerEntity db = this.entity.getDatabase();
	    
	    for (BlockPos pos : machines) {

	    	ItemStack stack = getItemFromBlock(this.entity, pos);
	    	if (stack == null)
	    		continue;
	    	
	    	if (!this.matchesSearch(pos))
	    		continue;
	    	
	    	if (y > 0 && y + slotHeight < ySize) {
		    	this.drawItem(stack, x, y);
		    	
		    	String name = stack.getDisplayName();
		    	String subName = pos.getX()+", "+pos.getY()+", "+pos.getZ();
		    	
		    	if (db != null) {
		    		String customName = db.getMachineName(pos);
		    		if (customName != null && !"".equals(customName)) {
		    			subName = name+" ["+subName+"]";
		    			name = customName;
		    		}
		    	}
		    	
		    	this.fontRendererObj.drawString(
		    		name, 
		    		x + this.iconWidth + 2, 
		    		y + 1, 
		    		0xFF000000
		    	);
		    	this.fontRendererObj.drawString(
		    		subName, 
		    		x + this.iconWidth + 2, 
		    		y + 10, 
		    		0xFF666666
		    	);
	    	}
	    	
	    	y += slotHeight;
	    	
	    }
    }
    
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

	    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	    
	    int rfBarX = 231;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = rfBarHeight - (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
	    Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + usedHeight, 0xFF000000);

	    DatabaseServerEntity db = this.entity.getDatabase();
		this.nameField.setVisible(this.selectedMachine != null && db != null);
		
	    if (this.selectedMachine != null) {
	    	BlockPos pos = this.selectedMachine;
	    	ItemStack stack = getItemFromBlock(this.entity, pos);
	    	
	    	if (stack != null) {
		    	String name = stack.getDisplayName();
		    	String stackName = stack.getDisplayName();
		    	String position = pos.getX()+", "+pos.getY()+", "+pos.getZ();
		    	
		    	if (!this.nameField.getVisible()) {
		    		this.fontRendererObj.drawString(stackName, 10, 126, 0xFF000000);
		    		this.fontRendererObj.drawString(position, 10, 141, 0xFF666666);
		    	} else {
		    		this.fontRendererObj.drawString(stackName, 10, 141, 0xFF666666);
		    		this.fontRendererObj.drawString(position, 10, 153, 0xFF666666);
		    	}
	    	}
	    } else {

    		this.fontRendererObj.drawString("Right click a machine for info.", 10, 126, 0xFF666666);
	    }
	}
}
