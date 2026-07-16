package net.MechGaming.EndlessSands.worldgen.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class EndlessSandsChunkGenerator extends ChunkGenerator {
    public static final Codec<EndlessSandsChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(generator -> generator.biome)
    ).apply(instance, EndlessSandsChunkGenerator::new));

    private static final int MIN_Y = 0;
    private static final int WORLD_HEIGHT = 384;
    private static final int BASE_SURFACE_Y = 64;
    private static final int CURSED_SAND_DEPTH = 5;

    private final Holder<Biome> biome;

    public EndlessSandsChunkGenerator(Holder<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetLookup,
                                                    RandomState randomState,
                                                    long seed) {
        return ChunkGeneratorStructureState.createForFlat(randomState, seed, this.biomeSource, Stream.empty());
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor,
                                                        Blender blender,
                                                        RandomState random,
                                                        StructureManager structureManager,
                                                        ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = chunkPos.getMinBlockX() + localX;

            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = chunkPos.getMinBlockZ() + localZ;
                int surfaceY = clampSurfaceHeight(surfaceHeight(worldX, worldZ), minY, maxY);

                for (int y = minY; y <= surfaceY; y++) {
                    BlockState state = blockForY(y, surfaceY);
                    chunk.setBlockState(mutablePos.set(localX, y, localZ), state, false);
                    oceanFloor.update(localX, y, localZ, state);
                    worldSurface.update(localX, y, localZ, state);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return clampSurfaceHeight(surfaceHeight(x, z), level.getMinBuildHeight(), level.getMaxBuildHeight()) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        int minY = height.getMinBuildHeight();
        int maxY = height.getMaxBuildHeight();
        int surfaceY = clampSurfaceHeight(surfaceHeight(x, z), minY, maxY);
        BlockState[] states = new BlockState[height.getHeight()];

        for (int y = minY; y < maxY; y++) {
            states[y - minY] = y <= surfaceY ? blockForY(y, surfaceY) : Blocks.AIR.defaultBlockState();
        }

        return new NoiseColumn(minY, states);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return BASE_SURFACE_Y + 25;
    }

    @Override
    public int getGenDepth() {
        return WORLD_HEIGHT;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }

    @Override
    public void applyCarvers(WorldGenRegion level,
                             long seed,
                             RandomState random,
                             BiomeManager biomeManager,
                             StructureManager structureManager,
                             ChunkAccess chunk,
                             GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion level,
                             StructureManager structureManager,
                             RandomState random,
                             ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Endless Sands surface height: " + surfaceHeight(pos.getX(), pos.getZ()));
    }

    private static int surfaceHeight(int x, int z) {
        double height = BASE_SURFACE_Y
                + 10.0D * Math.sin(x * 0.018D)
                + 8.0D * Math.sin(z * 0.016D + 1.7D)
                + 6.0D * Math.sin((x + z) * 0.010D)
                + 5.0D * Math.sin((x - z) * 0.012D + 2.4D);

        return (int) Math.floor(height);
    }

    private static int clampSurfaceHeight(int surfaceY, int minY, int maxY) {
        return Math.max(minY, Math.min(surfaceY, maxY - 1));
    }

    private static BlockState blockForY(int y, int surfaceY) {
        if (y <= MIN_Y) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        if (surfaceY - y < CURSED_SAND_DEPTH){
            return ModBlocks.CURSED_SAND.get().defaultBlockState();
        }

        return ModBlocks.CURSED_SAPROLITE.get().defaultBlockState();
    }
}