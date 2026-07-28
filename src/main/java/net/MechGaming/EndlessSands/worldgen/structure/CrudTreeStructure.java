package net.MechGaming.EndlessSands.worldgen.structure;

import com.mojang.serialization.Codec;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CrudLogBlock;
import net.MechGaming.EndlessSands.block.custom.VultureNestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
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
        int eggs = 1 + random.nextInt(3);
        int birdDroppingsMask = Piece.createBirdDroppingsMask(random);

        return Optional.of(new Structure.GenerationStub(
                basePos,
                builder -> builder.addPiece(new Piece(basePos, direction, riseIndex, eggs, birdDroppingsMask))
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
        private static final String BIRD_DROPPINGS_MASK = "BirdDroppingsMask";

        private final BlockPos basePos;
        private final Direction direction;
        private final int riseIndex;
        private final int eggs;
        private final int birdDroppingsMask;

        public Piece(BlockPos basePos, Direction direction, int riseIndex, int eggs, int birdDroppingsMask) {
            super(ModStructurePieces.CRUD_TREE.get(), 0, createBoundingBox(basePos));
            this.basePos = basePos;
            this.direction = direction;
            this.riseIndex = riseIndex;
            this.eggs = eggs;
            this.birdDroppingsMask = birdDroppingsMask;
            this.setOrientation(direction);
        }

        public Piece(CompoundTag tag) {
            super(ModStructurePieces.CRUD_TREE.get(), tag);
            this.basePos = new BlockPos(tag.getInt(BASE_X), tag.getInt(BASE_Y), tag.getInt(BASE_Z));
            this.direction = Direction.from2DDataValue(tag.getInt(DIRECTION));
            this.riseIndex = clamp(tag.getInt(RISE_INDEX), 2, 3);
            this.eggs = clamp(tag.getInt(EGGS), 1, 3);
            this.birdDroppingsMask = tag.getInt(BIRD_DROPPINGS_MASK);
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
            tag.putInt(BIRD_DROPPINGS_MASK, this.birdDroppingsMask);
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
                boolean hasBirdDroppings = (this.birdDroppingsMask & (1 << (branchIndex - 1))) != 0;
                placeIfInside(level, box, branchPos,
                        horizontalLog.setValue(CrudLogBlock.BIRD_DROPPINGS, hasBirdDroppings));
            }

            BlockPos nestPos = this.basePos.relative(this.direction, 4).atY(branchY + 1);
            BlockState nest = ModBlocks.VULTURE_NEST.get().defaultBlockState()
                    .setValue(VultureNestBlock.EGGS, this.eggs);
            placeIfInside(level, box, nestPos, nest);
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