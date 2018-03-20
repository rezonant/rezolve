package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class StorageView {

	public StorageView(GuiContainerBase screen, int x, int y, int width, int height) {

		this.screen = screen;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		this.windowColumnCount = (int)(this.width / this.itemSize);
		this.windowRowCount = (int)(this.height / this.itemSize);

		this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		this.sendStateToServer();
	}

	private GuiContainerBase screen;
	private FontRenderer fontRenderer;
	private int x;
	private int y;
	private int width;
	private int height;
	private List<ItemStack> visibleItems;
	private int displayedOffset;
	private int offset;
	private int totalItemCount;
	private int totalStackCount;
	private int itemSize = 18;


	private int scrollOffset = 0;

	private int windowColumnCount = 0;
	private int windowRowCount = 0;

	public void handleUpdate(StorageViewMessage updateMessage) {
		//System.out.println("Received storage view update");
		this.totalItemCount = updateMessage.totalItemsCount;
		this.totalStackCount = updateMessage.totalStackCount;
		this.displayedOffset = updateMessage.startIndex;
		this.visibleItems = updateMessage.items;

		//System.out.println("Visible items:");
		//for (ItemStack stack : this.visibleItems) {
		//	System.out.println(" - "+stack.toString());
		//}
	}

	public void handleResponse(StorageViewResponseMessage message) {
		// TODO: nothing right now, because we set the player's held item from the server anyway
	}


	/**
	 * Draws an ItemStack.
	 */
	private void drawItemStack(ItemStack stack, int x, int y, String altText)
	{
		GlStateManager.translate(0.0F, 0.0F, 1);

		net.minecraft.client.gui.FontRenderer font = null;
		if (stack != null) font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = this.fontRenderer;

		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

		GlStateManager.enableLighting();

		itemRender.zLevel = 200.0F;
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		itemRender.renderItemOverlayIntoGUI(font, stack, x, y, "");

		float textWidth = this.fontRenderer.getStringWidth(altText);

		GL11.glPushMatrix();
			GL11.glTranslatef(x + 17.0f - textWidth / 2, y + 11, 0);
			GL11.glScalef(0.5f, 0.5f, 0.5f);

			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.disableBlend();
			this.fontRenderer.drawStringWithShadow(altText, 0, 0, 16777215);
			GlStateManager.enableLighting();
			//GlStateManager.enableDepth();
			// Fixes opaque cooldown overlay a bit lower
			// TODO: check if enabled blending still screws things up down the line.
			//GlStateManager.enableBlend();

		GL11.glPopMatrix();

		itemRender.zLevel = 0.0F;
	}

	public String shortenCount(int count) {
		if (count > 1000 * 1000 * 1000) {
			// B
			return (count / 1000000000) + "B";
		} else if (count > 1000 * 1000) {
			// M
			return (count / 1000000) + "M";
		} else if (count > 1000) {
			// K
			return (count / 1000) + "K";
		} else {
			return count + "";
		}
	}

	public void onClick(int x, int y, ItemStack stack) {
		int maxStackSize = stack.getItem().getItemStackLimit(stack);


		System.out.println("StorageView:onClick("+x+", "+y+", "+stack.toString()+"): Trying for "+maxStackSize+" items");

		StorageViewRequestMessage request = StorageViewRequestMessage.takeItems(Minecraft.getMinecraft().thePlayer, ItemStackUtil.getStackSized(stack, maxStackSize));
		RezolvePacketHandler.INSTANCE.sendToServer(request);
	}


	public void onClick(int x, int y) {

		ItemStack inHand = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();

		if (inHand != null && inHand.stackSize > 0) {
			// Player is giving item to system

			System.out.println("StorageView:giveItems() : "+inHand.toString());

			StorageViewRequestMessage request = StorageViewRequestMessage.giveItems(Minecraft.getMinecraft().thePlayer, inHand);
			RezolvePacketHandler.INSTANCE.sendToServer(request);

			return;
		}

		System.out.println("StorageView:onClick("+x+", "+y+")");

		int px = this.displayedOffset % this.windowColumnCount;
		int py = this.displayedOffset / this.windowColumnCount;

		for (ItemStack stack : this.visibleItems) {
			int slotPosX = this.x + px * this.itemSize;
			int slotPosY = this.y + this.scrollOffset + py * this.itemSize;

			if (slotPosX < x && slotPosY < y && x <= slotPosX + this.itemSize && y < slotPosY + this.itemSize) {
				System.out.println("StorageView:onClick("+x+", "+y+") clicked on item!");
				this.onClick(x, y, stack);
				break;
			}

			px += 1;
			if (px >= this.windowColumnCount) {
				py += 1;
				px = 0;
			}
		}
	}

	private boolean dbg1 = false;
	private int dbgX;
	private int dbgY;
	private int dbgWidth;
	private int dbgHeight;
	private String query = "";

	public boolean setQuery(String query) {
		if (query == null)
			query = "";

		if (query.equals(this.query))
			return false;

		this.query = query;
		this.scrollOffset = 0;
		this.offset = 0;
		this.sendStateToServer();
		return true;
	}

	public void render() {
		GL11.glPushMatrix();
		GL11.glTranslatef(this.x, this.y, 0);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);

		Minecraft.getMinecraft();

		if (dbg1) {
			this.dbgX = 0;
			this.dbgY = 0;
			//this.dbgWidth = Minecraft.getMinecraft().displayWidth;
			//this.dbgHeight = Minecraft.getMinecraft().displayHeight;
			GL11.glScissor(dbgX, dbgY, dbgWidth, dbgHeight);
		} else {
			ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

			GL11.glScissor(
				(this.screen.getX() + this.x) * resolution.getScaleFactor(),
				Minecraft.getMinecraft().displayHeight - (this.screen.getY() + this.y + this.height) * resolution.getScaleFactor(),
				this.width * resolution.getScaleFactor(),
				this.height * resolution.getScaleFactor()
			);
		}

		try {
			if (this.visibleItems != null) {
				int x = this.displayedOffset % this.windowColumnCount;
				int y = this.displayedOffset / this.windowColumnCount;

				for (ItemStack stack : this.visibleItems) {

					int slotPosX = this.itemSize*x;
					int slotPosY = this.itemSize*y + this.scrollOffset;

					if (slotPosY + this.itemSize >= 0 && slotPosY < this.height && slotPosX >= 0 && slotPosX < this.width) {
						this.drawItemStack(stack, slotPosX, slotPosY, this.shortenCount(stack.stackSize));
					}

					x += 1;
					if (x >= this.windowColumnCount) {
						y += 1;
						x = 0;
					}
				}

			} else {
				this.fontRenderer.drawString("Please wait...", 7, 30, 0xFFFFFFFF);
			}
		} finally {
			GL11.glPopMatrix();
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
	}

	public boolean inBounds(int mouseX, int mouseY) {
		return (
			mouseX > this.x
			&& mouseX < this.x + this.width
			&& mouseY > this.y
			&& mouseY < this.y + this.height
		);
	}

	private void sendStateToServer() {
		StorageViewStateMessage state = new StorageViewStateMessage();
		state.offset = this.offset;
		state.limit = this.windowColumnCount * (this.windowRowCount + 2);
		state.playerId = Minecraft.getMinecraft().thePlayer.getUniqueID().toString();
		state.query = this.query;

		RezolvePacketHandler.INSTANCE.sendToServer(state);
	}

	public void handleMouseWheel(int dWheel) {

		//System.out.println("Wheel: "+dWheel);
		//System.out.println("Wheel final value: "+dWheel/30);

		this.scrollOffset += (dWheel / 30);
		if (this.scrollOffset > 0)
			this.scrollOffset = 0;

		int desiredOffset = Math.max(0, -this.scrollOffset) / this.itemSize * this.windowColumnCount;

		if (desiredOffset != offset) {
			this.offset = desiredOffset;
			this.sendStateToServer();
		}

		//System.out.println("Scroll offset: "+this.scrollOffset);
		//System.out.println("Item offset: "+this.offset);
	}
}
