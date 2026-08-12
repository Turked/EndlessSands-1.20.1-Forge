package net.MechGaming.EndlessSands.worldgen;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.VultureNestBlock;
import net.MechGaming.EndlessSands.block.entity.VultureNestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A compact, dimension-local index of generated vulture landmarks. Vultures query
 * this data instead of scanning blocks or loading chunks while travelling.
 */
public final class VultureHomeSavedData extends SavedData {
    private static final String DATA_NAME = "endlesssands_vulture_homes";
    private static final int NATURAL_HOME_ADOPTION_RADIUS = 48;
    public static final int MAX_RESIDENTS = 6;
    public static final int TWIGS_FOR_NEST = 10;

    private final Map<UUID, HomeRecord> homes = new HashMap<>();
    private final Map<Long, IslandRecord> islands = new HashMap<>();

    public static VultureHomeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VultureHomeSavedData::load,
                VultureHomeSavedData::new,
                DATA_NAME
        );
    }

    public void registerNaturalHome(UUID id, BlockPos basePos, BlockPos nestPos, Direction nestFacing,
                                    BoundingBox structureBounds) {
        HomeRecord existing = this.homes.get(id);
        if (existing != null) {
            existing.basePos = basePos.immutable();
            existing.originalNestPos = nestPos.immutable();
            existing.nestFacing = nestFacing;
            existing.bounds = structureBounds;
            setDirty();
            return;
        }

        HomeRecord home = new HomeRecord(id);
        home.basePos = basePos.immutable();
        home.originalNestPos = nestPos.immutable();
        home.currentNestPos = nestPos.immutable();
        home.nestFacing = nestFacing;
        home.bounds = structureBounds;
        home.hasNest = true;
        home.treeIntact = true;
        home.natural = true;
        this.homes.put(id, home);
        setDirty();
    }

    public void registerFloatingIsland(BlockPos origin, BoundingBox bounds) {
        long key = new ChunkPos(origin).toLong();
        IslandRecord previous = this.islands.put(key, new IslandRecord(origin.immutable(), bounds));
        if (previous == null || !previous.origin.equals(origin)) {
            setDirty();
        }
    }

    public Optional<HomeSnapshot> getHome(ServerLevel level, UUID id) {
        HomeRecord home = this.homes.get(id);
        if (home == null) {
            return Optional.empty();
        }
        refreshLoadedState(level, home);
        return Optional.of(home.snapshot());
    }

    public Optional<HomeSnapshot> findNearestAvailableNaturalHome(ServerLevel level, BlockPos origin,
                                                                  @Nullable UUID excludedHome) {
        return this.homes.values().stream()
                .filter(home -> home.natural && !home.domesticated)
                .filter(home -> excludedHome == null || !excludedHome.equals(home.id))
                .peek(home -> refreshLoadedState(level, home))
                .filter(home -> home.treeIntact)
                .filter(home -> home.adultResidents.size() < MAX_RESIDENTS)
                .filter(home -> horizontalDistanceSqr(home.basePos, origin)
                        <= NATURAL_HOME_ADOPTION_RADIUS * NATURAL_HOME_ADOPTION_RADIUS)
                .min(Comparator.comparingDouble(home -> home.basePos.distSqr(origin)))
                .map(HomeRecord::snapshot);
    }

    public Optional<HomeSnapshot> findNearestNaturalHome(ServerLevel level, BlockPos origin, int radius) {
        long radiusSqr = (long) radius * radius;
        return this.homes.values().stream()
                .filter(home -> home.natural && !home.domesticated)
                .peek(home -> refreshLoadedState(level, home))
                .filter(home -> horizontalDistanceSqr(home.basePos, origin) <= radiusSqr)
                .min(Comparator.comparingDouble(home -> home.basePos.distSqr(origin)))
                .map(HomeRecord::snapshot);
    }

    public Optional<HomeSnapshot> findNearbyDomesticHome(ServerLevel level, BlockPos origin, @Nullable UUID ownerId) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        return this.homes.values().stream()
                .filter(home -> home.domesticated && home.hasNest && home.currentNestPos != null)
                .filter(home -> ownerId == null || home.ownerId == null || ownerId.equals(home.ownerId))
                .filter(home -> Math.abs((home.currentNestPos.getX() >> 4) - originChunkX) <= 1)
                .filter(home -> Math.abs((home.currentNestPos.getZ() >> 4) - originChunkZ) <= 1)
                .peek(home -> refreshLoadedState(level, home))
                .filter(home -> home.hasNest && home.adultResidents.size() < MAX_RESIDENTS)
                .min(Comparator.comparingDouble(home -> home.currentNestPos.distSqr(origin)))
                .map(HomeRecord::snapshot);
    }

    public Optional<IslandSnapshot> findNearestIsland(BlockPos origin) {
        return this.islands.values().stream()
                .min(Comparator.comparingDouble(island -> island.origin.distSqr(origin)))
                .map(IslandRecord::snapshot);
    }

    public boolean addAdultResident(UUID homeId, UUID vultureId) {
        HomeRecord home = this.homes.get(homeId);
        if (home == null || home.natural && !home.treeIntact
                || (!home.adultResidents.contains(vultureId)
                && home.adultResidents.size() >= MAX_RESIDENTS)) {
            return false;
        }
        if (home.adultResidents.add(vultureId)) {
            setDirty();
        }
        return true;
    }

    public void removeAdultResident(UUID homeId, UUID vultureId) {
        HomeRecord home = this.homes.get(homeId);
        if (home != null && home.adultResidents.remove(vultureId)) {
            setDirty();
        }
    }

    public void markNestMissing(UUID homeId) {
        HomeRecord home = this.homes.get(homeId);
        if (home != null && !home.domesticated && home.hasNest) {
            home.hasNest = false;
            home.twigProgress = 0;
            setDirty();
        }
    }

    public void refreshTreeAt(ServerLevel level, BlockPos changedPos) {
        this.homes.values().stream()
                .filter(home -> home.natural && home.bounds.isInside(changedPos))
                .forEach(home -> refreshLoadedState(level, home));
    }

    public void domesticateHome(UUID homeId, UUID ownerId) {
        HomeRecord home = this.homes.get(homeId);
        if (home == null) {
            return;
        }
        home.domesticated = true;
        home.hasNest = false;
        home.currentNestPos = null;
        home.ownerId = ownerId;
        home.twigProgress = 0;
        setDirty();
    }

    public UUID registerPlacedNest(@Nullable UUID requestedId, BlockPos nestPos, Direction facing,
                                   @Nullable UUID ownerId) {
        UUID id = requestedId == null ? UUID.randomUUID() : requestedId;
        HomeRecord home = this.homes.computeIfAbsent(id, HomeRecord::new);
        home.currentNestPos = nestPos.immutable();
        if (home.originalNestPos == null) {
            home.originalNestPos = nestPos.immutable();
        }
        if (home.basePos == null) {
            home.basePos = nestPos.immutable();
        }
        home.nestFacing = facing;
        home.bounds = new BoundingBox(
                nestPos.getX(), nestPos.getY(), nestPos.getZ(),
                nestPos.getX(), nestPos.getY(), nestPos.getZ()
        );
        home.domesticated = true;
        home.natural = false;
        home.hasNest = true;
        home.treeIntact = false;
        home.ownerId = ownerId;
        home.twigProgress = 0;
        setDirty();
        return id;
    }

    public int depositTwig(ServerLevel level, UUID homeId) {
        HomeRecord home = this.homes.get(homeId);
        if (home == null || home.domesticated || home.hasNest) {
            return home == null ? 0 : home.twigProgress;
        }

        home.twigProgress = Math.min(TWIGS_FOR_NEST, home.twigProgress + 1);
        if (home.twigProgress >= TWIGS_FOR_NEST) {
            tryBuildNest(level, home);
        }
        setDirty();
        return home.twigProgress;
    }

    private void tryBuildNest(ServerLevel level, HomeRecord home) {
        BlockPos nestPos = home.originalNestPos;
        if (nestPos == null || !level.hasChunkAt(nestPos) || !level.getBlockState(nestPos).canBeReplaced()) {
            return;
        }

        level.setBlock(nestPos, ModBlocks.VULTURE_NEST.get().defaultBlockState()
                .setValue(VultureNestBlock.EGGS, 0)
                .setValue(VultureNestBlock.VARIANT, 0)
                .setValue(VultureNestBlock.FACING, home.nestFacing), Block.UPDATE_ALL);
        if (level.getBlockEntity(nestPos) instanceof VultureNestBlockEntity nest) {
            nest.initializeHome(home.id, true);
        }
        home.currentNestPos = nestPos.immutable();
        home.hasNest = true;
        home.twigProgress = 0;
    }

    private void refreshLoadedState(ServerLevel level, HomeRecord home) {
        boolean changed = false;
        if (home.natural && home.domesticated && home.currentNestPos == null) {
            // Older builds accidentally domesticated a natural home when its nest was broken.
            home.domesticated = false;
            home.ownerId = null;
            home.currentNestPos = home.originalNestPos;
            changed = true;
        }
        if (home.currentNestPos != null && level.hasChunkAt(home.currentNestPos)) {
            boolean hasNestNow = level.getBlockState(home.currentNestPos).is(ModBlocks.VULTURE_NEST.get());
            if (home.hasNest != hasNestNow) {
                home.hasNest = hasNestNow;
                if (!hasNestNow && !home.domesticated) {
                    home.twigProgress = 0;
                }
                changed = true;
            }
        }

        if (home.natural && home.basePos != null && level.hasChunkAt(home.basePos)) {
            boolean treeNow = hasCrudLog(level, home.bounds);
            if (home.treeIntact != treeNow) {
                home.treeIntact = treeNow;
                if (!treeNow) {
                    home.adultResidents.clear();
                }
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
    }

    private static long horizontalDistanceSqr(BlockPos first, BlockPos second) {
        long x = first.getX() - (long) second.getX();
        long z = first.getZ() - (long) second.getZ();
        return x * x + z * z;
    }

    private static boolean hasCrudLog(ServerLevel level, BoundingBox box) {
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            if (level.getBlockState(pos).is(ModBlocks.CRUD_LOG.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag homeList = new ListTag();
        this.homes.values().forEach(home -> homeList.add(home.save()));
        tag.put("Homes", homeList);

        ListTag islandList = new ListTag();
        this.islands.values().forEach(island -> islandList.add(island.save()));
        tag.put("Islands", islandList);
        return tag;
    }

    private static VultureHomeSavedData load(CompoundTag tag) {
        VultureHomeSavedData data = new VultureHomeSavedData();
        ListTag homeList = tag.getList("Homes", Tag.TAG_COMPOUND);
        for (int i = 0; i < homeList.size(); i++) {
            HomeRecord home = HomeRecord.load(homeList.getCompound(i));
            data.homes.put(home.id, home);
        }

        ListTag islandList = tag.getList("Islands", Tag.TAG_COMPOUND);
        for (int i = 0; i < islandList.size(); i++) {
            IslandRecord island = IslandRecord.load(islandList.getCompound(i));
            data.islands.put(new ChunkPos(island.origin).toLong(), island);
        }
        return data;
    }

    public record HomeSnapshot(UUID id, BlockPos basePos, @Nullable BlockPos nestPos, BlockPos originalNestPos,
                               BoundingBox bounds, boolean hasNest, boolean treeIntact, boolean natural,
                               boolean domesticated, @Nullable UUID ownerId, int adultResidents,
                               int twigProgress) {
        public ChunkPos homeChunk() {
            BlockPos anchor = nestPos == null ? originalNestPos : nestPos;
            return new ChunkPos(anchor);
        }
    }

    public record IslandSnapshot(BlockPos origin, BoundingBox bounds) {
        public ChunkPos chunk() {
            return new ChunkPos(origin);
        }
    }

    private static final class HomeRecord {
        private final UUID id;
        private BlockPos basePos;
        private BlockPos originalNestPos;
        @Nullable
        private BlockPos currentNestPos;
        private Direction nestFacing = Direction.NORTH;
        private BoundingBox bounds;
        private boolean hasNest;
        private boolean treeIntact;
        private boolean natural;
        private boolean domesticated;
        @Nullable
        private UUID ownerId;
        private int twigProgress;
        private final Set<UUID> adultResidents = new HashSet<>();

        private HomeRecord(UUID id) {
            this.id = id;
        }

        private HomeSnapshot snapshot() {
            return new HomeSnapshot(this.id, this.basePos, this.currentNestPos, this.originalNestPos,
                    this.bounds, this.hasNest, this.treeIntact, this.natural, this.domesticated,
                    this.ownerId, this.adultResidents.size(), this.twigProgress);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", this.id);
            putPos(tag, "Base", this.basePos);
            putPos(tag, "OriginalNest", this.originalNestPos);
            if (this.currentNestPos != null) {
                putPos(tag, "CurrentNest", this.currentNestPos);
            }
            tag.putInt("Facing", this.nestFacing.get2DDataValue());
            putBox(tag, this.bounds);
            tag.putBoolean("HasNest", this.hasNest);
            tag.putBoolean("TreeIntact", this.treeIntact);
            tag.putBoolean("Natural", this.natural);
            tag.putBoolean("Domesticated", this.domesticated);
            if (this.ownerId != null) {
                tag.putUUID("Owner", this.ownerId);
            }
            tag.putInt("TwigProgress", this.twigProgress);
            ListTag residentList = new ListTag();
            this.adultResidents.forEach(uuid -> {
                CompoundTag resident = new CompoundTag();
                resident.putUUID("Id", uuid);
                residentList.add(resident);
            });
            tag.put("Residents", residentList);
            return tag;
        }

        private static HomeRecord load(CompoundTag tag) {
            HomeRecord home = new HomeRecord(tag.getUUID("Id"));
            home.basePos = getPos(tag, "Base");
            home.originalNestPos = getPos(tag, "OriginalNest");
            home.currentNestPos = tag.contains("CurrentNestX") ? getPos(tag, "CurrentNest") : null;
            home.nestFacing = Direction.from2DDataValue(tag.getInt("Facing"));
            home.bounds = getBox(tag);
            home.hasNest = tag.getBoolean("HasNest");
            home.treeIntact = tag.getBoolean("TreeIntact");
            home.natural = tag.getBoolean("Natural");
            home.domesticated = tag.getBoolean("Domesticated");
            home.ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
            home.twigProgress = tag.getInt("TwigProgress");
            ListTag residents = tag.getList("Residents", Tag.TAG_COMPOUND);
            for (int i = 0; i < residents.size(); i++) {
                home.adultResidents.add(residents.getCompound(i).getUUID("Id"));
            }
            return home;
        }
    }

    private record IslandRecord(BlockPos origin, BoundingBox bounds) {
        private IslandSnapshot snapshot() {
            return new IslandSnapshot(this.origin, this.bounds);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            putPos(tag, "Origin", this.origin);
            putBox(tag, this.bounds);
            return tag;
        }

        private static IslandRecord load(CompoundTag tag) {
            return new IslandRecord(getPos(tag, "Origin"), getBox(tag));
        }
    }

    private static void putPos(CompoundTag tag, String prefix, BlockPos pos) {
        tag.putInt(prefix + "X", pos.getX());
        tag.putInt(prefix + "Y", pos.getY());
        tag.putInt(prefix + "Z", pos.getZ());
    }

    private static BlockPos getPos(CompoundTag tag, String prefix) {
        return new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"));
    }

    private static void putBox(CompoundTag tag, BoundingBox box) {
        tag.putInt("MinX", box.minX());
        tag.putInt("MinY", box.minY());
        tag.putInt("MinZ", box.minZ());
        tag.putInt("MaxX", box.maxX());
        tag.putInt("MaxY", box.maxY());
        tag.putInt("MaxZ", box.maxZ());
    }

    private static BoundingBox getBox(CompoundTag tag) {
        return new BoundingBox(tag.getInt("MinX"), tag.getInt("MinY"), tag.getInt("MinZ"),
                tag.getInt("MaxX"), tag.getInt("MaxY"), tag.getInt("MaxZ"));
    }
}
