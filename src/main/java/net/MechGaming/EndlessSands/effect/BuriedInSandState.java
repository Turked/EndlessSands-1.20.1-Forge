package net.MechGaming.EndlessSands.effect;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class BuriedInSandState {
    private static final String ACTIVE =
            EndlessSands.MOD_ID + ".buried_in_sand.active";
    private static final String X =
            EndlessSands.MOD_ID + ".buried_in_sand.x";
    private static final String Y =
            EndlessSands.MOD_ID + ".buried_in_sand.y";
    private static final String Z =
            EndlessSands.MOD_ID + ".buried_in_sand.z";
    private static final String Y_ROT =
            EndlessSands.MOD_ID + ".buried_in_sand.y_rot";
    private static final String FOOD =
            EndlessSands.MOD_ID + ".buried_in_sand.food";
    private static final String SATURATION =
            EndlessSands.MOD_ID + ".buried_in_sand.saturation";

    private BuriedInSandState() {}

    public static boolean isActive(ServerPlayer player) {
        return player.getPersistentData().getBoolean(ACTIVE);
    }

    public static void activate(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        data.putBoolean(ACTIVE, true);
        data.putDouble(X, player.getX());
        data.putDouble(Y, player.getY());
        data.putDouble(Z, player.getZ());
        data.putFloat(Y_ROT, player.getYRot());
        data.putInt(FOOD, player.getFoodData().getFoodLevel());
        data.putFloat(
                SATURATION,
                player.getFoodData().getSaturationLevel()
        );

        player.removeAllEffects();
        player.clearFire();
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.fallDistance = 0.0F;
    }

    public static void deactivate(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        data.remove(ACTIVE);
        data.remove(X);
        data.remove(Y);
        data.remove(Z);
        data.remove(Y_ROT);
        data.remove(FOOD);
        data.remove(SATURATION);
    }

    public static double getX(ServerPlayer player) {
        return player.getPersistentData().getDouble(X);
    }

    public static double getY(ServerPlayer player) {
        return player.getPersistentData().getDouble(Y);
    }

    public static double getZ(ServerPlayer player) {
        return player.getPersistentData().getDouble(Z);
    }

    public static float getYRot(ServerPlayer player) {
        return player.getPersistentData().getFloat(Y_ROT);
    }

    public static int getFood(ServerPlayer player) {
        return player.getPersistentData().getInt(FOOD);
    }

    public static float getSaturation(ServerPlayer player) {
        return player.getPersistentData().getFloat(SATURATION);
    }
}