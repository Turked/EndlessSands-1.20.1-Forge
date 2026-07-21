package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.effect.BuriedInSandState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = EndlessSands.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class BuriedInSandEvents {
    private BuriedInSandEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || !BuriedInSandState.isActive(player)) {
            return;
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.setSprinting(false);
        player.fallDistance = 0.0F;
        player.clearFire();
        player.setAirSupply(player.getMaxAirSupply());

        player.getFoodData().setFoodLevel(
                BuriedInSandState.getFood(player)
        );
        player.getFoodData().setSaturation(
                BuriedInSandState.getSaturation(player)
        );

        player.removeAllEffects();

        double dx = player.getX() - BuriedInSandState.getX(player);
        double dy = player.getY() - BuriedInSandState.getY(player);
        double dz = player.getZ() - BuriedInSandState.getZ(player);

        if (dx * dx + dy * dy + dz * dz > 0.0001D) {
            player.connection.teleport(
                    BuriedInSandState.getX(player),
                    BuriedInSandState.getY(player),
                    BuriedInSandState.getZ(player),
                    BuriedInSandState.getYRot(player),
                    0.0F
            );
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(
            LivingAttackEvent event
    ) {
        if (event.getEntity() instanceof ServerPlayer player
                && BuriedInSandState.isActive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onTargetChanged(
            LivingChangeTargetEvent event
    ) {
        if (event.getNewTarget() instanceof ServerPlayer player
                && BuriedInSandState.isActive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(
            MobEffectEvent.Applicable event
    ) {
        if (event.getEntity() instanceof ServerPlayer player
                && BuriedInSandState.isActive(player)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {
        cancelInteraction(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onRightClickItem(
            PlayerInteractEvent.RightClickItem event
    ) {
        cancelInteraction(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(
            PlayerInteractEvent.LeftClickBlock event
    ) {
        cancelInteraction(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onEntityInteract(
            PlayerInteractEvent.EntityInteract event
    ) {
        cancelInteraction(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(
            PlayerInteractEvent.EntityInteractSpecific event
    ) {
        cancelInteraction(event.getEntity(), event);
    }

    private static void cancelInteraction(
            Player player,
            net.minecraftforge.eventbus.api.Event event
    ) {
        if (player instanceof ServerPlayer serverPlayer
                && BuriedInSandState.isActive(serverPlayer)
                && event.isCancelable()) {
            event.setCanceled(true);
        }
    }
}