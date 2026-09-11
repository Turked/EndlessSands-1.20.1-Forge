package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.block.custom.ZenioniteStairBlock;
import net.MechGaming.EndlessSands.mixin.FlowingFluidAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public class ZenioniteStairBlockEntity extends BlockEntity {
    public enum Lane {
        LEFT,
        RIGHT
    }

    public record FlowContext(
            BlockPos pos,
            Lane lane,
            Direction worldLaneSide,
            FluidState fluidState
    ) {
    }

    private static final String LEFT_FLUID_TAG = "LeftFluid";
    private static final String RIGHT_FLUID_TAG = "RightFluid";
    private static final ThreadLocal<FlowContext> ACTIVE_FLOW = new ThreadLocal<>();

    private int leftFluid;
    private int rightFluid;
    private int leftTickCountdown;
    private int rightTickCountdown;
    private boolean loading;

    public ZenioniteStairBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZENIONITE_STAIRS.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ZenioniteStairBlockEntity stair
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        stair.tickLane(serverLevel, pos, state, Lane.LEFT);
        stair.tickLane(serverLevel, pos, state, Lane.RIGHT);
    }

    private void tickLane(ServerLevel level, BlockPos pos, BlockState state, Lane lane) {
        FluidState stored = getFluid(lane);
        if (stored.isEmpty()) {
            setCountdown(lane, 0);
            return;
        }

        int countdown = getCountdown(lane);
        if (countdown < 0) {
            countdown = tickCountdownFor(stored);
            setCountdown(lane, countdown);
        }
        if (countdown > 0) {
            setCountdown(lane, countdown - 1);
            return;
        }

        FlowingFluid fluid = (FlowingFluid) stored.getType();
        FlowingFluidAccessor accessor = (FlowingFluidAccessor) fluid;
        FluidState active = stored;
        if (!stored.isSource()) {
            FluidState next = getNewLaneLiquid(level, pos, state, lane, fluid, accessor);
            if (next.isEmpty()) {
                setFluid(lane, next);
                setCountdown(lane, 0);
                return;
            }
            if (!next.equals(stored)) {
                setFluid(lane, next);
                active = next;
            }
        }

        Direction laneSide = ZenioniteStairBlock.worldSideForLane(state, lane);
        FluidState spreadState = active;
        runWithFlowContext(new FlowContext(pos.immutable(), lane, laneSide, spreadState),
                () -> accessor.endlessSands$spread(level, pos, spreadState));
        setCountdown(lane, Math.max(1, active.getType().getTickDelay(level)) - 1);
    }

    private FluidState getNewLaneLiquid(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            Lane lane,
            FlowingFluid fluid,
            FlowingFluidAccessor accessor
    ) {
        int strongestAmount = 0;
        int sourceNeighbors = 0;
        Lane oppositeLane = lane == Lane.LEFT ? Lane.RIGHT : Lane.LEFT;
        Direction forbiddenSide = ZenioniteStairBlock.worldSideForLane(state, oppositeLane);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction == forbiddenSide) {
                continue;
            }
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!accessor.endlessSands$canPassThroughWall(
                    direction, level, pos, state, neighborPos, neighborState)) {
                continue;
            }

            FluidState neighborFluid = connectedFluidForLane(
                    level, neighborPos, neighborState, direction.getOpposite(),
                    state, lane, fluid);
            if (!neighborFluid.getType().isSame(fluid)) {
                continue;
            }
            if (neighborFluid.isSource()
                    && ForgeEventFactory.canCreateFluidSource(
                    level, neighborPos, neighborState,
                    neighborFluid.canConvertToSource(level, neighborPos))) {
                sourceNeighbors++;
            }
            strongestAmount = Math.max(strongestAmount, neighborFluid.getAmount());
        }

        if (sourceNeighbors >= 2 && hasSourceSupport(level, pos, state, lane, fluid)) {
            return fluid.getSource(false);
        }

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        FluidState aboveFluid = connectedFluidForLane(
                level, abovePos, aboveState, Direction.DOWN, state, lane, fluid);
        if (aboveFluid.getType().isSame(fluid)
                && accessor.endlessSands$canPassThroughWall(
                Direction.UP, level, pos, state, abovePos, aboveState)) {
            return fluid.getFlowing(8, true);
        }

        int amount = strongestAmount - accessor.endlessSands$getDropOff(level);
        return amount <= 0
                ? Fluids.EMPTY.defaultFluidState()
                : fluid.getFlowing(Mth.clamp(amount, 1, 8), false);
    }

    private FluidState connectedFluidForLane(
            ServerLevel level,
            BlockPos neighborPos,
            BlockState neighborState,
            Direction movement,
            BlockState targetState,
            Lane targetLane,
            FlowingFluid fluid
    ) {
        if (!(neighborState.getBlock() instanceof ZenioniteStairBlock)
                || !(level.getBlockEntity(neighborPos) instanceof ZenioniteStairBlockEntity neighbor)) {
            FluidState ordinaryFluid = neighborState.getFluidState();
            return ordinaryFluid.getType().isSame(fluid)
                    ? ordinaryFluid
                    : Fluids.EMPTY.defaultFluidState();
        }

        FluidState strongest = Fluids.EMPTY.defaultFluidState();
        for (Lane neighborLane : Lane.values()) {
            if (!ZenioniteStairBlock.canLaneSpreadToward(
                    neighborState, neighborLane, movement)) {
                continue;
            }
            Direction sourceWorldLaneSide = ZenioniteStairBlock.worldSideForLane(
                    neighborState, neighborLane);
            Lane mappedLane = ZenioniteStairBlock.laneForFlow(
                    targetState, movement, sourceWorldLaneSide);
            FluidState candidate = neighbor.getFluid(neighborLane);
            if (mappedLane == targetLane && candidate.getType().isSame(fluid)
                    && fluidStrength(candidate) > fluidStrength(strongest)) {
                strongest = candidate;
            }
        }
        return strongest;
    }

    private boolean hasSourceSupport(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            Lane lane,
            FlowingFluid fluid
    ) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.isSolid()) {
            return true;
        }

        Direction sourceWorldLaneSide = ZenioniteStairBlock.worldSideForLane(state, lane);
        FluidState belowFluid;
        if (belowState.getBlock() instanceof ZenioniteStairBlock
                && level.getBlockEntity(belowPos) instanceof ZenioniteStairBlockEntity below) {
            Lane belowLane = ZenioniteStairBlock.laneForFlow(
                    belowState, Direction.DOWN, sourceWorldLaneSide);
            belowFluid = below.getFluid(belowLane);
        } else {
            belowFluid = belowState.getFluidState();
        }
        return belowFluid.isSource() && belowFluid.getType().isSame(fluid);
    }

    public FluidState getFluid(Lane lane) {
        return decode(lane == Lane.LEFT ? leftFluid : rightFluid);
    }

    public boolean setFluid(Lane lane, FluidState incoming) {
        if (!incoming.isEmpty() && !ZenioniteStairBlock.isSupportedFluid(incoming.getType())) {
            return false;
        }

        int encoded = encode(incoming);
        int current = lane == Lane.LEFT ? leftFluid : rightFluid;
        if (current == encoded) {
            return false;
        }
        if (lane == Lane.LEFT) {
            leftFluid = encoded;
            leftTickCountdown = tickCountdownFor(incoming);
        } else {
            rightFluid = encoded;
            rightTickCountdown = tickCountdownFor(incoming);
        }
        onFluidsChanged();
        return true;
    }

    public boolean tryPlaceFluid(Lane lane, FluidState incoming) {
        if (!ZenioniteStairBlock.isSupportedFluid(incoming.getType())) {
            return false;
        }

        FluidState current = getFluid(lane);
        if (!current.isEmpty()) {
            if (!current.getType().isSame(incoming.getType())
                    || fluidStrength(incoming) <= fluidStrength(current)) {
                return false;
            }
        }
        return setFluid(lane, incoming);
    }

    public boolean canAccept(Lane lane, Fluid fluid) {
        if (!ZenioniteStairBlock.isSupportedFluid(fluid)) {
            return false;
        }
        FluidState current = getFluid(lane);
        return current.isEmpty() || current.getType().isSame(fluid) && !current.isSource();
    }

    public boolean canAcceptAny(Fluid fluid) {
        return canAccept(Lane.LEFT, fluid) || canAccept(Lane.RIGHT, fluid);
    }

    public int countLoggedHalves(TagKey<Fluid> tag) {
        int count = 0;
        if (getFluid(Lane.LEFT).is(tag)) {
            count++;
        }
        if (getFluid(Lane.RIGHT).is(tag)) {
            count++;
        }
        return count;
    }

    public int countLoggedHalves(Fluid fluid) {
        int count = 0;
        for (Lane lane : Lane.values()) {
            FluidState state = getFluid(lane);
            if (!state.isEmpty() && state.getType().isSame(fluid)) {
                count++;
            }
        }
        return count;
    }

    public boolean hasLava() {
        return getFluid(Lane.LEFT).is(net.minecraft.tags.FluidTags.LAVA)
                || getFluid(Lane.RIGHT).is(net.minecraft.tags.FluidTags.LAVA);
    }

    @Nullable
    public static FlowContext activeFlow() {
        return ACTIVE_FLOW.get();
    }

    public static void runWithFlowContext(FlowContext context, Runnable action) {
        FlowContext previous = ACTIVE_FLOW.get();
        ACTIVE_FLOW.set(context);
        try {
            action.run();
        } finally {
            if (previous == null) {
                ACTIVE_FLOW.remove();
            } else {
                ACTIVE_FLOW.set(previous);
            }
        }
    }

    private int getCountdown(Lane lane) {
        return lane == Lane.LEFT ? leftTickCountdown : rightTickCountdown;
    }

    private void setCountdown(Lane lane, int value) {
        if (lane == Lane.LEFT) {
            leftTickCountdown = value;
        } else {
            rightTickCountdown = value;
        }
    }

    private int tickCountdownFor(FluidState state) {
        if (state.isEmpty()) {
            return 0;
        }
        return level == null
                ? -1
                : Math.max(1, state.getType().getTickDelay(level)) - 1;
    }

    private void onFluidsChanged() {
        if (loading) {
            return;
        }
        setChanged();
        if (level == null || isRemoved()) {
            return;
        }

        BlockState state = getBlockState();
        boolean hasLava = hasLava();
        if (state.hasProperty(ZenioniteStairBlock.HAS_LAVA)
                && state.getValue(ZenioniteStairBlock.HAS_LAVA) != hasLava) {
            BlockState updated = state.setValue(ZenioniteStairBlock.HAS_LAVA, hasLava);
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
            state = updated;
        }
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(LEFT_FLUID_TAG, leftFluid);
        tag.putInt(RIGHT_FLUID_TAG, rightFluid);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        try {
            leftFluid = sanitize(tag.getInt(LEFT_FLUID_TAG));
            rightFluid = sanitize(tag.getInt(RIGHT_FLUID_TAG));
            leftTickCountdown = leftFluid == 0 ? 0 : -1;
            rightTickCountdown = rightFluid == 0 ? 0 : -1;
        } finally {
            loading = false;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
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

    private static int encode(FluidState state) {
        if (state.isEmpty()) {
            return 0;
        }

        int exactLevel;
        if (state.isSource()) {
            exactLevel = 9;
        } else if (state.hasProperty(FlowingFluid.FALLING)
                && state.getValue(FlowingFluid.FALLING)) {
            exactLevel = 0;
        } else {
            exactLevel = Mth.clamp(state.getAmount(), 1, 8);
        }
        int familyOffset;
        if (state.getType().isSame(net.MechGaming.EndlessSands.fluid.ModFluids.ANCIENT_OCEAN_WATER.get())) {
            familyOffset = 21;
        } else if (state.getType().isSame(net.MechGaming.EndlessSands.fluid.ModFluids.STAR_TOUCHED_LAVA.get())) {
            familyOffset = 31;
        } else {
            familyOffset = state.getType().isSame(Fluids.LAVA) ? 11 : 1;
        }
        return familyOffset + exactLevel;
    }

    private static FluidState decode(int encoded) {
        if (encoded <= 0 || encoded > 40) {
            return Fluids.EMPTY.defaultFluidState();
        }

        int family = (encoded - 1) / 10;
        int exactLevel = encoded - (family * 10 + 1);
        FlowingFluid source = switch (family) {
            case 1 -> Fluids.LAVA;
            case 2 -> net.MechGaming.EndlessSands.fluid.ModFluids.ANCIENT_OCEAN_WATER.get();
            case 3 -> net.MechGaming.EndlessSands.fluid.ModFluids.STAR_TOUCHED_LAVA.get();
            default -> Fluids.WATER;
        };
        FlowingFluid flowing = switch (family) {
            case 1 -> Fluids.FLOWING_LAVA;
            case 2 -> net.MechGaming.EndlessSands.fluid.ModFluids.FLOWING_ANCIENT_OCEAN_WATER.get();
            case 3 -> net.MechGaming.EndlessSands.fluid.ModFluids.FLOWING_STAR_TOUCHED_LAVA.get();
            default -> Fluids.FLOWING_WATER;
        };
        if (exactLevel == 9) {
            return source.getSource(false);
        }
        if (exactLevel == 0) {
            return flowing.getFlowing(8, true);
        }
        return flowing.getFlowing(exactLevel, false);
    }

    private static int sanitize(int encoded) {
        return encoded >= 0 && encoded <= 20 ? encoded : 0;
    }

    private static int fluidStrength(FluidState state) {
        return state.isEmpty() ? 0 : state.isSource() ? 9 : state.getAmount();
    }
}
