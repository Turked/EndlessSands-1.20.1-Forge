package net.MechGaming.EndlessSands.client;

import net.MechGaming.EndlessSands.client.screen.BuriedInSandScreen;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.EscapeBuriedInSandC2SPacket;
import net.minecraft.client.Minecraft;

import java.util.HashSet;
import java.util.Set;

public final class BuriedInSandClientState {
    private static final Set<Integer> BURIED_PLAYERS =
            new HashSet<>();

    private static boolean dontAskAgain;

    private BuriedInSandClientState() {}

    public static void preview(int entityId) {
        BURIED_PLAYERS.add(entityId);

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || minecraft.player.getId() != entityId) {
            return;
        }

        /*
         * Always cover the screen during the short period before the
         * server's BeginBuriedInSand packet arrives.
         *
         * Do not auto-escape here. The server may not have activated
         * its authoritative state yet.
         */
        if (!(minecraft.screen instanceof BuriedInSandScreen)) {
            minecraft.setScreen(new BuriedInSandScreen());
        }
    }

    public static void begin(int entityId) {
        BURIED_PLAYERS.add(entityId);

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || minecraft.player.getId() != entityId) {
            return;
        }

        if (dontAskAgain) {
            ModMessages.sendToServer(
                    new EscapeBuriedInSandC2SPacket()
            );
        } else if (!(minecraft.screen instanceof BuriedInSandScreen)) {
            minecraft.setScreen(new BuriedInSandScreen());
        }
    }

    public static void end(int entityId) {
        BURIED_PLAYERS.remove(entityId);

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null
                && minecraft.player.getId() == entityId
                && minecraft.screen instanceof BuriedInSandScreen) {
            minecraft.setScreen(null);
        }
    }

    public static boolean isBuried(int entityId) {
        return BURIED_PLAYERS.contains(entityId);
    }

    public static boolean getDontAskAgain() {
        return dontAskAgain;
    }

    public static void setDontAskAgain(boolean value) {
        dontAskAgain = value;
    }
}