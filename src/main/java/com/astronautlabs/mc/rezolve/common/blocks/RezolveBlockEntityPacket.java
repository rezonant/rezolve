package com.astronautlabs.mc.rezolve.common.blocks;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacketReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RezolveBlockEntityPacket extends RezolvePacket {
    private static Logger LOGGER = LogManager.getLogger(RezolveMod.ID);
    public BlockPos blockPos;
    public String dimension;

    public void setBlockEntity(BlockEntity entity) {
        blockPos = entity.getBlockPos();
        dimension = entity.getLevel().dimension().location().getPath();
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        blockPos = buf.readBlockPos();
        dimension = buf.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeUtf(dimension);
    }

    @OnlyIn(Dist.CLIENT)
    protected final void receiveOnClient() {
        receiveForLevel(
                Minecraft.getInstance().level, "server",
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    @Override
    protected final void receiveOnServer(ServerPlayer sender) {
        receiveForLevel(
                sender.level,
                String.format("player %s [%s]", sender.getName().getString(), sender.getStringUUID()),
                NetworkDirection.PLAY_TO_SERVER
        );
    }

    private void receiveForLevel(Level level, String source, NetworkDirection direction) {

        String currentDimension = level.dimension().location().getPath();

        if (currentDimension != dimension) {
            LOGGER.warn(
                    "Received BlockEntity packet for dimension {} from {}, "
                            + "but player is currently in dimension {}. Ignoring.",
                    dimension, source, currentDimension
            );
            return;
        }

        if (!level.hasChunkAt(blockPos)) {
            LOGGER.warn(
                    "Received BlockEntity packet for {}/{} from {}, "
                            + "but this is outside of known chunks. Ignoring.",
                    dimension, blockPos, source
            );
            return;
        }

        var entity = level.getBlockEntity(blockPos);

        if (entity == null) {
            LOGGER.warn(
                    "Received BlockEntity packet for {}/{} from {}, "
                            + "but this is outside of known chunks. Ignoring.",
                    dimension, blockPos, source
            );
            return;
        }

        if (!(entity instanceof RezolvePacketReceiver)) {
            LOGGER.warn(
                    "Received BlockEntity packet for {}/{} from {}, "
                            + "but entity {} does not accept Rezolve packets. Ignoring.",
                    dimension, blockPos, source, entity.getClass().getCanonicalName()
            );
            return;
        }

        var packetReceiver = (RezolvePacketReceiver) entity;
        if (direction == NetworkDirection.PLAY_TO_CLIENT)
            packetReceiver.receivePacketOnClient(this);
        else if (direction == NetworkDirection.PLAY_TO_SERVER)
            packetReceiver.receivePacketOnServer(this);
    }
}
