package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public final class ModFluidTagGenerator extends FluidTagsProvider {
    public ModFluidTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                               ExistingFileHelper helper) {
        super(output, lookup, EndlessSands.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(FluidTags.WATER).add(ModFluids.ANCIENT_OCEAN_WATER.get(),
                ModFluids.FLOWING_ANCIENT_OCEAN_WATER.get());
        tag(FluidTags.LAVA).add(ModFluids.STAR_TOUCHED_LAVA.get(),
                ModFluids.FLOWING_STAR_TOUCHED_LAVA.get());
    }
}
