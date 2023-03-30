package com.astronautlabs.mc.rezolve.thunderbolt.databaseServer;

import com.astronautlabs.mc.rezolve.common.blocks.EntityBlockBase;
import com.astronautlabs.mc.rezolve.common.blocks.BlockEntityBase;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.google.common.base.Predicate;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

@RegistryId("database_server")
@WithBlockEntity(DatabaseServerEntity.class)
public class DatabaseServer extends EntityBlockBase {
	public DatabaseServer() {
		super(BlockBehaviour.Properties.of(Material.METAL));
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
	}

//	@Override
//	public void registerRecipes() {
//
//		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"nen",
//				"CMC",
//				"cac",
//
//				'n', Items.NAME_TAG,
//				'e', Items.ENDER_EYE,
//				'C', Blocks.CHEST,
//				'M', Item.REGISTRY.getObject(new ResourceLocation("enderio:itemMachinePart")),
//				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
//				'a', Blocks.ANVIL
//			);
//		} else {
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"ene",
//				"CpC",
//				"cac",
//
//				'e', Items.ENDER_EYE,
//				'n', Items.NAME_TAG,
//				'C', Blocks.CHEST,
//				'p', Items.PRISMARINE_SHARD,
//				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
//				'a', Blocks.ANVIL
//			);
//		}
//	}
	
	@Override
	public Class<? extends BlockEntityBase> getBlockEntityClass() {
		return DatabaseServerEntity.class;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING);
	}

	public static final DirectionProperty FACING = DirectionProperty.create("facing", new Predicate<Direction>() {
		@Override
		public boolean apply(Direction input) {
			return input != Direction.DOWN && input != Direction.UP;
		}
	});

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
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
