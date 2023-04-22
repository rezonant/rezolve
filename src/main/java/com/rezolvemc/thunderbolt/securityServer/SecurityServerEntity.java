package com.rezolvemc.thunderbolt.securityServer;

import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SecurityServerEntity extends MachineEntity {
	public SecurityServerEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(SecurityServerEntity.class), pPos, pBlockState);

		this.ruleSet.add(new SecurityRule("<machines>", SecurityRule.MODE_OPEN));
		this.ruleSet.add(new SecurityRule("<players>", SecurityRule.MODE_RESTRICTED));
	}

	void setRootUser(Player player) {
		this.rootUser = player.getGameProfile().getName();
		
		SecurityRule playerRule = ruleSet.getRuleByName(this.rootUser);
		
		if (playerRule == null) {
			System.out.println("CREATING ROOT USER OF "+this.rootUser);
			playerRule = new SecurityRule(this.rootUser, SecurityRule.MODE_OWNER);
			this.ruleSet.add(playerRule);
		} else {
			playerRule.mode = SecurityRule.MODE_OWNER;
		}
	}

	SecurityRuleSet ruleSet = new SecurityRuleSet();
	String rootUser = null;

	public String getRootUser() {
		return rootUser;
	}

	public SecurityRule[] getRules() {
		return ruleSet.asCopiedArray();
	}

	public void addRule(SecurityRule rule) {
		this.ruleSet.add(rule);
		this.setChanged();
	}
	
	public boolean editRule(SecurityRule editedRule) {
		SecurityRule rule = ruleSet.getRuleById(editedRule.id);
		
		if (rule == null)
			return false;

		if (!"<players>".equals(rule.getName()) && !"<machines>".equals(rule.getName())) {
			rule.name = editedRule.name;
		}
		
		rule.mode = editedRule.mode;
		this.setChanged();
		
		return true;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("rootUser")) {
			this.rootUser = tag.getString("rootUser");
		}

		if (tag.contains("ruleSet")) {
			this.ruleSet = SecurityRuleSet.of(tag.getCompound("ruleSet"));
		}

		if (ruleSet.isEmpty()) {
			// Reset rules.
			ruleSet.add(new SecurityRule("<machines>", SecurityRule.MODE_OPEN));
			ruleSet.add(new SecurityRule("<players>", SecurityRule.MODE_RESTRICTED));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (this.rootUser != null) {
			tag.putString("rootUser", this.rootUser);
		}

		tag.put("ruleSet", ruleSet.asTag());
	}

	public void removeRule(SecurityRule selectedRule) {
		if (selectedRule == null)
			return;

		SecurityRule ruleToRemove = ruleSet.resolve(selectedRule);
		if (ruleToRemove == null)
			return;

		if ("<players>".equals(ruleToRemove.getName()))
			return;
		if ("<machines>".equals(ruleToRemove.getName()))
			return;
		
		ruleSet.remove(ruleToRemove);
		this.setChanged();
	}

	public boolean canPlayerOpen(Player player) {
		if (this.rootUser != null && this.rootUser.equals(player.getName())) 
			return true;
		
		int playerAccess = this.getPlayerAccess(player);
		if (playerAccess == SecurityRule.MODE_OWNER)
			return true;
		
		return false;
	}
	
	public int getPlayerAccess(Player player) {
		int playerAccess = SecurityRule.MODE_RESTRICTED;

		// Get default player policy

		for (SecurityRule rule : ruleSet.rules) {
			if ("<players>".equals(rule.getName())) {
				playerAccess = rule.getMode();
			}
		}

		// Get specific policy for this player, if one exists

		var playerRule = ruleSet.getRuleByName(player.getGameProfile().getName());
		if (playerRule != null)
			playerAccess = playerRule.mode;
		
		return playerAccess;
	}
	
	public boolean canPlayerUse(Player entityPlayer, BlockPos pos) {

		int playerAccess = this.getPlayerAccess(entityPlayer);
		
		int machineAccess = SecurityRule.MODE_NONE;
		SecurityRule machineRule = ruleSet.getRuleByName("<machines>");
		
		if (machineRule != null) {
			machineAccess = machineRule.getMode();
		}
		
		if (machineAccess == SecurityRule.MODE_NONE) {
			return true;
		} else if (machineAccess == SecurityRule.MODE_PROTECTED) {
			if (playerAccess == SecurityRule.MODE_ALLOWED || playerAccess == SecurityRule.MODE_OWNER)
				return true;
			return false;
		} else if (machineAccess == SecurityRule.MODE_OPEN) {
			if (playerAccess == SecurityRule.MODE_RESTRICTED)
				return false;
			return true;
		} else if (machineAccess == SecurityRule.MODE_OWNER) {
			if (playerAccess == SecurityRule.MODE_OWNER)
				return true;
			return false;
		}
		
		// Well we don't know how to handle it.
		
		return true;
	}
}
