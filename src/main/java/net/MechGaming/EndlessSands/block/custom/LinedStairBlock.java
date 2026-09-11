package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.entity.LinedStairBlockEntity;
import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.MechGaming.EndlessSands.util.LinedStairData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class LinedStairBlock extends StairBlock implements EntityBlock {
    public static final BooleanProperty LAVA_LOGGED =
            BooleanProperty.create("endlesssands_lava_logged");
    public static final BooleanProperty ANCIENT_OCEAN_LOGGED =
            BooleanProperty.create("endlesssands_ancient_ocean_logged");
    public static final BooleanProperty STAR_TOUCHED_LOGGED =
            BooleanProperty.create("endlesssands_star_touched_logged");
    public static final IntegerProperty EXACT_FLUID_LEVEL =
            IntegerProperty.create("endlesssands_water_level", 0, 9);
    /**
     * @deprecated The stored level now applies to either water or lava. Kept as an
     * alias so existing integrations continue to compile and saved water states
     * retain their serialized property name.
     */
    @Deprecated
    public static final IntegerProperty EXACT_WATER_LEVEL = EXACT_FLUID_LEVEL;

    public LinedStairBlock(BlockBehaviour.Properties properties) {
        super(
                () -> Blocks.SANDSTONE_STAIRS.defaultBlockState(),
                properties.lightLevel(state -> state.hasProperty(LAVA_LOGGED)
                        && (state.getValue(LAVA_LOGGED) || state.getValue(STAR_TOUCHED_LOGGED)) ? 15 : 0)
        );
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false)
                .setValue(LAVA_LOGGED, false)
                .setValue(ANCIENT_OCEAN_LOGGED, false)
                .setValue(STAR_TOUCHED_LOGGED, false)
                .setValue(EXACT_FLUID_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAVA_LOGGED, ANCIENT_OCEAN_LOGGED, STAR_TOUCHED_LOGGED, EXACT_FLUID_LEVEL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : withFluidState(state, context.getLevel().getFluidState(context.getClickedPos()));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        boolean lavaLogged = state.getValue(LAVA_LOGGED);
        boolean waterlogged = state.getValue(WATERLOGGED);
        boolean ancientLogged = state.getValue(ANCIENT_OCEAN_LOGGED);
        boolean starTouchedLogged = state.getValue(STAR_TOUCHED_LOGGED);
        if (!lavaLogged && !waterlogged && !ancientLogged && !starTouchedLogged) {
            return Fluids.EMPTY.defaultFluidState();
        }

        FlowingFluid source = ancientLogged ? ModFluids.ANCIENT_OCEAN_WATER.get()
                : starTouchedLogged ? ModFluids.STAR_TOUCHED_LAVA.get()
                : lavaLogged ? Fluids.LAVA : Fluids.WATER;
        FlowingFluid flowing = ancientLogged ? ModFluids.FLOWING_ANCIENT_OCEAN_WATER.get()
                : starTouchedLogged ? ModFluids.FLOWING_STAR_TOUCHED_LAVA.get()
                : lavaLogged ? Fluids.FLOWING_LAVA : Fluids.FLOWING_WATER;
        int encodedLevel = state.getValue(EXACT_FLUID_LEVEL);
        if (encodedLevel == 9) {
            return source.getSource(false);
        }
        if (encodedLevel == 0) {
            return flowing.getFlowing(8, true);
        }
        return flowing.getFlowing(encodedLevel, false);
    }

    public static BlockState withFluidState(BlockState state, FluidState fluidState) {
        Fluid type = fluidState.getType();
        boolean ancient = isAncientOcean(type);
        boolean starTouched = isStarTouched(type);
        boolean water = !ancient && fluidState.is(FluidTags.WATER);
        boolean lava = !starTouched && fluidState.is(FluidTags.LAVA);
        if (!water && !lava && !ancient && !starTouched) {
            return state
                    .setValue(WATERLOGGED, false)
                    .setValue(LAVA_LOGGED, false)
                    .setValue(ANCIENT_OCEAN_LOGGED, false)
                    .setValue(STAR_TOUCHED_LOGGED, false)
                    .setValue(EXACT_FLUID_LEVEL, 0);
        }

        int encodedLevel;
        if (fluidState.isSource()) {
            encodedLevel = 9;
        } else if (fluidState.hasProperty(FlowingFluid.FALLING) && fluidState.getValue(FlowingFluid.FALLING)) {
            encodedLevel = 0;
        } else {
            encodedLevel = Mth.clamp(fluidState.getAmount(), 1, 8);
        }
        return state
                .setValue(WATERLOGGED, water)
                .setValue(LAVA_LOGGED, lava)
                .setValue(ANCIENT_OCEAN_LOGGED, ancient)
                .setValue(STAR_TOUCHED_LOGGED, starTouched)
                .setValue(EXACT_FLUID_LEVEL, encodedLevel);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        if (!isSupportedFluid(fluid)) {
            return false;
        }

        FluidState current = getFluidState(state);
        return current.isEmpty()
                || sameFluidFamily(current, fluid) && !current.isSource();
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState incoming) {
        if (!isSupportedFluid(incoming.getType())) {
            return false;
        }

        FluidState current = getFluidState(state);
        if (!current.isEmpty()) {
            if (!sameFluidFamily(current, incoming.getType())
                    || fluidStrength(incoming) <= fluidStrength(current)) {
                return false;
            }
        }

        if (!level.isClientSide()) {
            BlockState updated = withFluidState(state, incoming);
            level.setBlock(pos, updated, 3);
            scheduleFluidTick(level, pos, updated);
        }
        return true;
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        FluidState storedFluid = getFluidState(state);
        if (!storedFluid.isSource()) {
            return ItemStack.EMPTY;
        }

        level.setBlock(pos, withFluidState(state, Fluids.EMPTY.defaultFluidState()), 3);
        return new ItemStack(storedFluid.getType().getBucket());
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    public Optional<SoundEvent> getPickupSound(BlockState state) {
        FluidState fluidState = getFluidState(state);
        return fluidState.isEmpty() ? Optional.empty() : fluidState.getType().getPickupSound();
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        scheduleFluidTick(level, pos, updated);
        return updated;
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean moving
    ) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        scheduleFluidTick(level, pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        scheduleFluidTick(level, pos, state);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LinedStairBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof LinedStairBlockEntity linedStair) {
            linedStair.setOriginalStairId(LinedStairData.getSourceId(stack));
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return List.of(createPreservingStack(blockEntity));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return createPreservingStack(level.getBlockEntity(pos));
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState sourceState = getSourceState(level.getBlockEntity(pos), state);
        return sourceState.getSoundType(level, pos, entity);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState sourceState = getSourceState(level.getBlockEntity(pos), state);
        return sourceState.getDestroyProgress(player, level, pos);
    }

    private ItemStack createPreservingStack(@Nullable BlockEntity blockEntity) {
        return LinedStairData.createStack(this, sourceId(blockEntity));
    }

    private static BlockState getSourceState(@Nullable BlockEntity blockEntity, BlockState linedState) {
        return LinedStairData.toSourceState(linedState, sourceId(blockEntity));
    }

    private static net.minecraft.resources.ResourceLocation sourceId(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof LinedStairBlockEntity linedStair
                ? linedStair.getOriginalStairId()
                : LinedStairData.FALLBACK_STAIR_ID;
    }

    private static boolean isSupportedFluid(Fluid fluid) {
        return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
    }

    private static boolean sameFluidFamily(FluidState state, Fluid fluid) {
        return state.getType().isSame(fluid);
    }

    private static boolean isAncientOcean(Fluid fluid) {
        return fluid == ModFluids.ANCIENT_OCEAN_WATER.get()
                || fluid == ModFluids.FLOWING_ANCIENT_OCEAN_WATER.get();
    }

    private static boolean isStarTouched(Fluid fluid) {
        return fluid == ModFluids.STAR_TOUCHED_LAVA.get()
                || fluid == ModFluids.FLOWING_STAR_TOUCHED_LAVA.get();
    }

    private static int fluidStrength(FluidState state) {
        return state.isSource() ? 9 : state.getAmount();
    }

    public static void scheduleFluidTick(LevelAccessor level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    /** @deprecated Use {@link #scheduleFluidTick(LevelAccessor, BlockPos, BlockState)}. */
    @Deprecated
    public static void scheduleExactWaterTick(LevelAccessor level, BlockPos pos, BlockState state) {
        scheduleFluidTick(level, pos, state);
    }
}
