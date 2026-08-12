package net.MechGaming.EndlessSands.worldgen.structure;

import com.mojang.serialization.Codec;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.worldgen.VultureHomeSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.Optional;

public class FloatingIslandStructure extends Structure {
    public static final Codec<FloatingIslandStructure> CODEC = Structure.simpleCodec(FloatingIslandStructure::new);
    private static final ResourceLocation TEMPLATE = ResourceLocation.fromNamespaceAndPath(
            EndlessSands.MOD_ID, "floating_island");
    private static final int SURFACE_OFFSET = 40;
    private static final int TEMPLATE_WIDTH = 14;
    private static final int TEMPLATE_HEIGHT = 16;
    private static final int TEMPLATE_DEPTH = 15;

    public FloatingIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int centerX = context.chunkPos().getMiddleBlockX();
        int centerZ = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getBaseHeight(centerX, centerZ,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos templatePos = new BlockPos(centerX - TEMPLATE_WIDTH / 2,
                surfaceY + SURFACE_OFFSET, centerZ - TEMPLATE_DEPTH / 2);

        return Optional.of(new GenerationStub(templatePos, builder -> builder.addPiece(
                new Piece(context.structureTemplateManager(), templatePos, surfaceY))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.FLOATING_ISLAND_TYPE.get();
    }

    public static class Piece extends TemplateStructurePiece {
        private static final String SURFACE_Y = "SurfaceY";
        private final int surfaceY;

        public Piece(StructureTemplateManager manager, BlockPos templatePos, int surfaceY) {
            super(ModStructurePieces.FLOATING_ISLAND.get(), 0, manager, TEMPLATE, TEMPLATE.toString(),
                    placementSettings(), templatePos);
            this.surfaceY = surfaceY;
            expandBoundingBox();
        }

        public Piece(StructureTemplateManager manager, CompoundTag tag) {
            super(ModStructurePieces.FLOATING_ISLAND.get(), tag, manager, ignored -> placementSettings());
            this.surfaceY = tag.getInt(SURFACE_Y);
            expandBoundingBox();
        }

        private static StructurePlaceSettings placementSettings() {
            return new StructurePlaceSettings()
                    .setMirror(Mirror.NONE)
                    .setRotation(Rotation.NONE)
                    .setIgnoreEntities(true)
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        private void expandBoundingBox() {
            this.boundingBox = new BoundingBox(
                    this.templatePosition.getX() - 4,
                    this.surfaceY,
                    this.templatePosition.getZ() - 4,
                    this.templatePosition.getX() + TEMPLATE_WIDTH + 3,
                    this.templatePosition.getY() + TEMPLATE_HEIGHT - 1,
                    this.templatePosition.getZ() + TEMPLATE_DEPTH + 3
            );
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putInt(SURFACE_Y, this.surfaceY);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                                RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.placeSettings.setBoundingBox(chunkBox);
            this.template.placeInWorld(level, this.templatePosition, pivot, this.placeSettings, random, 2);

            VultureHomeSavedData.get(level.getLevel()).registerFloatingIsland(this.templatePosition, this.boundingBox);
            placeSurfaceTwigs(level, chunkBox);
        }

        private void placeSurfaceTwigs(WorldGenLevel level, BoundingBox chunkBox) {
            int centerX = this.templatePosition.getX() + TEMPLATE_WIDTH / 2;
            int centerZ = this.templatePosition.getZ() + TEMPLATE_DEPTH / 2;
            RandomSource twigRandom = RandomSource.create(this.templatePosition.asLong() ^ 0x4F1BBCDCBFA53E0DL);

            for (int i = 0; i < 16; i++) {
                double angle = Mth.TWO_PI * i / 16.0D + (twigRandom.nextDouble() - 0.5D) * 0.22D;
                double radius = 10.0D + twigRandom.nextDouble() * 3.0D;
                int x = centerX + Mth.floor(Mth.cos((float) angle) * radius);
                int z = centerZ + Mth.floor(Mth.sin((float) angle) * radius);
                BlockPos twigPos = findSurface(level, x, z);
                if (twigPos != null && chunkBox.isInside(twigPos)
                        && level.getBlockState(twigPos).canBeReplaced()
                        && ModBlocks.TWIG.get().defaultBlockState().canSurvive(level, twigPos)) {
                    level.setBlock(twigPos, ModBlocks.TWIG.get().defaultBlockState(), 2);
                }
            }
        }

        private BlockPos findSurface(WorldGenLevel level, int x, int z) {
            int top = Math.min(this.surfaceY + 10, level.getMaxBuildHeight() - 1);
            for (int y = top; y >= level.getMinBuildHeight(); y--) {
                BlockPos support = new BlockPos(x, y, z);
                if (level.getBlockState(support).isFaceSturdy(level, support, net.minecraft.core.Direction.UP)) {
                    return support.above();
                }
            }
            return null;
        }

        @Override
        protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level,
                                        RandomSource random, BoundingBox box) {
        }
    }
}
