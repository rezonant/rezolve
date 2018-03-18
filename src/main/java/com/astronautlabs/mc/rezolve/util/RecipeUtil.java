package com.astronautlabs.mc.rezolve.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RecipeUtil {

	/**
	 * Offers much better error messages compared to the standard recipe registration method
	 * while also being a bit less verbose. If a string is passed instead of an Item/Block, it will
	 * be looked up using the appropriate registry (Item.REGISTRY for a prefix of "item|" and Block.REGISTRY
	 * for a prefix of "block|"). If a null value is encountered, an explanatory exception will be thrown to crash
	 * the game. Also helpful during development if you aren't sure what the IDs are for a mod's item or block.
	 *
	 * I suspect this will be great for getting to the root of crash reports from users as well.
	 *
	 * @param output
	 * @param params
	 */
	public static void add(ItemStack output, Object... params) {

		Character lastChar = null;
		Object[] resolvedParams = new Object[params.length];
		int index = 0;

		for (Object param : params) {
			int thisIndex = index;
			resolvedParams[index++] = param;

			if (param instanceof String && lastChar == null)
				continue;

			if (param instanceof Character) {
				lastChar = (Character)param;
				continue;
			}

			if (param == null) {
				if (lastChar != null) {
					throw new RuntimeException(
						"The recipe ingredient labelled '"+lastChar+"' used in '"+output.getItem().getRegistryName()+"' could not be loaded, "
							+ "this indicates that a mod has removed/renamed an item or block "
							+ "and Rezolve has not been updated to match yet :-(. Please file a bug "
							+ "and include the versions of Rezolve and the other mod."
					);
				}
			}

			if (param instanceof String) {
				Object resolvedParam = null;
				String identifier = (String)param;
				ResourceLocation resloc = null;
				String[] parts = identifier.split("\\|");

				if ("item".equals(parts[0]))
					resolvedParam = Item.REGISTRY.getObject(resloc = new ResourceLocation(parts[1]));
				else if ("block".equals(parts[0]))
					resolvedParam = Block.REGISTRY.getObject(resloc = new ResourceLocation(parts[1]));
				else
					throw new RuntimeException("Invalid recipe identifier: "+identifier);

				if (parts.length > 2) {
					// Metadata

					if (resolvedParam instanceof Item)
						resolvedParam = new ItemStack((Item)resolvedParam, 1, Integer.parseInt(parts[2]));
					else if (resolvedParam instanceof Block)
						resolvedParam = new ItemStack((Block)resolvedParam, 1, Integer.parseInt(parts[2]));
					else
						throw new RuntimeException("Resolved parameter is not a block or item, cannot create an ItemStack from it.");
				}

				if (resolvedParam == null) {

					System.out.println("Cannot find "+identifier);
					System.out.println("Registered items in mod "+resloc.getResourceDomain()+" are:");
					for (ResourceLocation loc : Item.REGISTRY.getKeys()) {
						if (!loc.getResourceDomain().equals(resloc.getResourceDomain()))
							continue;

						System.out.println(" - "+loc.getResourcePath());
					}

					System.out.println("Registered blocks in mod "+resloc.getResourceDomain()+" are:");
					for (ResourceLocation loc : Block.REGISTRY.getKeys()) {
						if (!loc.getResourceDomain().equals(resloc.getResourceDomain()))
							continue;

						System.out.println(" - "+loc.getResourcePath());
					}

					throw new RuntimeException(
						"The recipe ingredient '"+identifier+"' used in '"+output.getItem().getRegistryName()+"' could not be loaded, "
							+ "this indicates that a mod has removed/renamed an item or block "
							+ "and Rezolve has not been updated to match yet :-(. Please file a bug "
							+ "and include the versions of Rezolve and the other mod."
					);
				}

				resolvedParams[thisIndex] = resolvedParam;
			}
		}

		GameRegistry.addRecipe(output, resolvedParams);
	}

}
