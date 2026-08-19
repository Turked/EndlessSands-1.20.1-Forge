package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, EndlessSands.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.SAPLINGS)
                .add(ModBlocks.OLDWORLD_SAPLING.get());

        this.tag(ModTags.Blocks.BLOODIED)
                .add(ModBlocks.CURSED_SAND.get());

        this.tag(ModTags.Blocks.GRANULAR)
                .add(
                        ModBlocks.CURSED_SAND.get(),
                        Blocks.SAND,
                        Blocks.GRAVEL
                );

        this.tag(ModTags.Blocks.XYLOPHAGE)
                .add(   ModBlocks.ROTTED_LOG.get(),
                        ModBlocks.ROTTED_PLANKS.get(),
                        ModBlocks.ROTTED_DOOR.get(),
                        ModBlocks.ROTTED_TRAPDOOR.get(),
                        ModBlocks.ROTTED_FENCE.get(),
                        ModBlocks.ROTTED_WALL.get(),
                        ModBlocks.ROTTED_SLAB.get(),
                        ModBlocks.ROTTED_BUTTON.get(),
                        ModBlocks.ROTTED_FENCE_GATE.get()
                );

        this.tag(BlockTags.NEEDS_IRON_TOOL);

        this.tag(BlockTags.NEEDS_DIAMOND_TOOL);

        this.tag(BlockTags.NEEDS_STONE_TOOL);

        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL);

        this.tag(ModTags.Blocks.NEEDS_DRAGONITE_TOOL);

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        ModBlocks.CURSED_SAPROLITE.get(),
                        ModBlocks.CURSED_COBBLED_SAPROLITE.get(),
                        ModBlocks.ZENIONITE.get(),
                        ModBlocks.ZENIONITE_PORTAL_FRAME.get(),
                        ModBlocks.ZENIONITE_BEACON.get(),
                        ModBlocks.ZENIONITE_STAIRS.get(),
                        ModBlocks.LINED_STAIRS.get()
                );

        this.tag(BlockTags.STAIRS)
                .add(ModBlocks.ZENIONITE_STAIRS.get(), ModBlocks.LINED_STAIRS.get());

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(
                        ModBlocks.ROTTED_LOG.get(),
                        ModBlocks.CRUD_LOG.get(),
                        ModBlocks.VULTURE_NEST.get()
                );

        this.tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(
                        ModBlocks.CURSED_SAND.get(),
                        ModBlocks.SUSPICIOUS_CURSED_SAND.get()
                );

        this.tag(BlockTags.FENCES)
                .add(
                        ModBlocks.ROTTED_FENCE.get()
                );
        this.tag(BlockTags.FENCE_GATES)
                .add(
                        ModBlocks.ROTTED_FENCE_GATE.get()
                );
        this.tag(BlockTags.WALLS)
                .add(
                        ModBlocks.ROTTED_WALL.get()
                );
    }
}
