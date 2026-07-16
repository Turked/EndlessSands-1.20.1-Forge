package net.MechGaming.EndlessSands.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CursedSandBlock extends FallingBlock {
    public CursedSandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving){
        super.onRemove(state, level, pos, newState, isMoving);

        if (state.getBlock() != newState.getBlock() && CursedSandPhysics.shouldReactToSupportRemoval()){
            CursedSandPhysics.collapseNearbyAfterSupportChanged(level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (CursedSandPhysics.shouldCollapse(level, pos)){
            CursedSandPhysics.splitIntoFallingLayers(level, pos, CursedSandPhysics.fullBlockFallUnits());
            return;
        }

        super.tick(state, level, pos, random);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random){
        this.tick(state, level, pos, random);
    }
}
