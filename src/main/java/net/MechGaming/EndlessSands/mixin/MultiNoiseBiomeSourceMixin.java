package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
    @Unique
    private Holder<Biome> endlesssands$cachedDesertBiome;

    @Shadow
    public abstract boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> preset);

    @Shadow
    protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

    @Inject(
            method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void endlesssands$replaceOverworldBiomesWithDesert(int x, int y, int z, Climate.Sampler sampler,
                                                               CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!EndlessSandsConfig.ENDLESS_CURSE_FREE_DESERT.get()) {
            return;
        }

        if (!this.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
            return;
        }

        Holder<Biome> desert = endlesssands$getDesertBiome();

        if (desert != null) {
            cir.setReturnValue(desert);
        }
    }

    @Unique
    private Holder<Biome> endlesssands$getDesertBiome() {
        if (this.endlesssands$cachedDesertBiome != null) {
            return this.endlesssands$cachedDesertBiome;
        }

        this.endlesssands$cachedDesertBiome = this.collectPossibleBiomes()
                .filter(holder -> holder.is(Biomes.DESERT))
                .findFirst()
                .orElse(null);

        return this.endlesssands$cachedDesertBiome;
    }
}