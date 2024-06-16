package com.rezolvemc.common;

import java.util.Objects;
import java.util.function.Consumer;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class ItemBase extends Item {
	public ItemBase(Item.Properties properties) {
		super(properties);
	}

	public String getRegistryName() {
		return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this)).getPath();
	}

	public void applyTags(Consumer<RezolveRegistry.Tagger<Item>> configurer) {
		RezolveRegistry.registerForTagging(this, configurer);
	}
}
