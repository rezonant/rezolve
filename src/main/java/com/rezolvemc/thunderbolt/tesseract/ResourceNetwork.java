package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.thunderbolt.tesseract.network.ChannelListing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.torchmc.events.Event;
import org.torchmc.events.EventEmitter;
import org.torchmc.events.EventType;

import java.lang.ref.WeakReference;
import java.util.*;

public class ResourceNetwork implements EventEmitter {
    private ResourceNetwork(MinecraftServer server) {
        this.server = server;
        load();
    }

    public static final EventType<ChannelEvent> CHANNEL_CREATED = new EventType<>();
    public static final EventType<ChannelEvent> CHANNEL_REMOVED = new EventType<>();

    private static final WeakHashMap<MinecraftServer, ResourceNetwork> networkMap = new WeakHashMap();
    private final Map<String, ResourceChannel> channels = new HashMap<>();

    private MinecraftServer server;
    private NetworkSavedData savedData;
    private EventMap eventMap = new EventMap();

    @Override
    public EventMap eventMap() {
        return eventMap;
    }

    public void load() {
        savedData = server.overworld().getDataStorage().computeIfAbsent(NetworkSavedData::load, NetworkSavedData::new, "rezolve_resource_network");

        for (var channelListing : savedData.channels) {
            if (!channels.containsKey(channelListing.uuid)) {
                var channel = new ResourceChannel(channelListing.uuid, channelListing.name);
                channels.put(channel.getUuid(), channel);
                emitEvent(CHANNEL_CREATED, new ChannelEvent(channel));
            }
        }
    }

    public ResourceChannel[] getChannels() {
        var coll = this.channels.values();
        return coll.toArray(new ResourceChannel[coll.size()]);
    }

    public ResourceChannel getOrCreateChannel(String uuid) {
        if (channels.containsKey(uuid) && channels.get(uuid) != null) {
            var channel = new ResourceChannel(uuid);
            channels.put(uuid, channel);
            savedData.addChannel(channel);
            return channel;
        }

        return channels.get(uuid);
    }

    public ResourceChannel createChannel(String name) {
        var channel = new ResourceChannel(UUID.randomUUID().toString(), name);
        channels.put(channel.getUuid(), channel);
        savedData.addChannel(channel);
        emitEvent(CHANNEL_CREATED, new ChannelEvent(channel));
        return channel;
    }

    public void removeChannel(ResourceChannel channel) {
        channels.remove(channel.getUuid());
        savedData.removeChannel(channel);
        channel.notifyRemoved();
        emitEvent(CHANNEL_REMOVED, new ChannelEvent(channel));
    }

    public ResourceChannel getChannel(String uuid) {
        if (channels.containsKey(uuid))
            return channels.get(uuid);

        return null;
    }


    public static ResourceNetwork get(MinecraftServer server) {
        if (!networkMap.containsKey(server)) {
            var network = new ResourceNetwork(server);
            networkMap.put(server, network);
            return network;
        }

        return networkMap.get(server);
    }

    private static class NetworkSavedData extends SavedData {
        public final List<ChannelListing> channels = new ArrayList<>();

        public void addChannel(ResourceChannel channel) {
            channels.add(new ChannelListing(channel));
            setDirty();
        }

        public void removeChannel(ResourceChannel channel) {
            channels.remove(new ChannelListing(channel));
        }

        public static NetworkSavedData load(CompoundTag tag) {
            var data = new NetworkSavedData();
            var list = tag.getList("channels", Tag.TAG_COMPOUND);
            for (var channelTag : list) {
                var channel = new ChannelListing();
                channel.deserializeNBT((CompoundTag)channelTag);
                data.channels.add(channel);
            }

            return data;
        }

        @Override
        public CompoundTag save(CompoundTag pCompoundTag) {
            var list = new ListTag();

            for (var channel : channels)
                list.add(channel.serializeNBT());

            pCompoundTag.put("channels", list);
            return pCompoundTag;
        }
    }

    public static class ChannelEvent extends Event {
        public ChannelEvent(ResourceChannel channel) {
            this.channel = channel;
        }

        public final ResourceChannel channel;
    }
}
