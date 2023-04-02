package com.astronautlabs.mc.rezolve.common.core;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * What this does: Enables interacting with menus whose corresponding blocks are far away.
 * In addition to the general tick check on Player, server packet handling also has additional checks to ensure the player
 * isn't trying to manipulate inventories that are far away. To enable the Remote Shell, we need to redirect these to Rezolve's
 * handling instead.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class MixinMenuPacketReachability {
    // Sites

    private static final String HANDLE_RENAME_ITEM = "handleRenameItem(Lnet/minecraft/network/protocol/game/ServerboundRenameItemPacket;)V";
    private static final String HANDLE_CONTAINER_CLICK = "handleContainerClick(Lnet/minecraft/network/protocol/game/ServerboundContainerClickPacket;)V";
    private static final String HANDLE_SET_BEACON_PACKET = "handleSetBeaconPacket(Lnet/minecraft/network/protocol/game/ServerboundSetBeaconPacket;)V";
    private static final String HANDLE_SELECT_TRADE = "handleSelectTrade(Lnet/minecraft/network/protocol/game/ServerboundSelectTradePacket;)V";
    private static final String HANDLE_PLACE_RECIPE = "handlePlaceRecipe(Lnet/minecraft/network/protocol/game/ServerboundPlaceRecipePacket;)V";
    private static final String HANDLE_CONTAINER_BUTTON_CLICK = "handleContainerButtonClick(Lnet/minecraft/network/protocol/game/ServerboundContainerButtonClickPacket;)V";

    // INVOKE targets

    private static final String ABSTRACT_CONTAINER_MENU_STILL_VALID = "Lnet/minecraft/world/inventory/AbstractContainerMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z";
    private static final String ANVIL_STILL_VALID = "Lnet/minecraft/world/inventory/AnvilMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z";
    private static final String MERCHANT_MENU_STILL_VALID = "Lnet/minecraft/world/inventory/MerchantMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z";

    // Redirects

    @Redirect(method = HANDLE_RENAME_ITEM, at = @At(value = "INVOKE", target = ANVIL_STILL_VALID))
    private boolean anvilRenameStillValid(AnvilMenu menu, Player player) {
        return RezolveMod.stillValid(menu, player);
    }

    @Redirect(method = HANDLE_SET_BEACON_PACKET, at = @At(value = "INVOKE", target = ABSTRACT_CONTAINER_MENU_STILL_VALID))
    private boolean setBeaconPacketStillValid(AbstractContainerMenu menu, Player player) {
        return RezolveMod.stillValid(menu, player);
    }

    @Redirect(method = HANDLE_SELECT_TRADE, at = @At(value = "INVOKE", target = MERCHANT_MENU_STILL_VALID))
    private boolean selectTradeStillValid(MerchantMenu menu, Player player) {
        return RezolveMod.stillValid(menu, player);
    }

    @Redirect(method = HANDLE_CONTAINER_CLICK, at = @At(value = "INVOKE", target = ABSTRACT_CONTAINER_MENU_STILL_VALID))
    private boolean containerClickStillValid(AbstractContainerMenu menu, Player player) {
        return RezolveMod.stillValid(menu, player);
    }

    @Redirect(method = HANDLE_PLACE_RECIPE, at = @At(value = "INVOKE", target = ABSTRACT_CONTAINER_MENU_STILL_VALID))
    private boolean placeRecipeStillValid(AbstractContainerMenu menu, Player player) {
        return RezolveMod.stillValid(menu, player);
    }

    @Redirect(method = HANDLE_CONTAINER_BUTTON_CLICK, at = @At(value = "INVOKE", target = ABSTRACT_CONTAINER_MENU_STILL_VALID))
    private boolean containerButtonClickStillValid(AbstractContainerMenu menu, Player player) {
        return RezolveMod.stillValid(menu, player);
    }

}
