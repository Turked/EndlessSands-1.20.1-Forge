package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public class ModEvents {
    private static final String HAS_BORN_OF_THE_SAND_SPAWN = EndlessSands.MOD_ID + ".has_born_of_the_sands";
    private static final String RESPAWN_IN_ENDLESS_SANDS = EndlessSands.MOD_ID + ".respawn_in_endless_sands";
    private static final int RANDOM_SPAWN_RADIUS = 1_000_000;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        if (!EndlessSandsConfig.BORN_OF_THE_SAND.get()){
            return;
        }

        if(!(event.getEntity() instanceof ServerPlayer serverPlayer)){
            return;
        }

        CompoundTag data = serverPlayer.getPersistentData();

        if (data.getBoolean(HAS_BORN_OF_THE_SAND_SPAWN)){
            return;
        }

        serverPlayer.getServer().execute(() -> {
            if (teleportToRandomEndlessSpawn(serverPlayer)){
                serverPlayer.getPersistentData().putBoolean(HAS_BORN_OF_THE_SAND_SPAWN, true);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event){
        if (!event.isWasDeath()){
            return;
        }

        boolean diedInEndlessSands = event.getOriginal().level().dimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL)
                || event.getOriginal().getPersistentData().getBoolean(RESPAWN_IN_ENDLESS_SANDS);

        if (diedInEndlessSands){
            event.getEntity().getPersistentData().putBoolean(RESPAWN_IN_ENDLESS_SANDS, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event){
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)){
            return;
        }

        boolean shouldRandomRespawn = serverPlayer.level().dimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL)
                || serverPlayer.getPersistentData().getBoolean(RESPAWN_IN_ENDLESS_SANDS);

        if (!shouldRandomRespawn){
            return;
        }

        serverPlayer.getServer().execute(() -> {
            if (teleportToRandomEndlessSpawn(serverPlayer)){
                serverPlayer.getPersistentData().remove(RESPAWN_IN_ENDLESS_SANDS);
            }
        });
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event){
        if (!(event.getEntity() instanceof ServerPlayer player)){
            return;
        }

        if (player.level().dimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL)
        && !event.getDimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL)){
            event.setCanceled(true);
        }
    }

    private static boolean teleportToRandomEndlessSpawn(ServerPlayer player){
        MinecraftServer server = player.getServer();
        ServerLevel endlessLevel = server.getLevel(ModDimensions.ENDLESS_SANDS_LEVEL);

        if (endlessLevel == null){
            return false;
        }

        BlockPos spawnPos = getRandomEndlessSpawn(server, endlessLevel);

        player.teleportTo(
                endlessLevel,
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot()
        );

        player.setRespawnPosition(ModDimensions.ENDLESS_SANDS_LEVEL, spawnPos, player.getYRot(), true, false);

        return true;
    }

    private static BlockPos getRandomEndlessSpawn(MinecraftServer server, ServerLevel endlessLevel){
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        BlockPos origin = overworld != null ? overworld.getSharedSpawnPos() : BlockPos.ZERO;

        int x = origin.getX() + endlessLevel.getRandom().nextInt(RANDOM_SPAWN_RADIUS * 2 + 1) - RANDOM_SPAWN_RADIUS;
        int z = origin.getZ() + endlessLevel.getRandom().nextInt(RANDOM_SPAWN_RADIUS * 2 + 1) - RANDOM_SPAWN_RADIUS;

        endlessLevel.getChunk(x >> 4, z >> 4);

        int y = endlessLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        return new BlockPos(x, y ,z);
    }
}
