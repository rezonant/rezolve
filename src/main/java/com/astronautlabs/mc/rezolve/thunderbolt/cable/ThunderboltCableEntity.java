package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.common.util.RezolveCapHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThunderboltCableEntity extends MachineEntity {
    public ThunderboltCableEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(ThunderboltCableEntity.class), pPos, pBlockState);
    }

    @Override
    public Component getMenuTitle() {
        return Component.translatable("block.rezolve.thunderbolt_connection");
    }

    /**
     * True if this cable interfaces to one or more blocks.
     * @return
     */
    public boolean isInterface() {
        return this.isInterface;
    }

    private boolean isInterface = false;
    private boolean initialized = false;

    @Override
    public void tick() {
        super.tick();

        if (!initialized) {
            if (!level.isClientSide)
                updateBlockStates();
            initialized = true;
        }
    }

    @Override
    public boolean actsAsCable() {
        return true;
    }

    @Override
    public void updatePeriodically() {
        if (isInterface) {
            var cableNetwork = getNetwork();

            for (var inletBlock : configuration.values()) {
                var pos = inletBlock.getPosition();
                var entity = level.getBlockEntity(pos);
                if (entity == null)
                    continue;

                for (var face : inletBlock.getFaces()) {
                    for (var transmitTypeConfig : face.getTransmissionConfigurations()) {
                        if (transmitTypeConfig.getMode() == TransmissionMode.PULL) {
                            switch (transmitTypeConfig.getType()) {
                                case ITEMS -> {
                                    transferItem(cableNetwork, transmitTypeConfig, face, inletBlock);
                                }
                                case FLUIDS -> {
                                    var handler = entity.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getDirection()).orElse(null);
                                    if (handler == null)
                                        continue;
                                }
                                case ENERGY -> {
                                    var handler = entity.getCapability(ForgeCapabilities.ENERGY, face.getDirection()).orElse(null);
                                    if (handler == null)
                                        continue;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void adoptNetwork(CableNetwork network) {
        super.adoptNetwork(network);

        for (var config : configuration.values()) {
            var endpoint = network.getEndpoint(config.getPosition());
            if (endpoint != null)
                endpoint.addInterface(getBlockPos(), config);
        }
    }

    /**
     * Perform an item transfer from the given start point to whatever end point will accept it.
     * @param transmitTypeConfig
     * @param face
     * @param inletBlock
     */
    private void transferItem(
            CableNetwork network,
            TransmitConfiguration transmitTypeConfig,
            FaceConfiguration face,
            BlockConfiguration inletBlock)
    {
        var amount = 1;
        var entity = level.getBlockEntity(inletBlock.getPosition());
        var sourceHandler = RezolveCapHelper.getItemHandler(entity, face.getDirection());
        if (sourceHandler == null)
            return;

        for (int sourceSlot = 0, max = sourceHandler.getSlots(); sourceSlot < max; ++sourceSlot) {
            var potentialStack = sourceHandler.extractItem(sourceSlot, amount, true);
            if (potentialStack != null && !potentialStack.isEmpty()) {
                if (pushItem(inletBlock, sourceHandler, sourceSlot, potentialStack))
                    return;
            }
        }
    }

    private boolean pushItem(
            BlockConfiguration inletBlock,
            IItemHandler sourceHandler,
            int sourceSlot,
            ItemStack itemsToTransfer
    ) {
        for (var dest : getExistingNetwork().getEndpoints()) {
            if (Objects.equals(inletBlock.getPosition(), dest.getPosition()))
                continue;

            var destEntity = level.getBlockEntity(dest.getPosition());

            for (var blockInterface : dest.getInterfaces()) {
                for (var destFace : blockInterface.getFaces()) {
                    var itemInterface = destFace.getTransmissionConfiguration(TransmissionType.ITEMS);
                    if (itemInterface.getMode().canPush()) {
                        var destHandler = RezolveCapHelper.getItemHandler(destEntity, destFace.getDirection());
                        if (destHandler == null)
                            continue;

                        for (int destinationSlot = 0, destSlotCount = destHandler.getSlots(); destinationSlot < destSlotCount; ++destinationSlot) {
                            var result = destHandler.insertItem(destinationSlot, itemsToTransfer, true);
                            if (result != null && !result.isEmpty())
                                continue;

                            // This will work

                            var item = sourceHandler.extractItem(sourceSlot, itemsToTransfer.getCount(), false);
                            destHandler.insertItem(destinationSlot, item, false);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("configuration")) {
            var config = tag.getCompound("configuration");
            configuration.clear();
            for (var dir : Direction.values()) {
                if (!config.contains(dir.getName()))
                    continue;

                var blockConfig = new BlockConfiguration();
                blockConfig.deserializeNBT(config.getCompound(dir.getName()));
                configuration.put(dir, blockConfig);
            }
        }

        updateInterfaceState();
        initialized = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        var config = new CompoundTag();
        for (var dir : configuration.keySet()) {
            config.put(dir.getName(), configuration.get(dir).serializeNBT());
        }

        tag.put("configuration", config);
    }

    Map<Direction, BlockConfiguration> configuration = new HashMap<>();

    public BlockConfiguration getBlockConfiguration(Direction dir) {
        if (configuration.containsKey(dir)) {
            return configuration.get(dir);
        } else {
            BlockConfiguration blockConfig = new BlockConfiguration(getBlockPos().relative(dir));
            blockConfig.updateBlockState(getLevel());
            configuration.put(dir, blockConfig);
            updateInterfaceState();
            return blockConfig;
        }
    }

    private void updateInterfaceState() {
        isInterface = configuration.values().stream().anyMatch(c -> c.isInterface());
    }

    public void updateBlockStates() {
        for (var cableDir : Direction.values()) {
            getBlockConfiguration(cableDir).updateBlockState(getLevel());
        }
        updateInterfaceState();
    }

    public void onNeighborChanged(BlockPos pFromPos) {
        updateBlockStates();
        if (getExistingNetwork() != null)
            getExistingNetwork().invalidate();
    }
}
