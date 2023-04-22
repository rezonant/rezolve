package com.rezolvemc.cities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(RezolveCitiesMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RezolveCitiesMod {
    public static final String MODID = "rezolvecities";

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey() == ForgeRegistries.Keys.BIOMES) {
            event.register(
                ForgeRegistries.Keys.BIOMES,
                new ResourceLocation(MODID, "city"),
                () -> new Biome.BiomeBuilder()
                        .mobSpawnSettings(MobSpawnSettings.EMPTY)
                        .generationSettings(BiomeGenerationSettings.EMPTY)
                    .build() 
            );
        }
    }
}
