package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenioniteBatteryBlock;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.inventory.ZenioniteBatteryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ZenioniteBatteryBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_CAPACITY = 80;
    public static final int OUTPUT_PER_TRANSFER = 1;
    public static final int SIDE_COUNT = 4;

    public static final int DATA_NORTH = 0;
    public static final int DATA_EAST = 1;
    public static final int DATA_SOUTH = 2;
    public static final int DATA_WEST = 3;
    public static final int DATA_SIDE_HIGH_START = 4;
    public static final int DATA_SIDE_CAPACITY_LOW = 8;
    public static final int DATA_SIDE_CAPACITY_HIGH = 9;
    public static final int DATA_COUNT = 10;

    private static final String SIDE_ENERGY_TAG = "SideEnergy";
    private static final String FILL_ORDER_TAG = "FillOrder";
    private static final String FILL_ORDER_SIZE_TAG = "FillOrderSize";
    private static final String SAVED_FACING_TAG = "SavedFacing";
    private static final String NEXT_OUTPUT_TAG = "NextOutput";
    private static final String SYNCED_SIDE_CAPACITY_TAG = "SyncedSideCapacity";
    private static final String TRANSFER_COOLDOWN_TAG = "TransferCooldown";

    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    private static final Direction[] OUTPUT_DIRECTIONS = {
            Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    private final int[] sideEnergy = new int[SIDE_COUNT];
    private final int[] fillOrder = {-1, -1, -1, -1};
    private int fillOrderSize;
    private int nextOutputIndex;
    private int syncedSideCapacity;

    private final IEnergyStorage bottomEnergyInput = new EnergyInputHandler();
    private final IEnergyStorage outwardEnergyOutput = new EnergyOutputHandler();
    private LazyOptional<IEnergyStorage> inputCapability =
            LazyOptional.of(() -> bottomEnergyInput);
    private LazyOptional<IEnergyStorage> outputCapability =
            LazyOptional.of(() -> outwardEnergyOutput);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index >= 0 && index < SIDE_COUNT) {
                return lowWord(sideEnergy[index]);
            }
            if (index >= DATA_SIDE_HIGH_START
                    && index < DATA_SIDE_HIGH_START + SIDE_COUNT) {
                return highWord(sideEnergy[index - DATA_SIDE_HIGH_START]);
            }
            return switch (index) {
                case DATA_SIDE_CAPACITY_LOW -> lowWord(getSideCapacity());
                case DATA_SIDE_CAPACITY_HIGH -> highWord(getSideCapacity());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index >= 0 && index < SIDE_COUNT) {
                sideEnergy[index] = withLowWord(sideEnergy[index], value);
            } else if (index >= DATA_SIDE_HIGH_START
                    && index < DATA_SIDE_HIGH_START + SIDE_COUNT) {
                int side = index - DATA_SIDE_HIGH_START;
                sideEnergy[side] = withHighWord(sideEnergy[side], value);
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private int transferCooldownTicks;
    private boolean loading;

    public ZenioniteBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZENIONITE_BATTERY.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ZenioniteBatteryBlockEntity battery
    ) {
        battery.clampEnergyToCapacity();
        battery.tickTransferCooldown();
        battery.refreshHorizontalConnections();
        battery.pushEnergy(level, pos);
    }

    public int getEnergyCapacity() {
        if (level != null && level.isClientSide && syncedSideCapacity > 0) {
            return syncedSideCapacity * SIDE_COUNT;
        }
        return ENERGY_CAPACITY * EndlessSandsConfig.getRfMultiplier();
    }

    public int getSideCapacity() {
        return getEnergyCapacity() / SIDE_COUNT;
    }

    public int getEnergyStored() {
        long total = 0;
        for (int amount : sideEnergy) {
            total += amount;
        }
        return (int) Math.min(total, getEnergyCapacity());
    }

    public int getSideEnergy(Direction direction) {
        return direction.getAxis().isHorizontal() ? sideEnergy[directionIndex(direction)] : 0;
    }

    public int[] getSideEnergySnapshot() {
        return Arrays.copyOf(sideEnergy, sideEnergy.length);
    }

    public int[] getFillOrderSnapshot() {
        return Arrays.copyOf(fillOrder, fillOrderSize);
    }

    public ContainerData getContainerData() {
        return data;
    }

    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    public void refreshHorizontalConnections() {
        if (level == null || level.isClientSide || isRemoved()) {
            return;
        }

        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof ZenioniteBatteryBlock)) {
            return;
        }

        boolean anyConnected = false;
        BlockState updated = current;
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            boolean connected = hasHorizontalEnergyInput(level, worldPosition, direction);
            anyConnected |= connected;
            updated = updated.setValue(
                    ZenioniteBatteryBlock.connectionProperty(direction), connected);
        }
        updated = updated.setValue(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION, anyConnected);

        if (updated != current) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
    }

    private static boolean hasHorizontalEnergyInput(Level level, BlockPos pos, Direction direction) {
        BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
        if (neighbor == null) {
            return false;
        }
        return neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                .map(IEnergyStorage::canReceive)
                .orElse(false);
    }

    private void pushEnergy(Level level, BlockPos pos) {
        int allowance = getOutputAllowance();
        if (allowance <= 0) {
            return;
        }

        for (int offset = 0; offset < OUTPUT_DIRECTIONS.length; offset++) {
            int outputIndex = Math.floorMod(nextOutputIndex + offset, OUTPUT_DIRECTIONS.length);
            Direction direction = OUTPUT_DIRECTIONS[outputIndex];
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor == null) {
                continue;
            }

            IEnergyStorage receiver = neighbor
                    .getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                    .orElse(null);
            if (receiver == null || !receiver.canReceive()) {
                continue;
            }

            int requested = Math.min(allowance,
                    Math.max(0, receiver.receiveEnergy(allowance, true)));
            if (requested <= 0) {
                continue;
            }

            int[] energyBeforeTransfer = Arrays.copyOf(sideEnergy, sideEnergy.length);
            int[] orderBeforeTransfer = Arrays.copyOf(fillOrder, fillOrder.length);
            int orderSizeBeforeTransfer = fillOrderSize;
            int drained = drainSides(requested);
            int accepted = Math.min(drained,
                    Math.max(0, receiver.receiveEnergy(drained, false)));
            if (accepted <= 0) {
                restoreStorage(energyBeforeTransfer, orderBeforeTransfer, orderSizeBeforeTransfer);
                continue;
            }

            if (accepted < drained) {
                restoreStorage(energyBeforeTransfer, orderBeforeTransfer, orderSizeBeforeTransfer);
                drained = drainSides(accepted);
            }
            restartTransferCooldown();
            nextOutputIndex = (outputIndex + 1) % OUTPUT_DIRECTIONS.length;
            onStorageChanged();
            return;
        }
    }

    private int receiveEnergyInternal(int maxReceive, boolean simulate) {
        boolean wasEmpty = getEnergyStored() == 0;
        int received = Math.min(Math.max(0, maxReceive),
                Math.max(0, getEnergyCapacity() - getEnergyStored()));
        if (simulate || received <= 0) {
            return received;
        }

        int remaining = received;
        int sideCapacity = getSideCapacity();
        while (remaining > 0) {
            int side = currentFillSide(sideCapacity);
            if (side < 0) {
                side = selectNextSide(sideCapacity);
                if (side < 0) {
                    break;
                }
                appendFillSide(side);
            }

            int added = Math.min(remaining, sideCapacity - sideEnergy[side]);
            sideEnergy[side] += added;
            remaining -= added;
        }

        int actuallyReceived = received - remaining;
        if (actuallyReceived > 0) {
            if (wasEmpty) {
                restartTransferCooldown();
            }
            onStorageChanged();
        }
        return actuallyReceived;
    }

    private int extractEnergyInternal(int maxExtract, boolean simulate) {
        int extracted = Math.min(Math.max(0, maxExtract), getOutputAllowance());
        if (simulate || extracted <= 0) {
            return extracted;
        }

        int drained = drainSides(extracted);
        if (drained > 0) {
            restartTransferCooldown();
            onStorageChanged();
        }
        return drained;
    }

    private int drainSides(int requested) {
        ensureFillOrderContainsStoredSides();
        int remaining = Math.min(Math.max(0, requested), getEnergyStored());
        int original = remaining;

        while (remaining > 0 && fillOrderSize > 0) {
            int side = fillOrder[fillOrderSize - 1];
            int drained = Math.min(remaining, sideEnergy[side]);
            sideEnergy[side] -= drained;
            remaining -= drained;
            if (sideEnergy[side] == 0) {
                fillOrder[--fillOrderSize] = -1;
            }
        }
        return original - remaining;
    }

    private int currentFillSide(int sideCapacity) {
        ensureFillOrderContainsStoredSides();
        for (int orderIndex = fillOrderSize - 1; orderIndex >= 0; orderIndex--) {
            int side = fillOrder[orderIndex];
            if (sideEnergy[side] < sideCapacity) {
                return side;
            }
        }
        return -1;
    }

    private int selectNextSide(int sideCapacity) {
        BlockState state = getBlockState();
        Direction facing = state.hasProperty(ZenioniteBatteryBlock.FACING)
                ? state.getValue(ZenioniteBatteryBlock.FACING)
                : Direction.NORTH;
        boolean hasConnections = state.hasProperty(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION)
                && state.getValue(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION);

        if (fillOrderSize == 0) {
            if (!hasConnections) {
                return directionIndex(facing);
            }
            Direction[] startPriority = {
                    facing, facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite()
            };
            for (Direction candidate : startPriority) {
                if (ZenioniteBatteryBlock.isConnected(state, candidate)) {
                    return directionIndex(candidate);
                }
            }
            return directionIndex(facing);
        }

        Direction start = directionFromIndex(fillOrder[0]);
        Direction[] priority = hasConnections
                ? new Direction[]{start.getClockWise(), start.getCounterClockWise(), start.getOpposite()}
                : new Direction[]{facing.getCounterClockWise(), facing.getClockWise(), facing.getOpposite()};

        if (hasConnections) {
            int connected = firstEligibleSide(priority, sideCapacity, state, true);
            if (connected >= 0) {
                return connected;
            }
        }
        return firstEligibleSide(priority, sideCapacity, state, false);
    }

    private int firstEligibleSide(
            Direction[] priority,
            int sideCapacity,
            BlockState state,
            boolean requireConnection
    ) {
        for (Direction candidate : priority) {
            int side = directionIndex(candidate);
            if (sideEnergy[side] >= sideCapacity
                    || containsFillSide(side)
                    || requireConnection && !ZenioniteBatteryBlock.isConnected(state, candidate)
                    || !hasFullyChargedAdjacentSide(candidate, sideCapacity)) {
                continue;
            }
            return side;
        }
        return -1;
    }

    private boolean hasFullyChargedAdjacentSide(Direction direction, int sideCapacity) {
        return sideEnergy[directionIndex(direction.getClockWise())] >= sideCapacity
                || sideEnergy[directionIndex(direction.getCounterClockWise())] >= sideCapacity;
    }

    private void appendFillSide(int side) {
        if (fillOrderSize < SIDE_COUNT && !containsFillSide(side)) {
            fillOrder[fillOrderSize++] = side;
        }
    }

    private boolean containsFillSide(int side) {
        for (int index = 0; index < fillOrderSize; index++) {
            if (fillOrder[index] == side) {
                return true;
            }
        }
        return false;
    }

    private void ensureFillOrderContainsStoredSides() {
        boolean[] seen = new boolean[SIDE_COUNT];
        int writeIndex = 0;
        for (int index = 0; index < fillOrderSize; index++) {
            int side = fillOrder[index];
            if (side >= 0 && side < SIDE_COUNT && sideEnergy[side] > 0 && !seen[side]) {
                fillOrder[writeIndex++] = side;
                seen[side] = true;
            }
        }
        for (int side = 0; side < SIDE_COUNT; side++) {
            if (sideEnergy[side] > 0 && !seen[side]) {
                fillOrder[writeIndex++] = side;
            }
        }
        fillOrderSize = writeIndex;
        while (writeIndex < SIDE_COUNT) {
            fillOrder[writeIndex++] = -1;
        }
    }

    private void clampEnergyToCapacity() {
        int sideCapacity = getSideCapacity();
        boolean changed = false;
        for (int side = 0; side < SIDE_COUNT; side++) {
            int clamped = Math.max(0, Math.min(sideEnergy[side], sideCapacity));
            if (clamped != sideEnergy[side]) {
                sideEnergy[side] = clamped;
                changed = true;
            }
        }
        ensureFillOrderContainsStoredSides();
        if (changed) {
            onStorageChanged();
        }
    }

    private int getOutputAllowance() {
        if (level == null || level.isClientSide || transferCooldownTicks > 0) {
            return 0;
        }
        return Math.min(getEnergyStored(), OUTPUT_PER_TRANSFER);
    }

    private void restartTransferCooldown() {
        transferCooldownTicks = getEnergyStored() > 0
                ? EndlessSandsConfig.getRfTransferIntervalTicks() : 0;
    }

    private void tickTransferCooldown() {
        int clamped = Math.min(transferCooldownTicks,
                EndlessSandsConfig.getRfTransferIntervalTicks());
        int next = Math.max(0, clamped - 1);
        if (next != transferCooldownTicks) {
            transferCooldownTicks = next;
            setChanged();
        }
    }

    private void onStorageChanged() {
        if (loading) {
            return;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void restoreStorage(int[] energy, int[] order, int orderSize) {
        System.arraycopy(energy, 0, sideEnergy, 0, SIDE_COUNT);
        System.arraycopy(order, 0, fillOrder, 0, SIDE_COUNT);
        fillOrderSize = orderSize;
    }

    public void syncAfterPlacement() {
        refreshHorizontalConnections();
        onStorageChanged();
    }

    public void saveToItem(ItemStack stack) {
        BlockItem.setBlockEntityData(stack, ModBlockEntities.ZENIONITE_BATTERY.get(),
                saveWithoutMetadata());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putIntArray(SIDE_ENERGY_TAG, sideEnergy);
        tag.putIntArray(FILL_ORDER_TAG, Arrays.copyOf(fillOrder, fillOrderSize));
        tag.putInt(FILL_ORDER_SIZE_TAG, fillOrderSize);
        tag.putString(SAVED_FACING_TAG, currentFacing().getName());
        tag.putInt(NEXT_OUTPUT_TAG, nextOutputIndex);
        tag.putInt(TRANSFER_COOLDOWN_TAG, transferCooldownTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        try {
            Arrays.fill(sideEnergy, 0);
            Arrays.fill(fillOrder, -1);
            fillOrderSize = 0;

            int[] loadedEnergy = tag.contains(SIDE_ENERGY_TAG, Tag.TAG_INT_ARRAY)
                    ? tag.getIntArray(SIDE_ENERGY_TAG) : new int[0];
            int[] loadedOrder = tag.contains(FILL_ORDER_TAG, Tag.TAG_INT_ARRAY)
                    ? tag.getIntArray(FILL_ORDER_TAG) : new int[0];
            Direction savedFacing = Direction.byName(tag.getString(SAVED_FACING_TAG));
            if (savedFacing == null || !savedFacing.getAxis().isHorizontal()) {
                savedFacing = currentFacing();
            }
            int rotation = clockwiseSteps(savedFacing, currentFacing());
            int sideCapacity = getSideCapacity();
            if (tag.contains(SYNCED_SIDE_CAPACITY_TAG, Tag.TAG_INT)) {
                syncedSideCapacity = Math.max(1, tag.getInt(SYNCED_SIDE_CAPACITY_TAG));
                sideCapacity = syncedSideCapacity;
            }

            for (int oldSide = 0; oldSide < Math.min(SIDE_COUNT, loadedEnergy.length); oldSide++) {
                int newSide = rotateSideIndex(oldSide, rotation);
                sideEnergy[newSide] = Math.max(0, Math.min(loadedEnergy[oldSide], sideCapacity));
            }

            int requestedOrderSize = tag.contains(FILL_ORDER_SIZE_TAG, Tag.TAG_INT)
                    ? tag.getInt(FILL_ORDER_SIZE_TAG) : loadedOrder.length;
            for (int index = 0;
                 index < Math.min(Math.min(requestedOrderSize, loadedOrder.length), SIDE_COUNT);
                 index++) {
                int oldSide = loadedOrder[index];
                if (oldSide < 0 || oldSide >= SIDE_COUNT) {
                    continue;
                }
                int newSide = rotateSideIndex(oldSide, rotation);
                if (sideEnergy[newSide] > 0 && !containsFillSide(newSide)) {
                    fillOrder[fillOrderSize++] = newSide;
                }
            }
            ensureFillOrderContainsStoredSides();
            nextOutputIndex = Math.floorMod(tag.getInt(NEXT_OUTPUT_TAG), OUTPUT_DIRECTIONS.length);
            int defaultCooldown = getEnergyStored() > 0
                    ? EndlessSandsConfig.getRfTransferIntervalTicks() : 0;
            transferCooldownTicks = tag.contains(TRANSFER_COOLDOWN_TAG)
                    ? Math.max(0, Math.min(tag.getInt(TRANSFER_COOLDOWN_TAG),
                    EndlessSandsConfig.getRfTransferIntervalTicks()))
                    : defaultCooldown;
        } finally {
            loading = false;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.putInt(SYNCED_SIDE_CAPACITY_TAG, getSideCapacity());
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(ModBlocks.ZENIONITE_BATTERY.get().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ZenioniteBatteryMenu(containerId, inventory, this, data);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ENERGY) {
            if (side == Direction.DOWN) {
                return inputCapability.cast();
            }
            if (side == Direction.UP || side != null && side.getAxis().isHorizontal()) {
                return outputCapability.cast();
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCapability.invalidate();
        outputCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inputCapability = LazyOptional.of(() -> bottomEnergyInput);
        outputCapability = LazyOptional.of(() -> outwardEnergyOutput);
    }

    private Direction currentFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(ZenioniteBatteryBlock.FACING)
                ? state.getValue(ZenioniteBatteryBlock.FACING) : Direction.NORTH;
    }

    private static int clockwiseSteps(Direction from, Direction to) {
        Direction rotated = from;
        for (int steps = 0; steps < SIDE_COUNT; steps++) {
            if (rotated == to) {
                return steps;
            }
            rotated = rotated.getClockWise();
        }
        return 0;
    }

    private static int rotateSideIndex(int side, int clockwiseSteps) {
        Direction direction = directionFromIndex(side);
        for (int step = 0; step < clockwiseSteps; step++) {
            direction = direction.getClockWise();
        }
        return directionIndex(direction);
    }

    public static int directionIndex(Direction direction) {
        return switch (direction) {
            case NORTH -> DATA_NORTH;
            case EAST -> DATA_EAST;
            case SOUTH -> DATA_SOUTH;
            case WEST -> DATA_WEST;
            default -> throw new IllegalArgumentException("Battery energy direction must be horizontal");
        };
    }

    public static Direction directionFromIndex(int index) {
        return HORIZONTAL_DIRECTIONS[Math.floorMod(index, SIDE_COUNT)];
    }

    public static int lowWord(int value) {
        return value & 0xFFFF;
    }

    public static int highWord(int value) {
        return value >>> 16 & 0xFFFF;
    }

    public static int combineWords(int low, int high) {
        return (high & 0xFFFF) << 16 | low & 0xFFFF;
    }

    private static int withLowWord(int current, int low) {
        return current & 0xFFFF0000 | low & 0xFFFF;
    }

    private static int withHighWord(int current, int high) {
        return current & 0xFFFF | (high & 0xFFFF) << 16;
    }

    private final class EnergyInputHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return receiveEnergyInternal(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return ZenioniteBatteryBlockEntity.this.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return getEnergyCapacity();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private final class EnergyOutputHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return extractEnergyInternal(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return ZenioniteBatteryBlockEntity.this.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return getEnergyCapacity();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
