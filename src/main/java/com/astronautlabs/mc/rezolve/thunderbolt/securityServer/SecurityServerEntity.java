package com.astronautlabs.mc.rezolve.thunderbolt.securityServer;

import java.util.ArrayList;
import java.util.UUID;
import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SecurityServerEntity extends MachineEntity {
	public SecurityServerEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(SecurityServerEntity.class), pPos, pBlockState);

		this.rules = new ArrayList<Rule>();
		this.rules.add(new Rule("<machines>", Rule.MODE_OPEN));
		this.rules.add(new Rule("<players>", Rule.MODE_RESTRICTED));
	}

	void setRootUser(LivingEntity player) {
		this.rootUser = player.getUUID().toString();
		
		Rule playerRule = this.getRuleByNameInternal(this.rootUser);
		
		if (playerRule == null) {
			System.out.println("CREATING ROOT USER OF "+this.rootUser);
			playerRule = new Rule(this.rootUser, Rule.MODE_OWNER);
			this.rules.add(playerRule);
		} else {
			playerRule.mode = Rule.MODE_OWNER;
		}
	}

	public class Rule {
		public Rule(String id, String name, int mode) {
			this.id = id;
			this.name = name;
			this.mode = mode;
		}

		public Rule(String name, int mode) {
			this.id = UUID.randomUUID().toString();
			this.name = name;
			this.mode = mode;
		}

		public static final int MODE_OPEN = -1;
		public static final int MODE_RESTRICTED = 0;
		public static final int MODE_ALLOWED = 1;
		public static final int MODE_OWNER = 2;

		public static final int MODE_NONE = 0;
		public static final int MODE_PROTECTED = 1;

		String id;
		String name;
		int mode;
		
		public String getId() {
			return this.id;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setMode(int mode) {
			this.mode = mode;
		}
		
		public int getMode() {
			return this.mode;
		}
		
		public Rule copy() {
			return new Rule(this.id, this.name, this.mode);
		}
	}
	
	ArrayList<Rule> rules = new ArrayList<Rule>();
	String rootUser = null;
	
	public void addRule(String name, int mode) {
		if (this.getLevel().isClientSide) {
			
//			RezolvePacketHandler.INSTANCE.sendToServer(new RuleModificationMessage(
//				Minecraft.getInstance().thePlayer,
//				this, "", name, mode
//			));
			
			return;
		}
		
		this.rules.add(new Rule(name, mode));
		this.setChanged();
	}

	public Rule getRuleById(String id) {
		for (Rule rule : this.rules) {
			if (id.equals(rule.id))
				return rule.copy();
		}
		
		return null;
	}

	private Rule getRuleByIdInternal(String id) {
		for (Rule rule : this.rules) {
			if (id.equals(rule.id))
				return rule;
		}
		
		return null;
	}
	
	public boolean editRule(String id, String name, int mode) {
		Rule rule = this.getRuleByIdInternal(id);
		
		if (rule == null)
			return false;
		
		if (this.getLevel().isClientSide) {
//			RezolvePacketHandler.INSTANCE.sendToServer(new RuleModificationMessage(
//				Minecraft.getInstance().thePlayer,
//				this, id, name, mode
//			));
				
			return true;
		}

		if (!"<players>".equals(rule.getName()) && !"<machines>".equals(rule.getName())) {
			rule.name = name;
		}
		
		rule.mode = mode;
		this.setChanged();
		
		return true;
	}
	
	public Rule[] getRules() {
		ArrayList<Rule> ruleCopies = new ArrayList<Rule>();
		for (Rule rule : this.rules)
			ruleCopies.add(rule.copy());
		
		return ruleCopies.toArray(new Rule[this.rules.size()]);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("RootUser")) {
			this.rootUser = tag.getString("RootUser");
		}

		if (tag.contains("Rules")) {
			ListTag rulesList = tag.getList("Rules", Tag.TAG_COMPOUND);
			this.rules = new ArrayList<Rule>();

			for (int i = 0, max = rulesList.size(); i < max; ++i) {
				CompoundTag ruleNBT = (CompoundTag)rulesList.get(i);
				this.rules.add(new Rule(ruleNBT.getString("ID"), ruleNBT.getString("Name"), ruleNBT.getInt("Mode")));
			}
		}

		if (this.rules == null || this.rules.size() == 0) {
			// Reset rules.
			this.rules = new ArrayList<Rule>();
			this.rules.add(new Rule("<machines>", Rule.MODE_OPEN));
			this.rules.add(new Rule("<players>", Rule.MODE_RESTRICTED));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		var compound = super.serializeNBT();
		if (this.rootUser != null) {
			compound.putString("RootUser", this.rootUser);
		}

		ListTag rulesList = new ListTag();

		for (Rule rule : this.rules) {
			CompoundTag ruleNBT = new CompoundTag();
			ruleNBT.putString("ID", rule.id);
			ruleNBT.putString("Name", rule.name);
			ruleNBT.putInt("Mode", rule.mode);
			rulesList.add(ruleNBT);
		}

		compound.put("Rules", rulesList);
	}

	public void removeRule(String id) {
		this.removeRule(this.getRuleById(id));
	}
	
	public void removeRule(Rule selectedRule) {
		if (selectedRule == null)
			return;
		
		if (this.getLevel().isClientSide) {

//			RezolvePacketHandler.INSTANCE.sendToServer(new RuleModificationMessage(
//				Minecraft.getInstance().thePlayer,
//				this, selectedRule.getId(), "", -2
//			));
			
			return;
		}
		
		Rule ruleToRemove = this.getRuleByIdInternal(selectedRule.getId());
		if (ruleToRemove == null)
			return;

		if ("<players>".equals(ruleToRemove.getName()))
			return;
		if ("<machines>".equals(ruleToRemove.getName()))
			return;
		
		this.rules.remove(ruleToRemove);
		this.setChanged();
	}

	public Rule getRuleByName(String name) {
		for (Rule rule : this.rules) {
			if (name.equals(rule.name))
				return rule.copy();
		}
		
		return null;
	}
	
	private Rule getRuleByNameInternal(String name) {
		for (Rule rule : this.rules) {
			if (name.equals(rule.name))
				return rule;
		}
		
		return null;
	}

	public boolean canPlayerOpen(Player player) {

		if (this.rootUser != null && this.rootUser.equals(player.getName())) 
			return true;
		
		int playerAccess = this.getPlayerAccess(player);
		if (playerAccess == Rule.MODE_OWNER)
			return true;
		
		return false;
	}
	
	public int getPlayerAccess(Player player) {

		int playerAccess = Rule.MODE_RESTRICTED;

		for (Rule rule : this.rules) {
			if ("<players>".equals(rule.getName())) {
				playerAccess = rule.getMode();
			}
		}
		
		for (Rule rule : this.rules) {
			if (rule.getName().equals(player.getName())) {
				playerAccess = rule.getMode();
			}
		}
		
		return playerAccess;
	}
	
	public boolean canPlayerUse(Player entityPlayer, BlockPos pos) {

		int playerAccess = this.getPlayerAccess(entityPlayer);
		
		int machineAccess = Rule.MODE_NONE;
		Rule machineRule = this.getRuleByName("<machines>");
		
		if (machineRule != null) {
			machineAccess = machineRule.getMode();
		}
		
		if (machineAccess == Rule.MODE_NONE) {
			return true;
		} else if (machineAccess == Rule.MODE_PROTECTED) {
			if (playerAccess == Rule.MODE_ALLOWED || playerAccess == Rule.MODE_OWNER)
				return true;
			return false;
		} else if (machineAccess == Rule.MODE_OPEN) {
			if (playerAccess == Rule.MODE_RESTRICTED)
				return false;
			return true;
		} else if (machineAccess == Rule.MODE_OWNER) {
			if (playerAccess == Rule.MODE_OWNER)
				return true;
			return false;
		}
		
		// Well we don't know how to handle it.
		
		return true;
	}
}
