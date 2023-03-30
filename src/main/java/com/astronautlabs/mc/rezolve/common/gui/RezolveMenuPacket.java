package com.astronautlabs.mc.rezolve.common.gui;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacketReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RezolveMenuPacket extends RezolvePacket {
    private static Logger LOGGER = LogManager.getLogger(RezolveMod.MODID);
    public int containerId;

    public void setMenu(AbstractContainerMenu menu) {
        this.containerId = menu.containerId;
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        containerId = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
    }

    @OnlyIn(Dist.CLIENT)
    protected final void receiveOnClient() {
        receiveForPlayer(
                Minecraft.getInstance().player,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    @Override
    protected final void receiveOnServer(ServerPlayer sender) {
        receiveForPlayer(
                sender,
                NetworkDirection.PLAY_TO_SERVER
        );
    }

    private void receiveForPlayer(Player player, NetworkDirection direction) {
        if (player.containerMenu == null) {
            LOGGER.warn(
                    "Received a Menu packet, but there is no menu open. Container ID = {}",
                    containerId
            );
            return;
        }

        if (player.containerMenu.containerId != containerId) {
            LOGGER.warn(
                    "Received a Menu packet for container #{}, but open container is currently #{}",
                    containerId, player.containerMenu.containerId
            );
            return;
        }

        if (!(player.containerMenu instanceof RezolvePacketReceiver)) {
            LOGGER.warn(
                    "Received a Menu packet, but the current Menu ({}) does not implement RezolvePacketReceiver",
                    player.containerMenu.getClass().getCanonicalName()
            );
            return;
        }

        var packetReceiver = (RezolvePacketReceiver) player.containerMenu;
        if (direction == NetworkDirection.PLAY_TO_CLIENT)
            packetReceiver.receivePacketOnClient(this);
        else if (direction == NetworkDirection.PLAY_TO_SERVER)
            packetReceiver.receivePacketOnServer(this);
    }
}
