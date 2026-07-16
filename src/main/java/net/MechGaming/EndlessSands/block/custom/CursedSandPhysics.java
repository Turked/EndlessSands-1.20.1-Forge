package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CursedSandPhysics {
    private static final int[][] SIDE_AND_CORNER_OFFSETS = {
            {0, -1},  // North
            {1, 0},   // East
            {0, 1},   // South
            {-1, 0},  // West
            {1, -1},  // North East
            {1, 1},   // South East
            {-1, 1},  // South West
            {-1, -1}  // North West
    };

    static int fullBlockFallUnits() {
        return 4;
    }

    private CursedSandPhysics() {
    }

    private static boolean supressSupportRemovalReaction = false;

    private record FallTarget(BlockPos supportPos, BlockPos spawnPos){

    }

    static boolean shouldReactToSupportRemoval(){
        return !supressSupportRemovalReaction;
    }

    private static void removeCollapsingBlock(ServerLevel level, BlockPos pos){
        supressSupportRemovalReaction = true;

        try{
            level.removeBlock(pos, false);
        }finally {
            supressSupportRemovalReaction = false;
        }
    }

    static void collapseNearbyAfterSupportChanged(Level level, BlockPos supportPos){
        if (!(level instanceof ServerLevel serverLevel)){
            return;
        }

        List<BlockPos> candidates = new ArrayList<>();

        for (int xOffset = -1; xOffset <= 1; xOffset++){
            for (int zOffset = -1; zOffset <= 1; zOffset++){
                if (xOffset == 0 && zOffset == 0){
                    continue;
                }

                candidates.add(supportPos.offset(xOffset, 1, zOffset));
            }
        }

        shuffle(serverLevel, candidates);

        Map<BlockPos, Integer> reservedLayers = new HashMap<>();

        for (BlockPos candidate : candidates){
            collapseIfNeeded(serverLevel, candidate, reservedLayers);
        }
    }

    private static void shuffle(ServerLevel level, List<BlockPos> positions){
        for (int i = positions.size() - 1; i > 0; i--){
            int j = level.getRandom().nextInt(i+1);
            BlockPos temp = positions.get(i);
            positions.set(i, positions.get(j));
            positions.set(j, temp);
        }
    }

    private static void collapseIfNeeded(ServerLevel level, BlockPos pos, Map<BlockPos, Integer> reservedLayers){
        BlockState state = level.getBlockState(pos);

        if (state.is(ModBlocks.CURSED_SAND.get())){
            if (shouldCollapse(level, pos, reservedLayers)){
                splitIntoFallingLayers(level, pos, fullBlockFallUnits(), reservedLayers);
            }
            return;
        }

        if (state.is(ModBlocks.CURSED_SAND_LAYER.get()) && shouldCollapse(level, pos, reservedLayers)){
            splitIntoFallingLayers(level, pos, CursedSandLayerBlock.getLayerCount(state), reservedLayers);
        }
    }

    static boolean shouldCollapse(ServerLevel level, BlockPos pos){
        return shouldCollapse(level, pos, new HashMap<>());
    }

    static boolean shouldCollapse(ServerLevel level, BlockPos pos, Map<BlockPos, Integer> reservedLayers) {
        if (FallingBlock.isFree(level.getBlockState(pos.below()))){
            return false;
        }

        return !findUnsupportedSideTargets(level, pos, reservedLayers).isEmpty();
    }

    static void splitIntoFallingLayers(ServerLevel level, BlockPos pos, int layerCount) {
        splitIntoFallingLayers(level, pos, layerCount, new HashMap<>());
    }

        static void splitIntoFallingLayers(ServerLevel level, BlockPos pos, int layerCount, Map<BlockPos, Integer> reservedLayers) {
        List<FallTarget> targets = findUnsupportedSideTargets(level, pos, reservedLayers);

        if (targets.isEmpty()) {
            return;
        }

        removeCollapsingBlock(level, pos);

        int remainingLayers = layerCount;

        while (remainingLayers > 0){
            FallTarget target = pickLowestTarget(level, targets, reservedLayers);

            if (target == null){
                break;
            }

            reserveLayer(reservedLayers, target.supportPos());
            spawnFallingLayer(level, target.spawnPos());
            remainingLayers--;
        }

        if (remainingLayers > 0){
            CursedSandLayerBlock.setLayerCount(level, pos, remainingLayers);
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
            BlockPos spawnPos = pos.above();

            if (FallingBlock.isFree(level.getBlockState(spawnPos))) {
                level.setBlock(spawnPos, layerState, 3);
                FallingBlockEntity.fall(level, spawnPos, layerState);
            } else {
                CursedSandLayerBlock.tryGrowLayer(level, pos, 1);
            }
        }
    }

    private static List<FallTarget> findUnsupportedSideTargets(ServerLevel level, BlockPos pos, Map<BlockPos, Integer> reservedLayers) {
        BlockPos supportPos = pos.below();
        List<FallTarget> targets = new ArrayList<>();

        for (int[] offset : SIDE_AND_CORNER_OFFSETS) {
            BlockPos supportNeighbor = supportPos.offset(offset[0], 0, offset[1]);

            if (!isFullSideSupport(level, supportNeighbor)
                    && targetLayerHeight(level, supportNeighbor, reservedLayers) < fullBlockFallUnits()
                    && !isFlowPathBlocked(level, pos, offset[0], offset[1])) {
                targets.add(new FallTarget(supportNeighbor, supportNeighbor.above()));
            }
        }

        return targets;
    }

    private static FallTarget pickLowestTarget(ServerLevel level, List<FallTarget> targets, Map<BlockPos, Integer> reservedLayers){
        int lowestHeight = fullBlockFallUnits();
        List<FallTarget> bestTargets = new ArrayList<>();

        for (FallTarget target : targets){
            int height = targetLayerHeight(level, target.supportPos(), reservedLayers);

            if (height >= fullBlockFallUnits()){
                continue;
            }

            if (height < lowestHeight){
                lowestHeight = height;
                bestTargets.clear();
            }

            if (height == lowestHeight){
                bestTargets.add(target);
            }
        }

        if (bestTargets.isEmpty()){
            return null;
        }

        return bestTargets.get(level.getRandom().nextInt(bestTargets.size()));
    }

    private static int targetLayerHeight(ServerLevel level, BlockPos pos, Map<BlockPos, Integer> reservedLayers){
        BlockState state = level.getBlockState(pos);
        int existingLayers;

        if (state.is(ModBlocks.CURSED_SAND.get())){
            existingLayers = fullBlockFallUnits();
        } else if (state.is(ModBlocks.CURSED_SAND_LAYER.get())){
            existingLayers = CursedSandLayerBlock.getLayerCount(state);
        } else if (FallingBlock.isFree(state)) {
            existingLayers = 0;
        }else {
            existingLayers = fullBlockFallUnits();
        }

        return Math.min(fullBlockFallUnits(), existingLayers + reservedLayers.getOrDefault(pos, 0));
    }

    private static void reserveLayer(Map<BlockPos, Integer> reservedLayers, BlockPos pos){
        reservedLayers.put(pos, reservedLayers.getOrDefault(pos, 0) + 1);
    }

    private static boolean isFlowPathBlocked(ServerLevel level, BlockPos pos, int xOffset, int zOffset){
        if (blocksGranularFlow(level, pos.offset(xOffset, 0, zOffset))){
            return true;
        }

        if(xOffset != 0 && zOffset != 0){
            return blocksGranularFlow(level, pos.offset(xOffset, 0, 0))
                    && blocksGranularFlow(level, pos.offset(0, 0, zOffset));
        }

        return false;
    }

    private static boolean blocksGranularFlow(ServerLevel level, BlockPos pos){
        BlockState state = level.getBlockState(pos);

        return !state.is(ModTags.Blocks.GRANULAR)
                && !FallingBlock.isFree(state)
                && state.isCollisionShapeFullBlock(level, pos);
    }

    private static boolean isFullSideSupport(ServerLevel level, BlockPos pos){
        BlockState state = level.getBlockState(pos);

        return !state.is(ModBlocks.CURSED_SAND_LAYER.get())
                && !FallingBlock.isFree(state)
                && state.isCollisionShapeFullBlock(level, pos);
    }
}
