package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public class VultureEvents {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        UUID playerId = player.getUUID();
        List<VultureEntity> vultures = level.getEntitiesOfClass(VultureEntity.class,
                new AABB(player.blockPosition()).inflate(512.0D),
                vulture -> vulture.isAlive() && !vulture.isBaby() && vulture.isTame() && playerId.equals(vulture.getOwnerUUID()));
        if (vultures.isEmpty()) {
            return;
        }

        List<ItemStack> carriedStacks = new ArrayList<>();
        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext() && carriedStacks.size() < 3) {
            ItemEntity drop = iterator.next();
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            carriedStacks.add(stack.copy());
            iterator.remove();
        }

        if (!carriedStacks.isEmpty()) {
            vultures.get(0).beginDeathCourier(player, carriedStacks);
        }
    }
}
