package com.astronautlabs.mc.rezolve;

import com.astronautlabs.mc.rezolve.common.BlockBase;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class ModBase {
	protected ArrayList<BlockBase> registeredBlocks = new ArrayList<BlockBase>();
	protected ArrayList<ItemBase> registeredItems = new ArrayList<ItemBase>();
	protected ArrayList<EntityRegistration> registeredEntities = new ArrayList<EntityRegistration>();
	private int nextEntityID = 1;

	ModGuiHandler guiHandler;

	public ModGuiHandler getGuiHandler() {
		return this.guiHandler;
	}

	private void registerBlock(BlockBase block) {
		GameRegistry.register(block);
		this.registeredBlocks.add(block);

		block.init(this);
	}

	public BlockBase[] getRegisteredBlocks() {
		return this.registeredBlocks.toArray(new BlockBase[this.registeredBlocks.size()]);
	}

	public void registerItem(ItemBase item) {
		this.log("Registering item " + item.getRegistryName().toString());
		this.registeredItems.add(item);
		GameRegistry.register(item);
	}

	public void registerItemBlock(BlockBase block) {
		this.registerBlock(block);

		ItemBlock item = new ItemBlock(block);
		item.setRegistryName(block.getRegistryName());
		GameRegistry.register(item);

		block.itemBlock = item;
	}

	public <T extends Entity> void registerEntity(EntityRegistration entity) {
		this.registeredEntities.add(entity);

		entity.id = this.nextEntityID++;

		if (entity.eggPrimaryColor >= 0) {
			EntityRegistry.registerModEntity(
				entity.entityClass,
				entity.name,
				entity.id,
				this,
				entity.trackingRange,
				entity.updateFrequency,
				entity.sendsVelocityUpdates,
				entity.eggPrimaryColor,
				entity.eggSecondaryColor
			);
		} else {
			EntityRegistry.registerModEntity(
				entity.entityClass,
				entity.name,
				entity.id,
				this,
				entity.trackingRange,
				entity.updateFrequency,
				entity.sendsVelocityUpdates
			);
		}
	}

	public void registerTileEntity(Class<? extends TileEntityBase> entityClass) {
		try {
			Constructor<? extends TileEntityBase> x = entityClass.getConstructor();

			TileEntityBase instance = x.newInstance();
			String registryName = instance.getRegistryName();

			GameRegistry.registerTileEntity(entityClass, registryName);

		} catch (Exception e) {
			 System.err.println("Cannot register tile entity class "+entityClass.getCanonicalName()+": Caught exception");
			 System.err.println(e.toString());
			 return;
		}
	}

	public void registerBlockRecipes() {
		this.log("Registering block recipes...");
		for (BlockBase block : this.registeredBlocks) {
			this.log("Registering recipes for: " + block.getRegistryName());
			block.registerRecipes();
		}
	}

	public void registerItemRecipes() {
		this.log("Registering item recipes...");
		for (ItemBase item : this.registeredItems) {
			this.log("Registering recipes for: " + item.getRegistryName());
			item.registerRecipes();
		}
	}

	public EntityRegistration[] getRegisteredEntities() {
		return this.registeredEntities.toArray(new EntityRegistration[this.registeredEntities.size()]);
	}

	public static class EntityRegistration<T extends Entity> {
		public EntityRegistration(Class<T> entityClass, IRenderFactory<T> factory) {
			this.entityClass = entityClass;
			this.renderFactory = factory;
		}

		public static <T extends Entity> Builder<T> create(Class<T> entityClass) {
			return new Builder<T>(entityClass);
		}

		public static class Builder<T extends Entity> {
			public Builder(Class<T> entityClass) {
				this.entityClass = entityClass;
			}

			private Class<T> entityClass;
			private IRenderFactory<T> renderFactory;
			private int eggPrimaryColor = -1;
			private int eggSecondaryColor = -1;
			private int updateFrequency = -1;
			private int trackingRange = -1;
			private boolean sendsVelocityUpdates = false;
			private String name = null;

			public Builder withRenderer(IRenderFactory<T> factory) {
				this.renderFactory = factory;

				return this;
			}

			public Builder named(String name) {
				this.name = name;
				return this;
			}

			public Builder sendsVelocityUpdates(boolean value) {
				this.sendsVelocityUpdates = value;
				return this;
			}

			public Builder withEgg(int eggPrimaryColor, int eggSecondaryColor) {
				this.eggPrimaryColor = eggPrimaryColor;
				this.eggSecondaryColor = eggSecondaryColor;

				return this;
			}

			public Builder updatesEvery(int ticks) {
				this.updateFrequency = ticks;
				return this;
			}

			public Builder trackedWithin(int blocks) {
				this.trackingRange = blocks;
				return this;
			}

			public EntityRegistration build() {
				EntityRegistration reg = new EntityRegistration<>(this.entityClass, this.renderFactory);
				reg.eggPrimaryColor = this.eggPrimaryColor;
				reg.eggSecondaryColor = this.eggSecondaryColor;
				reg.trackingRange = this.trackingRange;
				reg.updateFrequency = this.updateFrequency;
				reg.sendsVelocityUpdates = this.sendsVelocityUpdates;
				reg.name = this.name;

				return reg;
			}


		}

		public int id;
		public String name;
		public int eggPrimaryColor;
		public int eggSecondaryColor;
		public int updateFrequency;
		public int trackingRange;
		public boolean sendsVelocityUpdates = false;

		public Class<T> entityClass;
		public IRenderFactory<T> renderFactory;
	}

	public ItemBase[] getRegisteredItems() {
		return this.registeredItems.toArray(new ItemBase[this.registeredItems.size()]);
	}

	/**
	 * Register blocks!
	 */
	protected void registerBlocks() {
		this.log("Registering blocks...");
	}

	/**
	 * Register items!
	 */
	protected void registerItems() {
		this.log("Registering items...");
	}

	protected void registerEntities() {
		this.log("Registering entities...");
	}

	protected void registerPackets() { this.log("Registering packets..."); }

	public void log(String message) {
		System.out.println(message);
	}

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent ev) {
		MinecraftForge.EVENT_BUS.register(this);
		this.guiHandler = new ModGuiHandler();

		this.registerBlocks();
		this.registerItems();
		this.registerEntities();
		this.registerBlockRecipes();
		this.registerItemRecipes();
		this.registerPackets();
		this.registerGenerators();
	}

	protected void registerGenerators() {
	}
}
