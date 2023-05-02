package com.rezolvemc.thunderbolt.remoteShell.server;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.LevelPosition;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.manufacturing.ManufacturingPattern;
import com.rezolvemc.thunderbolt.cable.CableNetwork;
import com.rezolvemc.thunderbolt.remoteShell.common.MachineListing;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellSearchQuery;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellSearchResults;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.*;

public class RemoteAccessSession {
    RemoteAccessSession(RemoteAccessEndpoint endpoint, ServerPlayer player, boolean active, @Nonnull MachineListing activeMachine) {
        this.endpoint = endpoint;
        this.player = player;
        this.active = active;
        this.activeMachine = activeMachine;
    }

    public RemoteAccessEndpoint endpoint;
    public ServerPlayer player;
    public boolean active;
    public MachineListing activeMachine;
    public ItemStack recordedPattern;

    private boolean recording;
    private List<ManufacturingPattern.RecordedAction> recordedActions;

    public boolean isRecording() {
        return recording;
    }

    public void startRecording() {
        recording = true;
        recordedActions = new ArrayList<>();
        send();
    }

    public void stopRecording() {
        var actions = finishRecording();
        if (actions.length > 0) {
            recordedPattern = RezolveRegistry.item(ManufacturingPattern.class).writeActions(actions);
        }
        send();
    }

    public void takePattern() {
        if (player.containerMenu != null && recordedPattern != null) {
            player.containerMenu.setCarried(recordedPattern);
            player.containerMenu.setRemoteCarried(recordedPattern);
            player.containerMenu.broadcastChanges();
            recordedPattern = null;
        }

        send();
    }

    public void performSearch(RemoteShellSearchQuery query) {
        // TODO: limit/offset
        var results = new RemoteShellSearchResults();
        var unfilteredList = endpoint.getConnectedMachines();

        for (var machine : unfilteredList) {
            String name = machine.getItem().getDisplayName().getString();
            if (machine.getName() != null)
                name = machine.getName();

            name = name.toLowerCase(Locale.ROOT);

            if (query.query == null || name.contains(query.query.toLowerCase(Locale.ROOT)))
                results.machines.add(machine);
        }

        results.sendToPlayer(player);
    }

    public void record(ResourceKey<Level> level, BlockPos block, int slot, ManufacturingPattern.Action action, ItemStack items) {
        recordedActions.add(new ManufacturingPattern.RecordedAction(level, block, slot, action, items));
    }

    public ManufacturingPattern.RecordedAction[] finishRecording() {
        recording = false;
        var set = recordedActions.toArray(new ManufacturingPattern.RecordedAction[recordedActions.size()]);
        recordedActions = null;
        return set;
    }

    public void returnToShell() {
        //player.closeContainer();

        activeMachine = null;
        openBlock(endpoint.getLevel(), endpoint.getBlockPos());
    }

    public void send() {
        var packet = new RemoteShellStatePacket();

        // Endpoint properties
        packet.remoteShellDimension = endpoint.getLevel().dimension().location().toString();
        packet.remoteShellPosition = endpoint.getBlockPos();
        packet.remoteShellEnergy = endpoint.getStoredEnergy();
        packet.remoteShellEnergyCapacity = endpoint.getEnergyCapacity();

        // Session properties
        packet.active = active;
        packet.activeMachine = activeMachine;
        packet.recordedPattern = recordedPattern;
        packet.recording = recording;

        packet.sendToPlayer((ServerPlayer) player);
    }

    private void openBlock(Level level, BlockPos pos) {
        level.getBlockState(pos).use(
            level, player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)
        );
    }

    private void chargeForAccess(ItemStack items) {
        endpoint.expendEnergy(endpoint.accessCharge * items.getCount(), false);
    }

    public void connectToMachine(ResourceKey<Level> levelKey, BlockPos machinePos) {
        if (endpoint.expendEnergy(endpoint.openEnergyCost, true) < endpoint.openEnergyCost) {
            return;
        }

        var levelPos = new LevelPosition(levelKey, machinePos);

        // Ensure that the passed location is actually on an attached cable network before proceeding.

        if (!endpoint.isValidDestination(levelPos))
            return;

        CableNetwork.Endpoint machineEndpoint;
        var machineLevel = endpoint.getLevel().getServer().getLevel(levelKey);
        var machineBlockState = machineLevel.getBlockState(machinePos);
        var machineBlock = machineBlockState.getBlock();

        endpoint.expendEnergy(endpoint.openEnergyCost, false);

        // We add an override entry for this player to the endpoint location (in the specific level)
        // so that Rezolve's mixin can do container stillValid() checks against the simulated location,
        // so that any checks not specifically related to player location will still function as expected.

        Rezolve.setPlayerOverridePosition(
                player.getUUID(),
                levelPos
        );

        System.out.println("Activating block using Remote Shell: " + machineBlock.getName().toString());

        AbstractContainerMenu existingContainer = player.containerMenu;

        var chunkPos = new ChunkPos(machinePos);

        ForgeChunkManager.forceChunk((ServerLevel) machineLevel, Rezolve.ID, player, chunkPos.x, chunkPos.z, true, true);
        openBlock(machineLevel, machinePos);

        if (existingContainer != player.containerMenu) {
            var remoteContainer = player.containerMenu;

            // Open was successful.

            var machineListing = new MachineListing(
                    levelKey,
                    machinePos,
                    null, // TODO
                    new ItemStack(machineBlock.asItem(), 1)
            );

            active = true;
            activeMachine = machineListing;
            send();

            // Track events in the container

            remoteContainer.addSlotListener(new ContainerListener() {
                Map<Integer, ItemStack> itemState = new HashMap<>();
                ItemStack carried = remoteContainer.getCarried();

                {
                    for (int i = 0, max = remoteContainer.slots.size(); i < max; ++i) {
                        itemState.put(i, remoteContainer.getSlot(i).getItem());
                    }
                }

                @Override
                public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {

                    ItemStack oldItem = itemState.get(dataSlotIndex);
                    ItemStack oldCarried = carried;

                    itemState.put(dataSlotIndex, stack);
                    carried = remoteContainer.getCarried();

                    Slot slot = containerToSend.getSlot(dataSlotIndex);
                    boolean charge = true;

                    if (slot != null) {
                        if (slot.container instanceof Inventory) {
                            // We don't charge for changes to the player's inventory.
                            charge = false;
                        }
                    }

                    ItemStack netStack = null;
                    ManufacturingPattern.Action action = null;
                    if (oldItem.sameItem(stack)) {
                        netStack = new ItemStack(oldItem.getItem(), Math.abs(stack.getCount() - oldItem.getCount()));

                        if (stack.getCount() > oldItem.getCount())
                            action = ManufacturingPattern.Action.INSERT;
                        else
                            action = ManufacturingPattern.Action.EXTRACT;

                    } else if (!oldItem.isEmpty()) {
                        netStack = oldItem.copy();
                        action = ManufacturingPattern.Action.EXTRACT;
                    } else if (!stack.isEmpty()) {
                        netStack = stack.copy();
                        action = ManufacturingPattern.Action.INSERT;
                    }

                    if (charge && netStack != null) {
                        chargeForAccess(netStack);

                        if (isRecording() && action != null) {
                            record(levelKey, machinePos, dataSlotIndex, action, netStack);
                        }
                    }

                }

                @Override
                public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

                }
            });
        }

    }
}
