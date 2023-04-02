package com.astronautlabs.mc.rezolve.common.core;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * What this does: Enables interacting with menus whose corresponding blocks are far away.
 * Both client-side and server-side, the Player class will check every tick if it's "stillValid()" for the player to
 * have a Menu opened. Rezolve patches into this to enable the Remote Shell.
 */
@Mixin(Player.class)
public class MixinPlayerMenuReachability {
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean stillValid(AbstractContainerMenu menu, Player player) {
        return RezolveMod.stillValid(menu, player);
    }
}
