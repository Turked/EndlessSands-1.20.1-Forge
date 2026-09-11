package net.MechGaming.EndlessSands.worldgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

/** A template-backed structure available exclusively through the place command. */
public final class BattlegroundRuinsStructure extends Structure {
    public static final Codec<BattlegroundRuinsStructure> CODEC =
            Structure.simpleCodec(BattlegroundRuinsStructure::new);
    public static final ResourceLocation TEMPLATE = ResourceLocation.fromNamespaceAndPath(
            "endless_sands", "battleground_ruins");

    public BattlegroundRuinsStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int centerX = context.chunkPos().getMiddleBlockX();
        int centerZ = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getBaseHeight(centerX, centerZ,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        StructureTemplate template = context.structureTemplateManager().getOrCreate(TEMPLATE);
        Vec3i size = template.getSize();
        BlockPos templatePos = new BlockPos(centerX - size.getX() / 2,
                surfaceY, centerZ - size.getZ() / 2);

        return Optional.of(new GenerationStub(templatePos, builder -> builder.addPiece(
                new Piece(context.structureTemplateManager(), templatePos))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.BATTLEGROUND_RUINS_TYPE.get();
    }

    public static final class Piece extends TemplateStructurePiece {
        public Piece(StructureTemplateManager manager, BlockPos templatePos) {
            super(ModStructurePieces.BATTLEGROUND_RUINS.get(), 0, manager, TEMPLATE,
                    TEMPLATE.toString(), placementSettings(), templatePos);
        }

        public Piece(StructureTemplateManager manager, CompoundTag tag) {
            super(ModStructurePieces.BATTLEGROUND_RUINS.get(), tag, manager,
                    ignored -> placementSettings());
        }

        private static StructurePlaceSettings placementSettings() {
            return new StructurePlaceSettings()
                    .setMirror(Mirror.NONE)
                    .setRotation(Rotation.NONE)
                    .setIgnoreEntities(false)
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level,
                                        RandomSource random, BoundingBox box) {
        }
    }
}
