package net.MechGaming.EndlessSands.datagen.loot;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
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
        this.dropSelf(ModBlocks.CURSED_SANDSTONE.get());
        this.dropSelf(ModBlocks.PALM_LOG.get());
        this.dropSelf(ModBlocks.PALM_PLANKS.get());

        //Randomized Drops
        this.add(ModBlocks.CURSED_SAND.get(), createSingleItemTable(
                ModItems.CURSED_POCKET_SAND.get(), UniformGenerator.between(1.0F, 4.0F)));

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