package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.entity.LinedStairBlockEntity;
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
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class LinedStairBlock extends StairBlock implements EntityBlock {
    public static final IntegerProperty EXACT_WATER_LEVEL =
            IntegerProperty.create("endlesssands_water_level", 0, 9);

    public LinedStairBlock(BlockBehaviour.Properties properties) {
        super(() -> Blocks.SANDSTONE_STAIRS.defaultBlockState(), properties);
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false)
                .setValue(EXACT_WATER_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(EXACT_WATER_LEVEL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : withFluidState(state, context.getLevel().getFluidState(context.getClickedPos()));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (!state.getValue(WATERLOGGED)) {
            return Fluids.EMPTY.defaultFluidState();
        }

        int encodedLevel = state.getValue(EXACT_WATER_LEVEL);
        if (encodedLevel == 9) {
            return Fluids.WATER.getSource(false);
        }
        if (encodedLevel == 0) {
            return Fluids.FLOWING_WATER.getFlowing(8, true);
        }
        return Fluids.FLOWING_WATER.getFlowing(encodedLevel, false);
    }

    public static BlockState withFluidState(BlockState state, FluidState fluidState) {
        if (!fluidState.is(FluidTags.WATER)) {
            return state.setValue(WATERLOGGED, false).setValue(EXACT_WATER_LEVEL, 0);
        }

        int encodedLevel;
        if (fluidState.isSource()) {
            encodedLevel = 9;
        } else if (fluidState.getValue(net.minecraft.world.level.material.FlowingFluid.FALLING)) {
            encodedLevel = 0;
        } else {
            encodedLevel = Mth.clamp(fluidState.getAmount(), 1, 8);
        }
        return state.setValue(WATERLOGGED, true).setValue(EXACT_WATER_LEVEL, encodedLevel);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return (fluid.isSame(Fluids.WATER) || fluid.isSame(Fluids.FLOWING_WATER))
                && !getFluidState(state).isSource();
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState incoming) {
        if (!incoming.is(FluidTags.WATER)) {
            return false;
        }

        FluidState current = getFluidState(state);
        if (!current.isEmpty() && waterStrength(incoming) <= waterStrength(current)) {
            return false;
        }

        if (!level.isClientSide()) {
            BlockState updated = withFluidState(state, incoming);
            level.setBlock(pos, updated, 3);
            scheduleExactWaterTick(level, pos, updated);
        }
        return true;
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!getFluidState(state).isSource()) {
            return ItemStack.EMPTY;
        }

        level.setBlock(pos, withFluidState(state, Fluids.EMPTY.defaultFluidState()), 3);
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
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
        scheduleExactWaterTick(level, pos, updated);
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
        scheduleExactWaterTick(level, pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        scheduleExactWaterTick(level, pos, state);
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

    private static int waterStrength(FluidState state) {
        return state.isSource() ? 9 : state.getAmount();
    }

    public static void scheduleExactWaterTick(LevelAccessor level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }
}
