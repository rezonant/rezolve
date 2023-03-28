package com.astronautlabs.mc.rezolve.securityServer;

import java.io.IOException;

import com.astronautlabs.mc.rezolve.common.BaseScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.astronautlabs.mc.rezolve.securityServer.SecurityServerEntity.Rule;

public class SecurityServerScreen extends BaseScreen<SecurityServerMenu> {

	public SecurityServerScreen(SecurityServerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, "rezolve:textures/gui/container/security_server_gui.png", 255, 191);
	}
	
	private SecurityServerEntity entity; // TODO
	
	public boolean matchesSearch(Rule rule) {
		if (rule.getName().toLowerCase().contains(this.searchField.getValue()))
			return true;
		
		return false;
	}

	@Override
	protected void init() {
		super.init();
		
		this.searchField = new EditBox(this.font, this.leftPos + 10, this.topPos + 7, 218, 18, Component.literal("Search"));
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
			Component.literal("Name")
		);
		this.nameField.setVisible(false);
		this.nameField.setValue("namen");
		this.nameField.setBordered(false);
		//this.nameField.setTextColor(0x000000);
		this.addRenderableWidget(this.nameField);

		this.playerModeBtn = new Button(this.leftPos + 44, this.topPos + 166, 90, 20, Component.literal("-Unset-"), (button) -> {

			// Player mode button clicked
			if (this.selectedRule != null) {
				int newMode = this.selectedRule.getMode() + 1;
				if (newMode > Rule.MODE_OWNER)
					newMode = Rule.MODE_RESTRICTED;

				this.selectedRule.setMode(newMode);

				this.updateModeButtons();
			}

		});

		this.playerModeBtn.visible = false;
		this.addRenderableWidget(this.playerModeBtn);

		this.machineModeBtn = new Button(this.leftPos + 44, this.topPos + 166, 90, 20, Component.literal("-Unset-"), (button) -> {

			if (this.selectedRule != null) {
				int newMode = this.selectedRule.getMode() + 1;
				if (newMode > Rule.MODE_OWNER)
					newMode = Rule.MODE_OPEN;

				this.selectedRule.setMode(newMode);
				this.updateModeButtons();
			}

		});
		this.machineModeBtn.visible = false;
		this.addRenderableWidget(this.machineModeBtn);

		this.editBtn = new Button(this.leftPos + 218, this.topPos + 167, 30, 20, Component.literal("Edit"), (button) -> {
			this.editing = true;
		});
		this.editBtn.visible = false;
		this.addRenderableWidget(this.editBtn);
		
		this.removeBtn = new Button(this.leftPos + 167, this.topPos + 167, 45, 20, Component.literal("Remove"), (button) -> {
			if (this.selectedRule != null) {
				if ("<players>".equals(this.selectedRule.getName()) || "<machines>".equals(this.selectedRule.getName()))
					return;

				this.entity.removeRule(this.selectedRule);
				this.selectedRule = null;
			}
		});
		this.removeBtn.visible = false;
		this.addRenderableWidget(this.removeBtn);
		
		this.saveBtn = new Button(this.leftPos + 218, this.topPos + 167, 30, 20, Component.literal("Save"), (button) -> {

			this.editing = false;

			if (this.selectedRule != null) {

				if (!"<machines>".equals(this.selectedRule.getName()) && !"<players>".equals(this.selectedRule.getName()))
					this.selectedRule.setName(this.nameField.getValue());

				if (this.selectedRule.getId() == null) {
					// New rule
					if (!"<players>".equals(this.selectedRule.getName()) && !"<machines>".equals(this.selectedRule.getName())) {
						this.entity.addRule(this.selectedRule.getName(), this.selectedRule.getMode());
					}
				} else {
					System.out.println("Editing rule "+this.selectedRule.getName()+" to be mode "+this.selectedRule.getMode());
					// Existing rule
					this.entity.editRule(this.selectedRule.getId(), this.selectedRule.getName(), this.selectedRule.getMode());
				}
			}
			this.selectedRule = null;
		});
		this.saveBtn.visible = false;
		this.addRenderableWidget(this.saveBtn);

		this.cancelBtn = new Button(this.leftPos + 176, this.topPos + 167, 40, 20, Component.literal("Cancel"), (button) -> {
			if (this.selectedRule != null) {
				if ("<machines>".equals(this.selectedRule.getName())) {
					this.selectRule(this.entity.getRuleByName("<machines>"));
				} else if (this.selectedRule.getId() == null) {
					this.selectedRule = null;
				}
			}
			this.editing = false;
		});
		this.cancelBtn.visible = false;
		this.addRenderableWidget(this.cancelBtn);
		
		this.addBtn = new Button(this.leftPos + 227, this.topPos + 2, 18, 20, Component.literal("+"), (button) -> {
			this.selectRule(this.entity.new Rule(null, "", Rule.MODE_ALLOWED));
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
	
	private Rule selectedRule = null;

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
	
	protected String getUserPolicyMode(int mode) {
		String modeStr = "Unknown ["+mode+"]";
		
		switch (mode) {
		case Rule.MODE_RESTRICTED: 
			modeStr = "Restricted";
			break;
		case Rule.MODE_ALLOWED:
			modeStr = "Allowed";
			break;
		case Rule.MODE_OWNER:
			modeStr = "Owner";
			break;
		}
		
		return modeStr;
	}
	
	protected String getMachinePolicyMode(int mode) {
		String modeStr = "Unknown ["+mode+"]";
		
		switch (mode) {
		case Rule.MODE_NONE:
			modeStr = "None";
			break;
		case Rule.MODE_OPEN: 
			modeStr = "Open";
			break;
		case Rule.MODE_PROTECTED: 
			modeStr = "Protected";
			break;
		case Rule.MODE_OWNER:
			modeStr = "Owners Only";
			break;
		}
		
		return modeStr;
	}
	
	private void updateModeButtons() {
		if ("<machines>".equals(this.selectedRule.getName())) {
			this.machineModeBtn.setMessage(Component.literal(this.getMachinePolicyMode(this.selectedRule.getMode())));
		} else {
			this.playerModeBtn.setMessage(Component.literal(this.getUserPolicyMode(this.selectedRule.getMode())));
		}
	}
	
	@Override
	protected void drawSubWindows(PoseStack poseStack, int mouseX, int mouseY) {
		
		int x = this.listX;
		int y = this.listY + this.listScrollPosition;
		
		for (Rule rule : this.entity.getRules()) {
			
			boolean mouseOver = 
				(x < mouseX && mouseX < x + this.listWidth) 
				&& (y < mouseY && mouseY < y + this.entryHeight)
				&& (listY < mouseY && mouseY < listY + this.listHeight)
			;
			
			if (!mouseDown && minecraft.mouseHandler.isLeftPressed() && mouseOver) {
				this.mouseDown = true;

				// We were clicked.

				this.selectRule(rule);

			}
			
			if (!minecraft.mouseHandler.isLeftPressed())
				mouseDown = false;
			
			if (y > 0 && y < this.listY + this.listHeight) {
				if (mouseOver) {
					colorQuad(0xFF999999, x, y, x + listWidth, y + entryHeight);
				}

				String modeStr = "";
				String nameStr = rule.getName();
				
				modeStr = this.getUserPolicyMode(rule.getMode());
				
				if ("<machines>".equals(rule.getName())) {
					nameStr = "Default Machine Policy";
					modeStr = this.getMachinePolicyMode(rule.getMode());
				} else if ("<players>".equals(rule.getName())) {
					nameStr = "Default User Policy";
				}
				
				if (this.entity.rootUser != null && this.entity.rootUser.equals(rule.getName())) {
					modeStr = modeStr + " [root]";
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
	}
	
	private void selectRule(Rule rule) {
		this.selectedRule = rule;
		this.editing = false;
		this.nameField.setValue(this.selectedRule.getName());
		this.updateModeButtons();
	}

	boolean editing = false;
	boolean mouseDown = false;

	@Override
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		
		// Handle a clicked button.
		
		if (!this.mouseDown && minecraft.mouseHandler.isLeftPressed()) {
			this.mouseDown = true;
		} else if (!minecraft.mouseHandler.isLeftPressed()) {
			this.mouseDown = false;
		}
		
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
			
			Rule rule = selectedRule;
			
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
					String modeName = this.getUserPolicyMode(rule.getMode());
					
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
