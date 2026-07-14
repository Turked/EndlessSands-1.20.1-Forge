package net.MechGaming.EndlessSands.datagen.loot;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;


import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        //Drop Self
        this.dropSelf(ModBlocks.CURSED_SAPROLITE.get());
        this.dropSelf(ModBlocks.PALM_LOG.get());
        this.dropSelf(ModBlocks.PALM_PLANKS.get());

        this.dropSelf(ModBlocks.PALM_STAIRS.get());
        this.dropSelf(ModBlocks.PALM_BUTTON.get());
        this.dropSelf(ModBlocks.PALM_PRESSURE_PLATE.get());
        this.dropSelf(ModBlocks.PALM_TRAPDOOR.get());
        this.dropSelf(ModBlocks.PALM_FENCE.get());
        this.dropSelf(ModBlocks.PALM_FENCE_GATE.get());
        this.dropSelf(ModBlocks.PALM_WALL.get());

        this.add(ModBlocks.PALM_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.PALM_SLAB.get()));
        this.add(ModBlocks.PALM_DOOR.get(),
                block -> createDoorTable(ModBlocks.PALM_DOOR.get()));

        //Randomized Drops
        this.add(ModBlocks.CURSED_SAND.get(), createSingleItemTable(
                ModItems.CURSED_POCKET_SAND.get(), UniformGenerator.between(1.0F, 4.0F)));

        this.add(ModBlocks.CURSED_SAND_LAYER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.CURSED_POCKET_SAND.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.CURSED_SAND_LAYER.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CursedSandLayerBlock.LAYERS, 1)))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))))
                        .add(LootItem.lootTableItem(ModItems.CURSED_POCKET_SAND.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.CURSED_SAND_LAYER.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CursedSandLayerBlock.LAYERS, 2)))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))))
                        .add(LootItem.lootTableItem(ModItems.CURSED_POCKET_SAND.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.CURSED_SAND_LAYER.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CursedSandLayerBlock.LAYERS, 3)))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 3.0F))))));

        //Grass like behavior
        this.add(ModBlocks.FERTILE_SOIL.get(), createSingleItemTableWithSilkTouch(
                ModBlocks.FERTILE_SOIL.get(), ModBlocks.CURSED_SAND.get()));
        this.add(ModBlocks.VILLAGE_POT.get(), createSingleItemTableWithSilkTouch(
                ModBlocks.VILLAGE_POT.get(), ModItems.ROTTED_WOOD.get()));

        //Drop Nothing
        //- suspicious sand
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }

}
