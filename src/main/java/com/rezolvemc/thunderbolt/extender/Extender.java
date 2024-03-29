package com.rezolvemc.thunderbolt.extender;

import com.rezolvemc.common.blocks.EntityBlockBase;
import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RegistryId("extender")
@WithBlockEntity(ExtenderEntity.class)
public class Extender extends EntityBlockBase {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    public Extender() {
        super(BlockBehaviour.Properties.of(Material.METAL));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        return List.of(new ItemStack(asItem()));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection());
    }
}
