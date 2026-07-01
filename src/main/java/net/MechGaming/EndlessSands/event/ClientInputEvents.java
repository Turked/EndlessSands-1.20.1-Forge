package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.ability.DragoniteArmorSetBonus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles client-side input detection for Dragonite armor abilities.
 *
 * This class should only detect input.
 * It should not apply the real boost, because real movement must happen server-side.
 */
@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID, value = Dist.CLIENT)
public class ClientInputEvents {
    private static boolean jumpWasDownLastTick = false;
    private static boolean hasReleasedJumpSinceLeavingGround = false;

    /**
     * Runs once every client tick.
     *
     * Current job:
     * - detect a second jump press while airborne
     * - check if the player is wearing full Dragonite armor
     * - show a debug message only if both are true
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            jumpWasDownLastTick = false;
            hasReleasedJumpSinceLeavingGround = false;
            return;
        }

        LocalPlayer player = minecraft.player;

        boolean jumpDownNow = minecraft.options.keyJump.isDown();
        boolean jumpWasJustPressed = jumpDownNow && !jumpWasDownLastTick;
        boolean playerIsAirborne = !player.onGround();

        updateAirJumpState(playerIsAirborne, jumpDownNow);

        if (shouldRequestDragoniteBoost(player, playerIsAirborne, jumpWasJustPressed)) {
            player.displayClientMessage(Component.literal("Dragonite boost requested"), true);
        }

        jumpWasDownLastTick = jumpDownNow;
    }

    /**
     * Tracks whether the player has released jump after leaving the ground.
     *
     * This prevents the original ground jump from counting as the Dragonite boost.
     */
    private static void updateAirJumpState(boolean playerIsAirborne, boolean jumpDownNow) {
        if (!playerIsAirborne) {
            hasReleasedJumpSinceLeavingGround = false;
        } else if (!jumpDownNow) {
            hasReleasedJumpSinceLeavingGround = true;
        }
    }

    /**
     * Decides whether this exact tick should request a Dragonite boost.
     *
     * Right now this only controls the debug message.
     * Later, this same moment will send a packet to the server.
     */
    private static boolean shouldRequestDragoniteBoost(LocalPlayer player, boolean playerIsAirborne, boolean jumpWasJustPressed) {
        return playerIsAirborne
                && hasReleasedJumpSinceLeavingGround
                && jumpWasJustPressed
                && DragoniteArmorSetBonus.hasFullDragoniteArmor(player);
    }
}