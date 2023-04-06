package com.astronautlabs.mc.rezolve.thunderbolt.securityServer;

import com.astronautlabs.mc.rezolve.thunderbolt.securityServer.packets.SecurityRulePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SecurityRuleSet implements INBTSerializable<CompoundTag> {
    public List<SecurityRule> rules = new ArrayList<>();

    public SecurityRuleSet() {
    }

    public SecurityRuleSet(List<SecurityRule> rules) {
        for (var rule : rules) {
            this.rules.add(rule);
        }
    }

    public static SecurityRuleSet of(List<SecurityRule> rules) {
        return new SecurityRuleSet(rules);
    }

    public static SecurityRuleSet of(CompoundTag tag) {
        var ruleSet = new SecurityRuleSet();
        ruleSet.deserializeNBT(tag);
        return ruleSet;
    }

    public int size() {
        return rules.size();
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    public void add(SecurityRule rule) {
        rule.id = UUID.randomUUID().toString();
        rules.add(rule);
    }

    public SecurityRule[] asArray() {
        return rules.toArray(new SecurityRule[rules.size()]);
    }

    public SecurityRule[] asCopiedArray() {
        var ruleCopies = new ArrayList<SecurityRule>();

        for (SecurityRule rule : rules)
            ruleCopies.add(rule.copy());

        return ruleCopies.toArray(new SecurityRule[ruleCopies.size()]);
    }

    public void remove(SecurityRule rule) {
        rules.removeIf(r -> Objects.equals(r.id, rule.id));
    }

    public SecurityRule getRuleById(String id) {
        for (SecurityRule rule : rules) {
            if (id.equals(rule.id))
                return rule;
        }

        return null;
    }

    public SecurityRule getRuleByName(String s) {
        if (s == null)
            return null;

        return rules.stream().filter(r -> s.equalsIgnoreCase(r.name)).findFirst().orElse(null);
    }

    @Override
    public CompoundTag serializeNBT() {
        var compound = new CompoundTag();

        var list = new ListTag();
        for (var rule : rules) {
            list.add(rule.serializeNBT());
        }

        compound.put("rules", list);

        return compound;
    }

    public SecurityRule resolve(SecurityRule rule) {
        return getRuleById(rule.id);
    }

    public CompoundTag asTag() {
        return serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        var listTag = compoundTag.getList("rules", Tag.TAG_COMPOUND);

        rules.clear();
        for (var tag : listTag) {
            var rule = new SecurityRule();
            rule.deserializeNBT((CompoundTag) tag);
            rules.add(rule);
        }
    }
}
