package com.rezolvemc.thunderbolt.remoteShell.server;


import com.rezolvemc.Rezolve;
import com.rezolvemc.common.LevelPosition;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.thunderbolt.databaseServer.DatabaseServer;
import com.rezolvemc.thunderbolt.databaseServer.DatabaseServerEntity;
import com.rezolvemc.thunderbolt.remoteShell.RemoteShellEntity;
import com.rezolvemc.thunderbolt.remoteShell.common.MachineListing;
import com.rezolvemc.thunderbolt.remoteShell.packets.*;
import com.rezolvemc.thunderbolt.remoteShell.server.RemoteAccessSession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Consumer;

public abstract class RemoteAccessEndpoint {
    public RemoteAccessEndpoint(RemoteShellEntity remoteShell) {
        this.remoteShellx = remoteShell;
    }

    private RemoteShellEntity remoteShellx;
    protected int openEnergyCost = 1000;
    protected int constantDrawCost = 50;
    protected int accessCharge = 10;

    public int getOpenEnergyCost() {
        return openEnergyCost;
    }

    public int getConstantDrawCost() {
        return constantDrawCost;
    }

    public int getAccessCharge() {
        return accessCharge;
    }

    List<RemoteAccessSession> activatedPlayers = new ArrayList<>();

    public RemoteAccessSession getPlayerState(Player player) {
        if (player == null)
            return null;

        return this.activatedPlayers.stream()
                .filter(s -> Objects.equals(s.player.getStringUUID(), player.getStringUUID()))
                .findFirst()
                .orElse(null)
                ;
    }

    private RemoteAccessSession getOrCreateState(ServerPlayer player) {
        var activation = getPlayerState(player);
        if (activation == null) {
            this.activatedPlayers.add(activation = new RemoteAccessSession(this, player, true, null));
        }

        return activation;
    }

    public RemoteAccessSession startSession(ServerPlayer player) {
        var session = getOrCreateState(player);
        session.send();
        return session;
    }

    protected boolean stillValid(RemoteAccessSession session) {
        return session.player.containerMenu != session.player.inventoryMenu;
    }

    protected boolean shouldChargeUpkeep(RemoteAccessSession session) {
        return session.activeMachine != null;
    }

    protected void sessionTick(RemoteAccessSession session) {
        if (shouldChargeUpkeep(session))
            expendUpkeep(session);
    }

    protected void expendUpkeep(RemoteAccessSession session) {
        if (getStoredEnergy() < constantDrawCost) {
            session.returnToShell();
        } else {
            expendEnergy(constantDrawCost, false);
        }
    }

    protected void beforeConnect(RemoteAccessSession session, MachineListing machine) {

    }

    protected void afterDisconnect(RemoteAccessSession session, MachineListing machine) {

    }

    public abstract BlockPos getBlockPos();

    public abstract Level getLevel();

    public abstract int getStoredEnergy();

    public abstract int getEnergyCapacity();

    public abstract int expendEnergy(int amount, boolean simulate);

    public abstract List<MachineListing> getConnectedMachines();

    public abstract boolean isValidDestination(LevelPosition position);

    public void updatePeriodically() {
        ArrayList<RemoteAccessSession> deactivatedPlayers = new ArrayList<>();

        synchronized (activatedPlayers) {
            for (RemoteAccessSession state : this.activatedPlayers) {
                if (stillValid(state)) {
                    sessionTick(state);
                    state.send();
                } else {
                    deactivatedPlayers.add(state);
                }
            }

            for (var activationState : deactivatedPlayers) {
                endPlayerSession(activationState);
            }
        }
    }

    private void endPlayerSession(RemoteAccessSession activationState) {
        var player = activationState.player;
        Rezolve.clearPlayerOverridePosition(player.getUUID());

        var machine = activationState.activeMachine;

        activationState.active = false;
        activationState.activeMachine = null;
        activationState.send();

        if (machine != null)
            afterDisconnect(activationState, machine);

        this.activatedPlayers.remove(activationState);
    }

    private void withPlayerState(Player player, Consumer<RemoteAccessSession> func) {
        if (player.level.isClientSide)
            return;

        var state = getPlayerState(player);
        if (state == null)
            return;

        func.accept(state);
    }

    public boolean handlePacket(RezolvePacket rezolvePacket, Player player) {
        if (rezolvePacket instanceof RemoteShellEntityReturnPacket) {
            withPlayerState(player, state -> state.returnToShell());
        } else if (rezolvePacket instanceof RemoteShellStartRecordingPacket) {
            withPlayerState(player, state -> state.startRecording());
        } else if (rezolvePacket instanceof RemoteShellStopRecordingPacket) {
            withPlayerState(player, state -> state.stopRecording());
        } else if (rezolvePacket instanceof RemoteShellTakePatternPacket) {
            withPlayerState(player, state -> state.takePattern());
        } else if (rezolvePacket instanceof RemoteShellActivatePacket activatePacket) {
            withPlayerState(player, state -> state.connectToMachine(activatePacket.getLevel(), activatePacket.getActivatedMachine()));
        } else if (rezolvePacket instanceof RemoteShellSearchQuery query) {
            withPlayerState(player, state -> state.performSearch(query));
        } else {
            return false;
        }

        return true;
    }
}