package com.astronautlabs.mc.rezolve.parts;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.worlds.Metal;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = RezolveMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum MachinePart {
	DAC("dac"),
	ADC("adc"),
	ACTIVATOR("activator"),
	DISPLAY_PANEL("display_panel"),
	FRAME("frame"),
	INTEGRATED_CIRCUIT("integrated_circuit"),
	TRANSCODER("transcoder"),
	PHYSICAL_MAPPER("physical_mapper"),
	MOTOR_BLADE("motor_blade"),
	NETWORK_CARD("network_card"),
	SERVO("servo"),
	CPU("cpu"),
	FPO("fpo"),
	CIRCUIT_BOARD("circuit_board"),
	GEAR("gear"),
	GEARBOX("gearbox")
	;

	MachinePart(String name) {
		this.name = name;
	}

	private String name;
	private Item item;

	public Item item() {
		return item != null ? item : (item = new Item());
	}

	public String getName() {
		return name;
	}

	@SubscribeEvent
	public static void handleRegisterEvent(RegisterEvent event) {
		if (event.getRegistryKey() == ForgeRegistries.Keys.ITEMS) {
			for (var part : values())
				event.register(ForgeRegistries.Keys.ITEMS, RezolveMod.loc(part.getName()), () -> part.item());
		}
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		for (var part : values()) {
			event.getGenerator().addProvider(true, part.new ItemsGenerator(event));
		}
	}

	public class ItemsGenerator extends ItemModelProvider {
		public ItemsGenerator(GatherDataEvent event) {
			super(event.getGenerator(), RezolveMod.ID, event.getExistingFileHelper());
		}

		@Override
		protected void registerModels() {
			getBuilder(MachinePart.this.getName())
					.parent(new ModelFile.UncheckedModelFile(RezolveMod.loc("item/standard_item")))
					.texture("layer0", RezolveMod.loc("machine_parts/" + MachinePart.this.getName()))
			;
		}
	}

	public class Item extends ItemBase {
		public Item() {
			super(new Properties());
		}

		public MachinePart getMachinePart() {
			return MachinePart.this;
		}
	}
}
