package com.rezolvemc.thunderbolt.securityServer;

import com.rezolvemc.common.machines.MachineScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.common.registry.ScreenFor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

@ScreenFor(SecurityServerMenu.class)
public class SecurityServerScreen extends MachineScreen<SecurityServerMenu> {

	public SecurityServerScreen(SecurityServerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 255, 191);

		enableInventoryLabel = false;
	}
	
	//private SecurityServerEntity entity; // TODO
	
	public boolean matchesSearch(SecurityRule rule) {
		if (rule.getName().toLowerCase(Locale.ROOT).contains(this.searchField.getValue()))
			return true;
		
		return false;
	}

	@Override
	protected void setup() {
		super.setup();
		
		this.searchField = new EditBox(this.font, this.leftPos + 10, this.topPos + 7, 218, 18, Component.translatable("screens.rezolve.search"));
		this.searchField.setVisible(true);
		this.searchField.setValue("");
		this.searchField.setBordered(false);
		//this.searchField.setTextColor(0x000000);
		this.addRenderableWidget(this.searchField);
		
		this.nameField = new EditBox(
			this.font,
			this.leftPos + 11,
			this.topPos + this.listX + this.listHeight + 14,
			235, 18,
			Component.translatable("screens.rezolve.name")
		);
		this.nameField.setVisible(false);
		this.nameField.setValue("namen");
		this.nameField.setBordered(false);
		//this.nameField.setTextColor(0x000000);
		this.addRenderableWidget(this.nameField);

		this.playerModeBtn = new Button(this.leftPos + 44, this.topPos + 166, 90, 20,
				Component.translatable("screens.rezolve.unset"), (button) -> {

			// Player mode button clicked
			if (this.selectedRule != null) {
				int newMode = this.selectedRule.getMode() + 1;
				if (newMode > SecurityRule.MODE_OWNER)
					newMode = SecurityRule.MODE_RESTRICTED;

				this.selectedRule.setMode(newMode);

				this.updateModeButtons();
			}

		});

		this.playerModeBtn.visible = false;
		this.addRenderableWidget(this.playerModeBtn);

		this.machineModeBtn = new Button(this.leftPos + 44, this.topPos + 166, 90, 20,
				Component.translatable("screens.rezolve.unset"), (button) -> {

			if (this.selectedRule != null) {
				int newMode = this.selectedRule.getMode() + 1;
				if (newMode > SecurityRule.MODE_OWNER)
					newMode = SecurityRule.MODE_OPEN;

				this.selectedRule.setMode(newMode);
				this.updateModeButtons();
			}

		});
		this.machineModeBtn.visible = false;
		this.addRenderableWidget(this.machineModeBtn);

		this.editBtn = new Button(this.leftPos + 218, this.topPos + 167, 30, 20,
				Component.translatable("screens.rezolve.edit"), (button) -> {
			this.editing = true;
		});
		this.editBtn.visible = false;
		this.addRenderableWidget(this.editBtn);
		
		this.removeBtn = new Button(this.leftPos + 167, this.topPos + 167, 45, 20,
				Component.translatable("screens.rezolve.remove"), (button) -> {
			if (this.selectedRule != null) {
				if ("<players>".equals(this.selectedRule.getName()) || "<machines>".equals(this.selectedRule.getName()))
					return;

				this.menu.removeRule(this.selectedRule);
				this.selectedRule = null;
			}
		});
		this.removeBtn.visible = false;
		this.addRenderableWidget(this.removeBtn);
		
		this.saveBtn = new Button(this.leftPos + 218, this.topPos + 167, 30, 20,
				Component.translatable("screens.rezolve.save"), (button) -> {

			this.editing = false;

			if (this.selectedRule != null) {

				if (!"<machines>".equals(this.selectedRule.getName()) && !"<players>".equals(this.selectedRule.getName()))
					this.selectedRule.setName(this.nameField.getValue());

				if (this.selectedRule.getId() == null) {
					// New rule
					if (!"<players>".equals(this.selectedRule.getName()) && !"<machines>".equals(this.selectedRule.getName())) {
						this.menu.addRule(this.selectedRule);
					}
				} else {
					System.out.println("Editing rule "+this.selectedRule.getName()+" to be mode "+this.selectedRule.getMode());
					// Existing rule
					this.menu.editRule(this.selectedRule);
				}
			}
			this.selectedRule = null;
		});
		this.saveBtn.visible = false;
		this.addRenderableWidget(this.saveBtn);

		this.cancelBtn = new Button(this.leftPos + 176, this.topPos + 167, 40, 20,
				Component.translatable("screens.rezolve.cancel"), (button) -> {
			if (this.selectedRule != null) {
				if ("<machines>".equals(this.selectedRule.getName())) {
					this.selectRule(this.menu.ruleSet.getRuleByName("<machines>"));
				} else if (this.selectedRule.getId() == null) {
					this.selectedRule = null;
				}
			}
			this.editing = false;
		});
		this.cancelBtn.visible = false;
		this.addRenderableWidget(this.cancelBtn);
		
		this.addBtn = new Button(this.leftPos + 227, this.topPos + 2, 18, 20,
				Component.literal("+"), (button) -> {

			var rule = new SecurityRule(null, "", SecurityRule.MODE_ALLOWED);

//			rule.draft = true;
//			menu.ruleSet.add(rule);

			this.selectRule(rule);
			this.editing = true;
		});
		this.addBtn.visible = true;
		this.addRenderableWidget(this.addBtn);
	}

	private static int SEARCH_FIELD = 0;
	private static int NAME_FIELD = 1;
	private static int MACHINE_MODE_BTN = 2;
	private static int PLAYER_MODE_BTN = 3;
	private static int SAVE_BTN = 4;
	private static int CANCEL_BTN = 5;
	private static int EDIT_BTN = 6;
	private static int ADD_BTN = 7;
	private static int REMOVE_BTN = 7;
	
	private EditBox searchField;
	private EditBox nameField;
	private Button playerModeBtn;
	private Button machineModeBtn;
	private Button editBtn;
	private Button saveBtn;
	private Button cancelBtn;
	private Button addBtn;
	private Button removeBtn;
	
	private int listX = 12;
	private int listY = 22;
	private int listScrollPosition = 0;
	private int listWidth = 235;
	private int listHeight = 117;
	private int entryHeight = 30;
	
	private SecurityRule selectedRule = null;

//	@Override
//	public void handleMouseInput() throws IOException {
//		super.handleMouseInput();
//
//		Rule[] rules = this.entity.getRules();
//
//		this.listScrollPosition += Mouse.getEventDWheel() / 10;
//		this.listScrollPosition = Math.min(0, this.listScrollPosition);
//		this.listScrollPosition = Math.max(Math.min(0, rules.length * this.entryHeight * -1 + this.listHeight), this.listScrollPosition);
//	}
	
	protected Component getUserPolicyMode(int mode) {
		Component modeStr = Component.empty()
				.append(Component.translatable("screens.rezolve.unknown"))
				.append(" [")
				.append(mode + "")
				.append("]")
				;
		
		switch (mode) {
		case SecurityRule.MODE_RESTRICTED:
			modeStr = Component.translatable("screens.rezolve.restricted");
			break;
		case SecurityRule.MODE_ALLOWED:
			modeStr = Component.translatable("screens.rezolve.allowed");
			break;
		case SecurityRule.MODE_OWNER:
			modeStr = Component.translatable("screens.rezolve.owner");
			break;
		}
		
		return modeStr;
	}

	protected Component getMachinePolicyMode(int mode) {
		Component modeStr = Component.empty()
				.append(Component.translatable("screens.rezolve.unknown"))
				.append(" [")
				.append(mode+"")
				.append("]");
		
		switch (mode) {
		case SecurityRule.MODE_NONE:
			modeStr = Component.translatable("screens.rezolve.none");
			break;
		case SecurityRule.MODE_OPEN:
			modeStr = Component.translatable("screens.rezolve.open");
			break;
		case SecurityRule.MODE_PROTECTED:
			modeStr = Component.translatable("screens.rezolve.protected");
			break;
		case SecurityRule.MODE_OWNER:
			modeStr = Component.translatable("screens.rezolve.owners_only");
			break;
		}
		
		return modeStr;
	}
	
	private void updateModeButtons() {
		if ("<machines>".equals(this.selectedRule.getName())) {
			this.machineModeBtn.setMessage(this.getMachinePolicyMode(this.selectedRule.getMode()));
		} else {
			this.playerModeBtn.setMessage(this.getUserPolicyMode(this.selectedRule.getMode()));
		}
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (pButton == 0 && hoveredRule != null) {
			this.selectRule(hoveredRule);
			return true;
		}

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	SecurityRule hoveredRule;

	@Override
	protected void renderSubWindows(PoseStack poseStack, double mouseX, double mouseY) {
		enableScissor(leftPos + listX, topPos + listY, leftPos + listX + listWidth + 1, topPos + listY + listHeight);
		colorQuad(poseStack, 0xFFCCCCCC, listX - 10, listY - 10, listWidth + 20, listHeight + 20);

		int x = this.listX;
		int y = this.listY + this.listScrollPosition;

		hoveredRule = null;
		for (SecurityRule rule : this.menu.ruleSet.rules) {
			
			boolean mouseOver = 
				(x < mouseX && mouseX < x + this.listWidth) 
				&& (y < mouseY && mouseY < y + this.entryHeight)
				&& (listY < mouseY && mouseY < listY + this.listHeight)
			;

			if (mouseOver)
				hoveredRule = rule;

			if (y > 0 && y < this.listY + this.listHeight) {
				if (mouseOver) {
					colorQuad(poseStack, 0xFF999999, x, y, listWidth, entryHeight);
				} else if (selectedRule == rule) {
					colorQuad(poseStack, 0xFF666666, x, y, listWidth, entryHeight);
				}

				Component modeStr = Component.literal("");
				Component nameStr = Component.literal(rule.getName());
				
				modeStr = this.getUserPolicyMode(rule.getMode());
				
				if ("<machines>".equals(rule.getName())) {
					nameStr = Component.translatable("screens.rezolve.default_machine_policy");
					modeStr = this.getMachinePolicyMode(rule.getMode());
				} else if ("<players>".equals(rule.getName())) {
					nameStr = Component.translatable("screens.rezolve.default_user_policy");
				}
				
				if (this.menu.rootUser != null && this.menu.rootUser.equals(rule.getName())) {
					modeStr = Component.empty()
							.append(modeStr)
							.append(" [")
							.append(Component.translatable("screens.rezolve.root"))
							.append("]")
					;
				}
				
				this.font.draw(
					poseStack,
					nameStr, 
					x + 2, y + 4, 
					0x000000
				);
				this.font.draw(
						poseStack,
					modeStr, 
					x + 2, y + 16, 
					0x666666
				);
			}
			
			y += this.entryHeight;
		}

		disableScissor();
	}
	
	private void selectRule(SecurityRule rule) {
		this.selectedRule = rule;
		this.editing = false;
		this.nameField.setValue(this.selectedRule.getName());
		this.updateModeButtons();
	}

	boolean editing = false;
	boolean mouseDown = false;

	@Override
	public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
		
		if (!this.searchField.isFocused() && "".equals(this.searchField.getValue())) {
			this.font.draw(
					pPoseStack,
				"Search", 
				this.addBtn.x - this.leftPos - 40,
				this.searchField.y - this.topPos + 1,
				0x666666
			);
		}
		
		this.nameField.setVisible(false);
		this.playerModeBtn.visible = false;
		this.machineModeBtn.visible = false;
		this.editBtn.visible = false;
		this.removeBtn.visible = false;
		this.saveBtn.visible = false;
		this.cancelBtn.visible = false;
		
		int listMargin = 12;
		
		if (selectedRule != null) {
			
			SecurityRule rule = selectedRule;
			
			if ("<machines>".equals(rule.getName())) {
				this.font.draw(
						pPoseStack,
					"Default Machine Policy", 
					this.nameField.x - this.leftPos,
					this.nameField.y - this.topPos + 2,
					0x000000
				);
				
				this.machineModeBtn.visible = true;

				this.font.draw(
						pPoseStack,
					"Mode:", 
					11, 
					this.playerModeBtn.y - this.topPos + listMargin - 6,
					0x000000
				);
				this.saveBtn.visible = true;
				this.cancelBtn.visible = true;
			} else {
				if (editing) {
					if ("<players>".equals(rule.getName())) {
						this.font.draw(
								pPoseStack,
							"Default User Policy", 
							this.nameField.x - this.leftPos,
							this.nameField.y - this.topPos + 2,
							0x000000
						);
					} else {
						this.nameField.setVisible(true);
					}

					this.playerModeBtn.visible = true;
					this.saveBtn.visible = true;
					this.cancelBtn.visible = true;

					this.font.draw(
							pPoseStack,
						"Mode:", 
						11, 
						this.playerModeBtn.y - this.topPos + listMargin - 6,
						0x000000
					);
					
					if (!this.nameField.isFocused() && "".equals(this.nameField.getValue())) {
						this.font.draw(
								pPoseStack,
							"Enter Player Name", 
							this.nameField.x - this.leftPos,
							this.nameField.y - this.topPos + 2,
							0x666666
						);
					}
				} else {
					String ruleName = 
						"<players>".equals(rule.getName()) 
							? "Default User Policy" 
							: rule.getName()
					;
					Component modeName = this.getUserPolicyMode(rule.getMode());
					
					this.font.draw(
							pPoseStack,
						ruleName, 
						this.nameField.x - this.leftPos,
						this.nameField.y - this.topPos + 2,
						0x000000
					);

					this.font.draw(
							pPoseStack,
						modeName, 
						this.nameField.x - this.leftPos,
						this.nameField.y - this.topPos + 12,
						0x666666
					);

					this.editBtn.visible = true;
					this.removeBtn.visible = !"<players>".equals(rule.getName());
				}
			}
			
		} else {
			
			this.font.draw(
					pPoseStack,
				"Select a rule above.", 
				this.nameField.x - this.leftPos,
				this.nameField.y - this.topPos + 2,
				0x666666
			);
		}
	}
}
