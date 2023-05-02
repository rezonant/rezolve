package com.rezolvemc.thunderbolt.remoteShell.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.rezolvemc.Rezolve;
import com.rezolvemc.thunderbolt.remoteShell.common.MachineListing;
import com.rezolvemc.thunderbolt.remoteShell.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.torchmc.TorchScreen;
import org.torchmc.events.Event;
import org.torchmc.events.EventEmitter;
import org.torchmc.events.EventType;
import org.torchmc.util.TorchUtil;

import java.util.List;

@Mod.EventBusSubscriber(modid = Rezolve.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RemoteAccessClientSession implements EventEmitter {
    public static final Logger LOGGER = Rezolve.logger(RemoteAccessClientSession.class);
    public static final RemoteAccessClientSession INSTANCE = new RemoteAccessClientSession();
    public static final EventType<QueryResultsEvent> RESULTS_RECEIVED = new EventType<>();

    public static class QueryResultsEvent extends Event {
        public QueryResultsEvent(List<MachineListing> machines, int offset, int total) {
            this.machines = machines;
            this.offset = offset;
            this.total = total;
        }

        public final List<MachineListing> machines;
        public final int offset;
        public final int total;
    }

    public RemoteAccessClientSession() {
        minecraft = Minecraft.getInstance();
    }

    private Minecraft minecraft;
    private EventEmitter.EventMap eventMap = new EventEmitter.EventMap();

    public boolean active;
    public BlockPos remoteShellPosition;
    public String remoteShellDimension;
    public MachineListing activeMachine;
    public int remoteShellEnergy;
    public int remoteShellEnergyCapacity;
    public ItemStack recordedPattern;
    public boolean recording = false;
    public boolean hasDatabase = false;

    public List<MachineListing> machines;
    public int machinesCount;
    public int machinesOffset;

    @Override
    public EventMap eventMap() {
        return eventMap;
    }

    public void updateState(RemoteShellStatePacket newState) {
        active = newState.active;
        remoteShellPosition = newState.remoteShellPosition;
        remoteShellDimension = newState.remoteShellDimension;
        activeMachine = newState.activeMachine;
        remoteShellEnergy = newState.remoteShellEnergy;
        remoteShellEnergyCapacity = newState.remoteShellEnergyCapacity;
        recordedPattern = newState.recordedPattern;
        recording = newState.recording;
        hasDatabase = newState.hasDatabase;
    }

    public void receiveResults(RemoteShellSearchResults results) {
        machines = results.machines;
        machinesOffset = results.offset;
        machinesCount = results.total;

        emitEvent(RESULTS_RECEIVED, new QueryResultsEvent(machines, machinesOffset, machinesCount));
    }

    public void connectToMachine(MachineListing listing) {
        if (!active) {
            LOGGER.error("Should not attempt to connectToMachine() when the session is not active.");
            return;
        }

        var packet = new RemoteShellActivatePacket(listing.getLevel(), listing.getBlockPos(), minecraft.player.getStringUUID());
        packet.dimension = remoteShellDimension;
        packet.blockPos = remoteShellPosition;
        packet.sendToServer();
    }

    public void takePattern(ItemStack pattern) {
        var packet = new RemoteShellTakePatternPacket();
        packet.dimension = remoteShellDimension;
        packet.blockPos = remoteShellPosition;
        packet.sendToServer();

        boolean hasEmptySlot = minecraft.player.getInventory().getFreeSlot() >= 0;

        if (hasEmptySlot && TorchUtil.isKeyDown(InputConstants.KEY_LSHIFT)) {
            minecraft.player.getInventory().add(pattern);
        } else {
            minecraft.player.containerMenu.setCarried(pattern);
        }
    }

    public void toggleRecording() {
        if (recording)
            stopRecording();
        else
            startRecording();
    }

    public void startRecording() {
        var packet = new RemoteShellStartRecordingPacket();
        packet.dimension = remoteShellDimension;
        packet.blockPos = remoteShellPosition;
        packet.sendToServer();
    }

    public void stopRecording() {
        var packet = new RemoteShellStopRecordingPacket();
        packet.dimension = remoteShellDimension;
        packet.blockPos = remoteShellPosition;
        packet.sendToServer();
    }

    public void returnToShell() {
        var packet = new RemoteShellEntityReturnPacket();
        packet.dimension = remoteShellDimension;
        packet.blockPos = remoteShellPosition;
        packet.sendToServer();
    }

    public void updateMachineList(String query, int offset, int limit) {
        if (!this.active)
            return;

        var packet = new RemoteShellSearchQuery();
        packet.dimension = remoteShellDimension;
        packet.blockPos = remoteShellPosition;
        packet.query = query;
        packet.offset = offset;
        packet.limit = limit;
        packet.sendToServer();
    }

    @SubscribeEvent
    public static void addOverlay(ScreenEvent.Init.Post event) {
        if (!INSTANCE.active)
            return;

        if (event.getScreen() instanceof AbstractContainerScreen<?>) {
            var window = new RemoteShellOverlay();
            if (event.getScreen() instanceof TorchScreen<?> torchScreen) {
                torchScreen.addWindow(window);
                torchScreen.addWindow(window.getMachineList());
            } else {
                event.addListener(window);
                event.addListener(window.getMachineList());
            }
        }
    }
}
