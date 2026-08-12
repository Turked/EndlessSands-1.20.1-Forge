package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.VultureNestBlockEntity;
import net.MechGaming.EndlessSands.worldgen.VultureHomeSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class VultureNestBlock extends BaseEntityBlock {
    private static final String ITEM_HOME_ID = "VultureHomeId";
    private static final String ITEM_EGGS = "VultureNestEggs";
    private static final String ITEM_VARIANT = "VultureNestVariant";
    public static final int MAX_EGGS = 3;
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 0, MAX_EGGS);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 3);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 6, 14);

    public VultureNestBlock(Properties properties){
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(EGGS, 3)
                .setValue(VARIANT, 0)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
        builder.add(EGGS, VARIANT, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(EGGS, 3)
                .setValue(VARIANT, 0)
                .setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID requestedHome = getHomeId(stack);
        UUID owner = placer instanceof Player player ? player.getUUID() : null;
        UUID homeId = VultureHomeSavedData.get(serverLevel).registerPlacedNest(
                requestedHome, pos, state.getValue(FACING), owner);
        if (level.getBlockEntity(pos) instanceof VultureNestBlockEntity nest) {
            nest.initializeHome(homeId, false);
        }

        if (stack.hasTag()) {
            int eggs = Math.max(0, Math.min(MAX_EGGS, stack.getTag().getInt(ITEM_EGGS)));
            int variant = Math.max(0, Math.min(3, stack.getTag().getInt(ITEM_VARIANT)));
            level.setBlock(pos, state.setValue(EGGS, eggs).setValue(VARIANT, variant), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof VultureNestBlockEntity nest) {
            UUID homeId = nest.getHomeId();
            if (homeId != null) {
                VultureHomeSavedData.get(serverLevel).markNestMissing(homeId);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static ItemStack createCarriedNest(BlockState state, @Nullable UUID homeId) {
        ItemStack stack = new ItemStack(ModBlocks.VULTURE_NEST.get());
        if (homeId != null) {
            stack.getOrCreateTag().putUUID(ITEM_HOME_ID, homeId);
        }
        stack.getOrCreateTag().putInt(ITEM_EGGS, state.getValue(EGGS));
        stack.getOrCreateTag().putInt(ITEM_VARIANT, state.getValue(VARIANT));
        return stack;
    }

    @Nullable
    public static UUID getHomeId(ItemStack stack) {
        return stack.hasTag() && stack.getTag().hasUUID(ITEM_HOME_ID)
                ? stack.getTag().getUUID(ITEM_HOME_ID)
                : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        int eggs = state.getValue(EGGS);
        if (eggs <= 0) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack egg = new ItemStack(ModItems.VULTURE_EGG.get());
        if (!player.addItem(egg)) player.drop(egg, false);

        int nextEggs = eggs - 1;
        int nextVariant = nextVariantAfterTaking(eggs, state.getValue(VARIANT), level.getRandom());

        level.setBlock(pos, state.setValue(EGGS, nextEggs).setValue(VARIANT, nextVariant), Block.UPDATE_ALL);
        return InteractionResult.CONSUME;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VultureNestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.VULTURE_NEST.get(), VultureNestBlockEntity::serverTick);
    }

    public static int randomVariantForEggCount(int eggs, RandomSource random) {
        if (eggs == 1 || eggs == 2) {
            return 1 + random.nextInt(3);
        }
        return 0;
    }

    private static int nextVariantAfterTaking(int eggs, int variant, RandomSource random) {
        if (eggs == 3) return 1 + random.nextInt(3);
        if (eggs == 1) return 0;

        return switch (variant) {
            case 1 -> random.nextBoolean() ? 1 : 2;
            case 2 -> random.nextBoolean() ? 1 : 3;
            case 3 -> random.nextBoolean() ? 2 : 3;
            default -> 1 + random.nextInt(3);
        };
    }
}
