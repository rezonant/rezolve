package com.astronautlabs.mc.rezolve.common;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemBase extends Item {
	public ItemBase(Item.Properties properties) {
		super(properties.tab(RezolveMod.CREATIVE_MODE_TAB));
	}

	public String getRegistryName() {
		return ForgeRegistries.ITEMS.getKey(this).getPath();
	}

	public void registerRecipes() {
		
	}

	public void applyTags(Consumer<RezolveRegistry.Tagger<Item>> configurer) {
		RezolveRegistry.registerForTagging(this, configurer);
	}

	public void registerRenderer() {
		
//		ArrayList<ItemStack> subtypes = new ArrayList<ItemStack>();
//		this.getSubItems(this, this.getCreativeTab(), subtypes);
//
//		for (ItemStack subtype : subtypes) {
//			String unlocalizedName = this.getUnlocalizedName(subtype);
//			String modelName = unlocalizedName.replace("item.", "");
//
//			System.out.println("Registering "+this.getRegistryName()+" metadata "+subtype.getMetadata()+" as model "+modelName);
//			ModelLoader.setCustomModelResourceLocation(
//				this, subtype.getMetadata(),
//				new ModelResourceLocation(modelName, "inventory")
//			);
//		}

		//Minecraft.getInstance().getRenderItem().getItemModelMesher()
	    //	.register(this, 0, new ModelResourceLocation(this.getRegistryName().toString(), "inventory"));
	}
}
