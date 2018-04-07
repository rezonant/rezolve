package com.astronautlabs.mc.rezolve.worlds.ores;

import com.astronautlabs.mc.rezolve.IItemBlockProvider;
import com.astronautlabs.mc.rezolve.common.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import java.util.*;

public class MetalStateBlock extends BlockBase implements IItemBlockProvider {
	public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 15);

	public MetalStateBlock(String stateName, Material material, float hardness, float resistance) {
		super(String.format("block_metal_%s", stateName), material, hardness, resistance);
		this.setDefaultState(this.getDefaultState().withProperty(TYPE, 0));

		this.stateName = stateName;
		this.itemBlock = new MetalItemBlock(this);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Arrays.asList(new ItemStack(this.itemBlock, 1 + (fortune > 0 ? new Random().nextInt(fortune) : 0), state.getBlock().getMetaFromState(state)));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}

	private String stateName;

	public IBlockState getStateFor(Metal metal) {
		return this.getDefaultState().withProperty(TYPE, Metal.indexOf(metal));
	}

	@Override
	public void registerRenderer() {
		super.registerRenderer();


		for (int i = 0, max = Metal.all().size(); i < max; ++i) {
			IBlockState state = this.getDefaultState().withProperty(TYPE, i);
			Metal metal = Metal.get(i);

			ModelLoader.setCustomModelResourceLocation(this.itemBlock, i,
				new ModelResourceLocation(
					String.format("rezolve:block_%s_%s", metal != null ? metal.getName() : "metal", MetalStateBlock.this.stateName),
					"inventory"
				)
			);
		}

		ModelLoader.setCustomStateMapper(this, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				Metal metal = Metal.get(state);
				return new ModelResourceLocation(
					String.format("rezolve:block_%s_%s", metal != null ? metal.getName() : "metal", MetalStateBlock.this.stateName)
				);
			}
		});
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		super.getSubBlocks(itemIn, tab, list);

		for (int i = 0, max = Metal.all().size(); i < max; ++i)
			list.add(new ItemStack(this, 1, i));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((Integer) state.getProperties().get(TYPE));
	}

	@Override
	public float getExplosionResistance(Entity exploder) {
		return super.getExplosionResistance(exploder);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		Metal metal = Metal.get(world.getBlockState(pos));
		return metal != null ? metal.getResistance() : this.blockResistance / 5.0F;
	}

	@Override
	public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos) {
		Metal metal = Metal.get(state);
		return metal != null ? metal.getHardness()
			: this.blockResistance / 5.0F;
	}

	@Override
	public ItemBlock getItemBlock() {
		return this.itemBlock;
	}

	public ItemStack getStackOf(Metal metal) {
		return this.getStackOf(metal, 1);
	}

	public ItemStack getStackOf(Metal metal, int amount) {
		return new ItemStack(this.itemBlock, amount, Metal.indexOf(metal));
	}

	public class MetalItemBlock extends ItemBlock {
		public MetalItemBlock(Block block) {
			super(block);
			this.setHasSubtypes(true);
		}

		@Override
		public int getMetadata(int damage) {
			return damage;
		}

		@Override
		public int getDamage(ItemStack stack) {
			return stack.getMetadata();
		}

		@Override
		public String getUnlocalizedName() {
			return String.format("block_metal_%s", MetalStateBlock.this.stateName);
		}

		@Override
		public String getUnlocalizedName(ItemStack stack) {

			// return "item." + this.getRegistryName().getResourceDomain() + ":item_" + metal.getName()+"_"+this.stateName;

			Metal metal = Metal.get(stack.getMetadata());

			if (metal == null)
				return this.getUnlocalizedName();

			return String.format(
				"block_%s_%s",
				metal.getName(),
				MetalStateBlock.this.stateName
			);
		}

		@Override
		public String getItemStackDisplayName(ItemStack stack) {

			Metal metal = Metal.get(stack.getMetadata());

			if (metal == null)
				return super.getItemStackDisplayName(stack);

			return I18n.format("item.rezolve:metal_"+metal.getName()+".name") + " " + I18n.format("item.rezolve:"+MetalStateBlock.this.stateName);
		}
	}
}
