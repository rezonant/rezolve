package com.astronautlabs.mc.rezolve.remoteShell;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import com.astronautlabs.mc.rezolve.common.GuiContainerBase;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
		super(new RemoteShellContainer(playerInv, entity), "rezolve:textures/gui/container/remote_shell.png");

		this.playerInv = playerInv;
		this.entity = entity;
	    this.xSize = 255;
	    this.ySize = 212;
	}
	
	private IInventory playerInv;
	private RemoteShellEntity entity;
	
	private int listX = 13;
	private int listY = 23;
	private int slotWidth = 207;
	private int slotHeight = 22; 
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		
		BlockPos[] machines = this.entity.getConnectedMachines();
		
		this.listScrollPosition += Mouse.getEventDWheel() / 10;
		this.listScrollPosition = Math.min(0, this.listScrollPosition);
		this.listScrollPosition = Math.max(machines.length * this.slotHeight * -1 + 85, this.listScrollPosition);
		
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
	    
		System.out.println("MOUSE CLICK "+mouseButton);
		
	    BlockPos[] machines = this.entity.getConnectedMachines();
	    int x = listX + guiLeft;
	    int y = listY + guiTop + this.listScrollPosition;
    	
	    for (BlockPos pos : machines) {
	    	ItemStack stack = this.getItemFromBlock(pos);
	    	if (stack == null)
	    		continue;
	    	
	    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight) {
	    		this.activateMachine(pos);
	    	}
	    	
	    	y += slotHeight;
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
    
    private ItemStack getItemFromBlock(BlockPos pos) {
    	
    	if (pos.equals(this.entity.getPos()))
    		return null;
    	
    	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    	IBlockState state = this.entity.getWorld().getBlockState(pos);
    	
    	if (state == null || state.getBlock() == null)
    		return null;
    	
    	ItemStack stack = state.getBlock().getPickBlock(
    			state, new RayTraceResult(player), 
    			this.entity.getWorld(), pos, player);
    	
    	// Sometimes that doesn't work...
    	if (stack == null) {
    		int meta = state.getBlock().getMetaFromState(state);
    		stack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, meta);
    	}
    	
    	if (stack == null || stack.getItem() == null)
    		return null;
    	
    	return stack;
    }
    
    @Override
    protected void drawSubWindows(int mouseX, int mouseY) {
    	
		int scrollOffset = this.listScrollPosition;
	    BlockPos[] machines = this.entity.getConnectedMachines();
	    
	    int x = listX;
	    int y = listY + scrollOffset;

	    for (BlockPos pos : machines) {
	    	ItemStack stack = this.getItemFromBlock(pos);
	    	if (stack == null)
	    		continue;
	    	
	    	if (x < mouseX && mouseX < x + slotWidth && y < mouseY && mouseY < y + slotHeight) {
	    		drawRect(x, y, x + slotWidth, y + slotHeight, 0x44FFFFFF);
	    	}
	    	
	    	y += slotHeight;
	    }
	    
	    x = listX;
	    y = listY + scrollOffset;
    	
	    for (BlockPos pos : machines) {

	    	ItemStack stack = this.getItemFromBlock(pos);
	    	if (stack == null)
	    		continue;
	    	
	    	if (y > 0 && y + slotHeight < ySize) {
		    	this.drawItem(stack, x, y);
		    	this.fontRendererObj.drawString(
		    		stack.getDisplayName(), 
		    		x + this.iconWidth + 2, 
		    		y + 1, 
		    		0xFF000000
		    	);
		    	this.fontRendererObj.drawString(
		    		pos.getX()+", "+pos.getY()+", "+pos.getZ(), 
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
		
	    int rfBarX = 231;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = rfBarHeight - (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
	    Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + usedHeight, 0xFF000000);

	}
}
