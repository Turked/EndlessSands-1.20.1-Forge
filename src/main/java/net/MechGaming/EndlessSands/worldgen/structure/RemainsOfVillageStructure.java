package net.MechGaming.EndlessSands.worldgen.structure;

import com.mojang.serialization.Codec;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.SuspiciousCursedSandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
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

import java.util.Optional;

public class RemainsOfVillageStructure extends Structure {
    public static final Codec<RemainsOfVillageStructure> CODEC =
            Structure.simpleCodec(RemainsOfVillageStructure::new);

    private static final ResourceLocation TEMPLATE = ResourceLocation.fromNamespaceAndPath(
            EndlessSands.MOD_ID, "remains_of_a_village");
    private static final int TEMPLATE_WIDTH = 18;
    private static final int TEMPLATE_HEIGHT = 3;
    private static final int TEMPLATE_DEPTH = 20;

    public RemainsOfVillageStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int centerX = context.chunkPos().getMiddleBlockX();
        int centerZ = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getBaseHeight(centerX, centerZ,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos templatePos = new BlockPos(centerX - TEMPLATE_WIDTH / 2,
                surfaceY, centerZ - TEMPLATE_DEPTH / 2);

        return Optional.of(new GenerationStub(templatePos, builder -> builder.addPiece(
                new Piece(context.structureTemplateManager(), templatePos))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.REMAINS_OF_A_VILLAGE_TYPE.get();
    }

    public static class Piece extends TemplateStructurePiece {
        public Piece(StructureTemplateManager manager, BlockPos templatePos) {
            super(ModStructurePieces.REMAINS_OF_A_VILLAGE.get(), 0, manager, TEMPLATE, TEMPLATE.toString(),
                    placementSettings(), templatePos);
        }

        public Piece(StructureTemplateManager manager, CompoundTag tag) {
            super(ModStructurePieces.REMAINS_OF_A_VILLAGE.get(), tag, manager, ignored -> placementSettings());
        }

        private static StructurePlaceSettings placementSettings() {
            return new StructurePlaceSettings()
                    .setMirror(Mirror.NONE)
                    .setRotation(Rotation.NONE)
                    .setIgnoreEntities(true)
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                                RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.placeSettings.setBoundingBox(chunkBox);
            this.template.placeInWorld(level, this.templatePosition, pivot, this.placeSettings, random, 2);
            seedSuspiciousSandLoot(level, random, chunkBox);
        }

        private void seedSuspiciousSandLoot(WorldGenLevel level, RandomSource random, BoundingBox chunkBox) {
            for (int x = 0; x < TEMPLATE_WIDTH; x++) {
                for (int y = 0; y < TEMPLATE_HEIGHT; y++) {
                    for (int z = 0; z < TEMPLATE_DEPTH; z++) {
                        BlockPos pos = this.templatePosition.offset(x, y, z);
                        if (chunkBox.isInside(pos)
                                && level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable
                                && brushable.getItem().isEmpty()) {
                            CompoundTag saved = brushable.saveWithFullMetadata();
                            if (!saved.contains("LootTable")) {
                                brushable.setLootTable(SuspiciousCursedSandBlock.ARCHAEOLOGY_LOOT, random.nextLong());
                                brushable.setChanged();
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level,
                                        RandomSource random, BoundingBox box) {
        }
    }
}
