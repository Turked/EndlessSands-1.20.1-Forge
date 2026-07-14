package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.util.ModTags;
import net.MechGaming.EndlessSands.worldgen.biome.ModBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModBiomeTagGenerator extends BiomeTagsProvider {
    public ModBiomeTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, EndlessSands.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider){
        this.tag(Tags.Biomes.IS_DESERT).add(ModBiomes.ENDLESS_DESERT);
        this.tag(Tags.Biomes.IS_HOT).add(ModBiomes.ENDLESS_DESERT);
        this.tag(Tags.Biomes.IS_HOT_NETHER).add(ModBiomes.ENDLESS_DESERT);
        this.tag(Tags.Biomes.IS_DRY).add(ModBiomes.ENDLESS_DESERT);
        this.tag(Tags.Biomes.IS_SANDY).add(ModBiomes.ENDLESS_DESERT);
        this.tag(ModTags.Biomes.IS_CURSED_DESERT).add(ModBiomes.ENDLESS_DESERT);
    }
}
