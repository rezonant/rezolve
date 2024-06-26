package com.rezolvemc.thunderbolt.securityServer;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.Sync;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.thunderbolt.securityServer.packets.AddSecurityRulePacket;
import com.rezolvemc.thunderbolt.securityServer.packets.EditSecurityRulePacket;
import com.rezolvemc.thunderbolt.securityServer.packets.RemoveSecurityRulePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class SecurityServerMenu extends MachineMenu<SecurityServerEntity> {
	public SecurityServerMenu(int containerId, Inventory playerInventory, SecurityServerEntity entity) {
		super(containerId, playerInventory, entity);
	}

	public SecurityServerMenu(int containerId, Inventory playerInventory) {
		super(containerId, playerInventory, null);
	}

	@Sync public SecurityRuleSet ruleSet = new SecurityRuleSet();
	@Sync public String rootUser;

	@Override
	protected void updateState() {
		ruleSet = machine.ruleSet;
		rootUser = machine.rootUser;
	}

	public void removeRule(SecurityRule rule) {
		var packet = new RemoveSecurityRulePacket();
		packet.setMenu(this);
		packet.rule = rule;
		packet.sendToServer();
	}

	public void addRule(SecurityRule rule) {
		var packet = new AddSecurityRulePacket();
		packet.setMenu(this);
		packet.rule = rule;
		packet.sendToServer();
	}

	public void editRule(SecurityRule rule) {
		var packet = new EditSecurityRulePacket();
		packet.setMenu(this);
		packet.rule = rule;
		packet.sendToServer();
	}

	@Override
	public void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) {
		if (rezolvePacket instanceof RemoveSecurityRulePacket removeSecurityRule) {
			machine.removeRule(removeSecurityRule.rule);
		} else if (rezolvePacket instanceof AddSecurityRulePacket addSecurityRule) {
			machine.addRule(addSecurityRule.rule);
		} else if (rezolvePacket instanceof EditSecurityRulePacket editSecurityRule) {
			machine.editRule(editSecurityRule.rule);
		} else {
			super.receivePacketOnServer(rezolvePacket, player);
		}
	}
}
