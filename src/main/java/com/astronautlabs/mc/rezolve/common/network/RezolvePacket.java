package com.astronautlabs.mc.rezolve.common.network;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public abstract class RezolvePacket {

    private static final Logger LOGGER = LogManager.getLogger(RezolveMod.MODID);
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(RezolveMod.MODID, "packets"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void init() {
        // No-op, ensures that static members are initialized early.
    }

    static {

        // Server to client

        CHANNEL.registerMessage(
                1,
                RezolvePacketClientBoundEnvelope.class,
                (message, buf) -> message.write(buf),
                buf -> RezolvePacketClientBoundEnvelope.parse(buf),
                (envelope, contextSupplier) -> envelope.packet.handleOnClient(contextSupplier.get()),
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        // Client to server

        CHANNEL.registerMessage(
                2,
                RezolvePacketServerBoundEnvelope.class,
                (message, buf) -> message.write(buf),
                buf -> RezolvePacketServerBoundEnvelope.parse(buf),
                (envelope, contextSupplier) -> envelope.packet.handleOnServer(contextSupplier.get()),
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public abstract void read(FriendlyByteBuf buf);
    public abstract void write(FriendlyByteBuf buf);

    protected void receiveOnClient() {
        LOGGER.warn("Received valid packet {}, but it is unhandled on the client", getClass().getCanonicalName());
    }

    protected void receiveOnServer(ServerPlayer sender) {
        LOGGER.warn("Received valid packet {}, but it is unhandled on the server", getClass().getCanonicalName());
    }

    private final void handleOnServer(NetworkEvent.Context context) {
        receiveOnServer(context.getSender());
        context.setPacketHandled(true);
    }

    private final void handleOnClient(NetworkEvent.Context context) {
        receiveOnClient();
        context.setPacketHandled(true);
    }

    static RezolvePacket parse(String id, FriendlyByteBuf buf) {
        Class<? extends RezolvePacket> klass = RezolveRegistry.getPacketClass(id);

        if (klass == null) {
            LOGGER.error("Unknown packet ID '{}'", id);
            return new RezolveUnknownPacket(id);
        }

        try {
            RezolvePacket inst = (RezolvePacket)klass.newInstance();
            inst.read(buf);
            return inst;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format("Failed to parse packet %s: %s", klass.getCanonicalName(), e.getMessage()), e);
        }
    }

    public void sendToPlayer(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), encloseForClient());
    }

    public void sendToAllPlayers() {
        CHANNEL.send(PacketDistributor.ALL.noArg(), encloseForClient());
    }

    public void sendToChunkTrackers(LevelChunk chunk) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), encloseForClient());
    }

    public void sendToEntityTrackers(Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), encloseForClient());
    }

    public void sendToServer() {
        CHANNEL.sendToServer(encloseForServer());
    }

    public RezolvePacketEnvelope encloseForServer() {
        return RezolvePacketServerBoundEnvelope.enclose(this);
    }

    public RezolvePacketEnvelope encloseForClient() {
        return RezolvePacketClientBoundEnvelope.enclose(this);
    }

    protected String getRegistryKey() {
        return RezolveRegistry.getRegistryId(getClass());
    }
}
