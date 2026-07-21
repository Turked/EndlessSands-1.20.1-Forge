package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.worldgen.biome.ModBiomes;
import net.MechGaming.EndlessSands.worldgen.dimension.EndlessSandsChunkGenerator;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Optional;
import java.util.OptionalLong;

public class ModBiomeProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, ModBiomeProvider::bootstrapBiomes)
            .add(Registries.DIMENSION_TYPE, ModBiomeProvider::bootstrapDimensionTypes)
            .add(Registries.LEVEL_STEM, ModBiomeProvider::bootstrapLevelStems);

    private static void bootstrapBiomes(BootstapContext<Biome> context){
        context.register(ModBiomes.ENDLESS_DESERT, endlessDesert(context));
    }

    private static void bootstrapDimensionTypes(BootstapContext<DimensionType> context){
        context.register(ModDimensions.ENDLESS_SANDS_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                true,
                true,
                1.0D,
                false,
                false,
                -160,
                672,
                560,
                BlockTags.INFINIBURN_NETHER,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)
        ));
    }

    private static void bootstrapLevelStems(BootstapContext<LevelStem> context){
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        Holder<Biome> endlessDesert = biomes.getOrThrow(ModBiomes.ENDLESS_DESERT);

        context.register(ModDimensions.ENDLESS_SANDS_LEVEL_STEM, new LevelStem(
                dimensionTypes.getOrThrow(ModDimensions.ENDLESS_SANDS_TYPE),
                new EndlessSandsChunkGenerator(endlessDesert)
        ));
    }

    //Add Biomes Here
    private static Biome endlessDesert(BootstapContext<Biome> context){
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .skyColor(7907327)
                        .fogColor(14059067)
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .ambientParticle(new AmbientParticleSettings(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                        ModBlocks.CURSED_SAND.get().defaultBlockState()),
                                0.01F))
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, carvers).build())
                .build();
    }
}
