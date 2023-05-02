package com.rezolvemc.manufacturing;

import com.rezolvemc.common.ItemBase;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RegistryId("manufacturing_pattern")
public class ManufacturingPattern extends ItemBase {
    public ManufacturingPattern() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        var actions = readActions(pStack);

        if (actions != null && actions.length > 0) {
            pTooltipComponents.add(Component.empty()
                .append(Component.empty()
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append("Encoded")
                )
                .append(Component.empty()
                    .withStyle(ChatFormatting.GRAY)
                    .append(" (" + actions.length + " actions)")
                )
            );
        }
    }


    public RecordedAction[] readActions(ItemStack stack) {
        if (stack.getTag() == null)
            return new RecordedAction[0];

        var actionsList = stack.getTag().getList("actions", Tag.TAG_COMPOUND);
        var actions = new ArrayList<RecordedAction>();

        for (var actionTag : actionsList) {
            actions.add(RecordedAction.of((CompoundTag)actionTag));
        }

        return actions.toArray(new RecordedAction[actions.size()]);
    }

    public ItemStack writeActions(RecordedAction[] actions) {
        return writeActions(actions, 1);
    }

    public ItemStack writeActions(RecordedAction[] actions, int count) {
        var tag = new CompoundTag();
        var list = new ListTag();

        for (var action : actions) {
            list.add(action.serializeNBT());
        }

        tag.put("actions", list);

        var stack = new ItemStack(this, count);
        stack.setTag(tag);

        return stack;
    }


    public static class RecordedAction implements INBTSerializable<CompoundTag> {

        public RecordedAction(ResourceKey<Level> level, BlockPos position, int slot, Action action, ItemStack items) {
            this.level = level;
            this.position = position;
            this.slot = slot;
            this.action = action;
            this.items = items;
        }

        public RecordedAction() {
        }

        private ResourceKey<Level> level;
        private BlockPos position;
        private int slot;
        private Action action;
        private ItemStack items;

        public static RecordedAction of(CompoundTag tag) {
            var action = new RecordedAction();
            action.deserializeNBT(tag);
            return action;
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putString("level", level.location().toString());
            tag.put("pos", NbtUtils.writeBlockPos(position));
            tag.putInt("slot", slot);
            tag.putString("action", action.name());
            tag.put("items", items.serializeNBT());

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            level = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("level")));
            position = NbtUtils.readBlockPos(nbt.getCompound("pos"));
            slot = nbt.getInt("slot");
            action = Action.valueOf(nbt.getString("action"));
            items = ItemStack.of(nbt.getCompound("items"));
        }
    }

    public enum Action {
        INSERT,
        EXTRACT
    }
}
