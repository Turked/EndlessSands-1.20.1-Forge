package net.MechGaming.EndlessSands.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class EndlessSandsConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue TRULY_ENDLESS_DESERT;
    public static final ForgeConfigSpec.BooleanValue BORN_OF_THE_SAND;
    public static final ForgeConfigSpec.BooleanValue ENDLESS_CURSE_FREE_DESERT;
    public static final ForgeConfigSpec.BooleanValue TAG_ENDLESS_DESERT_AS_DESERT_FOR_MOB_SPAWNING;
    public static final ForgeConfigSpec.BooleanValue TAG_ENDLESS_DESERT_AS_DESERT_FOR_STRUCTURE_SPAWNING;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("worldgen");

        TRULY_ENDLESS_DESERT = builder
                .comment("If true, Endless Sands only generates the Endless Desert biome.")
                .define("trulyEndlessDesert", false);

        BORN_OF_THE_SAND = builder
                .comment("If true, new players spawn inside the Endless Sands dimension.")
                .define("bornOfTheSand", false);

        ENDLESS_CURSE_FREE_DESERT = builder
                .comment("If true, every vanilla Overworld biome source query returns the vanilla desert biome.")
                .define("endlessCurseFreeDesert", false);

        TAG_ENDLESS_DESERT_AS_DESERT_FOR_MOB_SPAWNING = builder
                .comment("If true, Endless Desert should count as a desert biome for desert mob spawning logic.")
                .define("tagEndlessDesertAsDesertForMobSpawning", true);

        TAG_ENDLESS_DESERT_AS_DESERT_FOR_STRUCTURE_SPAWNING = builder
                .comment("If true, Endless Desert should count as a desert biome for desert structure spawning logic. Not wired yet.")
                .define("tagEndlessDesertAsDesertForStructureSpawning", false);

        builder.pop();

        SPEC = builder.build();
    }
}