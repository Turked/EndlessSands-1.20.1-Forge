package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.worldgen.biome.OldworldGrowthSpreader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OldworldSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final BooleanProperty BRITTLE_CHARGED = BooleanProperty.create("brittle_charged");
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

    public OldworldSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BRITTLE_CHARGED, false));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos)
                || state.is(ModBlocks.CURSED_SAND.get())
                || state.is(ModBlocks.FERTILE_SOIL.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClientSide) {
        BlockState soil = level.getBlockState(pos.below());
        return soil.is(ModBlocks.CURSED_SAND.get()) || soil.is(ModBlocks.FERTILE_SOIL.get());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(BRITTLE_CHARGED)) {
            level.setBlock(pos, state.setValue(BRITTLE_CHARGED, false), Block.UPDATE_CLIENTS);
        }
        OldworldGrowthSpreader.grow(level, pos, random);
    }

    public boolean applyBrittleBoneMeal(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isValidBonemealTarget(level, pos, state, false)) {
            return false;
        }

        if (!state.getValue(BRITTLE_CHARGED)) {
            level.setBlock(pos, state.setValue(BRITTLE_CHARGED, true), Block.UPDATE_CLIENTS);
            return true;
        }

        level.setBlock(pos, state.setValue(BRITTLE_CHARGED, false), Block.UPDATE_CLIENTS);
        OldworldGrowthSpreader.grow(level, pos, level.getRandom());
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BRITTLE_CHARGED);
    }
}
