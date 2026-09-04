package net.MechGaming.EndlessSands.worldgen.dimension;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import net.minecraft.world.level.Level;

public class ModDimensions {
    public static final ResourceKey<Level> ENDLESS_SANDS_LEVEL = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "endless_sands"));

    public static final ResourceKey<DimensionType> ENDLESS_SANDS_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "endless_sands"));

    public static final ResourceKey<LevelStem> ENDLESS_SANDS_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "endless_sands"));

    public static final ResourceKey<Level> PRISON_REALM_LEVEL = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "prison_realm"));

    public static final ResourceKey<DimensionType> PRISON_REALM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "prison_realm"));

    public static final ResourceKey<LevelStem> PRISON_REALM_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "prison_realm"));

    public static final ResourceLocation PRISON_REALM_EFFECTS =
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "prison_realm");
}
