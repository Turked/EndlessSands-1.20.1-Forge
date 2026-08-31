package net.MechGaming.EndlessSands.block.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity.FlowContext;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity.Lane;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ZenioniteStairBlock extends StairBlock implements EntityBlock {
    public static final BooleanProperty HAS_LAVA = BooleanProperty.create("has_lava");

    public ZenioniteStairBlock(BlockBehaviour.Properties properties) {
        super(
                () -> net.MechGaming.EndlessSands.block.ModBlocks.ZENIONITE.get().defaultBlockState(),
                properties.lightLevel(state -> state.hasProperty(HAS_LAVA) && state.getValue(HAS_LAVA) ? 15 : 0)
        );
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false)
                .setValue(HAS_LAVA, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_LAVA);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(WATERLOGGED, false).setValue(HAS_LAVA, false);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        if (!isSupportedFluid(fluid)
                || !(level.getBlockEntity(pos) instanceof ZenioniteStairBlockEntity stair)) {
            return false;
        }

        FlowContext context = ZenioniteStairBlockEntity.activeFlow();
        if (context != null) {
            int deltaX = pos.getX() - context.pos().getX();
            int deltaY = pos.getY() - context.pos().getY();
            int deltaZ = pos.getZ() - context.pos().getZ();
            if (Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaZ) == 1) {
                Direction movement = Direction.fromDelta(deltaX, deltaY, deltaZ);
                if (movement != null) {
                    Lane mapped = laneForFlow(state, movement, context.worldLaneSide());
                    return stair.canAccept(mapped, fluid);
                }
            }
        }
        return stair.canAcceptAny(fluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState incoming) {
        if (!isSupportedFluid(incoming.getType())
                || !(level.getBlockEntity(pos) instanceof ZenioniteStairBlockEntity stair)) {
            return false;
        }
        if (stair.tryPlaceFluid(Lane.LEFT, incoming)) {
            return true;
        }
        return stair.tryPlaceFluid(Lane.RIGHT, incoming);
    }

    public boolean placeLiquidFromFlow(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            Direction movement,
            FluidState incoming
    ) {
        if (!isSupportedFluid(incoming.getType())
                || !(level.getBlockEntity(pos) instanceof ZenioniteStairBlockEntity stair)) {
            return false;
        }

        FlowContext context = ZenioniteStairBlockEntity.activeFlow();
        if (context != null) {
            Lane mapped = laneForFlow(state, movement, context.worldLaneSide());
            return stair.tryPlaceFluid(mapped, incoming);
        }

        Direction sourceSide = movement.getOpposite();
        Lane sideLane = laneForExactWorldSide(state, sourceSide);
        if (sideLane != null) {
            return stair.tryPlaceFluid(sideLane, incoming);
        }

        boolean placedLeft = stair.tryPlaceFluid(Lane.LEFT, incoming);
        boolean placedRight = stair.tryPlaceFluid(Lane.RIGHT, incoming);
        return placedLeft || placedRight;
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof ZenioniteStairBlockEntity stair)) {
            return ItemStack.EMPTY;
        }
        for (Lane lane : Lane.values()) {
            FluidState fluid = stair.getFluid(lane);
            if (fluid.isSource()) {
                stair.setFluid(lane, Fluids.EMPTY.defaultFluidState());
                return new ItemStack(fluid.is(FluidTags.LAVA) ? Items.LAVA_BUCKET : Items.WATER_BUCKET);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    public Optional<SoundEvent> getPickupSound(BlockState state) {
        return getPickupSound();
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        ItemStack held = player.getItemInHand(hand);
        Lane lane = laneFromHit(state, pos, hit);
        FluidState incoming = held.is(Items.WATER_BUCKET)
                ? Fluids.WATER.getSource(false)
                : held.is(Items.LAVA_BUCKET) ? Fluids.LAVA.getSource(false) : null;
        if (incoming != null) {
            if (!(level.getBlockEntity(pos) instanceof ZenioniteStairBlockEntity stair)
                    || !stair.canAccept(lane, incoming.getType())) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                if (level.dimensionType().ultraWarm() && incoming.is(FluidTags.WATER)) {
                    level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS, 0.5F,
                            2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                    for (int particle = 0; particle < 8; particle++) {
                        level.addParticle(ParticleTypes.LARGE_SMOKE,
                                pos.getX() + Math.random(),
                                pos.getY() + Math.random(),
                                pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
                    }
                } else if (stair.tryPlaceFluid(lane, incoming)) {
                    level.playSound(player, pos,
                            incoming.is(FluidTags.LAVA)
                                    ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY,
                            SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
                } else {
                    return InteractionResult.FAIL;
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, held);
                }
                player.awardStat(Stats.ITEM_USED.get(held.getItem()));
                player.setItemInHand(hand, BucketItem.getEmptySuccessItem(held, player));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.is(Items.BUCKET)
                && level.getBlockEntity(pos) instanceof ZenioniteStairBlockEntity stair) {
            FluidState stored = stair.getFluid(lane);
            if (!stored.isSource()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                ItemStack filled = new ItemStack(stored.is(FluidTags.LAVA)
                        ? Items.LAVA_BUCKET : Items.WATER_BUCKET);
                stair.setFluid(lane, Fluids.EMPTY.defaultFluidState());
                player.awardStat(Stats.ITEM_USED.get(Items.BUCKET));
                level.playSound(player, pos,
                        stored.is(FluidTags.LAVA)
                                ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, filled));
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, filled);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZenioniteStairBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide || type != ModBlockEntities.ZENIONITE_STAIRS.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                ZenioniteStairBlockEntity.serverTick(
                        tickLevel, tickPos, tickState, (ZenioniteStairBlockEntity) blockEntity);
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean moving
    ) {
        if (!state.is(newState.getBlock())) {
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    public static boolean isSupportedFluid(Fluid fluid) {
        return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
    }

    public static Direction worldSideForLane(BlockState state, Lane lane) {
        Direction facing = state.getValue(FACING);
        return lane == Lane.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
    }

    @Nullable
    public static Lane laneForExactWorldSide(BlockState state, Direction worldSide) {
        if (!worldSide.getAxis().isHorizontal()) {
            return null;
        }
        if (worldSide == worldSideForLane(state, Lane.LEFT)) {
            return Lane.LEFT;
        }
        if (worldSide == worldSideForLane(state, Lane.RIGHT)) {
            return Lane.RIGHT;
        }
        return null;
    }

    public static Lane closestLaneForWorldSide(BlockState state, Direction worldSide) {
        Lane exact = laneForExactWorldSide(state, worldSide);
        if (exact != null) {
            return exact;
        }
        Direction right = worldSideForLane(state, Lane.RIGHT);
        int dot = right.getStepX() * worldSide.getStepX() + right.getStepZ() * worldSide.getStepZ();
        return dot >= 0 ? Lane.RIGHT : Lane.LEFT;
    }

    /**
     * Maps one source lane into the physically connected half of a target stair.
     * A lateral connection always enters the half touching the source block. For
     * front/back/downward flow, the source lane's world-space side is preserved.
     */
    public static Lane laneForFlow(
            BlockState targetState,
            Direction movement,
            Direction sourceWorldLaneSide
    ) {
        Lane touchingLane = laneForExactWorldSide(targetState, movement.getOpposite());
        return touchingLane != null
                ? touchingLane
                : closestLaneForWorldSide(targetState, sourceWorldLaneSide);
    }

    public static boolean canLaneSpreadToward(
            BlockState sourceState,
            Lane sourceLane,
            Direction movement
    ) {
        if (!movement.getAxis().isHorizontal()) {
            return true;
        }
        Lane oppositeLane = sourceLane == Lane.LEFT ? Lane.RIGHT : Lane.LEFT;
        return movement != worldSideForLane(sourceState, oppositeLane);
    }

    private static Lane laneFromHit(BlockState state, BlockPos pos, BlockHitResult hit) {
        double localX = hit.getLocation().x - pos.getX() - 0.5D;
        double localZ = hit.getLocation().z - pos.getZ() - 0.5D;
        Direction right = worldSideForLane(state, Lane.RIGHT);
        double projection = localX * right.getStepX() + localZ * right.getStepZ();
        return projection >= 0.0D ? Lane.RIGHT : Lane.LEFT;
    }
}
