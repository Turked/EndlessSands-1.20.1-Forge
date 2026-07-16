package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CursedSandLayerBlock extends FallingBlock {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 3);

    private static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{
            Block.box(0, 0, 0, 16, 0, 16),
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(0, 0, 0, 16, 8, 16),
            Block.box(0, 0, 0, 16, 12, 16),
    };

    public CursedSandLayerBlock(BlockBehaviour.Properties properties){
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving){
        super.onRemove(state, level, pos, newState, isMoving);

        if (state.getBlock() != newState.getBlock() && CursedSandPhysics.shouldReactToSupportRemoval()){
            CursedSandPhysics.collapseNearbyAfterSupportChanged(level, pos);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
        builder.add(LAYERS);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (CursedSandPhysics.shouldCollapse(level, pos)){
            CursedSandPhysics.splitIntoFallingLayers(level, pos, state.getValue(LAYERS));
            return;
        }

        super.tick(state, level, pos, random);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random){
        this.tick(state, level, pos, random);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context){
        return state.getValue(LAYERS) < 3 || context.getItemInHand().isEmpty();
    }

    static int getLayerCount(BlockState state){
        return state.hasProperty(LAYERS) ? state.getValue(LAYERS):1;
    }

    static void setLayerCount(Level level, BlockPos pos, int totalLayers){
        if (totalLayers >= 4){
            level.setBlock(pos, ModBlocks.CURSED_SAND.get().defaultBlockState(), 3);
        } else {
            level.setBlock(pos, ModBlocks.CURSED_SAND_LAYER.get().defaultBlockState()
                    .setValue(LAYERS, totalLayers), 3);
        }
    }

    static boolean tryGrowLayer(Level level, BlockPos pos, int fallingLayers){
        BlockState existingState = level.getBlockState(pos);

        if (!existingState.is(ModBlocks.CURSED_SAND_LAYER.get())){
            return false;
        }

        int totalLayers = existingState.getValue(LAYERS) + fallingLayers;

        setLayerCount(level, pos, totalLayers);

        return true;
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState fallingState, BlockState replacedState, FallingBlockEntity fallingEntity){
        if (level.isClientSide()){
            return;
        }

        int fallingLayers = getLayerCount(fallingState);

        if (replacedState.is(this)){
            int totalLayers = getLayerCount(replacedState) + fallingLayers;
            setLayerCount(level, pos, totalLayers);
            return;
        }

        if(tryGrowLayer(level,pos.below(), fallingLayers)){
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingEntity) {
        if (!level.isClientSide()){
            int fallingLayers = getLayerCount(fallingEntity.getBlockState());

            if (tryGrowLayer(level, pos, fallingLayers) || tryGrowLayer(level, pos.below(), fallingLayers)){
                return;
            }
        }

        Block.dropResources(fallingEntity.getBlockState(), level, pos);
    }
}
