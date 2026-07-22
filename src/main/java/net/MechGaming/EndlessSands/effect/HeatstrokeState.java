package net.MechGaming.EndlessSands.effect;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class HeatstrokeState {
    private static final String EXPOSURE =
            EndlessSands.MOD_ID + ".heatstroke_exposure";

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

    public static void reset(ServerPlayer player) {
        player.getPersistentData().remove(EXPOSURE);
    }
}