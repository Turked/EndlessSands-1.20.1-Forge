package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.block.custom.VultureNestBlock;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class VultureNestBlockEntity extends BlockEntity {
    private static final String PROGRESS_TICKS = "ProgressTicks";
    private static final String LAST_VULTURE_COUNT = "LastVultureCount";
    private static final String HOME_ID = "HomeId";
    private static final String NATURAL_HOME = "NaturalHome";
    private static final int EGG_INTERVAL_TICKS = 20 * 60 * 20;
    private static final int SCAN_INTERVAL_TICKS = 100;

    private int progressTicks;
    private int lastVultureCount;
    private int scanCooldown;
    @Nullable
    private UUID homeId;
    private boolean naturalHome;

    public VultureNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VULTURE_NEST.get(), pos, state);
    }

    public void initializeHome(UUID homeId, boolean naturalHome) {
        this.homeId = homeId;
        this.naturalHome = naturalHome;
        setChanged();
    }

    @Nullable
    public UUID getHomeId() {
        return this.homeId;
    }

    public boolean isNaturalHome() {
        return this.naturalHome;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VultureNestBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (--blockEntity.scanCooldown > 0) {
            return;
        }
        blockEntity.scanCooldown = SCAN_INTERVAL_TICKS;

        int vultureCount = countNearbyAdultVultures(serverLevel, pos);
        if (vultureCount < 2 || vultureCount > 6) {
            blockEntity.progressTicks = 0;
            blockEntity.lastVultureCount = vultureCount;
            blockEntity.setChanged();
            if (state.getValue(VultureNestBlock.EGGS) > 0) {
                attractHostiles(serverLevel, pos);
            }
            return;
        }

        if (blockEntity.lastVultureCount != vultureCount) {
            blockEntity.progressTicks = 0;
            blockEntity.lastVultureCount = vultureCount;
        }

        blockEntity.progressTicks += SCAN_INTERVAL_TICKS;
        if (state.getValue(VultureNestBlock.EGGS) > 0) {
            attractHostiles(serverLevel, pos);
        }

        if (blockEntity.progressTicks >= EGG_INTERVAL_TICKS) {
            blockEntity.progressTicks = 0;
            layEggs(serverLevel, pos, state, vultureCount);
        }
        blockEntity.setChanged();
    }

    public static int countNearbyAdultVultures(ServerLevel level, BlockPos nestPos) {
        return level.getEntitiesOfClass(VultureEntity.class, eggGridBox(nestPos),
                vulture -> vulture.isAlive() && !vulture.isBaby()).size();
    }

    private static AABB eggGridBox(BlockPos nestPos) {
        ChunkPos center = new ChunkPos(nestPos);
        int minX = (center.x - 1) << 4;
        int minZ = (center.z - 1) << 4;
        int maxX = ((center.x + 2) << 4);
        int maxZ = ((center.z + 2) << 4);
        return new AABB(minX, -64, minZ, maxX, 384, maxZ);
    }

    private static void layEggs(ServerLevel level, BlockPos pos, BlockState state, int vultureCount) {
        int nestEggs = state.getValue(VultureNestBlock.EGGS);
        if (nestEggs >= VultureNestBlock.MAX_EGGS) {
            return;
        }

        int eggsToCreate = Mth.clamp(vultureCount / 2, 1, 6);
        int visualSpace = Math.max(0, VultureNestBlock.MAX_EGGS - nestEggs);
        int eggsIntoNest = Math.min(visualSpace, eggsToCreate);

        if (eggsIntoNest > 0) {
            int nextEggs = nestEggs + eggsIntoNest;
            int nextVariant = VultureNestBlock.randomVariantForEggCount(nextEggs, level.getRandom());
            level.setBlock(pos, state.setValue(VultureNestBlock.EGGS, nextEggs)
                    .setValue(VultureNestBlock.VARIANT, nextVariant), Block.UPDATE_ALL);
        }
    }

    private static void attractHostiles(ServerLevel level, BlockPos pos) {
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, new AABB(pos).inflate(16.0D),
                monster -> monster.getTarget() == null);
        for (Monster monster : monsters) {
            monster.getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 1.0D);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(PROGRESS_TICKS, this.progressTicks);
        tag.putInt(LAST_VULTURE_COUNT, this.lastVultureCount);
        if (this.homeId != null) {
            tag.putUUID(HOME_ID, this.homeId);
        }
        tag.putBoolean(NATURAL_HOME, this.naturalHome);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.progressTicks = tag.getInt(PROGRESS_TICKS);
        this.lastVultureCount = tag.getInt(LAST_VULTURE_COUNT);
        this.homeId = tag.hasUUID(HOME_ID) ? tag.getUUID(HOME_ID) : null;
        this.naturalHome = tag.getBoolean(NATURAL_HOME);
    }
}
