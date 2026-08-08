package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.worldgen.biome.ModBiomes;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public class VultureEvents {
    private static final int SKY_SPAWN_CHECK_INTERVAL = 20 * 10;
    private static final int SKY_SPAWN_CHANCE = 120;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % SKY_SPAWN_CHECK_INTERVAL != 0) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL) || !level.isDay()) {
            return;
        }

        if (!level.getBiome(player.blockPosition()).is(ModBiomes.ENDLESS_DESERT)) {
            return;
        }

        if (level.random.nextInt(SKY_SPAWN_CHANCE) != 0) {
            return;
        }

        int nearby = level.getEntitiesOfClass(VultureEntity.class, player.getBoundingBox().inflate(96.0D),
                vulture -> vulture.isAlive() && !vulture.isTame()).size();
        if (nearby >= 3) {
            return;
        }

        int x = player.getBlockX() + level.random.nextInt(97) - 48;
        int z = player.getBlockZ() + level.random.nextInt(97) - 48;
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawnPos = new BlockPos(x, groundY + 30, z);

        VultureEntity vulture = ModEntities.VULTURE.get().create(level);
        if (vulture == null) {
            return;
        }
        vulture.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        vulture.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null, null);
        vulture.markDaySpawned();
        level.addFreshEntity(vulture);
    }

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
