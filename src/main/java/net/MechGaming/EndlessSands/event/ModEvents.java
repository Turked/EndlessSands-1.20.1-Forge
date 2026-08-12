package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.effect.BuriedInSandState;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.BeginBuriedInSandS2CPacket;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public class ModEvents {
    // Negative moves the chain up; positive moves it down.
    private static final float HEAT_CHAIN_VERTICAL_OFFSET_ROWS = -1.0F;
    private static final String[] HEAT_CHAIN = {
            "cant_beat_the_heat",
            "too_hot_to_handle",
            "the_sun_is_a_deadly_laser",
            "scorched_earth"
    };
    private static final ResourceLocation ENDLESS_SANDS_ROOT =
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "what_have_i_done");
    private static final ResourceLocation FIND_A_VULTURE = advancementId("find_a_vulture");
    private static final ResourceLocation GUARD_UP = advancementId("guard_up");
    private static final ResourceLocation NEW_BEST_FRIEND = advancementId("new_best_friend");
    private static final String ENTERED_ENDLESS_SANDS_CRITERION = "entered_endless_sands";
    private static final String LOOK_AT_VULTURE_CRITERION = "look_at_vulture";
    private static final double VULTURE_LOOK_RANGE = 64.0D;
    private static final String HAS_BORN_OF_THE_SAND_SPAWN = EndlessSands.MOD_ID + ".has_born_of_the_sands";
    private static final String RESPAWN_IN_ENDLESS_SANDS = EndlessSands.MOD_ID + ".respawn_in_endless_sands";
    private static final String ENDLESS_SANDS_RESPAWN_POS = EndlessSands.MOD_ID + ".endless_sands_respawn_pos";
    private static final int RANDOM_SPAWN_RADIUS = 1_000_000;

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var advancementManager = event.getPlayerList().getServer().getAdvancements();
        Advancement root = advancementManager.getAdvancement(
                ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "what_have_i_done")
        );

        if (root == null || root.getDisplay() == null) {
            return;
        }

        float rootX = root.getDisplay().getX();
        float chainY = root.getDisplay().getY() + HEAT_CHAIN_VERTICAL_OFFSET_ROWS;

        for (int i = 0; i < HEAT_CHAIN.length; i++) {
            Advancement advancement = advancementManager.getAdvancement(
                    ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, HEAT_CHAIN[i])
            );

            if (advancement != null && advancement.getDisplay() != null) {
                advancement.getDisplay().setLocation(rootX + i + 1.0F, chainY);
            }
        }

        setAdvancementLocation(advancementManager.getAdvancement(advancementId("craft_yourself_some_twig_headgear")),
                rootX + 5.0F, chainY);
        setAdvancementLocation(advancementManager.getAdvancement(FIND_A_VULTURE), rootX + 1.0F, root.getDisplay().getY());
        setAdvancementLocation(advancementManager.getAdvancement(advancementId("what_a_cruddy_tree")),
                rootX + 2.0F, root.getDisplay().getY());
        setAdvancementLocation(advancementManager.getAdvancement(advancementId("what_goes_up_mustnt_come_down")),
                rootX + 3.0F, root.getDisplay().getY());
        setAdvancementLocation(advancementManager.getAdvancement(GUARD_UP),
                rootX + 2.0F, root.getDisplay().getY() + 1.0F);
        setAdvancementLocation(advancementManager.getAdvancement(NEW_BEST_FRIEND),
                rootX + 3.0F, root.getDisplay().getY() + 1.0F);
        setAdvancementLocation(advancementManager.getAdvancement(advancementId("the_ruins_of_the_oldworld")),
                rootX + 4.0F, root.getDisplay().getY() + 2.0F);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 5 != 0) {
            return;
        }

        Advancement advancement = player.getServer().getAdvancements().getAdvancement(FIND_A_VULTURE);
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            return;
        }

        if (isLookingAtVulture(player)) {
            player.getAdvancements().award(advancement, LOOK_AT_VULTURE_CRITERION);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer serverPlayer)){
            return;
        }

        if (serverPlayer.level().dimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL)) {
            ExpandedInventoryHelper.unlock(serverPlayer);
            awardEndlessSandsRoot(serverPlayer);
        }

        if (!EndlessSandsConfig.BORN_OF_THE_SAND.get()){
            return;
        }

        CompoundTag data = serverPlayer.getPersistentData();

        if (data.getBoolean(HAS_BORN_OF_THE_SAND_SPAWN)){
            return;
        }

        if (teleportToRandomEndlessSpawn(serverPlayer)) {
            serverPlayer.getPersistentData().putBoolean(
                    HAS_BORN_OF_THE_SAND_SPAWN,
                    true
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.level().dimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL)) {
            return;
        }

        prepareRandomEndlessRespawn(player);
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
            CompoundTag originalData = event.getOriginal().getPersistentData();
            if (originalData.contains(ENDLESS_SANDS_RESPAWN_POS)) {
                event.getEntity().getPersistentData().putLong(
                        ENDLESS_SANDS_RESPAWN_POS,
                        originalData.getLong(ENDLESS_SANDS_RESPAWN_POS)
                );
            }
        }

        if (event.getOriginal().getPersistentData().getBoolean(HAS_BORN_OF_THE_SAND_SPAWN)) {
            event.getEntity().getPersistentData().putBoolean(HAS_BORN_OF_THE_SAND_SPAWN, true);
        }

        ExpandedInventoryHelper.copyUnlock(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)
                || !event.getTo().equals(ModDimensions.ENDLESS_SANDS_LEVEL)) {
            return;
        }

        ExpandedInventoryHelper.unlock(serverPlayer);
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

        CompoundTag data = serverPlayer.getPersistentData();
        boolean teleported = data.contains(ENDLESS_SANDS_RESPAWN_POS)
                ? teleportToEndlessSpawn(serverPlayer, BlockPos.of(data.getLong(ENDLESS_SANDS_RESPAWN_POS)))
                : teleportToRandomEndlessSpawn(serverPlayer);
        if (teleported) {
            serverPlayer.getPersistentData().remove(
                    RESPAWN_IN_ENDLESS_SANDS
            );
            serverPlayer.getPersistentData().remove(ENDLESS_SANDS_RESPAWN_POS);
        }
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

        return teleportToEndlessSpawn(player, spawnPos);
    }

    private static boolean prepareRandomEndlessRespawn(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel endlessLevel = server.getLevel(ModDimensions.ENDLESS_SANDS_LEVEL);
        if (endlessLevel == null) {
            return false;
        }

        BlockPos spawnPos = getRandomEndlessSpawn(server, endlessLevel);
        player.setRespawnPosition(
                ModDimensions.ENDLESS_SANDS_LEVEL,
                spawnPos,
                player.getYRot(),
                true,
                false
        );
        player.getPersistentData().putBoolean(RESPAWN_IN_ENDLESS_SANDS, true);
        player.getPersistentData().putLong(ENDLESS_SANDS_RESPAWN_POS, spawnPos.asLong());
        return true;
    }

    private static boolean teleportToEndlessSpawn(ServerPlayer player, BlockPos spawnPos) {
        MinecraftServer server = player.getServer();
        ServerLevel endlessLevel = server.getLevel(ModDimensions.ENDLESS_SANDS_LEVEL);
        if (endlessLevel == null) {
            return false;
        }

        player.teleportTo(
                endlessLevel,
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot()
        );

        player.setRespawnPosition(
                ModDimensions.ENDLESS_SANDS_LEVEL,
                spawnPos,
                player.getYRot(),
                true,
                false
        );

        ExpandedInventoryHelper.unlock(player);
        awardEndlessSandsRoot(player);
        BuriedInSandState.activate(player);
        ModMessages.sendToPlayer(
                new BeginBuriedInSandS2CPacket(player.getId()),
                player
        );

        return true;
    }

    private static void awardEndlessSandsRoot(ServerPlayer player) {
        Advancement root = player.getServer().getAdvancements().getAdvancement(ENDLESS_SANDS_ROOT);
        if (root != null) {
            player.getAdvancements().award(root, ENTERED_ENDLESS_SANDS_CRITERION);
        }
    }

    private static boolean isLookingAtVulture(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookDirection = player.getViewVector(1.0F);
        Vec3 endPosition = eyePosition.add(lookDirection.scale(VULTURE_LOOK_RANGE));
        HitResult blockHit = player.level().clip(new ClipContext(
                eyePosition,
                endPosition,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        double maximumDistance = blockHit.getType() == HitResult.Type.MISS
                ? VULTURE_LOOK_RANGE * VULTURE_LOOK_RANGE
                : eyePosition.distanceToSqr(blockHit.getLocation());
        AABB searchBounds = player.getBoundingBox()
                .expandTowards(lookDirection.scale(VULTURE_LOOK_RANGE))
                .inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                eyePosition,
                endPosition,
                searchBounds,
                entity -> entity instanceof VultureEntity && entity.isPickable() && !entity.isSpectator(),
                maximumDistance
        );

        return entityHit != null && entityHit.getEntity() instanceof VultureEntity;
    }

    private static void setAdvancementLocation(Advancement advancement, float x, float y) {
        if (advancement != null && advancement.getDisplay() != null) {
            advancement.getDisplay().setLocation(x, y);
        }
    }

    private static ResourceLocation advancementId(String path) {
        return ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, path);
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
