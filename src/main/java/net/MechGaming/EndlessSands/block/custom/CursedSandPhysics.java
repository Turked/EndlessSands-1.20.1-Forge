package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.List;

final class CursedSandPhysics {
    private static final int[][] SPREAD_OFFSETS = {
            {0, -1},  // North
            {1, 0},   // East
            {0, 1},   // South
            {-1, 0},  // West
            {1, -1},  // North East
            {1, 1},   // South East
            {-1, 1},  // South West
            {-1, -1}  // North West
    };

    private static final Direction[] SPREAD_DIRECTIONS = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };

    private CursedSandPhysics() {
    }

    static boolean isCursedSandFamily(BlockState state) {
        return state.is(ModBlocks.CURSED_SAND.get()) || state.is(ModBlocks.CURSED_SAND_LAYER.get());
    }

    static boolean shouldCollapse(ServerLevel level, BlockPos pos) {
        return isCursedSandFamily(level.getBlockState(pos.below()))
                && !findOpenSpreadTargets(level, pos).isEmpty();
    }

    static Direction findOpenSideAroundSupport(ServerLevel level, BlockPos supportPos){
        Direction[] directions = {
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        };

        for (Direction direction : directions){
            BlockPos neighborPos = supportPos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (FallingBlock.isFree(neighborState)){
                return direction;
            }
        }

        return null;
    }

    static void splitIntoFallingLayers(ServerLevel level, BlockPos pos, int layerCount, Direction direction){
        splitIntoFallingLayers(level, pos, layerCount);
    }

    static void splitIntoFallingLayers(ServerLevel level, BlockPos pos, int layerCount) {
        List<BlockPos> targets = findOpenSpreadTargets(level, pos);

        if (targets.isEmpty()) {
            return;
        }

        level.removeBlock(pos, false);

        int layersToMove = Math.min(layerCount, targets.size());
        for (int i = 0; i < layersToMove; i++) {
            spawnFallingLayer(level, targets.get(i));
        }
    }

    private static void spawnFallingLayer(ServerLevel level, BlockPos pos) {
        BlockState layerState = ModBlocks.CURSED_SAND_LAYER.get().defaultBlockState();
        BlockState existingState = level.getBlockState(pos);

        if (FallingBlock.isFree(existingState)) {
            level.setBlock(pos, layerState, 3);
            FallingBlockEntity.fall(level, pos, layerState);
            return;
        }

        if (existingState.is(ModBlocks.CURSED_SAND_LAYER.get())) {
            int layers = existingState.getValue(CursedSandLayerBlock.LAYERS);
            if (layers < 3) {
                level.setBlock(pos, existingState.setValue(CursedSandLayerBlock.LAYERS, layers + 1), 3);
            }
        }
    }

    private static List<BlockPos> findOpenSpreadTargets(ServerLevel level, BlockPos pos) {
        BlockPos supportPos = pos.below();
        List<BlockPos> targets = new ArrayList<>();

        for (int[] offset : SPREAD_OFFSETS) {
            BlockPos supportNeighbor = supportPos.offset(offset[0], 0, offset[1]);

            if (FallingBlock.isFree(level.getBlockState(supportNeighbor))) {
                targets.add(pos.offset(offset[0], 0, offset[1]));
            }
        }

        return targets;
    }
}
