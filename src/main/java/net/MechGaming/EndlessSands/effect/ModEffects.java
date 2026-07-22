package net.MechGaming.EndlessSands.effect;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(
                    ForgeRegistries.MOB_EFFECTS,
                    EndlessSands.MOD_ID
            );

    public static final RegistryObject<MobEffect> HEATSTROKE =
            EFFECTS.register(
                    "heatstroke",
                    () -> new HeatstrokeEffect(
                            MobEffectCategory.HARMFUL,
                            0xD88A32
                    )
            );

    public static final RegistryObject<MobEffect> SPF =
            EFFECTS.register(
                    "spf",
                    () -> new SimpleEffect(
                            MobEffectCategory.BENEFICIAL,
                            0xFFD778
                    )
            );

    private ModEffects() {}

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}