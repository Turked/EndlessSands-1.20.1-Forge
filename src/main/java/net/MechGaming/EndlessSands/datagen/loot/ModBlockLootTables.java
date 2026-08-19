package net.MechGaming.EndlessSands.datagen.loot;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;


import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        //Drop Self
        this.dropSelf(ModBlocks.ROTTED_LOG.get());
        this.dropSelf(ModBlocks.ROTTED_PLANKS.get());
        this.dropSelf(ModBlocks.CRUD_LOG.get());
        this.dropSelf(ModBlocks.TWIG.get());
        this.dropSelf(ModBlocks.OLDWORLD_SAPLING.get());

        this.dropSelf(ModBlocks.ROTTED_STAIRS.get());
        this.dropSelf(ModBlocks.ZENIONITE.get());
        this.dropSelf(ModBlocks.ZENIONITE_PORTAL_FRAME.get());
        this.dropSelf(ModBlocks.ZENIONITE_BEACON.get());
        this.dropSelf(ModBlocks.ZENIONITE_STAIRS.get());
        this.dropSelf(ModBlocks.ROTTED_BUTTON.get());
        this.dropSelf(ModBlocks.ROTTED_PRESSURE_PLATE.get());
        this.dropSelf(ModBlocks.ROTTED_TRAPDOOR.get());
        this.dropSelf(ModBlocks.ROTTED_FENCE.get());
        this.dropSelf(ModBlocks.ROTTED_FENCE_GATE.get());
        this.dropSelf(ModBlocks.ROTTED_WALL.get());

        this.dropSelf(ModBlocks.CURSED_COBBLED_SAPROLITE.get());
        this.dropSelf(ModBlocks.DEEP_CRYSTAL_ROCK.get());
        this.dropSelf(ModBlocks.LOWER_CRUST_ROCK.get());
        this.dropSelf(ModBlocks.CRYSTAL_ROCK.get());
        this.dropSelf(ModBlocks.CURSED_BEDROCK.get());

        this.add(ModBlocks.ROTTED_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.ROTTED_SLAB.get()));
        this.add(ModBlocks.ROTTED_DOOR.get(),
                block -> createDoorTable(ModBlocks.ROTTED_DOOR.get()));

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
        this.add(ModBlocks.VULTURE_NEST.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModBlocks.TWIG.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(5.0F, 10.0F))))));


        //Silk Touch Behavior (like grass)
        this.add(ModBlocks.FERTILE_SOIL.get(), createSingleItemTableWithSilkTouch(
                ModBlocks.FERTILE_SOIL.get(), ModBlocks.CURSED_SAND.get()));
        this.add(ModBlocks.VILLAGE_POT.get(), createSingleItemTableWithSilkTouch(
                ModBlocks.VILLAGE_POT.get(), ModItems.ROTTED_WOOD.get()));
        this.add(ModBlocks.CURSED_SAPROLITE.get(), createSingleItemTableWithSilkTouch(
                ModBlocks.CURSED_SAPROLITE.get(), ModBlocks.CURSED_COBBLED_SAPROLITE.get()));

        //Drop Nothing
        this.add(ModBlocks.CORE_ROCK.get(), noDrop());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }

}
