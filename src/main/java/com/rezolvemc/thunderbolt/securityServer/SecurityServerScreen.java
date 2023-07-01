package com.rezolvemc.thunderbolt.securityServer;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.common.registry.ScreenFor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.widgets.Button;
import org.torchmc.ui.widgets.EditBox;
import org.torchmc.ui.widgets.ListView;
import org.torchmc.ui.widgets.ListViewItem;

import java.util.Locale;

@ScreenFor(SecurityServerMenu.class)
public class SecurityServerScreen extends MachineScreen<SecurityServerMenu> {

	public SecurityServerScreen(SecurityServerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 255, 191);

		enableInventoryLabel = false;

		menu.addEventListener(menu.PROPERTIES_CHANGED, e -> updateRuleList());
	}
	
	//private SecurityServerEntity entity; // TODO
	private ListView ruleList;

	public boolean matchesSearch(SecurityRule rule) {
		if (rule.getName().toLowerCase(Locale.ROOT).contains(this.searchField.getValue()))
			return true;
		
		return false;
	}

	private void updateRuleList() {
		ruleList.clearItems();
		for (var rule : menu.ruleSet.rules) {
			ruleList.addItem(new RuleListViewItem(rule));
		}
	}

	@Override
	protected void setup() {
		super.setup();

		setPanel(new VerticalLayoutPanel(), root -> {
			root.addChild(new HorizontalLayoutPanel(), searchRow -> {
				searchRow.addChild(new EditBox(Rezolve.str("search")), field -> {
					field.setExpansionFactor(1);
					//field.setBordered(false);
					//field.setTextColor(0x000000);

					searchField = field;
				});

				searchRow.addChild(new Button("+"), button -> {
					button.setHandler(x -> {
						var rule = new SecurityRule(null, "", SecurityRule.MODE_ALLOWED);
						// rule.draft = true;
						// menu.ruleSet.add(rule);

						this.selectRule(rule);
						this.editing = true;
					});
				});
			});

			root.addChild(new ListView(Rezolve.str("policies")), listView -> {
				listView.setExpansionFactor(1);
				ruleList = listView;
			});
		});
//
//		this.playerModeBtn = new Button(this.leftPos + 44, this.topPos + 166, 90, 20,
//				Component.translatable("screens.rezolve.unset"), (button) -> {
//
//			// Player mode button clicked
//			if (this.selectedRule != null) {
//				int newMode = this.selectedRule.getMode() + 1;
//				if (newMode > SecurityRule.MODE_OWNER)
//					newMode = SecurityRule.MODE_RESTRICTED;
//
//				this.selectedRule.setMode(newMode);
//
//				this.updateModeButtons();
//			}
//
//		});
//
//		this.editBtn = new Button(this.leftPos + 218, this.topPos + 167, 30, 20,
//				Component.translatable("screens.rezolve.edit"), (button) -> {
//			this.editing = true;
//		});
//		this.editBtn.visible = false;
//		this.addRenderableWidget(this.editBtn);
//
//		this.removeBtn = new Button(this.leftPos + 167, this.topPos + 167, 45, 20,
//				Component.translatable("screens.rezolve.remove"), (button) -> {
//			if (this.selectedRule != null) {
//				if ("<players>".equals(this.selectedRule.getName()) || "<machines>".equals(this.selectedRule.getName()))
//					return;
//
//				this.menu.removeRule(this.selectedRule);
//				this.selectedRule = null;
//			}
//		});
//		this.removeBtn.visible = false;
//		this.addRenderableWidget(this.removeBtn);
//
//		this.saveBtn = new Button(this.leftPos + 218, this.topPos + 167, 30, 20,
//				Component.translatable("screens.rezolve.save"), (button) -> {
//
//			this.editing = false;
//
//			if (this.selectedRule != null) {
//
//				if (!"<machines>".equals(this.selectedRule.getName()) && !"<players>".equals(this.selectedRule.getName()))
//					this.selectedRule.setName(this.nameField.getValue());
//
//				if (this.selectedRule.getId() == null) {
//					// New rule
//					if (!"<players>".equals(this.selectedRule.getName()) && !"<machines>".equals(this.selectedRule.getName())) {
//						this.menu.addRule(this.selectedRule);
//					}
//				} else {
//					System.out.println("Editing rule "+this.selectedRule.getName()+" to be mode "+this.selectedRule.getMode());
//					// Existing rule
//					this.menu.editRule(this.selectedRule);
//				}
//			}
//			this.selectedRule = null;
//		});
//		this.saveBtn.visible = false;
//		this.addRenderableWidget(this.saveBtn);
//
//		this.cancelBtn = new Button(this.leftPos + 176, this.topPos + 167, 40, 20,
//				Component.translatable("screens.rezolve.cancel"), (button) -> {
//			if (this.selectedRule != null) {
//				if ("<machines>".equals(this.selectedRule.getName())) {
//					this.selectRule(this.menu.ruleSet.getRuleByName("<machines>"));
//				} else if (this.selectedRule.getId() == null) {
//					this.selectedRule = null;
//				}
//			}
//			this.editing = false;
//		});
//		this.cancelBtn.visible = false;
//		this.addRenderableWidget(this.cancelBtn);
	}
	
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

	public class RuleListViewItem implements ListViewItem {
		public RuleListViewItem(SecurityRule rule) {
			this.rule = rule;
		}

		public final SecurityRule rule;

		@Override
		public void mouseClicked(int button) {
			selectRule(rule);
		}

		@Override
		public int getHeight() {
			return font.lineHeight * 2;
		}

		@Override
		public void render(PoseStack poseStack, int width, int mouseX, int mouseY, float partialTicks) {

			if (selectedRule == rule) {
				colorQuad(poseStack, 0xFF666666, 0, 0, width, getHeight());
			}

			Component modeStr = Component.literal("");
			Component nameStr = Component.literal(rule.getName());

			modeStr = SecurityRuleEditor.getUserPolicyMode(rule.getMode());

			if ("<machines>".equals(rule.getName())) {
				nameStr = Component.translatable("screens.rezolve.default_machine_policy");
				modeStr = SecurityRuleEditor.getMachinePolicyMode(rule.getMode());
			} else if ("<players>".equals(rule.getName())) {
				nameStr = Component.translatable("screens.rezolve.default_user_policy");
			}

			if (menu.rootUser != null && menu.rootUser.equals(rule.getName())) {
				modeStr = Component.empty()
					.append(modeStr)
					.append(" [")
					.append(Component.translatable("screens.rezolve.root"))
					.append("]")
				;
			}

			font.draw(
				poseStack,
				nameStr,
				2, 4,
				0x000000
			);
			font.draw(
				poseStack,
				modeStr,
				2, 4 + font.lineHeight,
				0x666666
			);
		}
	}

	private void selectRule(SecurityRule rule) {
		new SecurityRuleEditor(rule).present();
	}

	boolean editing = false;

	@Override
	public Component getTitle() {
		return Rezolve.tr("block.rezolve.security_server");
	}

	@Override
	public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);

//		if (!this.searchField.isFocused() && "".equals(this.searchField.getValue())) {
//			this.font.draw(
//					pPoseStack,
//				"Search",
//				this.addBtn.x - this.leftPos - 40,
//				this.searchField.y - this.topPos + 1,
//				0x666666
//			);
//		}
//
//		this.nameField.setVisible(false);
//		this.playerModeBtn.visible = false;
//		this.machineModeBtn.visible = false;
//		this.editBtn.visible = false;
//		this.removeBtn.visible = false;
//		this.saveBtn.visible = false;
//		this.cancelBtn.visible = false;
//
//		int listMargin = 12;
//
//		if (selectedRule != null) {
//
//			SecurityRule rule = selectedRule;
//
//			if ("<machines>".equals(rule.getName())) {
//				this.font.draw(
//						pPoseStack,
//					"Default Machine Policy",
//					this.nameField.x - this.leftPos,
//					this.nameField.y - this.topPos + 2,
//					0x000000
//				);
//
//				this.machineModeBtn.visible = true;
//
//				this.font.draw(
//						pPoseStack,
//					"Mode:",
//					11,
//					this.playerModeBtn.y - this.topPos + listMargin - 6,
//					0x000000
//				);
//				this.saveBtn.visible = true;
//				this.cancelBtn.visible = true;
//			} else {
//				if (editing) {
//					if ("<players>".equals(rule.getName())) {
//						this.font.draw(
//								pPoseStack,
//							"Default User Policy",
//							this.nameField.x - this.leftPos,
//							this.nameField.y - this.topPos + 2,
//							0x000000
//						);
//					} else {
//						this.nameField.setVisible(true);
//					}
//
//					this.playerModeBtn.visible = true;
//					this.saveBtn.visible = true;
//					this.cancelBtn.visible = true;
//
//					this.font.draw(
//							pPoseStack,
//						"Mode:",
//						11,
//						this.playerModeBtn.y - this.topPos + listMargin - 6,
//						0x000000
//					);
//
//					if (!this.nameField.isFocused() && "".equals(this.nameField.getValue())) {
//						this.font.draw(
//								pPoseStack,
//							"Enter Player Name",
//							this.nameField.x - this.leftPos,
//							this.nameField.y - this.topPos + 2,
//							0x666666
//						);
//					}
//				} else {
//					String ruleName =
//						"<players>".equals(rule.getName())
//							? "Default User Policy"
//							: rule.getName()
//					;
//					Component modeName = this.getUserPolicyMode(rule.getMode());
//
//					this.font.draw(
//							pPoseStack,
//						ruleName,
//						this.nameField.x - this.leftPos,
//						this.nameField.y - this.topPos + 2,
//						0x000000
//					);
//
//					this.font.draw(
//							pPoseStack,
//						modeName,
//						this.nameField.x - this.leftPos,
//						this.nameField.y - this.topPos + 12,
//						0x666666
//					);
//
//					this.editBtn.visible = true;
//					this.removeBtn.visible = !"<players>".equals(rule.getName());
//				}
//			}
//
//		} else {
//
//			this.font.draw(
//					pPoseStack,
//				"Select a rule above.",
//				this.nameField.x - this.leftPos,
//				this.nameField.y - this.topPos + 2,
//				0x666666
//			);
//		}
	}
}
