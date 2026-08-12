package net.MechGaming.EndlessSands.worldgen.structure;

import com.mojang.serialization.Codec;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CrudLogBlock;
import net.MechGaming.EndlessSands.block.custom.VultureNestBlock;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.block.entity.VultureNestBlockEntity;
import net.MechGaming.EndlessSands.worldgen.VultureHomeSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Optional;
import java.util.UUID;

public class CrudTreeStructure extends Structure {
    public static final Codec<CrudTreeStructure> CODEC =
            Structure.simpleCodec(CrudTreeStructure::new);

    public CrudTreeStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int y = context.chunkGenerator().getBaseHeight(
                x,
                z,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );

        RandomSource random = context.random();
        BlockPos basePos = new BlockPos(x, y, z);
        Direction direction = Direction.from2DDataValue(random.nextInt(4));
        int riseIndex = 2 + random.nextInt(2);
        int eggs = random.nextInt(4);
        int eggVariant = eggs == 1 || eggs == 2 ? 1 + random.nextInt(3) : 0;
        int birdDroppingsMask = 0;
        UUID homeId = new UUID(random.nextLong(), random.nextLong());

        return Optional.of(new Structure.GenerationStub(
                basePos,
                builder -> builder.addPiece(new Piece(basePos, direction, riseIndex, eggs, eggVariant,
                        birdDroppingsMask, homeId))
        ));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CRUD_TREE_TYPE.get();
    }

    public static class Piece extends StructurePiece {
        private static final String BASE_X = "BaseX";
        private static final String BASE_Y = "BaseY";
        private static final String BASE_Z = "BaseZ";
        private static final String DIRECTION = "Direction";
        private static final String RISE_INDEX = "RiseIndex";
        private static final String EGGS = "Eggs";
        private static final String EGG_VARIANT = "EggVariant";
        private static final String BIRD_DROPPINGS_MASK = "BirdDroppingsMask";
        private static final String HOME_ID = "HomeId";

        private final BlockPos basePos;
        private final Direction direction;
        private final int riseIndex;
        private final int eggs;
        private final int eggVariant;
        private final int birdDroppingsMask;
        private final UUID homeId;

        public Piece(BlockPos basePos, Direction direction, int riseIndex, int eggs, int eggVariant,
                     int birdDroppingsMask, UUID homeId) {
            super(ModStructurePieces.CRUD_TREE.get(), 0, createBoundingBox(basePos));
            this.basePos = basePos;
            this.direction = direction;
            this.riseIndex = riseIndex;
            this.eggs = eggs;
            this.eggVariant = eggVariant;
            this.birdDroppingsMask = birdDroppingsMask;
            this.homeId = homeId;
            this.setOrientation(direction);
        }

        public Piece(CompoundTag tag) {
            super(ModStructurePieces.CRUD_TREE.get(), tag);
            this.basePos = new BlockPos(tag.getInt(BASE_X), tag.getInt(BASE_Y), tag.getInt(BASE_Z));
            this.direction = Direction.from2DDataValue(tag.getInt(DIRECTION));
            this.riseIndex = clamp(tag.getInt(RISE_INDEX), 2, 3);
            this.eggs = clamp(tag.getInt(EGGS), 0, 3);
            this.eggVariant = clamp(tag.getInt(EGG_VARIANT), 0, 3);
            this.birdDroppingsMask = tag.getInt(BIRD_DROPPINGS_MASK);
            this.homeId = tag.hasUUID(HOME_ID) ? tag.getUUID(HOME_ID)
                    : UUID.nameUUIDFromBytes(("crud_tree:" + this.basePos.asLong()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            this.setOrientation(this.direction);

        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putInt(BASE_X, this.basePos.getX());
            tag.putInt(BASE_Y, this.basePos.getY());
            tag.putInt(BASE_Z, this.basePos.getZ());
            tag.putInt(DIRECTION, this.direction.get2DDataValue());
            tag.putInt(RISE_INDEX, this.riseIndex);
            tag.putInt(EGGS, this.eggs);
            tag.putInt(EGG_VARIANT, this.eggVariant);
            tag.putInt(BIRD_DROPPINGS_MASK, this.birdDroppingsMask);
            tag.putUUID(HOME_ID, this.homeId);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
            BlockState verticalLog = ModBlocks.CRUD_LOG.get().defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .setValue(CrudLogBlock.BIRD_DROPPINGS, false);
            BlockState horizontalLog = ModBlocks.CRUD_LOG.get().defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, this.direction.getAxis())
                    .setValue(CrudLogBlock.BIRD_DROPPINGS, false);

            placeIfInside(level, box, this.basePos, verticalLog);
            placeIfInside(level, box, this.basePos.above(), verticalLog);

            int branchY = this.basePos.getY() + 1;

            for (int branchIndex = 1; branchIndex <= 4; branchIndex++) {
                if (branchIndex == this.riseIndex) {
                    BlockPos elbowPos = this.basePos.relative(this.direction, branchIndex - 1).atY(branchY + 1);
                    placeIfInside(level, box, elbowPos, verticalLog);
                    branchY++;
                }

                BlockPos branchPos = this.basePos.relative(this.direction, branchIndex).atY(branchY);
                placeIfInside(level, box, branchPos,
                        horizontalLog.setValue(CrudLogBlock.BIRD_DROPPINGS, false));
            }

            BlockPos nestPos = this.basePos.relative(this.direction, 4).atY(branchY + 1);
            if (box.isInside(nestPos)) {
                int generatedAdultCount = generatedAdultCountForEggs(this.eggs);
                VultureHomeSavedData homes = VultureHomeSavedData.get(level.getLevel());
                homes.registerNaturalHome(this.homeId, this.basePos, nestPos, this.direction.getClockWise(),
                        createBoundingBox(this.basePos));
                BlockState nest = ModBlocks.VULTURE_NEST.get().defaultBlockState()
                        .setValue(VultureNestBlock.EGGS, this.eggs)
                        .setValue(VultureNestBlock.VARIANT, this.eggVariant)
                        .setValue(VultureNestBlock.FACING, this.direction.getClockWise());
                placeIfInside(level, box, nestPos, nest);
                if (level.getBlockEntity(nestPos) instanceof VultureNestBlockEntity nestBlockEntity) {
                    nestBlockEntity.initializeHome(this.homeId, true);
                }
                spawnStructureVultures(level, random, nestPos, generatedAdultCount, this.homeId, homes);
            }
        }

        public UUID getHomeId() {
            return this.homeId;
        }

        private static int generatedAdultCountForEggs(int eggs) {
            return switch (eggs) {
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 4;
                default -> 6;
            };
        }

        private static void spawnStructureVultures(WorldGenLevel level, RandomSource random, BlockPos nestPos,
                                                   int generatedAdultCount, UUID homeId,
                                                   VultureHomeSavedData homes) {
            for (int i = 0; i < generatedAdultCount; i++) {
                VultureEntity vulture = ModEntities.VULTURE.get().create(level.getLevel());
                if (vulture == null) {
                    continue;
                }
                BlockPos spawnPos = nestPos.offset(random.nextInt(7) - 3, 6 + random.nextInt(5), random.nextInt(7) - 3);
                vulture.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
                vulture.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.STRUCTURE, null, null);
                if (!homes.addAdultResident(homeId, vulture.getUUID())) {
                    break;
                }
                vulture.setNaturalHome(homeId, nestPos);
                if (!level.addFreshEntity(vulture)) {
                    homes.removeAdultResident(homeId, vulture.getUUID());
                }
            }
        }

        private static BoundingBox createBoundingBox(BlockPos basePos) {
            return new BoundingBox(
                    basePos.getX() - 5,
                    basePos.getY(),
                    basePos.getZ() - 5,
                    basePos.getX() + 5,
                    basePos.getY() + 5,
                    basePos.getZ() + 5
            );
        }

        private static void placeIfInside(WorldGenLevel level, BoundingBox box, BlockPos pos, BlockState state) {
            if (box.isInside(pos) && level.getBlockState(pos).canBeReplaced()) {
                level.setBlock(pos, state, 2);
            }
        }

        private static int createBirdDroppingsMask(RandomSource random) {
            int targetCount = 3 + random.nextInt(2);
            int mask = 0;

            while (Integer.bitCount(mask) < targetCount) {
                mask |= 1 << random.nextInt(4);
            }

            return mask;
        }

        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
