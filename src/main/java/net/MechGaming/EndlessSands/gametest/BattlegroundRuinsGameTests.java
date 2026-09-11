package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.worldgen.structure.BattlegroundRuinsStructure;
import net.MechGaming.EndlessSands.worldgen.structure.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BattlegroundRuinsGameTests {
    private BattlegroundRuinsGameTests() {
    }

    @GameTest(template = "empty", batch = "battlegroundRuinsCommandOnly")
    public static void commandOnlyStructureIsRegisteredAndLoadable(GameTestHelper helper) {
        Registry<Structure> structures = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        helper.assertTrue(structures.containsKey(ModStructures.BATTLEGROUND_RUINS.location()),
                "The /place structure ID was not registered");

        Vec3i size = helper.getLevel().getStructureManager()
                .getOrCreate(BattlegroundRuinsStructure.TEMPLATE).getSize();
        helper.assertTrue(size.getX() > 0 && size.getY() > 0 && size.getZ() > 0,
                "The battleground ruins NBT template was missing or empty");

        Structure battleground = structures.getOrThrow(ModStructures.BATTLEGROUND_RUINS);
        var level = helper.getLevel();
        var generator = level.getChunkSource().getGenerator();
        StructureStart commandStart = battleground.generate(level.registryAccess(), generator,
                generator.getBiomeSource(), level.getChunkSource().randomState(),
                level.getStructureManager(), level.getSeed(),
                new ChunkPos(BlockPos.ZERO), 0, level, biome -> true);
        helper.assertTrue(commandStart.isValid(),
                "The configured structure could not create the start used by /place structure");

        Registry<StructureSet> structureSets = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.STRUCTURE_SET);
        boolean naturallyScheduled = structureSets.stream()
                .flatMap(set -> set.structures().stream())
                .anyMatch(entry -> entry.structure().unwrapKey()
                        .map(ModStructures.BATTLEGROUND_RUINS::equals).orElse(false));
        helper.assertTrue(!naturallyScheduled,
                "Battleground ruins was added to a structure set and can spawn naturally");
        helper.succeed();
    }
}
