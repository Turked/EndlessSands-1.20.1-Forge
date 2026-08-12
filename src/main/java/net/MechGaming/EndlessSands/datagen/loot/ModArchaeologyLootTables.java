package net.MechGaming.EndlessSands.datagen.loot;

import net.MechGaming.EndlessSands.block.custom.SuspiciousCursedSandBlock;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

public class ModArchaeologyLootTables implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(SuspiciousCursedSandBlock.ARCHAEOLOGY_LOOT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModBlocks.TWIG.get()))
                        .add(LootItem.lootTableItem(ModItems.TINY_OLDWORLD_JAR.get()))
                        .add(LootItem.lootTableItem(ModItems.OLDWORLD_SCROLL.get()))));
    }
}
