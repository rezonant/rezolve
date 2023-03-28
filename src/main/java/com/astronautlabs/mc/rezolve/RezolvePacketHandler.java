package com.astronautlabs.mc.rezolve;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class RezolvePacketHandler {
    private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(RezolveMod.MODID, "bundler"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int nextPacketID = 0;

	public static <MSG extends IMessage>
	void registerMessage(Class<MSG> messageClass, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler, NetworkDirection networkDirection) {
        try {
            var ctor = messageClass.getDeclaredConstructor();
            RezolvePacketHandler.INSTANCE.registerMessage(
                    nextPacketID++,
                    messageClass,
                    (MSG message, FriendlyByteBuf buf) -> message.write(buf),
                    (FriendlyByteBuf buf) -> {
                        try {
                            IMessage inst = ctor.newInstance();
                            inst.read(buf);
                            return (MSG)inst;
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(String.format("Failed to handle packet %s: %s", messageClass.getCanonicalName(), e.getMessage()), e);
                        }
                    },
                    handler,
                    Optional.of(networkDirection)
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Failed to register packet %s: %s", messageClass.getCanonicalName(), e.getMessage()), e);
        }
	}
}
