package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.common.registry.WithScreen;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.Sync;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.network.WithPacket;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.thunderbolt.cable.packets.SetTransmissionModePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

@WithScreen(ThunderboltCableScreen.class)
@WithPacket(SetTransmissionModePacket.class)
public class ThunderboltCableMenu extends MachineMenu<ThunderboltCableEntity> {
    public ThunderboltCableMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public ThunderboltCableMenu(int pContainerId, Inventory playerInventory, ThunderboltCableEntity machine) {
        super(RezolveRegistry.menuType(ThunderboltCableMenu.class), pContainerId, playerInventory, machine);
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

    @Override
    public void receivePacketOnServer(RezolvePacket rezolvePacket) {
        if (rezolvePacket instanceof SetTransmissionModePacket setTransmissionMode) {
            configuration.getFace(setTransmissionMode.face)
                    .getTransmissionConfiguration(setTransmissionMode.type)
                    .setMode(setTransmissionMode.mode)
            ;
        } else {
            super.receivePacketOnServer(rezolvePacket);
        }
    }
}