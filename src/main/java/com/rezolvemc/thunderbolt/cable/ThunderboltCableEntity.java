package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.common.LevelPosition;
import com.rezolvemc.common.capabilities.EnergyStack;
import com.rezolvemc.common.capabilities.ResourceHandler;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.util.RezolveCapHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ThunderboltCableEntity extends MachineEntity {
    private static final Capability<ResourceHandler> RESOURCE_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public ThunderboltCableEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(ThunderboltCableEntity.class), pPos, pBlockState);

        updateInterval = 1;
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
            if (cableNetwork == null)
                return;

            for (var inletBlock : configuration.values()) {
                var pos = inletBlock.getPosition();
                var entity = level.getBlockEntity(pos);
                if (entity == null)
                    continue;

                for (var face : inletBlock.getFaces()) {
                    for (var transmitTypeConfig : face.getTransmissionConfigurations()) {
                        if (!transmitTypeConfig.isSupported())
                            continue;

                        if (transmitTypeConfig.getMode().canPull()) {
                            switch (transmitTypeConfig.getType()) {
                                case ITEMS -> {
                                    transferItem(cableNetwork, transmitTypeConfig, face, inletBlock);
                                }
                                case FLUIDS -> {
                                    transferFluid(cableNetwork, transmitTypeConfig, face, inletBlock);
                                }
                                case ENERGY -> {
                                    transferEnergy(cableNetwork, transmitTypeConfig, face, inletBlock);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private LevelPosition getLevelPosition() {
        return getLevelPosition(null);
    }

    private LevelPosition getLevelPosition(Direction facing) {
        if (facing != null)
            return LevelPosition.of(getLevel(), getBlockPos().relative(facing));
        else
            return LevelPosition.of(getLevel(), getBlockPos());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (facing == null)
            return LazyOptional.empty();

        if (capability == ForgeCapabilities.ITEM_HANDLER)
            return LazyOptional.of(() -> (T)new ItemHandler(getLevelPosition(facing)));
        else if (capability == ForgeCapabilities.FLUID_HANDLER)
            return LazyOptional.of(() -> (T)new FluidHandler(getLevelPosition(facing)));
        else if (capability == ForgeCapabilities.ENERGY)
            return LazyOptional.of(() -> (T)new EnergyStorage(getLevelPosition(facing)));

        return super.getCapability(capability, facing);
    }

    @Override
    public void adoptNetwork(CableNetwork network) {
        super.adoptNetwork(network);

        for (var config : configuration.values()) {
            var endpoint = network.getEndpoint(getLevel(), config.getPosition());
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
        var amount = 64;
        var entity = level.getBlockEntity(inletBlock.getPosition());
        var levelPos = new LevelPosition(getLevel(), inletBlock.getPosition());

        var itemHandler = RezolveCapHelper.getItemHandler(entity, face.getDirection());
        if (itemHandler != null) {
            for (int sourceSlot = 0, max = itemHandler.getSlots(); sourceSlot < max; ++sourceSlot) {
                var potentialStack = itemHandler.extractItem(sourceSlot, amount, true);
                if (potentialStack != null && !potentialStack.isEmpty()) {
                    int finalSourceSlot = sourceSlot;
                    var success = pushItem(
                            potentialStack,
                            desiredAmount -> itemHandler.extractItem(finalSourceSlot, desiredAmount, false),
                            levelPos, false
                    );

                    if (success)
                        return;
                }
            }
        }
    }

    private boolean pushItem(
            ItemStack itemsToTransfer,
            Function<Integer, ItemStack> extractor,
            LevelPosition sourceEndpoint,
            boolean simulate
    ) {
        if (!hasNetwork())
            return false;

        itemsToTransfer = itemsToTransfer.copy();
        for (var dest : getExistingNetwork().getEndpoints()) {
            if (dest.is(sourceEndpoint))
                continue;

            var destEntity = dest.getBlockEntity();

            for (var blockInterface : dest.getInterfaces()) {
                for (var destFace : blockInterface.getFaces()) {
                    var itemInterface = destFace.getTransmissionConfiguration(TransmissionType.ITEMS);
                    if (itemInterface.getMode().canPush()) {
                        var destHandler = RezolveCapHelper.getItemHandler(destEntity, destFace.getDirection());
                        if (destHandler == null)
                            continue;

                        for (int destinationSlot = 0, destSlotCount = destHandler.getSlots(); destinationSlot < destSlotCount; ++destinationSlot) {
                            var remainder = destHandler.insertItem(destinationSlot, itemsToTransfer, true);
                            if (remainder == null)
                                remainder = ItemStack.EMPTY;

                            var acceptedAmount = itemsToTransfer.getCount() - remainder.getCount();

                            var item = extractor.apply(acceptedAmount);
                            destHandler.insertItem(destinationSlot, item, simulate);

                            itemsToTransfer = remainder;

                            if (itemsToTransfer.isEmpty())
                                return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void transferFluid(
            CableNetwork cableNetwork,
            TransmitConfiguration transmitTypeConfig,
            FaceConfiguration face,
            BlockConfiguration inletBlock
    ) {
        var amount = 1000;
        var entity = level.getBlockEntity(inletBlock.getPosition());
        var levelPos = new LevelPosition(getLevel(), inletBlock.getPosition());

        var sourceHandler = entity.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getDirection()).orElse(null);
        if (sourceHandler == null)
            return;

        var potentialStack = sourceHandler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
        if (potentialStack != null && !potentialStack.isEmpty()) {
            pushFluid(
                    potentialStack,
                    desiredAmount -> sourceHandler.drain(desiredAmount, IFluidHandler.FluidAction.EXECUTE),
                    levelPos, false
            );
        }
    }

    private boolean pushFluid(
            FluidStack fluidToTransfer,
            Function<Integer, FluidStack> extractor,
            LevelPosition sourceEndpoint,
            boolean simulate
    ) {
        if (!hasNetwork())
            return false;

        fluidToTransfer = fluidToTransfer.copy();

        for (var dest : getExistingNetwork().getEndpoints()) {
            if (dest.is(sourceEndpoint))
                continue;

            var destEntity = dest.getBlockEntity();

            for (var blockInterface : dest.getInterfaces()) {
                for (var destFace : blockInterface.getFaces()) {
                    var itemInterface = destFace.getTransmissionConfiguration(TransmissionType.FLUIDS);
                    if (itemInterface.getMode().canPush()) {
                        var destHandler = RezolveCapHelper.getFluidHandler(destEntity, destFace.getDirection());
                        if (destHandler == null)
                            continue;

                        var receivableAmount = destHandler.fill(fluidToTransfer, IFluidHandler.FluidAction.SIMULATE);
                        if (receivableAmount <= 0)
                            continue;

                        // This will work

                        var actualStack = extractor.apply(receivableAmount);
                        var filled = destHandler.fill(actualStack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);

                        fluidToTransfer.setAmount(fluidToTransfer.getAmount() - filled);

                        if (fluidToTransfer.isEmpty())
                            return true;
                    }
                }
            }
        }

        return false;
    }

    private void transferEnergy(
            CableNetwork cableNetwork,
            TransmitConfiguration transmitTypeConfig,
            FaceConfiguration face,
            BlockConfiguration inletBlock
    ) {
        var amount = 1000000;

        var entity = level.getBlockEntity(inletBlock.getPosition());
        var sourceHandler = entity.getCapability(ForgeCapabilities.ENERGY, face.getDirection()).orElse(null);
        if (sourceHandler == null)
            return;

        var levelPos = new LevelPosition(getLevel(), inletBlock.getPosition());
        var availableAmount = sourceHandler.extractEnergy(amount, true);
        if (availableAmount > 0) {
            pushEnergy(
                    new EnergyStack(availableAmount),
                    desiredAmount -> EnergyStack.of(sourceHandler.extractEnergy(desiredAmount, false)),
                    levelPos,
                    false
            );
        }
    }

    private boolean pushEnergy(
            EnergyStack potentialEnergyStack,
            Function<Integer, EnergyStack> extractor,
            LevelPosition sourceEndpoint,
            boolean simulate
    ) {
        if (!hasNetwork())
            return false;

        potentialEnergyStack = potentialEnergyStack.copy();

        for (var dest : getExistingNetwork().getEndpoints()) {
            if (dest.is(sourceEndpoint))
                continue;

            var destEntity = dest.getBlockEntity();

            for (var blockInterface : dest.getInterfaces()) {
                for (var destFace : blockInterface.getFaces()) {
                    var itemInterface = destFace.getTransmissionConfiguration(TransmissionType.ENERGY);
                    if (itemInterface.getMode().canPush()) {
                        var destHandler = RezolveCapHelper.getEnergyStorage(destEntity, destFace.getDirection());
                        if (destHandler == null)
                            continue;

                        var receivableAmount = destHandler.receiveEnergy(potentialEnergyStack.getAmount(), true);
                        if (receivableAmount <= 0)
                            continue;

                        var actualTransferred = extractor.apply(receivableAmount);
                        destHandler.receiveEnergy(actualTransferred.getAmount(), simulate);
                        potentialEnergyStack.split(actualTransferred.getAmount());

                        if (potentialEnergyStack.isEmpty())
                            return true;
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

    private class ItemHandler implements IItemHandler {
        ItemHandler(LevelPosition sourceEndpoint) {
            this.sourceEndpoint = sourceEndpoint;
        }

        private LevelPosition sourceEndpoint;

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack == null || stack.getCount() == 0)
                return ItemStack.EMPTY;

            var copy = stack.copy();
            pushItem(stack, amount -> copy.split(amount), sourceEndpoint, simulate);
            return copy;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY; // TODO
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    }

    private class FluidHandler implements IFluidHandler {
        FluidHandler(LevelPosition sourceEndpoint) {
            this.sourceEndpoint = sourceEndpoint;
        }

        private LevelPosition sourceEndpoint;

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            var copy = resource.copy();
            pushFluid(resource, desiredAmount -> {
                desiredAmount = Math.min(copy.getAmount(), desiredAmount);
                copy.setAmount(copy.getAmount() - desiredAmount);
                return new FluidStack(resource.getFluid(), desiredAmount);
            }, sourceEndpoint, action.simulate());

            return resource.getAmount() - copy.getAmount();
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY; // TODO
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY; // TODO
        }
    }

    private class EnergyStorage implements IEnergyStorage {
        EnergyStorage(LevelPosition sourceEndpoint) {
            this.sourceEndpoint = sourceEndpoint;
        }

        private LevelPosition sourceEndpoint;

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            var stack = EnergyStack.of(maxReceive);
            pushEnergy(stack, desiredAmount -> stack.split(desiredAmount), sourceEndpoint, simulate);
            return maxReceive - stack.getAmount();
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
