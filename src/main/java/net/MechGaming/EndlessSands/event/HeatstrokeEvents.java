package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.effect.BuriedInSandState;
import net.MechGaming.EndlessSands.effect.HeatstrokeState;
import net.MechGaming.EndlessSands.effect.ModEffects;
import net.MechGaming.EndlessSands.util.ModTags;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public final class HeatstrokeEvents {
    private static final int ONE_SECOND = 20;
    private static final int TIER_I_START = 60 * ONE_SECOND;
    private static final int TIER_II_START = 180 * ONE_SECOND;
    private static final int TIER_III_START = 240 * ONE_SECOND;
    private static final int TIER_IV_START = 270 * ONE_SECOND;

    private HeatstrokeEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END ||
                !(event.player instanceof ServerPlayer player)) {
            return;
        }

        boolean exposed = isExposedToDesertSun(player);

        if (exposed) {
            HeatstrokeState.increaseExposure(player);
        } else {
            // Four times faster recovery than accumulation.
            HeatstrokeState.coolDown(player, 4);
        }

        int exposure = HeatstrokeState.getExposure(player);
        int tier = getAllowedTier(player, exposure);

        if (tier < 0) {
            player.removeEffect(ModEffects.HEATSTROKE.get());
            return;
        }

        // Short duration, continuously refreshed while conditions remain valid.
        player.addEffect(new MobEffectInstance(
                ModEffects.HEATSTROKE.get(),
                30,
                tier,
                false,
                true,
                true
        ));

        applyTierConsequences(player, tier);
    }

    private static boolean isExposedToDesertSun(ServerPlayer player) {
        if (!player.level().dimension().equals(
                ModDimensions.ENDLESS_SANDS_LEVEL
        )) {
            return false;
        }

        if (BuriedInSandState.isActive(player)) {
            return false;
        }

        return player.level().isDay();
    }

    private static int getAllowedTier(
            ServerPlayer player,
            int exposure
    ) {
        if (exposure < TIER_I_START) {
            return -1;
        }

        int sunGear = countSunGear(player);

        int naturalTier;
        if (exposure >= TIER_IV_START) {
            naturalTier = 3;
        } else if (exposure >= TIER_III_START) {
            naturalTier = 2;
        } else if (exposure >= TIER_II_START) {
            naturalTier = 1;
        } else {
            naturalTier = 0;
        }

        /*
         * Four pieces prevent tier I.
         * Three pieces prevent tier II and above.
         * One piece prevents tier III and above.
         */
        int armorCap;
        if (sunGear >= 4) {
            armorCap = -1;
        } else if (sunGear >= 3) {
            armorCap = 0;
        } else if (sunGear >= 1) {
            armorCap = 1;
        } else {
            armorCap = 3;
        }

        int result = Math.min(naturalTier, armorCap);

        if (result == 3 && hasTierFourProtection(player)) {
            result = 2;
        }

        return result;
    }

    private static int countSunGear(ServerPlayer player) {
        int count = 0;

        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.is(ModTags.Items.IS_SUN_GEAR)) {
                count++;
            }
        }

        return count;
    }

    private static boolean hasTierFourProtection(
            ServerPlayer player
    ) {
        ItemStack helmet =
                player.getItemBySlot(EquipmentSlot.HEAD);

        return helmet.is(ModTags.Items.DOES_GIVE_SHADE)
                || player.hasEffect(ModEffects.SPF.get())
                || player.isInWaterOrBubble()
                || !player.level().canSeeSky(
                BlockPos.containing(
                        player.getX(),
                        player.getEyeY(),
                        player.getZ()
                )
        );
    }

    private static void applyTierConsequences(
            ServerPlayer player,
            int tier
    ) {
        // Tier I and every tier above it.
        player.addEffect(new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                30,
                0,
                false,
                false,
                false
        ));

        // Tier II and every tier above it.
        if (tier >= 1) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    30,
                    2,
                    false,
                    false,
                    false
            ));
        }

        // Tier III and every tier above it.
        if (tier >= 2) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.HUNGER,
                    30,
                    0,
                    false,
                    false,
                    false
            ));
        }

        // Tier IV: half a heart every 40 ticks.
        if (tier >= 3 && player.tickCount % 40 == 0) {
            player.hurt(
                    player.damageSources().magic(),
                    1.0F
            );
        }
    }
}