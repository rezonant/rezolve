package com.rezolvemc;

import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.thunderbolt.remoteShell.RemoteShellBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

@RegistryId("main")
public class RezolveCreativeTab extends CreativeModeTab {
    public RezolveCreativeTab() {
        super(
                CreativeModeTab.builder()
                        .icon(() -> new ItemStack(RezolveRegistry.block(RemoteShellBlock.class).asItem()))
                        .title(Rezolve.tr("rezolve"))
                        .withSearchBar()

                        // TODO: LEGACY FORGE BUG: REMOVE OR COMPLAIN IN NF 1.20.6
                        // Forge adds support for custom backgrounds. However, the patch only handles the case where
                        // a creative mode tab is built using Builder.build(). If no custom background iamge is specified,
                        // the build() method specifies the Minecraft default. Building a tab with new CreativeModeTab(builder)
                        // will miss this default since the builder constructor just copies the values without calling .build().
                        // See CreativeModeTab.java:338 (`build()`) for details.
                        .withBackgroundLocation(
                                new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png")
                        )
        );
    }

    @Override
    public ItemStack getIconItem() {
        return new ItemStack(RezolveRegistry.block(RemoteShellBlock.class).asItem(), 1);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("rezolve");
    }
}
