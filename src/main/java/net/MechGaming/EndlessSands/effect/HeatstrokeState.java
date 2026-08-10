package net.MechGaming.EndlessSands.effect;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class HeatstrokeState {
    private static final String EXPOSURE =
            EndlessSands.MOD_ID + ".heatstroke_exposure";
    private static final String CURRENT_TIER =
            EndlessSands.MOD_ID + ".heatstroke_current_tier";

    private HeatstrokeState() {}

    public static int getExposure(ServerPlayer player) {
        return player.getPersistentData().getInt(EXPOSURE);
    }

    public static void setExposure(ServerPlayer player, int ticks) {
        player.getPersistentData().putInt(EXPOSURE, Math.max(0, ticks));
    }

    public static void increaseExposure(ServerPlayer player) {
        setExposure(player, getExposure(player) + 1);
    }

    public static void coolDown(ServerPlayer player, int amount) {
        setExposure(player, getExposure(player) - amount);
    }

    public static int getCurrentTier(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(CURRENT_TIER) ? data.getInt(CURRENT_TIER) : -1;
    }

    public static void setCurrentTier(ServerPlayer player, int tier) {
        if (tier < 0) {
            player.getPersistentData().remove(CURRENT_TIER);
        } else {
            player.getPersistentData().putInt(CURRENT_TIER, tier);
        }
    }

    public static void reset(ServerPlayer player) {
        player.getPersistentData().remove(EXPOSURE);
        player.getPersistentData().remove(CURRENT_TIER);
    }
}
