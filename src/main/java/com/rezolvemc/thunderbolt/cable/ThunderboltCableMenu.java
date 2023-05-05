package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.Sync;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.thunderbolt.cable.packets.SetTransmissionModePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public class ThunderboltCableMenu extends MachineMenu<ThunderboltCableEntity> {
    public ThunderboltCableMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public ThunderboltCableMenu(int pContainerId, Inventory playerInventory, ThunderboltCableEntity machine) {
        super(pContainerId, playerInventory, machine);
    }

    @Sync
    public Direction direction;
    @Sync public BlockPos position;
    @Sync public int targetBlockId;
    @Sync public BlockConfiguration configuration = null;

    public Direction getDirection() {
        return direction;
    }

    @Override
    protected void updateState() {
        super.updateState();

        if (direction != null) {
            position = machine.getBlockPos().relative(direction);
            targetBlockId = Block.getId(machine.getLevel().getBlockState(position));
            configuration = machine.getBlockConfiguration(direction);
        }
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setTransmissionMode(Direction face, TransmissionType type, TransmissionMode mode) {
        var packet = new SetTransmissionModePacket();
        packet.setMenu(this);
        packet.face = face;
        packet.type = type;
        packet.mode = mode;
        packet.sendToServer();
    }

    public void cycleTransmissionMode(Direction face, TransmissionType type, int direction) {
        setTransmissionMode(face, type, configuration.getTransmitMode(face, type).seek(direction));
    }

    @Override
    public void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) {
        if (rezolvePacket instanceof SetTransmissionModePacket setTransmissionMode) {
            configuration.getFace(setTransmissionMode.face)
                    .getTransmissionConfiguration(setTransmissionMode.type)
                    .setMode(setTransmissionMode.mode)
            ;
        } else {
            super.receivePacketOnServer(rezolvePacket, player);
        }
    }
}
