package com.astronautlabs.mc.rezolve.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

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

			Object resolvedParam = param;

			if (param instanceof String) {
				String identifier = (String) param;
				ResourceLocation resloc = null;
				String partType = "item";
				String partRef = null;
				String partMetadata = null;

				{
					String[] parts = identifier.split("\\|");


					if (parts.length == 3) {
						// type|ref|meta

						partType = parts[0];
						partRef = parts[1];
						partMetadata = parts[2];
					} else if (parts.length == 2) {

						if ("block".equals(parts[0]) || "item".equals(parts[0])) {
							// type|ref
							partType = parts[0];
							partRef = parts[1];
						} else {
							// ref|meta
							partRef = parts[0];
							partMetadata = parts[1];
						}
					} else if (parts.length == 1) {
						// ref
						partRef = parts[0];
					} else {
						throw new RuntimeException("Malformed recipe string (must be 1-3 parts separated by '|'): " + identifier);
					}
				}

				if (!partRef.contains(":"))
					partRef = "rezolve:" + partRef;

				if (partRef.startsWith("mc:"))
					partRef = partRef.replace("mc:", "minecraft:");

				if ("item".equals(partType)) {
					resolvedParam = Item.REGISTRY.getObject(resloc = new ResourceLocation(partRef));
				} else if ("block".equals(partType)) {
					resolvedParam = Block.REGISTRY.getObject(resloc = new ResourceLocation(partRef));
				} else {
					// Assume they meant "item" since it works for anything anyway.
					resolvedParam = Item.REGISTRY.getObject(resloc = new ResourceLocation(partRef));
				}

				if (resolvedParam == null)
					throw new RuntimeException("No such " + partType + " '" + partRef + "'");

				// Normalize a Block reference into an ItemBlock one.
				if (resolvedParam instanceof Block)
					resolvedParam = ItemBlock.getItemFromBlock((Block) resolvedParam);


				if (partMetadata != null) {

					int metadata = -1;

					if (resolvedParam instanceof Item) {
						Item item = (Item) resolvedParam;
						if (item.getHasSubtypes()) {
							List<ItemStack> subItems = new ArrayList<>();
							item.getSubItems(item, null, subItems);
							String localizedName = item.getUnlocalizedName();

							for (ItemStack subItem : subItems) {
								String adjustedName = item.getUnlocalizedName(subItem);

								if (adjustedName.startsWith(localizedName+"_"))
									adjustedName = adjustedName.substring(localizedName.length() + 1);

								if (partMetadata.equals(adjustedName)) {
									metadata = subItem.getMetadata();
									break;
								}
							}
						}
					}

					if (metadata < 0)
						metadata = Integer.parseInt(partMetadata);

					if (resolvedParam instanceof Item)
						resolvedParam = new ItemStack((Item) resolvedParam, 1, metadata);
					else
						resolvedParam = new ItemStack((Block) resolvedParam, 1, metadata);
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
			}

			resolvedParams[thisIndex] = resolvedParam;
		}

		GameRegistry.addRecipe(output, resolvedParams);
	}

}
