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
    public static final Codec<EndlessSandsChunkGenerator> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Biome.CODEC.fieldOf("biome").forGetter(generator -> generator.biome)
            ).apply(instance, EndlessSandsChunkGenerator::new));

    private static final int MIN_Y = -160;
    private static final int WORLD_HEIGHT = 672;

    private static final int BASE_SURFACE_Y = 69;
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
    public ChunkGeneratorStructureState createState(
            HolderLookup<StructureSet> structureSetLookup,
            RandomState randomState,
            long seed
    ) {
        return ChunkGeneratorStructureState.createForFlat(
                randomState,
                seed,
                this.biomeSource,
                Stream.empty()
        );
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Executor executor,
            Blender blender,
            RandomState random,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        Heightmap oceanFloor =
                chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface =
                chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = chunkPos.getMinBlockX() + localX;

            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = chunkPos.getMinBlockZ() + localZ;
                int surfaceY =
                        clampSurfaceHeight(surfaceHeight(worldX, worldZ), minY, maxY);

                for (int y = minY; y <= surfaceY; y++) {
                    BlockState state = blockForPosition(worldX, y, worldZ, surfaceY);

                    if (!state.isAir()) {
                        chunk.setBlockState(
                                mutablePos.set(localX, y, localZ),
                                state,
                                false
                        );

                        oceanFloor.update(localX, y, localZ, state);
                        worldSurface.update(localX, y, localZ, state);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.Types type,
            LevelHeightAccessor level,
            RandomState random
    ) {
        return clampSurfaceHeight(
                surfaceHeight(x, z),
                level.getMinBuildHeight(),
                level.getMaxBuildHeight()
        ) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(
            int x,
            int z,
            LevelHeightAccessor height,
            RandomState random
    ) {
        int minY = height.getMinBuildHeight();
        int maxY = height.getMaxBuildHeight();
        int surfaceY = clampSurfaceHeight(surfaceHeight(x, z), minY, maxY);

        BlockState[] states = new BlockState[height.getHeight()];

        for (int y = minY; y < maxY; y++) {
            states[y - minY] = y <= surfaceY
                    ? blockForPosition(x, y, z, surfaceY)
                    : Blocks.AIR.defaultBlockState();
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
    public void applyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState random,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step
    ) {
    }

    @Override
    public void buildSurface(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState random,
            ChunkAccess chunk
    ) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public void addDebugScreenInfo(
            List<String> info,
            RandomState random,
            BlockPos pos
    ) {
        info.add(
                "Endless Sands surface height: "
                        + surfaceHeight(pos.getX(), pos.getZ())
        );
    }

    private static int surfaceHeight(int x, int z) {
        double hillHeight = 4.5D;
        double hillFrequency = 1.0D;
        double noise = 0.0D;

        double broadDunes = fbm(x*0.012D, z*0.012D, 4, 0.52D) * hillHeight;
        double smallHills = fbm(x*0.035D + 1000.0D, z * 0.035D - 1000.0D, 3, 0.50D) * hillFrequency;
        double surfaceNoise = fbm(x * 0.085D - 500.0D, z * 0.085D + 500.0D, 2, 0.45D) * noise;

        double height = BASE_SURFACE_Y + broadDunes + smallHills + surfaceNoise;
        return (int) Math.floor(height);
    }

    private static double fbm(double x, double z, int octaves, double persistence){
        double total = 0.0D;
        double amplitude = 1.0D;
        double frequency = 1.0D;
        double max = 0.0D;

        for (int octave = 0; octave < octaves; octave++){
            total += noise(x * frequency, z * frequency) * amplitude;
            max += amplitude;
            amplitude *= persistence;
            frequency *= 2.0D;
        }

        return total / max;
    }

    private static double noise(double x, double z){
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double xBlend = fade(x - x0);
        double zBlend = fade(z - z0);

        double north = lerp(randomNoise(x0, z0), randomNoise(x1, z0), xBlend);
        double south = lerp(randomNoise(x0, z1), randomNoise(x1, z1), xBlend);

        return lerp(north, south, zBlend);
    }

    private static double randomNoise(int x, int z){
        long h = x * 374761393L + z * 668265263L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= h >>> 16;
        return ((h & 0xFFFFFFL) / (double) 0xFFFFFFL) * 2.0D - 1.0D;
    }

    private static double fade(double value){
        return value * value * (3.0D - 2.0D * value);
    }

    private static double lerp(double a, double b, double amount){
        return a + (b - a) * amount;
    }

    private static int clampSurfaceHeight(int surfaceY, int minY, int maxY) {
        return Math.max(minY, Math.min(surfaceY, maxY - 1));
    }

    private static BlockState blockForPosition(
            int x,
            int y,
            int z,
            int surfaceY
    ) {
        // The only perfectly flat layer.
        if (y == MIN_Y) {
            return ModBlocks.CORE_ROCK.get().defaultBlockState();
        }

        int deepCrystalStart = blendedBoundary(-140, x, z, 11);
        int lowerCrustStart = blendedBoundary(-120, x, z, 23);
        int crystalRockStart = blendedBoundary(-90, x, z, 37);
        int cursedBedrockStart = blendedBoundary(-60, x, z, 53);
        int saproliteStart = blendedBoundary(0, x, z, 71);

        // Lower Mantle: approximately -159 through -141.
        if (y < deepCrystalStart) {
            return Blocks.AIR.defaultBlockState();
        }

        // High-Pressure Crystalline Zone: approximately -140 to -120.
        if (y < lowerCrustStart) {
            return ModBlocks.DEEP_CRYSTAL_ROCK.get().defaultBlockState();
        }

        // Lower Crust: approximately -120 to -90.
        if (y < crystalRockStart) {
            return ModBlocks.LOWER_CRUST_ROCK.get().defaultBlockState();
        }

        // Crystalline Basement: approximately -90 to -60.
        if (y < cursedBedrockStart) {
            return ModBlocks.CRYSTAL_ROCK.get().defaultBlockState();
        }

        // Cursed Bedrock: approximately -60 to 0.
        if (y < saproliteStart) {
            return ModBlocks.CURSED_BEDROCK.get().defaultBlockState();
        }

        // The upper five terrain blocks are Cursed Sand.
        if (surfaceY - y < CURSED_SAND_DEPTH) {
            return ModBlocks.CURSED_SAND.get().defaultBlockState();
        }

        return ModBlocks.CURSED_SAPROLITE.get().defaultBlockState();
    }

    private static int blendedBoundary(int baseY, int x, int z, int salt) {
        double broadNoise =
                Math.sin((x + salt * 17.0D) * 0.020D)
                        + Math.sin((z - salt * 13.0D) * 0.017D)
                        + Math.sin((x + z + salt * 7.0D) * 0.011D);

        double detailNoise =
                Math.sin((x - z + salt * 19.0D) * 0.043D)
                        * 0.65D;

        return baseY + (int) Math.round(broadNoise + detailNoise);
    }
}