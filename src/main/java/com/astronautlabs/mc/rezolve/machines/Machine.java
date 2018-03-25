package com.astronautlabs.mc.rezolve.machines;

import com.astronautlabs.mc.rezolve.ModBase;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.network.cable.CableNetwork;
import com.astronautlabs.mc.rezolve.network.cable.ICableCompatible;
import com.astronautlabs.mc.rezolve.common.IGuiProvider;
import com.astronautlabs.mc.rezolve.common.TileBlockBase;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.google.common.base.Predicate;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class Machine extends TileBlockBase implements IGuiProvider, ICableCompatible {
	public Machine(String registryName) {
		super(registryName);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}


	@Override
	public void onNeighborChange(IBlockAccess blockAccess, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(blockAccess, pos, neighbor);

		if (!(blockAccess instanceof World))
			return;

		World world = (World)blockAccess;

		// Update the cable network if we're on the server

		if (!world.isRemote) {
			CableNetwork network = CableNetwork.networkAt(world, pos, RezolveMod.ETHERNET_CABLE_BLOCK);
			if (network != null)
				network.endpointChanged(world, pos, neighbor);
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
		// Update the cable network if we're on the server

		if (!world.isRemote) {
			CableNetwork network = CableNetwork.networkAt(world, pos, RezolveMod.ETHERNET_CABLE_BLOCK);
			if (network != null)
				network.cableRemoved(world, pos);
		}

		super.onBlockDestroyedByPlayer(world, pos, state);
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosionIn) {
		// Update the cable network if we're on the server

		if (!world.isRemote) {
			CableNetwork network = CableNetwork.networkAt(world, pos, RezolveMod.ETHERNET_CABLE_BLOCK);
			if (network != null)
				network.cableRemoved(world, pos);
		}

		super.onBlockDestroyedByExplosion(world, pos, explosionIn);
	}

	public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>() {
		@Override
		public boolean apply(EnumFacing input) {
			return input != EnumFacing.DOWN && input != EnumFacing.UP;
		}
	});
	
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).ordinal();
	};
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.VALUES[meta];
		
		if (!FACING.getAllowedValues().contains(facing)) {
			for (EnumFacing value : FACING.getAllowedValues()) {
				facing = value;
				break;
			}
		}
		
		return this.blockState.getBaseState()
			.withProperty(FACING, facing);
	}
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override()
	public void init(ModBase mod) {
		super.init(mod);
		this.guiId = mod.getGuiHandler().registerGui(this);
	}
	
	private int guiId = -1;
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (stack.hasDisplayName()) {
	        TileEntity entity = worldIn.getTileEntity(pos);
	        if (entity != null && entity instanceof TileEntityBase)
	        		((TileEntityBase)entity).setCustomName(stack.getDisplayName());
	    }
		
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		if (!world.isRemote) {
	        player.openGui(this.mod, this.guiId, world, pos.getX(), pos.getY(), pos.getZ());
	    }
		
	    return true;
	}
	
	public void openGui(World world, BlockPos pos, EntityPlayer player)
	{
		if (!world.isRemote) {
	        player.openGui(this.mod, this.guiId, world, pos.getX(), pos.getY(), pos.getZ());
	    }
	}

	private MachineEntity machineEntityAt(World world, int x, int y, int z) {
		return (MachineEntity)world.getTileEntity(new BlockPos(x, y, z));
	}

	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return this.machineEntityAt(world, x, y, z).createContainerFor(player);
	}

	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return this.machineEntityAt(world, x, y, z).createGuiFor(player);
	}
}
