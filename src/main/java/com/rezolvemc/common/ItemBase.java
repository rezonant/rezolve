package com.rezolvemc.common;

import java.util.function.Consumer;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemBase extends Item {
	public ItemBase(Item.Properties properties) {
		super(properties.tab(Rezolve.CREATIVE_MODE_TAB));
	}

	public String getRegistryName() {
		return ForgeRegistries.ITEMS.getKey(this).getPath();
	}

	public void applyTags(Consumer<RezolveRegistry.Tagger<Item>> configurer) {
		RezolveRegistry.registerForTagging(this, configurer);
	}
}
