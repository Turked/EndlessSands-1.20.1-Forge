package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.util.ModTags;
import net.MechGaming.EndlessSands.worldgen.structure.ModStructures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModStructureTagGenerator extends TagsProvider<Structure> {
    public ModStructureTagGenerator(PackOutput output,
                                    CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.STRUCTURE, lookupProvider, EndlessSands.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Structures.CRUD_TREES).add(ModStructures.CRUD_TREE);
        tag(ModTags.Structures.FLOATING_ISLANDS).add(ModStructures.FLOATING_ISLAND);
    }
}
