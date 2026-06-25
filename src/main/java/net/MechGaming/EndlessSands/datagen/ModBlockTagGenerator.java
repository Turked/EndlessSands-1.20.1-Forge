package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
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
        this.tag(ModTags.Blocks.BLOODIED)
                .add(ModBlocks.CURSED_SAND.get());

        this.tag(ModTags.Blocks.XYLOPHAGE)
                .add(   ModBlocks.PALM_LOG.get(),
                        ModBlocks.PALM_PLANKS.get(),
                        ModBlocks.PALM_DOOR.get(),
                        ModBlocks.PALM_TRAPDOOR.get(),
                        ModBlocks.PALM_FENCE.get(),
                        ModBlocks.PALM_WALL.get(),
                        ModBlocks.PALM_SLAB.get(),
                        ModBlocks.PALM_BUTTON.get(),
                        ModBlocks.PALM_FENCE_GATE.get()
                );

        this.tag(BlockTags.NEEDS_IRON_TOOL);

        this.tag(BlockTags.NEEDS_DIAMOND_TOOL);

        this.tag(BlockTags.NEEDS_STONE_TOOL);

        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL);

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.CURSED_SANDSTONE.get());

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.PALM_LOG.get()
                );

        this.tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.CURSED_SAND.get()
                );

        this.tag(BlockTags.FENCES)
                .add(ModBlocks.PALM_FENCE.get());
        this.tag(BlockTags.FENCE_GATES)
                .add(ModBlocks.PALM_FENCE_GATE.get());
        this.tag(BlockTags.WALLS)
                .add(ModBlocks.PALM_WALL.get());
    }
}
