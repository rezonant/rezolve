package com.rezolvemc.parts;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.ItemBase;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = Rezolve.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
				event.register(ForgeRegistries.Keys.ITEMS, Rezolve.loc(part.getName()), () -> part.item());
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
			super(event.getGenerator(), Rezolve.ID, event.getExistingFileHelper());
		}

		@Override
		protected void registerModels() {
			getBuilder(MachinePart.this.getName())
					.parent(new ModelFile.UncheckedModelFile(Rezolve.loc("item/standard_item")))
					.texture("layer0", Rezolve.loc("machine_parts/" + MachinePart.this.getName()))
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
