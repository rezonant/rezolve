package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.common.LevelPosition;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.thunderbolt.tesseract.network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TesseractEntity extends MachineEntity {
    public TesseractEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(TesseractEntity.class), pPos, pBlockState);
    }

    private String channelUuid = null;
    private ResourceChannel channel;
    private List<Resource> resources = null;
    private ItemHandler itemHandler = new ItemHandler();
    private EnergyStorage energyStorage = new EnergyStorage();
    private FluidHandler fluidHandler = new FluidHandler();

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        channelUuid = tag.contains("channelUuid") ? tag.getString("channelUuid") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (channelUuid != null)
            tag.putString("channelUuid", channelUuid);
    }

    @Override
    public void setLevel(Level pLevel) {
        super.setLevel(pLevel);
        joinChannel();
    }

    public void setChannel(String uuid) {
        leaveChannel();
        this.channelUuid = uuid;
        joinChannel();
        setChanged();
    }

    private ResourceNetwork resourceNetwork;

    public ResourceNetwork getResourceNetwork() {
        if (resourceNetwork == null) {
            resourceNetwork = ResourceNetwork.get(getLevel().getServer());
            removeWhenDestroyed(resourceNetwork.addEventListener(resourceNetwork.CHANNEL_CREATED, e -> sendPacketToActivePlayers(new ChannelCreated(e.channel))));
            removeWhenDestroyed(resourceNetwork.addEventListener(resourceNetwork.CHANNEL_REMOVED, e -> sendPacketToActivePlayers(new ChannelRemoved(e.channel))));
        }

        return resourceNetwork;
    }

    private LevelPosition levelPos = null;

    public LevelPosition getLevelPos() {
        if (levelPos == null)
            levelPos = new LevelPosition(getLevel(), getBlockPos());
        return levelPos;
    }

    private void leaveChannel() {
        if (channel != null)
            channel.leave(getLevelPos());

        channel = null;
    }

    private void joinChannel() {
        if (channelUuid == null) {
            channel = null;
            return;
        }

        channel = getResourceNetwork().getChannel(channelUuid);

        if (resources == null) {
            resources = new ArrayList<>();
            for (var dir : Direction.values()) {
                var adj = getBlockPos().relative(dir);
                resources.add(new Resource(getLevel(), adj, dir.getOpposite()));
            }
        }

        channel.setResources(getLevelPos(), resources);
    }

    public ResourceChannel getChannel() {
        return channel;
    }

    @Override
    public Component getMenuTitle() {
        return Component.translatable("block.rezolve.tesseract");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> (T) itemHandler);
        } else if (capability == ForgeCapabilities.ENERGY) {
            return energyStorage.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> (T) energyStorage);
        } else if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> (T) fluidHandler);
        }

        return super.getCapability(capability, facing);
    }

    public boolean hasChannel() {
        return channel != null;
    }

    public <T> List<T> getReachableCapabilities(Capability<T> cap) {
        return hasChannel() ? getChannel().getResourceCapabilitiesNotOwnedBy(getLevelPos(), cap) : new ArrayList<>();
    }

    private class ItemHandler extends MultiplexedItemHandler {
        @Override
        public @NotNull List<IItemHandler> getHandlers() {
            return getReachableCapabilities(ForgeCapabilities.ITEM_HANDLER);
        }
    }

    private class EnergyStorage extends MultiplexedEnergyStorage {
        @Override
        public @NotNull List<IEnergyStorage> getHandlers() {
            return getReachableCapabilities(ForgeCapabilities.ENERGY);
        }
    }

    private class FluidHandler extends MultiplexedFluidHandler {
        @Override
        public List<IFluidHandler> getHandlers() {
            return getReachableCapabilities(ForgeCapabilities.FLUID_HANDLER);
        }
    }

    private ChannelListSearchResults search(String query) {
        var list = new ArrayList<ChannelListing>();
        for (var channel : getResourceNetwork().getChannels()) {
            list.add(new ChannelListing(channel));
        }

        return new ChannelListSearchResults(list);
    }

    @Override
    public void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) {
        if (rezolvePacket instanceof ChannelListSearch channelListSearch) {
            search(channelListSearch.query).sendToPlayer((ServerPlayer) player);
        } else if (rezolvePacket instanceof CreateChannel createChannel) {
            getResourceNetwork().createChannel(createChannel.name);
        } else if (rezolvePacket instanceof RemoveChannel removeChannel) {
            var channel = getResourceNetwork().getChannel(removeChannel.uuid);
            if (channel != null)
                getResourceNetwork().removeChannel(channel);
        } else if (rezolvePacket instanceof SetActiveChannel setActiveChannel) {
            setChannel(setActiveChannel.uuid);
        } else {
            super.receivePacketOnServer(rezolvePacket, player);
        }
    }
}
