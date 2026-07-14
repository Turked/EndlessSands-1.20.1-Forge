package net.MechGaming.EndlessSands.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CursedSandBlock extends FallingBlock {
    public CursedSandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (CursedSandPhysics.shouldCollapse(level, pos)){
            Direction direction = CursedSandPhysics.findOpenSideAroundSupport(level, pos.below());
            CursedSandPhysics.splitIntoFallingLayers(level, pos, 4, direction);
            return;
        }

        super.tick(state, level, pos, random);
    }
}
