package com.astronautlabs.mc.rezolve.worlds.cities;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CityMapGen {

	public CityMapGen() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.TERRAIN_GEN_BUS.register(this);

		GameRegistry.register(RezolveMod.CITY_BIOME);
		GameRegistry.register(RezolveMod.TOWN_BIOME);
		GameRegistry.registerWorldGenerator(RezolveMod.CITY_GENERATOR, Integer.MAX_VALUE);

		BiomeProvider.allowedBiomes.clear();

		int cityWeight = 4;
		int townWeight = 8;

		BiomeManager.addVillageBiome(RezolveMod.TOWN_BIOME, true);

		//cityWeight = 999999;
		BiomeManager.addBiome(BiomeManager.BiomeType.DESERT, new BiomeManager.BiomeEntry(RezolveMod.CITY_BIOME, cityWeight));
		BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(RezolveMod.CITY_BIOME, cityWeight));
		BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(RezolveMod.CITY_BIOME, cityWeight));
		BiomeManager.addBiome(BiomeManager.BiomeType.ICY, new BiomeManager.BiomeEntry(RezolveMod.CITY_BIOME, cityWeight));

		BiomeManager.addBiome(BiomeManager.BiomeType.DESERT, new BiomeManager.BiomeEntry(RezolveMod.TOWN_BIOME, townWeight));
		BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(RezolveMod.TOWN_BIOME, townWeight));
		BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(RezolveMod.TOWN_BIOME, townWeight));
		BiomeManager.addBiome(BiomeManager.BiomeType.ICY, new BiomeManager.BiomeEntry(RezolveMod.TOWN_BIOME, townWeight));

		BiomeManager.addSpawnBiome(RezolveMod.CITY_BIOME);
		BiomeManager.addSpawnBiome(RezolveMod.TOWN_BIOME);
	}

	@SubscribeEvent
	public void initMapGen(InitMapGenEvent ev) {
		if (ev.getType() == InitMapGenEvent.EventType.RAVINE) {
			ev.setNewGen(new CityMapGenRavine());
		} else if (ev.getType() == InitMapGenEvent.EventType.SCATTERED_FEATURE) {
			ev.setNewGen(new CityMapGenScatteredFeature());
		} else if (ev.getType() == InitMapGenEvent.EventType.VILLAGE) {
			ev.setNewGen(new CityMapGenVillage());
		} else if (ev.getType() == InitMapGenEvent.EventType.CAVE) {
			ev.setNewGen(new CityMapGenCave());
		}
	}

	@SubscribeEvent
	public void populateChunk(PopulateChunkEvent.Populate ev) {

		List<PopulateChunkEvent.Populate.EventType> typesToBlock = Arrays.asList(
			PopulateChunkEvent.Populate.EventType.ANIMALS,
			PopulateChunkEvent.Populate.EventType.LAKE,
			PopulateChunkEvent.Populate.EventType.LAVA
		);

		if (typesToBlock.indexOf(ev.getType()) >= 0) {

			ChunkPos chunk = new ChunkPos(ev.getChunkX(), ev.getChunkZ());
			if (CityBiome.isCity(ev.getWorld(), chunk))
				ev.setResult(Event.Result.DENY);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void handleOverlay(RenderGameOverlayEvent.Text ev) {
		if (ev.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
			if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
				return;
			}

			World world = Minecraft.getMinecraft().theWorld;
			EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
			ChunkPos chunkPos = new ChunkPos(new BlockPos((int)player.posX, (int)player.posY, (int)player.posZ));

			if (!CityBiome.isCity(world, chunkPos))
				return;

			ev.getLeft().clear();
			ev.getRight().clear();


			CityNode cityNode = CityNode.cityBlockFor(world, (int)player.posX, (int)player.posZ);

			ev.getLeft().add(String.format("City (%sc, %sc)", cityNode.getOriginX(), cityNode.getOriginZ()));
			cityNode.trace(ev.getLeft());

			// current node

			CityNode node = CityNode.nodeFor(world, (int)player.posX, (int)player.posY, (int)player.posZ);

			ev.getRight().add(String.format(
				"%s [%d, %d, %d] (%d, %d, %d)",
				node.getFeature().toString(),
				node.getSpanX(),
				node.getSpanY(),
				node.getSpanZ(),
				node.getOriginX(),
				node.getOriginY(),
				node.getOriginZ()
			));

			ev.getRight().add(String.format(
				"connects[north:%s, south:%s, east:%s, west:%s] - ",
				node.connectsNorth(world, chunkPos),
				node.connectsSouth(world, chunkPos),
				node.connectsEast(world, chunkPos),
				node.connectsWest(world, chunkPos)
			));

			ev.getRight().add("metadata - ");
			HashMap<String,Integer> params = node.getParameters();
			for (String key : params.keySet()) {
				int value = params.get(key);

				ev.getRight().add(value+" :"+key+" *    ");
			}

		}
	}

}
