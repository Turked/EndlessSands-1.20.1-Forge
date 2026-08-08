package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.VultureNestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class VultureNestBlock extends BaseEntityBlock {
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
