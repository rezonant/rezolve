package com.astronautlabs.mc.rezolve.securityServer;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.securityServer.SecurityServerEntity.Rule;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.math.BlockPos;

public class SecurityServerGuiContainer extends GuiContainerBase {

	public SecurityServerGuiContainer(EntityPlayer player, SecurityServerEntity entity) {
		super(new SecurityServerContainer(player, entity), "rezolve:textures/gui/container/security_server_gui.png", 255, 191);
		this.entity = entity;
	}
	
	private SecurityServerEntity entity;
	
	@Override
	public void initGui() {
		super.initGui();
		
		this.searchField = new GuiTextField(SEARCH_FIELD, this.fontRendererObj, this.guiLeft + 10, this.guiTop + 7, 235, 18);
		this.searchField.setVisible(true);
		this.searchField.setText("");
		this.searchField.setEnableBackgroundDrawing(false);
		//this.searchField.setTextColor(0x000000);
		this.addControl(this.searchField);
		
		this.nameField = new GuiTextField(
			NAME_FIELD, this.fontRendererObj, 
			this.guiLeft + 11, 
			this.guiTop + this.listX + this.listHeight + 14, 
			235, 18
		);
		this.nameField.setVisible(false);
		this.nameField.setText("namen");
		this.nameField.setEnableBackgroundDrawing(false);
		//this.nameField.setTextColor(0x000000);
		this.addControl(this.nameField);

		this.playerModeBtn = new GuiButton(PLAYER_MODE_BTN, this.guiLeft + 44, this.guiTop + 166, 90, 20, "-Unset-");
		this.playerModeBtn.visible = false;
		this.addControl(this.playerModeBtn);

		this.machineModeBtn = new GuiButton(MACHINE_MODE_BTN, this.guiLeft + 44, this.guiTop + 166, 90, 20, "-Unset-");
		this.machineModeBtn.visible = false;
		this.addControl(this.machineModeBtn);

		this.editBtn = new GuiButton(EDIT_BTN, this.guiLeft + 218, this.guiTop + 167, 30, 20, "Edit");
		this.editBtn.visible = false;
		this.addControl(this.editBtn);
		
		this.removeBtn = new GuiButton(REMOVE_BTN, this.guiLeft + 167, this.guiTop + 167, 45, 20, "Remove");
		this.removeBtn.visible = false;
		this.addControl(this.removeBtn);
		
		this.saveBtn = new GuiButton(SAVE_BTN, this.guiLeft + 218, this.guiTop + 167, 30, 20, "Save");
		this.saveBtn.visible = false;
		this.addControl(this.saveBtn);

		this.cancelBtn = new GuiButton(CANCEL_BTN, this.guiLeft + 176, this.guiTop + 167, 40, 20, "Cancel");
		this.cancelBtn.visible = false;
		this.addControl(this.cancelBtn);
		
		this.addBtn = new GuiButton(ADD_BTN, this.guiLeft + 227, this.guiTop + 2, 18, 20, "+");
		this.addBtn.visible = true;
		this.addControl(this.addBtn);
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
	
	private GuiTextField searchField;
	private GuiTextField nameField;
	private GuiButton playerModeBtn;
	private GuiButton machineModeBtn;
	private GuiButton editBtn;
	private GuiButton saveBtn;
	private GuiButton cancelBtn;
	private GuiButton addBtn;
	private GuiButton removeBtn;
	
	private int listX = 12;
	private int listY = 22;
	private int listScrollPosition = 0;
	private int listWidth = 235;
	private int listHeight = 117;
	private int entryHeight = 30;
	
	private Rule selectedRule = null;

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		
		Rule[] rules = this.entity.getRules();
		
		this.listScrollPosition += Mouse.getEventDWheel() / 10;
		this.listScrollPosition = Math.min(0, this.listScrollPosition);
		this.listScrollPosition = Math.max(Math.min(0, rules.length * this.entryHeight * -1 + this.listHeight), this.listScrollPosition);
	}
	
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
			this.machineModeBtn.displayString = this.getMachinePolicyMode(this.selectedRule.getMode());
		} else {
			this.playerModeBtn.displayString = this.getUserPolicyMode(this.selectedRule.getMode());
		}
	}
	
	@Override
	protected void drawSubWindows(int mouseX, int mouseY) {
		
		int x = this.listX;
		int y = this.listY + this.listScrollPosition;
		
		for (Rule rule : this.entity.getRules()) {
			
			boolean mouseOver = 
				(x < mouseX && mouseX < x + this.listWidth) 
				&& (y < mouseY && mouseY < y + this.entryHeight)
				&& (listY < mouseY && mouseY < listY + this.listHeight)
			;
			
			if (!mouseDown && Mouse.isButtonDown(0) && mouseOver) {
				this.mouseDown = true;
				
				// We were clicked.
				
				this.selectRule(rule);
				
			}
			
			if (!Mouse.isButtonDown(0))
				mouseDown = false;
			
			if (y > 0 && y < this.listY + this.listHeight) {
				if (mouseOver) {
					drawRect(x, y, x + listWidth, y + entryHeight, 0xFF999999);
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
				
				this.fontRendererObj.drawString(
					nameStr, 
					x + 2, y + 4, 
					0x000000
				);
				this.fontRendererObj.drawString(
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
		this.nameField.setText(this.selectedRule.getName());
		this.updateModeButtons();
	}

	boolean editing = false;
	boolean mouseDown = false;
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		// Handle a clicked button.
		
		if (!this.mouseDown && Mouse.isButtonDown(0)) {
			this.mouseDown = true;
			System.out.println("Mouse 0 is down -- "+this.cancelBtn.xPosition+" vs "+(guiLeft + mouseX));
			if (this.cancelBtn.visible && this.cancelBtn.mousePressed(mc, mouseX, mouseY)) {

				// Cancel button clicked
				if (this.selectedRule != null) {
					if ("<machines>".equals(this.selectedRule.getName())) {
						this.selectRule(this.entity.getRuleByName("<machines>"));
					} else if (this.selectedRule.getId() == null) {
						this.selectedRule = null;
					}
				}
				this.editing = false;
				
			} else if (this.editBtn.visible && this.editBtn.mousePressed(mc, mouseX, mouseY)) {
				
				// Edit button clicked
				this.editing = true;
				
			} else if (this.addBtn.visible && this.addBtn.mousePressed(mc, mouseX, mouseY)) {
				
				// Add button clicked
				
				this.selectRule(this.entity.new Rule(null, "", Rule.MODE_ALLOWED));
				this.editing = true;
				
			} else if (this.removeBtn.visible && this.removeBtn.mousePressed(mc, mouseX, mouseY)) {
				
				if (this.selectedRule != null) {
					if ("<players>".equals(this.selectedRule.getName()) || "<machines>".equals(this.selectedRule.getName()))
						return;
					
					// Remove button clicked
					this.entity.removeRule(this.selectedRule);
				}
				
			} else if (this.playerModeBtn.visible && this.playerModeBtn.mousePressed(mc, mouseX, mouseY)) {
				
				// Player mode button clicked
				if (this.selectedRule != null) {
					int newMode = this.selectedRule.getMode() + 1;
					if (newMode > Rule.MODE_OWNER)
						newMode = Rule.MODE_RESTRICTED;
					
					this.selectedRule.setMode(newMode);
					
					this.updateModeButtons();
				}
				
			} else if (this.machineModeBtn.visible && this.machineModeBtn.mousePressed(mc, mouseX, mouseY)) {
				
				// Machine mode button clicked
				if (this.selectedRule != null) {
					int newMode = this.selectedRule.getMode() + 1;
					if (newMode > Rule.MODE_OWNER)
						newMode = Rule.MODE_OPEN;
					
					this.selectedRule.setMode(newMode);
					this.updateModeButtons();
				}
				
			} else if (this.saveBtn.visible && this.saveBtn.mousePressed(mc, mouseX, mouseY)) {
				
				// Save button clicked
				this.editing = false;
				
				if (this.selectedRule != null) {
					
					if (!"<machines>".equals(this.selectedRule.getName()) && !"<players>".equals(this.selectedRule.getName()))
						this.selectedRule.setName(this.nameField.getText());
					
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
			}
		} else if (!Mouse.isButtonDown(0)) {
			this.mouseDown = false;
		}
		
		if (!this.searchField.isFocused() && "".equals(this.searchField.getText())) {
			this.fontRendererObj.drawString(
				"Search", 
				this.addBtn.xPosition - this.guiLeft - 40, 
				this.searchField.yPosition - this.guiTop + 1, 
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
				this.fontRendererObj.drawString(
					"Default Machine Policy", 
					this.nameField.xPosition - this.guiLeft, 
					this.nameField.yPosition - this.guiTop + 2, 
					0x000000
				);
				
				this.machineModeBtn.visible = true;

				this.fontRendererObj.drawString(
					"Mode:", 
					11, 
					this.playerModeBtn.yPosition - this.guiTop + listMargin - 6, 
					0x000000
				);
				this.saveBtn.visible = true;
				this.cancelBtn.visible = true;
			} else {
				if (editing) {
					if ("<players>".equals(rule.getName())) {
						this.fontRendererObj.drawString(
							"Default User Policy", 
							this.nameField.xPosition - this.guiLeft, 
							this.nameField.yPosition - this.guiTop + 2, 
							0x000000
						);
					} else {
						this.nameField.setVisible(true);
					}

					this.playerModeBtn.visible = true;
					this.saveBtn.visible = true;
					this.cancelBtn.visible = true;

					this.fontRendererObj.drawString(
						"Mode:", 
						11, 
						this.playerModeBtn.yPosition - this.guiTop + listMargin - 6, 
						0x000000
					);
					
					if (!this.nameField.isFocused() && "".equals(this.nameField.getText())) {
						this.fontRendererObj.drawString(
							"Enter Player Name", 
							this.nameField.xPosition - this.guiLeft, 
							this.nameField.yPosition - this.guiTop + 2, 
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
					
					this.fontRendererObj.drawString(
						ruleName, 
						this.nameField.xPosition - this.guiLeft, 
						this.nameField.yPosition - this.guiTop + 2, 
						0x000000
					);

					this.fontRendererObj.drawString(
						modeName, 
						this.nameField.xPosition - this.guiLeft, 
						this.nameField.yPosition - this.guiTop + 12,
						0x666666
					);

					this.editBtn.visible = true;
					this.removeBtn.visible = !"<players>".equals(rule.getName());
				}
			}
			
		} else {
			
			this.fontRendererObj.drawString(
				"Select a rule above.", 
				this.nameField.xPosition - this.guiLeft, 
				this.nameField.yPosition - this.guiTop + 2, 
				0x666666
			);
		}
	}
}
