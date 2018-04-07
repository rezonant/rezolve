package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.common.GuiControl;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class StorageView extends GuiControl {

	public StorageView(GuiContainerBase screen, int x, int y, int width, int height) {

		super(screen, x, y, width, height);

		this.mc = Minecraft.getMinecraft();
		this.windowColumnCount = this.width / this.itemSize;
		this.windowRowCount = this.height / this.itemSize;

		this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		this.sendStateToServer();
	}

	private Minecraft mc;
	private FontRenderer fontRenderer;
	private List<StorageViewMessage.ItemEntry> visibleItems;
	private int displayedOffset;
	private int offset;
	private int totalItemCount;
	private int totalStackCount;
	private int windowTotalRowCount;
	private int itemSize = 18;


	private int scrollOffset = 0;

	private int windowColumnCount = 0;
	private int windowRowCount = 0;

	public void handleUpdate(StorageViewMessage updateMessage) {
		//System.out.println("Received storage view update");
		this.totalItemCount = updateMessage.totalItemsCount;
		this.totalStackCount = updateMessage.totalStackCount;
		this.displayedOffset = updateMessage.startIndex;
		this.visibleItems = updateMessage.itemEntries;
		this.windowTotalRowCount = (int)Math.ceil((float)this.totalStackCount / this.windowColumnCount);

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
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.disableBlend();

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
			//GlStateManager.enableLighting();
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

	@Override
	public void onClick(int x, int y, int mouseButton) {
		if (!this.inBounds(x, y))
			return;

		x -= this.x;
		y -= this.y;

		ItemStack inHand = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();

		if (inHand != null && inHand.stackSize > 0) {
			// Player is giving item to system

			System.out.println("StorageView:giveItems() : "+inHand.toString());

			StorageViewRequestMessage request = StorageViewRequestMessage.giveItems(Minecraft.getMinecraft().thePlayer, inHand, "hand");
			RezolvePacketHandler.INSTANCE.sendToServer(request);

			return;
		}

		System.out.println("StorageView:onClick("+x+", "+y+")");

		int px = this.displayedOffset % this.windowColumnCount;
		int py = this.displayedOffset / this.windowColumnCount;

		for (StorageViewMessage.ItemEntry item : this.visibleItems) {
			int slotPosX = px * this.itemSize;
			int slotPosY = this.scrollOffset + py * this.itemSize;

			if (slotPosX < x && slotPosY < y && x <= slotPosX + this.itemSize && y < slotPosY + this.itemSize) {
				System.out.println("StorageView:onClick("+x+", "+y+") clicked on item!");
				this.handleStackClick(x, y, mouseButton, item);
				break;
			}

			px += 1;
			if (px >= this.windowColumnCount) {
				py += 1;
				px = 0;
			}
		}
	}

	public void handleStackClick(int x, int y, int mouseButton, StorageViewMessage.ItemEntry item) {
		ItemStack stack = item.stack;

		int maxStackSize = stack.getItem().getItemStackLimit(stack);

		boolean shift = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT));
		if (shift) {
			//Minecraft.getMinecraft().thePlayer.openContainer.transferStackInSlot();
			// ???
		}

		int requestedSize = maxStackSize;
		StorageViewRequestMessage request;

		if (mouseButton == 1)
			requestedSize = Math.min(stack.stackSize / 2, maxStackSize / 2);

		System.out.println("StorageView:onClick("+x+", "+y+", "+mouseButton+", "+stack.toString()+"): Trying for "+requestedSize+" items");
		request = StorageViewRequestMessage.takeItems(Minecraft.getMinecraft().thePlayer, ItemStackUtil.getStackSized(stack, maxStackSize), item.hash);
		RezolvePacketHandler.INSTANCE.sendToServer(request);
	}

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

	private String noConnectionMessage = "Please wait...";

	public void setNoConnectionMessage(String message) {
		this.noConnectionMessage = message;
	}

	@Override
	public void renderOverlay(int mouseX, int mouseY) {

		boolean inFrame = (mouseX > this.x && mouseX < this.x + this.width && mouseY > this.y && mouseY < this.y + this.height);

		if (this.hoveredItem != null && inFrame)
			this.renderToolTip(hoveredItem.stack, mouseX, mouseY);
	}

	private StorageViewMessage.ItemEntry hoveredItem;

	@Override
	public void render(int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glTranslatef(this.x, this.y, 0);

		int localMouseX = mouseX - this.x;
		int localMouseY = mouseY - this.y;

		ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

		boolean scissor = true;

		if (scissor) {
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor(
				this.x * resolution.getScaleFactor(),
				Minecraft.getMinecraft().displayHeight - (this.y + this.height) * resolution.getScaleFactor(),
				this.width * resolution.getScaleFactor(),
				this.height * resolution.getScaleFactor()
			);
		}

		try {
			if (this.visibleItems != null) {
				int x = this.displayedOffset % this.windowColumnCount;
				int y = this.displayedOffset / this.windowColumnCount;

				StorageViewMessage.ItemEntry hoveredItem = null;

				for (StorageViewMessage.ItemEntry item : this.visibleItems) {

					int slotPosX = this.itemSize*x;
					int slotPosY = this.itemSize*y + this.scrollOffset;

					if (slotPosY < localMouseY && slotPosX < localMouseX && slotPosY + this.itemSize > localMouseY && slotPosX + this.itemSize > localMouseX) {
						// mouse is on the thing
						hoveredItem = item;
					}

					if (slotPosY + this.itemSize >= 0 && slotPosY < this.height && slotPosX >= 0 && slotPosX < this.width) {
						this.drawItemStack(item.stack, slotPosX, slotPosY, this.shortenCount(item.amount));
					}

					x += 1;
					if (x >= this.windowColumnCount) {
						y += 1;
						x = 0;
					}
				}

				this.hoveredItem = hoveredItem;



				int scrollBarX = this.width - 1;
				int scrollBarY = 2;
				int scrollBarHeight = this.height - 4;
				int scrollBarWidth = 1;
				int scrollableRowCount = Math.max(0, this.windowTotalRowCount - this.windowRowCount);
				float scrollSpaceFactor = (float)this.windowRowCount / this.windowTotalRowCount;
				int puckHeight = (int)Math.max(10, scrollSpaceFactor * scrollBarHeight);
				float scrollFactor = -(float)this.scrollOffset / (this.itemSize*scrollableRowCount);
				int puckPosition = (int)(scrollFactor * (scrollBarHeight - puckHeight));

				if (puckPosition < 0)
					puckPosition = 0;

				Gui.drawRect(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0x44000000);
				Gui.drawRect(scrollBarX, scrollBarY + puckPosition, scrollBarX + scrollBarWidth, scrollBarY + puckPosition + puckHeight, 0x88FFFFFF);

			} else {
				this.fontRenderer.drawString(noConnectionMessage, 7, 30, 0xFFFFFFFF);
			}
		} finally {
			GL11.glPopMatrix();
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
	}

	protected void renderToolTip(ItemStack stack, int x, int y)
	{
		List<String> list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

		for (int i = 0; i < list.size(); ++i)
		{
			if (i == 0)
			{
				list.set(i, stack.getRarity().rarityColor + (String)list.get(i));
			}
			else
			{
				list.set(i, TextFormatting.GRAY + (String)list.get(i));
			}
		}

		FontRenderer font = stack.getItem().getFontRenderer(stack);
		net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack);
		this.drawHoveringText(list, x, y, (font == null ? fontRenderer : font));
		net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
	}

	protected void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font)
	{
		net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(textLines, x, y, this.screen.width, this.screen.height, -1, font);
		if (false && !textLines.isEmpty())
		{
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int i = 0;

			for (String s : textLines) {
				int j = this.fontRenderer.getStringWidth(s);

				if (j > i) {
					i = j;
				}
			}

			int l1 = x + 12;
			int i2 = y - 12;
			int k = 8;

			if (textLines.size() > 1)
			{
				k += 2 + (textLines.size() - 1) * 10;
			}

			if (l1 + i > this.width)
			{
				l1 -= 28 + i;
			}

			if (i2 + k + 6 > this.height)
			{
				i2 = this.height - k - 6;
			}

			RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
			this.zLevel = 300.0F;
			itemRender.zLevel = 300.0F;
			int l = -267386864;
			this.drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, -267386864, -267386864);
			this.drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, -267386864, -267386864);
			this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, -267386864, -267386864);
			this.drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, -267386864, -267386864);
			this.drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, -267386864, -267386864);
			int i1 = 1347420415;
			int j1 = 1344798847;
			this.drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
			this.drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
			this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
			this.drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, 1344798847, 1344798847);

			for (int k1 = 0; k1 < textLines.size(); ++k1)
			{
				String s1 = (String)textLines.get(k1);
				this.fontRenderer.drawStringWithShadow(s1, (float)l1, (float)i2, -1);

				if (k1 == 0)
				{
					i2 += 2;
				}

				i2 += 10;
			}

			this.zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
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

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
		int mouseX = Mouse.getX() / resolution.getScaleFactor();
		int mouseY = (Minecraft.getMinecraft().displayHeight - Mouse.getY()) / resolution.getScaleFactor();

		boolean inFrame = (mouseX > this.x && mouseX < this.x + this.width && mouseY > this.y && mouseY < this.y + this.height);

		int wheelDist = Mouse.getDWheel();
		if (inFrame) {
			this.handleMouseWheel(wheelDist);
		}
	}


	private int scrollSpeed = 30;

	public void handleMouseWheel(int dWheel) {

		this.scrollOffset += dWheel / this.scrollSpeed;
		if (this.scrollOffset > 0)
			this.scrollOffset = 0;
		else if (this.scrollOffset < Math.min(0, -(this.windowTotalRowCount - this.windowRowCount) * this.itemSize))
			this.scrollOffset = Math.min(0, -(this.windowTotalRowCount - this.windowRowCount) * this.itemSize);

		int desiredOffset = Math.max(0, -this.scrollOffset) / this.itemSize * this.windowColumnCount;

		if (desiredOffset != offset) {
			this.offset = desiredOffset;
			this.sendStateToServer();
		}

		//System.out.println("Scroll offset: "+this.scrollOffset);
		//System.out.println("Item offset: "+this.offset);
	}
}
