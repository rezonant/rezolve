package com.astronautlabs.mc.rezolve.worlds.ores;

import jdk.nashorn.internal.ir.Block;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.List;

public class Metal {
	public static final int DEFAULT_HARDNESS = 3;
	public static final int DEFAULT_RESISTANCE = 15;

	public static final Metal COPPER = create("copper").build();
	public static final Metal LEAD = create("lead").build();
	public static final Metal TIN = create("tin").build();

	public Metal(String name, float hardness, float resistance) {
		this.name = name;
		this.hardness = hardness;
		this.resistance = resistance;
	}

	public static Builder create(String name) {
		return new Builder(name);
	}

	public static Metal createAndRegister(String name) {
		Metal metal = new Metal(name, DEFAULT_HARDNESS, DEFAULT_RESISTANCE);
		metal.register();
		return metal;
	}

	@Override
	public String toString() {
		return String.format("Metal [%s]", this.name);
	}

	public static int indexOf(Metal metal) {
		return registeredMetals.indexOf(metal);
	}

	public static class Builder {
		public Builder(String name) {
			this.name = name;
		}

		private String name;
		private float hardness = DEFAULT_HARDNESS;
		private float resistance = DEFAULT_RESISTANCE;

		public Builder withHardness(float hardness) {
			this.hardness = hardness;
			return this;
		}

		public Builder withResistance(float resistance) {
			this.resistance = resistance;
			return this;
		}

		public Metal build() {
			return new Metal(name, hardness, resistance);
		}

		public Metal buildAndRegister() {
			Metal metal = new Metal(name, hardness, resistance);
			metal.register();
			return metal;
		}
	}

	private String name;
	private float hardness;
	private float resistance;

	public String getName() {
		return name;
	}

	public float getHardness() {
		return hardness;
	}

	public float getResistance() {
		return resistance;
	}

	private static List<Metal> registeredMetals = new ArrayList<>();

	public static Metal get(int index) {
		if (index >= registeredMetals.size())
			return null;

		return registeredMetals.get(index);
	}

	public static Metal get(IBlockState state) {
		return get(state.getBlock().getMetaFromState(state));
	}

	public static List<Metal> all() {
		return new ArrayList<>(registeredMetals);
	}

	public static void register(Metal metal) {
		registeredMetals.add(metal);
	}

	public void register() {
		Metal.register(this);
	}
}
