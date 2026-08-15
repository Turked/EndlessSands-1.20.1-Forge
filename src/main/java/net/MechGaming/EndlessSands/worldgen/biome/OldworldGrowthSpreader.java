package net.MechGaming.EndlessSands.worldgen.biome;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OldworldGrowthSpreader {
    private static final int BLOCKS_PER_GROWTH = 12;
    private static final int MAX_SPREAD_RADIUS = 32;
    private static final int MAX_CONNECTED_SOIL = 4096;
    private static final int MIN_FERTILE_SOIL_FOR_LAKE = 48;
    private static final int LAKE_CHANCE = 6;
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private OldworldGrowthSpreader() {
    }

    public static void grow(ServerLevel level, BlockPos saplingPos, RandomSource random) {
        BlockPos supportingSoil = saplingPos.below();
        Set<BlockPos> biomeColumns = new HashSet<>();

        if (level.getBlockState(supportingSoil).is(ModBlocks.CURSED_SAND.get())) {
            level.setBlock(supportingSoil, ModBlocks.FERTILE_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
            biomeColumns.add(supportingSoil.immutable());
            applyOldworldGrowthBiome(level, biomeColumns);
            return;
        }

        if (!level.getBlockState(supportingSoil).is(ModBlocks.FERTILE_SOIL.get())) {
            return;
        }

        GrowthArea growthArea = findGrowthArea(level, supportingSoil);
        List<BlockPos> frontier = new ArrayList<>(growthArea.frontier());
        Collections.shuffle(frontier, new java.util.Random(random.nextLong()));

        int converted = Math.min(BLOCKS_PER_GROWTH, frontier.size());
        for (int i = 0; i < converted; i++) {
            BlockPos target = frontier.get(i);
            if (level.getBlockState(target).is(ModBlocks.CURSED_SAND.get())) {
                level.setBlock(target, ModBlocks.FERTILE_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
                biomeColumns.add(target.immutable());
            }
        }

        int fertileCount = growthArea.fertileSoil().size() + biomeColumns.size();
        if (fertileCount >= MIN_FERTILE_SOIL_FOR_LAKE && random.nextInt(LAKE_CHANCE) == 0) {
            tryGenerateLake(level, saplingPos, growthArea.fertileSoil(), random, biomeColumns);
        }

        if (!biomeColumns.isEmpty()) {
            applyOldworldGrowthBiome(level, biomeColumns);
        }
    }

    private static GrowthArea findGrowthArea(ServerLevel level, BlockPos origin) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> visitedColumns = new HashSet<>();
        Set<BlockPos> fertileSoil = new HashSet<>();
        Set<BlockPos> frontier = new HashSet<>();
        queue.add(origin.immutable());

        while (!queue.isEmpty() && fertileSoil.size() < MAX_CONNECTED_SOIL) {
            BlockPos current = queue.removeFirst();
            long columnKey = ChunkPos.asLong(current.getX(), current.getZ());
            if (!visitedColumns.add(columnKey)) {
                continue;
            }

            int dx = current.getX() - origin.getX();
            int dz = current.getZ() - origin.getZ();
            if (dx * dx + dz * dz > MAX_SPREAD_RADIUS * MAX_SPREAD_RADIUS) {
                continue;
            }

            if (!level.getBlockState(current).is(ModBlocks.FERTILE_SOIL.get())) {
                continue;
            }
            fertileSoil.add(current.immutable());

            for (Direction direction : HORIZONTAL_DIRECTIONS) {
                int x = current.getX() + direction.getStepX();
                int z = current.getZ() + direction.getStepZ();
                BlockPos neighbor = surfaceBlock(level, x, z);
                BlockStateKind kind = classify(level, neighbor);
                if (kind == BlockStateKind.FERTILE) {
                    queue.addLast(neighbor);
                } else if (kind == BlockStateKind.CURSED) {
                    frontier.add(neighbor.immutable());
                }
            }
        }

        return new GrowthArea(fertileSoil, frontier);
    }

    private static BlockPos surfaceBlock(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        return new BlockPos(x, y, z);
    }

    private static BlockStateKind classify(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(ModBlocks.FERTILE_SOIL.get())) {
            return BlockStateKind.FERTILE;
        }
        if (level.getBlockState(pos).is(ModBlocks.CURSED_SAND.get())) {
            return BlockStateKind.CURSED;
        }
        return BlockStateKind.OTHER;
    }

    private static void tryGenerateLake(ServerLevel level, BlockPos saplingPos, Collection<BlockPos> fertileSoil,
                                        RandomSource random, Set<BlockPos> biomeColumns) {
        List<BlockPos> candidates = fertileSoil.stream()
                .filter(pos -> pos.distSqr(saplingPos) >= 64.0D)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        for (int i = 0; i < Math.min(16, candidates.size()); i++) {
            BlockPos center = candidates.get(i);
            int radiusX = 3 + random.nextInt(3);
            int radiusZ = 3 + random.nextInt(3);
            int depth = 2 + random.nextInt(2);
            if (canCarveLake(level, center, radiusX, radiusZ)) {
                carveLake(level, center, radiusX, radiusZ, depth, random, biomeColumns);
                return;
            }
        }
    }

    private static boolean canCarveLake(ServerLevel level, BlockPos center, int radiusX, int radiusZ) {
        int fertile = 0;
        int total = 0;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                if (!insideEllipse(dx, dz, radiusX, radiusZ)) {
                    continue;
                }
                BlockPos surface = surfaceBlock(level, center.getX() + dx, center.getZ() + dz);
                total++;
                if (level.getBlockState(surface).is(ModBlocks.FERTILE_SOIL.get())) {
                    fertile++;
                }
                if (level.getBlockState(surface.above()).is(ModBlocks.OLDWORLD_SAPLING.get())) {
                    return false;
                }
                minY = Math.min(minY, surface.getY());
                maxY = Math.max(maxY, surface.getY());
            }
        }

        return total > 0 && fertile * 4 >= total * 3 && maxY - minY <= 3;
    }

    private static void carveLake(ServerLevel level, BlockPos center, int radiusX, int radiusZ, int depth,
                                  RandomSource random, Set<BlockPos> biomeColumns) {
        int waterY = center.getY() - 1;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                if (!insideEllipse(dx, dz, radiusX, radiusZ)) {
                    continue;
                }

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                BlockPos surface = surfaceBlock(level, x, z);
                for (int y = waterY + 1; y <= surface.getY() + 2; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }

                BlockPos lakeBed = new BlockPos(x, waterY - depth, z);
                level.setBlock(lakeBed, random.nextBoolean()
                        ? Blocks.SAND.defaultBlockState()
                        : Blocks.GRAVEL.defaultBlockState(), Block.UPDATE_ALL);

                for (int y = waterY - depth + 1; y <= waterY; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                }
                biomeColumns.add(new BlockPos(x, waterY, z));
            }
        }
    }

    private static boolean insideEllipse(int dx, int dz, int radiusX, int radiusZ) {
        double x = (double) dx / radiusX;
        double z = (double) dz / radiusZ;
        return x * x + z * z <= 1.0D;
    }

    private static void applyOldworldGrowthBiome(ServerLevel level, Collection<BlockPos> columns) {
        Holder<Biome> oldworldGrowth = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ModBiomes.OLDWORLD_GROWTH);
        Map<ChunkPos, Set<Long>> quartColumnsByChunk = new HashMap<>();

        for (BlockPos pos : columns) {
            int quartX = QuartPos.fromBlock(pos.getX());
            int quartZ = QuartPos.fromBlock(pos.getZ());
            ChunkPos chunkPos = new ChunkPos(pos);
            quartColumnsByChunk.computeIfAbsent(chunkPos, ignored -> new HashSet<>())
                    .add(ChunkPos.asLong(quartX, quartZ));
        }

        List<ChunkAccess> changedChunks = new ArrayList<>();
        for (Map.Entry<ChunkPos, Set<Long>> entry : quartColumnsByChunk.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            Set<Long> quartColumns = entry.getValue();
            LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
            chunk.fillBiomesFromNoise((quartX, quartY, quartZ, sampler) ->
                            quartColumns.contains(ChunkPos.asLong(quartX, quartZ))
                                    ? oldworldGrowth
                                    : chunk.getNoiseBiome(quartX, quartY, quartZ),
                    Climate.empty());
            chunk.setUnsaved(true);
            changedChunks.add(chunk);
        }

        level.getChunkSource().chunkMap.resendBiomesForChunks(changedChunks);
    }

    private enum BlockStateKind {
        FERTILE,
        CURSED,
        OTHER
    }

    private record GrowthArea(Set<BlockPos> fertileSoil, Set<BlockPos> frontier) {
    }
}
