package net.MechGaming.EndlessSands.client;

import net.MechGaming.EndlessSands.client.screen.ArmGuardRadialScreen;
import net.minecraft.client.Minecraft;

public final class ArmGuardClientScreens {
    private ArmGuardClientScreens() {
    }

    public static void openRadial(int vultureId, boolean hasHome) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.setScreen(new ArmGuardRadialScreen(vultureId, hasHome));
        }
    }
}
