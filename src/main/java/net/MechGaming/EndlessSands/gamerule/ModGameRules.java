package net.MechGaming.EndlessSands.gamerule;

import net.minecraft.world.level.GameRules;

public final class ModGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> DEADLY_SUN = GameRules.register(
            "deadly_sun",
            GameRules.Category.PLAYER,
            GameRules.BooleanValue.create(true)
    );

    private ModGameRules() {}

    public static void register() {
    }
}
