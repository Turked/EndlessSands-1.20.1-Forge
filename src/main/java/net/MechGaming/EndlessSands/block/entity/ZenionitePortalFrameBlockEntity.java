package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenionitePortalBlock;
import net.MechGaming.EndlessSands.block.custom.ZenionitePortalFrameBlock;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZenionitePortalFrameBlockEntity extends BlockEntity {
    public static final int ENERGY_CAPACITY = 3;
    public static final int OUTPUT_PER_TRANSFER = 1;
    public static final int LOSS_INTERVAL_MULTIPLIER = 100;
    public static final int MINIMUM_PORTAL_FRAMES = 10;
    private static final int LAYOUT_REFRESH_TICKS = 10;
    // Geometry is built once, not dozens of times per frame per server tick.
    private static final List<PortalLayout> LAYOUT_TEMPLATES = List.of(
            createLayout(BlockPos.ZERO, Direction.Axis.Z, false),
            createLayout(BlockPos.ZERO, Direction.Axis.X, false),
            createLayout(BlockPos.ZERO, Direction.Axis.Z, true),
            createLayout(BlockPos.ZERO, Direction.Axis.X, true));

    private static final String ENERGY_TAG = "Energy";
    private static final String TRANSFER_COOLDOWN_TAG = "TransferCooldown";
    private static final String LOSS_COOLDOWN_TAG = "LossCooldown";
    private static final String PHARAOH_GATE_TAG = "PharaohGate";

    private final Map<Direction, IEnergyStorage> sidedEnergyHandlers =
            new EnumMap<>(Direction.class);
    private final Map<Direction, LazyOptional<IEnergyStorage>> sidedEnergyCapabilities =
            new EnumMap<>(Direction.class);
    private final IEnergyStorage unsidedEnergyHandler = new EnergyHandler(null);
    private LazyOptional<IEnergyStorage> unsidedEnergyCapability =
            LazyOptional.of(() -> unsidedEnergyHandler);

    private int energyStored;
    private int transferCooldownTicks;
    private int lossCooldownTicks;
    private boolean pharaohGate;
    private boolean loading;
    @Nullable
    private PortalLayout cachedLayout;
    private int layoutRefreshTicks;

    public ZenionitePortalFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZENIONITE_PORTAL_FRAME.get(), pos, state);
        createSidedCapabilities();
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ZenionitePortalFrameBlockEntity frame
    ) {
        if (frame.layoutRefreshTicks-- <= 0) {
            frame.layoutRefreshTicks = LAYOUT_REFRESH_TICKS - 1;
            PortalLayout layout = findPortalLayout(level, pos);
            if (layout == null) {
                frame.updateConnectionState(null);
            } else if (layout.lastMaintenanceTick != level.getGameTime()) {
                layout.lastMaintenanceTick = level.getGameTime();
                updatePortalState(level, pos);
            }
        }
        frame.clampEnergyToCapacity();
        if (frame.pharaohGate) {
            frame.drainPharaohGateEnergy();
            frame.updatePoweredState();
            return;
        }
        if (frame.energyStored <= 0) {
            frame.clearCooldowns();
            frame.updatePoweredState();
            return;
        }

        frame.tickCooldowns();

        if (frame.transferCooldownTicks == 0) {
            if (frame.energyStored >= 2) {
                FrameRoute route = findFrameRoute(level, pos);
                if (route == null) {
                    frame.breakWithoutDrops();
                    return;
                }
                frame.pushEnergyAlongPortal(level, route);
            }
            frame.restartTransferCooldown();
        }

        if (!frame.isRemoved() && frame.energyStored > 0 && frame.lossCooldownTicks == 0) {
            frame.energyStored--;
            frame.restartLossCooldown();
            frame.onStorageChanged();
        }
    }

    public int getEnergyCapacity() {
        return ENERGY_CAPACITY * EndlessSandsConfig.getRfMultiplier();
    }

    public int getEnergyStored() {
        return Math.min(energyStored, getEnergyCapacity());
    }

    public void fillEnergyToCapacity() {
        if (pharaohGate) {
            return;
        }
        energyStored = getEnergyCapacity();
        restartTransferCooldown();
        restartLossCooldown();
        onStorageChanged();
    }

    public int getTransferCooldownTicks() {
        return transferCooldownTicks;
    }

    public int getLossCooldownTicks() {
        return lossCooldownTicks;
    }

    public boolean isPharaohGate() {
        return pharaohGate;
    }

    public static boolean isValidCircuit(Level level, BlockPos origin) {
        return findFrameRoute(level, origin) != null;
    }

    public static boolean isBatteryInValidCircuit(Level level, BlockPos batteryPosition) {
        if (!level.getBlockState(batteryPosition).is(ModBlocks.ZENIONITE_BATTERY.get())) {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos framePosition = batteryPosition.relative(direction);
            PortalLayout layout = findPortalLayout(level, framePosition);
            FrameRoute route = layout == null ? null : layout.frames().get(framePosition);
            if (route != null && !layout.isPharaohGate(level)
                    && route.acceptsBatteryInput(direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static PortalConnection getActivePortalForBattery(
            Level level,
            BlockPos batteryPosition
    ) {
        if (!level.getBlockState(batteryPosition).is(ModBlocks.ZENIONITE_BATTERY.get())) {
            return null;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            PortalLayout layout = findPortalLayout(level, batteryPosition.relative(direction));
            if (layout == null
                    || !layout.containsBattery(batteryPosition)
                    || layout.isPharaohGate(level)
                    || !layout.isReadyToOpen(level)
                    || !layout.hasOpenPortal(level)) {
                continue;
            }

            BlockPos oppositeBattery = layout.oppositeBattery(batteryPosition);
            if (oppositeBattery != null) {
                return new PortalConnection(
                        layout.center(),
                        batteryPosition.immutable(),
                        oppositeBattery
                );
            }
        }
        return null;
    }

    public static boolean completePortalWithPharaoh(Level level, BlockPos portalCenter) {
        if (level.isClientSide) {
            return false;
        }
        PortalLayout layout = findPortalLayoutAtCenter(level, portalCenter);
        if (layout == null || !layout.hasOpenPortal(level)) {
            return false;
        }
        layout.completeWithPharaoh(level);
        return true;
    }

    public static boolean isPharaohGateForBattery(Level level, BlockPos batteryPosition) {
        if (!level.getBlockState(batteryPosition).is(ModBlocks.ZENIONITE_BATTERY.get())) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            PortalLayout layout = findPortalLayout(level, batteryPosition.relative(direction));
            if (layout != null && layout.containsBattery(batteryPosition)
                    && layout.isPharaohGate(level)) {
                return true;
            }
        }
        return false;
    }

    public static void setPortalTransferActive(Level level, BlockPos portalCenter, boolean transferActive) {
        PortalLayout layout = findPortalLayoutAtCenter(level, portalCenter);
        if (layout == null) return;
        layout.forEachPortalPosition(position -> {
            BlockState state = level.getBlockState(position);
            if (state.is(ModBlocks.ZENIONITE_PORTAL.get())
                    && state.getValue(ZenionitePortalBlock.TRANSFER_ACTIVE) != transferActive) {
                level.setBlock(position, state.setValue(ZenionitePortalBlock.TRANSFER_ACTIVE, transferActive),
                        Block.UPDATE_CLIENTS);
            }
        });
    }

    public static void updatePortalState(Level level, BlockPos framePosition) {
        if (level.isClientSide) return;
        PortalLayout layout = findPortalLayout(level, framePosition);
        if (layout == null) return;
        layout.frames().forEach((position, route) -> {
            if (level.getBlockEntity(position) instanceof ZenionitePortalFrameBlockEntity frame) {
                frame.updateConnectionState(route);
            }
        });
        if (layout.isReadyToOpen(level)) {
            layout.openPortal(level);
        } else {
            layout.closePortal(level);
        }
    }

    public static boolean isPortalInteriorActive(Level level, BlockPos portalPosition) {
        for (PortalLayout template : LAYOUT_TEMPLATES) {
            for (BlockPos interiorOffset : template.interior) {
                BlockPos center = portalPosition.subtract(interiorOffset);
                PortalLayout layout = findMatchingLayout(level, center, template);
                if (layout != null && layout.isReadyToOpen(level)) return true;
            }
        }
        return false;
    }

    @Nullable
    private static FrameRoute findFrameRoute(Level level, BlockPos origin) {
        PortalLayout layout = findPortalLayout(level, origin);
        return layout == null ? null : layout.frames().get(origin);
    }

    @Nullable
    private static PortalLayout findPortalLayout(Level level, BlockPos origin) {
        if (!level.hasChunkAt(origin)) return null;
        if (!(level.getBlockEntity(origin) instanceof ZenionitePortalFrameBlockEntity frame)) return null;
        if (frame.cachedLayout != null) {
            if (frame.cachedLayout.matches(level)) return frame.cachedLayout;
            frame.cachedLayout = null;
        }
        for (PortalLayout template : LAYOUT_TEMPLATES) {
            for (BlockPos frameOffset : template.frames().keySet()) {
                BlockPos center = origin.subtract(frameOffset);
                PortalLayout layout = findMatchingLayout(level, center, template);
                if (layout != null) return layout;
            }
        }
        return null;
    }

    @Nullable
    private static PortalLayout findPortalLayoutAtCenter(Level level, BlockPos center) {
        for (PortalLayout template : LAYOUT_TEMPLATES) {
            PortalLayout layout = findMatchingLayout(level, center, template);
            if (layout != null) return layout;
        }
        return null;
    }

    @Nullable
    private static PortalLayout findMatchingLayout(Level level, BlockPos center, PortalLayout template) {
        // Reject candidates using just two batteries before reading any frame blocks.
        if (!template.matchesAt(level, center)) return null;
        BlockPos firstFrame = center.offset(template.frames().keySet().iterator().next());
        if (level.getBlockEntity(firstFrame) instanceof ZenionitePortalFrameBlockEntity frame
                && frame.cachedLayout != null && frame.cachedLayout.center().equals(center)
                && frame.cachedLayout.large == template.large
                && frame.cachedLayout.batteryAxis == template.batteryAxis) {
            return frame.cachedLayout;
        }
        PortalLayout layout = createLayout(center, template.batteryAxis, template.large);
        for (BlockPos position : layout.frames().keySet()) {
            if (level.getBlockEntity(position) instanceof ZenionitePortalFrameBlockEntity frame) {
                frame.cachedLayout = layout;
            }
        }
        return layout;
    }

    private void updateConnectionState(@Nullable FrameRoute route) {
        if (level == null || level.isClientSide || isRemoved()) return;
        BlockState current = getBlockState();
        BlockState updated = current;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            ZenionitePortalFrameBlock.Port port = ZenionitePortalFrameBlock.Port.NONE;
            if (route != null) {
                if (side == route.firstInputSide() || side == route.secondInputSide()) {
                    port = ZenionitePortalFrameBlock.Port.INPUT;
                } else if (side == route.outputSide()) {
                    port = ZenionitePortalFrameBlock.Port.OUTPUT;
                }
            }
            updated = updated.setValue(ZenionitePortalFrameBlock.portProperty(side), port);
        }
        if (updated != current) level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
    }

    private int receiveEnergyInternal(int maxReceive, boolean simulate, @Nullable Direction side) {
        if (pharaohGate) {
            return 0;
        }
        int received = Math.min(
                Math.max(0, maxReceive),
                Math.max(0, getEnergyCapacity() - energyStored)
        );
        if (simulate || received <= 0) {
            return received;
        }
        if (level == null || level.isClientSide) {
            breakWithoutDrops();
            return 0;
        }

        FrameRoute route = findFrameRoute(level, worldPosition);
        if (route == null) {
            breakWithoutDrops();
            return 0;
        }
        if (!route.acceptsBatteryInput(side)) {
            return 0;
        }

        return storeEnergy(received);
    }

    private int receiveRoutedEnergy(int maxReceive, boolean simulate, Direction side) {
        if (pharaohGate) {
            return 0;
        }
        int received = Math.min(
                Math.max(0, maxReceive),
                Math.max(0, getEnergyCapacity() - energyStored)
        );
        if (simulate || received <= 0) {
            return received;
        }
        if (level == null || level.isClientSide) {
            return 0;
        }

        FrameRoute route = findFrameRoute(level, worldPosition);
        if (route == null) {
            breakWithoutDrops();
            return 0;
        }
        if (!route.acceptsRoutedInput(side)) {
            return 0;
        }

        return storeEnergy(received);
    }

    private int storeEnergy(int received) {
        boolean wasEmpty = energyStored == 0;
        energyStored += received;
        if (wasEmpty) {
            restartTransferCooldown();
            restartLossCooldown();
        }
        onStorageChanged();
        return received;
    }

    private void pushEnergyAlongPortal(Level level, FrameRoute route) {
        if (route.outputPosition() == null || route.outputInputSide() == null) {
            return;
        }

        BlockEntity targetBlockEntity = level.getBlockEntity(route.outputPosition());
        if (!(targetBlockEntity instanceof ZenionitePortalFrameBlockEntity target)) {
            return;
        }
        if (target.receiveRoutedEnergy(OUTPUT_PER_TRANSFER, true,
                route.outputInputSide()) <= 0) {
            return;
        }

        int accepted = Math.min(
                OUTPUT_PER_TRANSFER,
                Math.max(0, target.receiveRoutedEnergy(
                        OUTPUT_PER_TRANSFER, false, route.outputInputSide()))
        );
        if (accepted > 0) {
            energyStored -= accepted;
            onStorageChanged();
        }
    }

    private static PortalLayout createLayout(BlockPos center, Direction.Axis batteryAxis, boolean large) {
        Direction[] batterySides = batteryAxis == Direction.Axis.Z
                ? new Direction[]{Direction.NORTH, Direction.SOUTH}
                : new Direction[]{Direction.WEST, Direction.EAST};
        Direction[] crossSides = batteryAxis == Direction.Axis.Z
                ? new Direction[]{Direction.WEST, Direction.EAST}
                : new Direction[]{Direction.NORTH, Direction.SOUTH};
        int radius = large ? 3 : 2;
        Map<BlockPos, FrameRoute> frames = new HashMap<>();
        BlockPos[] batteries = new BlockPos[2];
        Set<BlockPos> boundary = new LinkedHashSet<>();
        for (int i = 0; i < batterySides.length; i++) {
            Direction outward = batterySides[i];
            Direction inward = outward.getOpposite();
            BlockPos battery = center.relative(outward, radius).immutable();
            batteries[i] = battery;
            for (Direction cross : crossSides) {
                BlockPos first = battery.relative(cross).immutable();
                if (large) {
                    BlockPos second = first.relative(inward).immutable();
                    BlockPos corner = second.relative(cross);
                    BlockPos third = corner.relative(inward).immutable();
                    BlockPos fourth = third.relative(cross).immutable();
                    BlockPos sink = center.relative(cross, radius).immutable();
                    frames.put(first, new FrameRoute(FrameRole.BATTERY_INPUT,
                            cross.getOpposite(), null, second, outward, inward));
                    frames.put(second, new FrameRoute(FrameRole.RELAY,
                            outward, null, third, outward, cross));
                    frames.put(third, new FrameRoute(FrameRole.RELAY,
                            outward, null, fourth, cross.getOpposite(), cross));
                    frames.put(fourth, new FrameRoute(FrameRole.RELAY,
                            cross.getOpposite(), null, sink, outward, inward));
                    boundary.add(corner);
                } else {
                    BlockPos corner = first.relative(cross);
                    BlockPos second = corner.relative(inward).immutable();
                    BlockPos sink = second.relative(inward).immutable();
                    frames.put(first, new FrameRoute(FrameRole.BATTERY_INPUT,
                            cross.getOpposite(), null, second, outward, cross));
                    frames.put(second, new FrameRoute(FrameRole.RELAY,
                            outward, null, sink, outward, inward));
                    boundary.add(corner);
                }
            }
        }
        for (Direction cross : crossSides) {
            frames.put(center.relative(cross, radius).immutable(), new FrameRoute(
                    FrameRole.SINK, batterySides[0], batterySides[1], null, null, null));
        }
        boundary.addAll(frames.keySet());
        boundary.addAll(List.of(batteries));
        List<BlockPos> interior = new ArrayList<>();
        for (int x = 1 - radius; x < radius; x++) {
            for (int z = 1 - radius; z < radius; z++) {
                if (!large || Math.abs(x) + Math.abs(z) <= 2) {
                    interior.add(center.offset(x, 0, z).immutable());
                }
            }
        }
        return new PortalLayout(center.immutable(), Map.copyOf(frames), batteries,
                List.copyOf(interior), Set.copyOf(boundary), batteryAxis, large);
    }

    private void tickCooldowns() {
        int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
        int lossInterval = lossIntervalTicks();
        int nextTransfer = Math.max(0, Math.min(transferCooldownTicks, transferInterval) - 1);
        int nextLoss = Math.max(0, Math.min(lossCooldownTicks, lossInterval) - 1);
        if (nextTransfer != transferCooldownTicks || nextLoss != lossCooldownTicks) {
            transferCooldownTicks = nextTransfer;
            lossCooldownTicks = nextLoss;
            setChanged();
        }
    }

    private void restartTransferCooldown() {
        transferCooldownTicks = energyStored > 0
                ? EndlessSandsConfig.getRfTransferIntervalTicks() : 0;
        setChanged();
    }

    private void restartLossCooldown() {
        lossCooldownTicks = energyStored > 0 ? lossIntervalTicks() : 0;
        setChanged();
    }

    private static int lossIntervalTicks() {
        long interval = (long) EndlessSandsConfig.getRfTransferIntervalTicks()
                * LOSS_INTERVAL_MULTIPLIER;
        return (int) Math.min(Integer.MAX_VALUE, interval);
    }

    private void clearCooldowns() {
        if (transferCooldownTicks != 0 || lossCooldownTicks != 0) {
            transferCooldownTicks = 0;
            lossCooldownTicks = 0;
            setChanged();
        }
    }

    private void beginPharaohGateDrain() {
        if (pharaohGate) {
            return;
        }
        pharaohGate = true;
        transferCooldownTicks = 0;
        lossCooldownTicks = 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void drainPharaohGateEnergy() {
        if (energyStored <= 0) {
            clearCooldowns();
            return;
        }
        energyStored = Math.max(0,
                energyStored - Math.max(1, EndlessSandsConfig.getRfMultiplier()));
        onStorageChanged();
    }

    private void clampEnergyToCapacity() {
        int clamped = Math.max(0, Math.min(energyStored, getEnergyCapacity()));
        if (clamped != energyStored) {
            energyStored = clamped;
            onStorageChanged();
        }
    }

    private void onStorageChanged() {
        if (loading) {
            return;
        }
        setChanged();
        updatePoweredState();
        if (level != null && !level.isClientSide) {
            updatePortalState(level, worldPosition);
        }
    }

    private void updatePoweredState() {
        if (level == null || level.isClientSide || isRemoved()) {
            return;
        }
        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof ZenionitePortalFrameBlock)
                || !current.hasProperty(ZenionitePortalFrameBlock.POWERED)) {
            return;
        }
        boolean powered = energyStored > 0;
        if (current.getValue(ZenionitePortalFrameBlock.POWERED) != powered) {
            level.setBlock(
                    worldPosition,
                    current.setValue(ZenionitePortalFrameBlock.POWERED, powered),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private void breakWithoutDrops() {
        if (level != null && !level.isClientSide && !isRemoved()
                && level.getBlockState(worldPosition).is(ModBlocks.ZENIONITE_PORTAL_FRAME.get())) {
            level.destroyBlock(worldPosition, false);
        }
    }

    private void createSidedCapabilities() {
        sidedEnergyHandlers.clear();
        sidedEnergyCapabilities.clear();
        for (Direction direction : Direction.values()) {
            IEnergyStorage handler = new EnergyHandler(direction);
            sidedEnergyHandlers.put(direction, handler);
            sidedEnergyCapabilities.put(direction, LazyOptional.of(() -> handler));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updatePoweredState();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(ENERGY_TAG, energyStored);
        tag.putInt(TRANSFER_COOLDOWN_TAG, transferCooldownTicks);
        tag.putInt(LOSS_COOLDOWN_TAG, lossCooldownTicks);
        tag.putBoolean(PHARAOH_GATE_TAG, pharaohGate);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        try {
            energyStored = Math.max(0, Math.min(tag.getInt(ENERGY_TAG), getEnergyCapacity()));
            int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
            int lossInterval = lossIntervalTicks();
            transferCooldownTicks = tag.contains(TRANSFER_COOLDOWN_TAG, Tag.TAG_INT)
                    ? Math.max(0, Math.min(tag.getInt(TRANSFER_COOLDOWN_TAG), transferInterval))
                    : energyStored > 0 ? transferInterval : 0;
            lossCooldownTicks = tag.contains(LOSS_COOLDOWN_TAG, Tag.TAG_INT)
                    ? Math.max(0, Math.min(tag.getInt(LOSS_COOLDOWN_TAG), lossInterval))
                    : energyStored > 0 ? lossInterval : 0;
            pharaohGate = tag.getBoolean(PHARAOH_GATE_TAG);
        } finally {
            loading = false;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
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
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ENERGY) {
            if (side == null) {
                return unsidedEnergyCapability.cast();
            }
            LazyOptional<IEnergyStorage> sided = sidedEnergyCapabilities.get(side);
            if (sided != null) {
                return sided.cast();
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        unsidedEnergyCapability.invalidate();
        sidedEnergyCapabilities.values().forEach(LazyOptional::invalidate);
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        unsidedEnergyCapability = LazyOptional.of(() -> unsidedEnergyHandler);
        createSidedCapabilities();
    }

    private final class EnergyHandler implements IEnergyStorage {
        @Nullable
        private final Direction side;

        private EnergyHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return receiveEnergyInternal(maxReceive, simulate, side);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return ZenionitePortalFrameBlockEntity.this.getEnergyStored();
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
            return !pharaohGate;
        }
    }

    private enum FrameRole {
        BATTERY_INPUT,
        RELAY,
        SINK
    }

    private record FrameRoute(
            FrameRole role,
            Direction firstInputSide,
            @Nullable Direction secondInputSide,
            @Nullable BlockPos outputPosition,
            @Nullable Direction outputInputSide,
            @Nullable Direction outputSide
    ) {
        private boolean acceptsBatteryInput(@Nullable Direction side) {
            return role == FrameRole.BATTERY_INPUT && side == firstInputSide;
        }

        private boolean acceptsRoutedInput(Direction side) {
            return role != FrameRole.BATTERY_INPUT
                    && (side == firstInputSide || side == secondInputSide);
        }
    }

    public record PortalConnection(
            BlockPos center,
            BlockPos sourceBattery,
            BlockPos oppositeBattery
    ) {
    }

    private static final class PortalLayout {
        private final BlockPos center;
        private final Map<BlockPos, FrameRoute> frames;
        private final BlockPos[] batteries;
        private final List<BlockPos> interior;
        private final Set<BlockPos> boundary;
        private final Direction.Axis batteryAxis;
        private final boolean large;
        private long lastMaintenanceTick = Long.MIN_VALUE;

        private PortalLayout(BlockPos center, Map<BlockPos, FrameRoute> frames, BlockPos[] batteries,
                             List<BlockPos> interior, Set<BlockPos> boundary,
                             Direction.Axis batteryAxis, boolean large) {
            this.center = center;
            this.frames = frames;
            this.batteries = batteries;
            this.interior = interior;
            this.boundary = boundary;
            this.batteryAxis = batteryAxis;
            this.large = large;
        }

        private BlockPos center() { return center; }
        private Map<BlockPos, FrameRoute> frames() { return frames; }

        private boolean matches(Level level) {
            return matchesAt(level, BlockPos.ZERO);
        }

        // Templates have relative positions; instantiated layouts pass a zero translation.
        private boolean matchesAt(Level level, BlockPos translation) {
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (BlockPos battery : batteries) {
                cursor.setWithOffset(battery, translation);
                if (!level.hasChunkAt(cursor)
                        || !level.getBlockState(cursor).is(ModBlocks.ZENIONITE_BATTERY.get())) return false;
            }
            for (BlockPos frame : frames.keySet()) {
                cursor.setWithOffset(frame, translation);
                if (!level.hasChunkAt(cursor)
                        || !level.getBlockState(cursor).is(ModBlocks.ZENIONITE_PORTAL_FRAME.get())) return false;
            }
            return true;
        }

        private boolean isReadyToOpen(Level level) {
            boolean completed = isPharaohGate(level);
            for (BlockPos framePosition : frames.keySet()) {
                BlockState state = level.getBlockState(framePosition);
                if ((!completed && (!state.hasProperty(ZenionitePortalFrameBlock.BEDROCK)
                        || !state.getValue(ZenionitePortalFrameBlock.BEDROCK)))
                        || !(level.getBlockEntity(framePosition)
                        instanceof ZenionitePortalFrameBlockEntity frame)
                        || frame.getEnergyStored() <= 0) {
                    return false;
                }
            }
            return true;
        }

        private boolean containsBattery(BlockPos position) {
            for (BlockPos battery : batteries) {
                if (battery.equals(position)) {
                    return true;
                }
            }
            return false;
        }

        @Nullable
        private BlockPos oppositeBattery(BlockPos position) {
            for (BlockPos battery : batteries) {
                if (!battery.equals(position)) {
                    return battery.immutable();
                }
            }
            return null;
        }

        private boolean hasOpenPortal(Level level) {
            boolean hasPortal = false;
            for (BlockPos position : interior) {
                BlockState state = level.getBlockState(position);
                if (state.is(ModBlocks.ZENIONITE_PORTAL.get())) {
                    hasPortal = true;
                } else if (state.isAir()) {
                    return false;
                }
            }
            // Occupied cells, including the central L, are deliberately not replaced.
            return hasPortal;
        }

        private boolean isPharaohGate(Level level) {
            for (BlockPos framePosition : frames.keySet()) {
                if (level.getBlockEntity(framePosition)
                        instanceof ZenionitePortalFrameBlockEntity frame
                        && frame.isPharaohGate()) {
                    return true;
                }
            }
            return false;
        }

        private void completeWithPharaoh(Level level) {
            for (BlockPos framePosition : frames.keySet()) {
                if (level.getBlockEntity(framePosition)
                        instanceof ZenionitePortalFrameBlockEntity frame) {
                    frame.beginPharaohGateDrain();
                }
            }
            for (BlockPos batteryPosition : batteries) {
                if (level.getBlockEntity(batteryPosition)
                        instanceof ZenioniteBatteryBlockEntity battery) {
                    battery.beginPharaohGateDrain();
                }
            }
            lockGateChargers(level);

            for (BlockPos framePosition : frames.keySet()) {
                BlockState state = level.getBlockState(framePosition);
                if (state.hasProperty(ZenionitePortalFrameBlock.BEDROCK)
                        && state.getValue(ZenionitePortalFrameBlock.BEDROCK)) {
                    level.setBlock(framePosition,
                            state.setValue(ZenionitePortalFrameBlock.BEDROCK, false),
                            Block.UPDATE_ALL);
                }
            }
            forEachPortalPosition(position -> {
                BlockState state = level.getBlockState(position);
                if (state.is(ModBlocks.ZENIONITE_PORTAL.get())) {
                    level.setBlock(position,
                            state.setValue(ZenionitePortalBlock.PHARAOH_IMPRISONED, true),
                            Block.UPDATE_ALL);
                }
            });
        }

        private void lockGateChargers(Level level) {
            for (BlockPos top : boundary) {
                lockCharger(level, top);
                BlockPos below = top.below();
                while (below.getY() >= level.getMinBuildHeight()
                        && (level.getBlockState(below).is(ModBlocks.ZENIONITE_CHARGER.get())
                        || level.getBlockState(below).is(ModBlocks.CREATIVE_ZENIONITE_CHARGER.get()))) {
                    lockCharger(level, below);
                    below = below.below();
                }
            }
        }

        private void lockCharger(Level level, BlockPos position) {
            if (level.getBlockEntity(position)
                    instanceof ZenioniteChargerBlockEntity charger) {
                charger.beginPharaohGateDrain();
            }
        }

        private void openPortal(Level level) {
            boolean completed = isPharaohGate(level);
            boolean transferActive = interior.stream().map(level::getBlockState)
                    .anyMatch(state -> state.is(ModBlocks.ZENIONITE_PORTAL.get())
                            && state.getValue(ZenionitePortalBlock.TRANSFER_ACTIVE));
            forEachPortalPosition(position -> {
                BlockState current = level.getBlockState(position);
                if (current.isAir()) {
                    level.setBlock(position,
                            ModBlocks.ZENIONITE_PORTAL.get().defaultBlockState()
                                    .setValue(ZenionitePortalBlock.TRANSFER_ACTIVE, transferActive)
                                    .setValue(ZenionitePortalBlock.PHARAOH_IMPRISONED, completed),
                            Block.UPDATE_ALL);
                } else if (current.is(ModBlocks.ZENIONITE_PORTAL.get())
                        && current.getValue(ZenionitePortalBlock.PHARAOH_IMPRISONED)
                        != completed) {
                    level.setBlock(position,
                            current.setValue(ZenionitePortalBlock.PHARAOH_IMPRISONED, completed),
                            Block.UPDATE_ALL);
                }
            });
        }

        private void closePortal(Level level) {
            forEachPortalPosition(position -> {
                if (level.getBlockState(position).is(ModBlocks.ZENIONITE_PORTAL.get())) {
                    level.setBlock(position, net.minecraft.world.level.block.Blocks.AIR
                            .defaultBlockState(), Block.UPDATE_ALL);
                }
            });
        }

        private void forEachPortalPosition(java.util.function.Consumer<BlockPos> action) {
            interior.forEach(action);
        }
    }
}
