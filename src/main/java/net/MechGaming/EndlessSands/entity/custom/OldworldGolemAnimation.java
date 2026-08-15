package net.MechGaming.EndlessSands.entity.custom;

import net.minecraft.util.Mth;

public enum OldworldGolemAnimation {
    IDLE,
    WALK,
    ATTACK,
    INSPECT_ROSE,
    REMOVE_SAPLING;

    public static OldworldGolemAnimation byId(int id) {
        OldworldGolemAnimation[] values = values();
        return values[Mth.clamp(id, 0, values.length - 1)];
    }
}
