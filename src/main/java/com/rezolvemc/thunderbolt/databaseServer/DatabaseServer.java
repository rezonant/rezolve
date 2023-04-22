package com.rezolvemc.thunderbolt.databaseServer;

import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.blocks.WithBlockEntity;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Material;

@RegistryId("database_server")
@WithBlockEntity(DatabaseServerEntity.class)
public class DatabaseServer extends Machine {
	public DatabaseServer() {
		super(BlockBehaviour.Properties.of(Material.METAL));
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
	}

//	@Override
//	public List<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, int fortune) {
//
//		System.out.println("GETDROPS");
//
//		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
//		DatabaseServerEntity entity = (DatabaseServerEntity)world.getBlockEntity(pos);
//
//		if (entity != null) {
//			CompoundTag nbt = new CompoundTag();
//
//			// Save the block entity state in the item. BlockItem automatically restores this when the block item
//			// is replaced on the ground.
//
//			CompoundTag blockEntityTag = new CompoundTag();
//			entity.writeToNBT(blockEntityTag);
//			nbt.setTag("BlockEntityTag", blockEntityTag);
//
//			stack.setTag(nbt);
//		}
//
//		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
//		stacks.add(stack);
//		return stacks;
//	}
	
//	@Override
//	public void harvestBlock(Level worldIn, Player player, BlockPos pos, BlockState state, BlockEntity te,
//			ItemStack tool) {
//
//		System.out.println("HARVEST");
//		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
//
//		if (te != null) {
//			DatabaseServerEntity dbEntity = (DatabaseServerEntity)te;
//
//			CompoundTag nbt = new CompoundTag();
//
//			// Save the block entity state in the item. BlockItem automatically restores this when the block item
//			// is replaced on the ground.
//
//			CompoundTag blockEntityTag = new CompoundTag();
//			dbEntity.writeToNBT(blockEntityTag);
//			nbt.setTag("BlockEntityTag", blockEntityTag);
//
//			System.out.println("SETTING NBT");
//			stack.setTag(nbt);
//		}
//
//		System.out.println("SPAWNIN");
//		spawnAsEntity(worldIn, pos, stack);
//	}

}
