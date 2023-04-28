package com.rezolvemc.storage.view;

import com.rezolvemc.Rezolve;
import net.minecraft.network.chat.Component;
import org.torchmc.WidgetBase;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.storage.view.packets.StorageViewChangeRequest;
import com.rezolvemc.storage.view.packets.StorageViewContentPacket;
import com.rezolvemc.util.ItemStackUtil;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.rezolvemc.storage.view.packets.StorageViewChangeResponse;
import com.rezolvemc.storage.view.packets.StorageViewQuery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StorageView extends WidgetBase {

	static {
		RezolveRegistry.register(
				StorageViewChangeRequest.class,
				StorageViewChangeResponse.class,
				StorageViewContentPacket.class,
				StorageViewQuery.class
		);
	}

	public StorageView() {
		this(0, 0, 0, 0);
	}

	public StorageView(int x, int y, int width, int height) {
		super(Component.translatable("screens.rezolve.storage_view"));

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		this.sendStateToServer();


		((MachineScreen)screen).getMachineMenu().addPacketHandler(packet -> {
			if (packet instanceof StorageViewContentPacket contentPacket) {
				handleUpdate(contentPacket);
			} else if (packet instanceof StorageViewChangeResponse changeResponse) {
				handleResponse(changeResponse);
			} else {
				return false;
			}

			return true;
		});
	}

	@Override
	protected void didResize() {
		super.didResize();
		this.windowColumnCount = this.width / this.itemSize;
		this.windowRowCount = this.height / this.itemSize;
	}

	private List<StorageViewContentPacket.ItemEntry> visibleItems;
	private int displayedOffset;
	private int offset;
	private int totalItemCount;
	private int totalStackCount;
	private int windowTotalRowCount;
	private int itemSize = 18;


	private int scrollOffset = 0;

	private int windowColumnCount = 0;
	private int windowRowCount = 0;

	public void handleUpdate(StorageViewContentPacket updateMessage) {
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

	public void handleResponse(StorageViewChangeResponse message) {
		// TODO: nothing right now, because we set the player's held item from the server anyway
	}


	/**
	 * Draws an ItemStack.
	 */
	private void drawItemStack(PoseStack poseStack, ItemStack stack, int x, int y, String altText)
	{
		Lighting.setupForFlatItems();

		float textWidth = font.width(altText);

		TorchUtil.drawItem(poseStack, stack, x, y);
		//minecraft.getItemRenderer().renderAndDecorateItem(stack, this.x + x, this.y + y);

		poseStack.pushPose();
			poseStack.translate(x + 17.0f - textWidth / 2, y + 11, 300);
			poseStack.scale(0.5f, 0.5f, 0.5f);
			RenderSystem.applyModelViewMatrix();

			RenderSystem.enableDepthTest();

			//this.font.draw(poseStack, altText, 0, 0, 16777215);
			this.font.drawShadow(poseStack, altText, 0, 0, 16777215);

		poseStack.popPose();
		RenderSystem.applyModelViewMatrix();

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
	public boolean mouseClicked(double x, double y, int mouseButton) {
		if (!this.inBounds(x, y))
			return false;

		x -= this.x;
		y -= this.y;

		ItemStack inHand = minecraft.player.containerMenu.getCarried();

		if (inHand != null && inHand.getCount() > 0) {
			// Player is giving item to system

			System.out.println("StorageView:giveItems() : "+inHand.toString());

			StorageViewChangeRequest request = StorageViewChangeRequest.giveItems(screen.getMenu(), minecraft.player, inHand, "hand");
			request.setMenu(screen.getMenu());
			request.sendToServer();

			return true;
		}

		System.out.println("StorageView:onClick("+x+", "+y+")");

		int px = this.displayedOffset % this.windowColumnCount;
		int py = this.displayedOffset / this.windowColumnCount;

		for (StorageViewContentPacket.ItemEntry item : this.visibleItems) {
			int slotPosX = margin + px * this.itemSize;
			int slotPosY = margin + this.scrollOffset + py * this.itemSize;

			if (slotPosX < x && slotPosY < y && x <= slotPosX + this.itemSize && y < slotPosY + this.itemSize) {
				System.out.println("StorageView:onClick("+x+", "+y+") clicked on item!");
				this.handleStackClick((int)x, (int)y, mouseButton, item);
				break;
			}

			px += 1;
			if (px >= this.windowColumnCount) {
				py += 1;
				px = 0;
			}
		}

		return false;
	}

	public void handleStackClick(int x, int y, int mouseButton, StorageViewContentPacket.ItemEntry item) {
		ItemStack stack = item.stack;

		int maxStackSize = stack.getItem().getMaxStackSize(stack);

//		boolean shift = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT));
//		if (shift) {
//			//minecraft.thePlayer.openContainer.transferStackInSlot();
//			// ???
//		}

		int requestedSize = maxStackSize;
		StorageViewChangeRequest request;

		if (mouseButton == 1)
			requestedSize = Math.min(stack.getCount() / 2, maxStackSize / 2);

		System.out.println("StorageView:onClick("+x+", "+y+", "+mouseButton+", "+stack.toString()+"): Trying for "+requestedSize+" items");
		request = StorageViewChangeRequest.takeItems(screen.getMenu(), minecraft.player, ItemStackUtil.getStackSized(stack, maxStackSize), item.hash);
		request.sendToServer();
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

	//@Override
	public void renderOverlay(int mouseX, int mouseY) {
		// TODO
//
//		boolean inFrame = (mouseX > this.x && mouseX < this.x + this.width && mouseY > this.y && mouseY < this.y + this.height);
//
//		if (this.hoveredItem != null && inFrame)
//			this.renderToolTip(hoveredItem.stack, mouseX, mouseY);
	}

	private StorageViewContentPacket.ItemEntry hoveredItem;

	private int margin = 3;

	@Override
	public void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float pPartialTick) {
		TorchUtil.insetBox(
				pPoseStack,
				Rezolve.loc("textures/gui/widgets/storage_view_background.png"),
				x, y, width, height
		);

		int localMouseX = mouseX - this.x;
		int localMouseY = mouseY - this.y;

		var min = new Vector3f(x, y, 0);
		min.transform(pPoseStack.last().normal());

		var max = new Vector3f(x + width, y + width, 0);
		max.transform(pPoseStack.last().normal());

		if (windowColumnCount == 0)
			windowColumnCount = 1;

		pushPose(pPoseStack, () -> {
			repose(pPoseStack, () -> {
				pPoseStack.translate(this.x, this.y, 0);
			});

			//enableScissor(x, y + 1, x + width, y + height - 1);
			scissor(pPoseStack, 0, 0 + 1, width, height - 3, () -> {
				if (this.visibleItems != null) {
					int x = this.displayedOffset % this.windowColumnCount;
					int y = this.displayedOffset / this.windowColumnCount;

					StorageViewContentPacket.ItemEntry hoveredItem = null;

					for (StorageViewContentPacket.ItemEntry item : this.visibleItems) {

						int slotPosX = margin + this.itemSize*x;
						int slotPosY = margin + this.itemSize*y + this.scrollOffset;

						if (slotPosY < localMouseY && slotPosX < localMouseX && slotPosY + this.itemSize > localMouseY && slotPosX + this.itemSize > localMouseX) {
							// mouse is on the thing
							hoveredItem = item;
						}

						if (slotPosY + this.itemSize >= 0 && slotPosY < this.height && slotPosX >= 0 && slotPosX < this.width) {
							this.drawItemStack(pPoseStack, item.stack, slotPosX, slotPosY, this.shortenCount(item.amount));
						}

						x += 1;
						if (x >= this.windowColumnCount) {
							y += 1;
							x = 0;
						}
					}

					this.hoveredItem = hoveredItem;

					int scrollBarX = this.width - 3;
					int scrollBarY = 2;
					int scrollBarHeight = this.height - 4;
					int scrollBarWidth = 1;
					int scrollableRowCount = Math.max(0, this.windowTotalRowCount - this.windowRowCount);
					float scrollSpaceFactor = (float)this.windowRowCount / this.windowTotalRowCount;
					int puckHeight = Math.min(scrollBarHeight, (int)Math.max(10, scrollSpaceFactor * scrollBarHeight));
					float scrollFactor = -(float)this.scrollOffset / (this.itemSize*scrollableRowCount);
					int puckPosition = (int)(scrollFactor * (scrollBarHeight - puckHeight));

					if (puckPosition < 0)
						puckPosition = 0;

					TorchUtil.colorQuad(pPoseStack, 0x44000000, scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight);
					TorchUtil.colorQuad(pPoseStack, 0x88FFFFFF, scrollBarX, scrollBarY + puckPosition, scrollBarWidth, puckHeight);

				} else {
					this.font.draw(pPoseStack, noConnectionMessage, 7, 30, 0xFFFFFFFF);
				}
			});
		});
	}

	protected void renderToolTip(ItemStack stack, int x, int y)
	{
		// TODO
//		List<Component> list = stack.getTooltipLines(minecraft.player, mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
//
//		for (int i = 0; i < list.size(); ++i)
//		{
//			if (i == 0)
//			{
//				list.set(i, stack.getRarity().color + (String)list.get(i));
//			}
//			else
//			{
//				list.set(i, ChatFormatting.GRAY + (String)list.get(i));
//			}
//		}
//
//		net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack);
//
//		this.drawHoveringText(list, x, y, (font == null ? this.font : font));
//		net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
	}

//	protected void drawHoveringText(List<String> textLines, int x, int y)
//	{
//		net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(textLines, x, y, screen.width, screen.height, -1, font);
//		if (false && !textLines.isEmpty())
//		{
//			GlStateManager.disableRescaleNormal();
//			RenderHelper.disableStandardItemLighting();
//			GlStateManager.disableLighting();
//			GlStateManager.disableDepth();
//			int i = 0;
//
//			for (String s : textLines) {
//				int j = this.font.getStringWidth(s);
//
//				if (j > i) {
//					i = j;
//				}
//			}
//
//			int l1 = x + 12;
//			int i2 = y - 12;
//			int k = 8;
//
//			if (textLines.size() > 1)
//			{
//				k += 2 + (textLines.size() - 1) * 10;
//			}
//
//			if (l1 + i > this.width)
//			{
//				l1 -= 28 + i;
//			}
//
//			if (i2 + k + 6 > this.height)
//			{
//				i2 = this.height - k - 6;
//			}
//
//			RenderItem itemRender = minecraft.getRenderItem();
//			this.zLevel = 300.0F;
//			itemRender.zLevel = 300.0F;
//			int l = -267386864;
//			this.drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, -267386864, -267386864);
//			this.drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, -267386864, -267386864);
//			this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, -267386864, -267386864);
//			this.drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, -267386864, -267386864);
//			this.drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, -267386864, -267386864);
//			int i1 = 1347420415;
//			int j1 = 1344798847;
//			this.drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
//			this.drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
//			this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
//			this.drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, 1344798847, 1344798847);
//
//			for (int k1 = 0; k1 < textLines.size(); ++k1)
//			{
//				String s1 = (String)textLines.get(k1);
//				this.font.drawStringWithShadow(s1, (float)l1, (float)i2, -1);
//
//				if (k1 == 0)
//				{
//					i2 += 2;
//				}
//
//				i2 += 10;
//			}
//
//			this.zLevel = 0.0F;
//			itemRender.zLevel = 0.0F;
//			GlStateManager.enableLighting();
//			GlStateManager.enableDepth();
//			RenderHelper.enableStandardItemLighting();
//			GlStateManager.enableRescaleNormal();
//		}
//	}

	public boolean inBounds(double mouseX, double mouseY) {
		return (
			mouseX > this.x
			&& mouseX < this.x + this.width
			&& mouseY > this.y
			&& mouseY < this.y + this.height
		);
	}

	private void sendStateToServer() {
		StorageViewQuery state = new StorageViewQuery();
		state.setMenu(screen.getMenu());
		state.offset = this.offset;
		state.limit = this.windowColumnCount * (this.windowRowCount + 2);
		state.playerId = minecraft.player.getStringUUID();
		state.query = this.query;

		state.sendToServer();
	}

	@Override
	public boolean isMouseOver(double pMouseX, double pMouseY) {
		return x < pMouseX && pMouseX < x + width && y < pMouseY && pMouseY < y + height;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double pDelta) {
		boolean inFrame = (mouseX > this.x && mouseX < this.x + this.width && mouseY > this.y && mouseY < this.y + this.height);
		if (inFrame) {
			this.handleMouseWheel(pDelta);
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, pDelta);
	}

	private int scrollSpeed = 10;

	public void handleMouseWheel(double dWheel) {

		this.scrollOffset += dWheel * this.scrollSpeed;
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

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.HOVERED;
	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
		// TODO
	}
}
