package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.MechGaming.EndlessSands.block.custom.ZenioniteSacrificeHolderBlock;
import net.MechGaming.EndlessSands.block.entity.ZenionitePortalFrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public final class DragonEggSacrificeEvents {
    public static final int DURATION_TICKS = 30 * 20;
    private static final int COVER_RADIUS = 24;
    private static final int COVER_DIAMETER = COVER_RADIUS * 2 + 1;
    private static final int COVER_COLUMNS = COVER_DIAMETER * COVER_DIAMETER;
    private static final int COLUMNS_PER_TICK = 5;
    private static final int CAMERA_PAN_TICKS = 80;
    private static final Map<UUID, Ritual> ACTIVE = new HashMap<>();

    private DragonEggSacrificeEvents() { }

    public static void tryBegin(ServerPlayer player, BlockPos holderPos, InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel level)
                || player.distanceToSqr(holderPos.getX() + 0.5D, holderPos.getY() + 0.5D,
                holderPos.getZ() + 0.5D) > 64.0D
                || ACTIVE.containsKey(player.getUUID())
                || !ZenionitePortalFrameBlockEntity.canAcceptDragonEgg(level, holderPos)) {
            return;
        }
        BlockState holder = level.getBlockState(holderPos);
        ItemStack sacrifice = player.getItemInHand(hand);
        if (!holder.is(ModBlocks.ZENIONITE_SACRIFICE_HOLDER.get())
                || holder.getValue(ZenioniteSacrificeHolderBlock.OCCUPIED)
                || !sacrifice.is(Items.DRAGON_EGG)) {
            return;
        }

        // The sacrifice is intentionally consumed even in creative mode: this action is irreversible.
        sacrifice.shrink(1);
        level.setBlock(holderPos, holder.setValue(ZenioniteSacrificeHolderBlock.OCCUPIED, true),
                Block.UPDATE_ALL);
        ACTIVE.put(player.getUUID(), new Ritual(holderPos.below(), holderPos,
                player.getX(), player.getY(), player.getZ(), player.getXRot()));
    }

    public static boolean isInProgress(ServerLevel level, BlockPos portalCenter) {
        return ACTIVE.values().stream().anyMatch(ritual -> ritual.portalCenter.equals(portalCenter)
                && level.dimension().equals(ritual.dimension));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Ritual ritual = ACTIVE.get(player.getUUID());
        if (ritual == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (ritual.dimension == null) {
            ritual.dimension = level.dimension();
        }
        if (!level.dimension().equals(ritual.dimension)) {
            ACTIVE.remove(player.getUUID());
            return;
        }

        ritual.ticks++;
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        float pitch = player.getXRot();
        int panStart = DURATION_TICKS - CAMERA_PAN_TICKS;
        if (ritual.ticks >= panStart) {
            float progress = Mth.clamp((ritual.ticks - panStart) / (float) CAMERA_PAN_TICKS, 0.0F, 1.0F);
            float eased = progress * progress * (3.0F - 2.0F * progress);
            pitch = Mth.lerp(eased, ritual.panStartPitch, -90.0F);
        } else {
            ritual.panStartPitch = player.getXRot();
        }
        player.connection.teleport(ritual.x, ritual.y, ritual.z, player.getYRot(), pitch);

        coverVisibleGround(level, ritual);
        spawnSandstorm(level, ritual);
        if (!ritual.finalBlockSpawned && ritual.ticks >= DURATION_TICKS - 35) {
            ritual.finalBlockSpawned = true;
            BlockPos fallPos = BlockPos.containing(ritual.x, ritual.y + 9.0D, ritual.z);
            if (level.getBlockState(fallPos).isAir()) {
                level.setBlock(fallPos, ModBlocks.CURSED_SAND.get().defaultBlockState(), Block.UPDATE_ALL);
                FallingBlockEntity.fall(level, fallPos, ModBlocks.CURSED_SAND.get().defaultBlockState());
            }
        }

        if (ritual.ticks >= DURATION_TICKS) {
            ACTIVE.remove(player.getUUID());
            ZenionitePortalFrameBlockEntity.completeDragonEggSacrifice(level, ritual.portalCenter);
            ModEvents.enterEndlessSands(player);
        }
    }

    private static void coverVisibleGround(ServerLevel level, Ritual ritual) {
        int start = Math.floorMod((ritual.ticks - 1) * COLUMNS_PER_TICK, COVER_COLUMNS);
        for (int offset = 0; offset < COLUMNS_PER_TICK; offset++) {
            int index = (start + offset) % COVER_COLUMNS;
            int dx = index % COVER_DIAMETER - COVER_RADIUS;
            int dz = index / COVER_DIAMETER - COVER_RADIUS;
            int x = Mth.floor(ritual.x) + dx;
            int z = Mth.floor(ritual.z) + dz;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos target = new BlockPos(x, y, z);
            BlockState current = level.getBlockState(target);
            if (current.is(ModBlocks.CURSED_SAND_LAYER.get())) {
                int layers = Math.min(3, current.getValue(CursedSandLayerBlock.LAYERS) + 1);
                level.setBlock(target, current.setValue(CursedSandLayerBlock.LAYERS, layers),
                        Block.UPDATE_CLIENTS);
            } else if (current.isAir() || current.canBeReplaced()) {
                level.setBlock(target, ModBlocks.CURSED_SAND_LAYER.get().defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }
    }

    private static void spawnSandstorm(ServerLevel level, Ritual ritual) {
        if ((ritual.ticks & 1) != 0) return;
        BlockParticleOption particle = new BlockParticleOption(
                ParticleTypes.FALLING_DUST, ModBlocks.CURSED_SAND.get().defaultBlockState());
        level.sendParticles(particle, ritual.x, ritual.y + 8.0D, ritual.z,
                70, 18.0D, 10.0D, 18.0D, 0.08D);

        if (ritual.ticks % 10 == 0) {
            int x = Mth.floor(ritual.x) + level.random.nextInt(25) - 12;
            int z = Mth.floor(ritual.z) + level.random.nextInt(25) - 12;
            BlockPos spawn = new BlockPos(x, Mth.floor(ritual.y) + 15, z);
            if (level.getBlockState(spawn).isAir()) {
                level.setBlock(spawn, ModBlocks.CURSED_SAND_LAYER.get().defaultBlockState(),
                        Block.UPDATE_CLIENTS);
                FallingBlockEntity.fall(level, spawn,
                        ModBlocks.CURSED_SAND_LAYER.get().defaultBlockState());
            }
        }
    }

    private static final class Ritual {
        private final BlockPos portalCenter;
        @SuppressWarnings("unused")
        private final BlockPos holderPos;
        private final double x;
        private final double y;
        private final double z;
        private net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        private int ticks;
        private float panStartPitch;
        private boolean finalBlockSpawned;

        private Ritual(BlockPos portalCenter, BlockPos holderPos,
                       double x, double y, double z, float panStartPitch) {
            this.portalCenter = portalCenter.immutable();
            this.holderPos = holderPos.immutable();
            this.x = x;
            this.y = y;
            this.z = z;
            this.panStartPitch = panStartPitch;
        }
    }
}
