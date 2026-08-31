package net.MechGaming.EndlessSands.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.MechGaming.EndlessSands.block.custom.ZenioniteStairBlock;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity.Lane;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.List;

/** Renders the two independently logged halves without exposing either as vanilla waterlogging. */
public class ZenioniteStairRenderer implements BlockEntityRenderer<ZenioniteStairBlockEntity> {
    private static final float CELL_SIZE = 0.5F;
    private static final float BOTTOM_SEAT_SURFACE = 0.503F;
    private static final float TOP_SEAT_CEILING = 0.497F;
    private static final float FACE_INSET = 0.002F;

    public ZenioniteStairRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            ZenioniteStairBlockEntity stair,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Level level = stair.getLevel();
        if (level == null) {
            return;
        }

        BlockState state = stair.getBlockState();
        if (!(state.getBlock() instanceof ZenioniteStairBlock)) {
            return;
        }

        BlockPos pos = stair.getBlockPos();
        int fluidLight = combineLight(
                packedLight,
                LevelRenderer.getLightColor(level, pos),
                LevelRenderer.getLightColor(level, pos.above())
        );
        List<AABB> collisionBoxes = state.getCollisionShape(level, pos).toAabbs();
        CellVolume[][] cells = buildCellVolumes(stair, state, collisionBoxes);
        for (int cellX = 0; cellX < 2; cellX++) {
            for (int cellZ = 0; cellZ < 2; cellZ++) {
                CellVolume cell = cells[cellX][cellZ];
                if (cell != null) {
                    renderCell(stair, state, cells, cellX, cellZ, cell, collisionBoxes,
                            poseStack, buffer, fluidLight, packedOverlay);
                }
            }
        }
    }

    private static CellVolume[][] buildCellVolumes(
            ZenioniteStairBlockEntity stair,
            BlockState state,
            List<AABB> collisionBoxes
    ) {
        CellVolume[][] cells = new CellVolume[2][2];
        boolean topStair = state.getValue(StairBlock.HALF) == Half.TOP;
        for (int cellX = 0; cellX < 2; cellX++) {
            for (int cellZ = 0; cellZ < 2; cellZ++) {
                float centerX = (cellX + 0.5F) * CELL_SIZE;
                float centerZ = (cellZ + 0.5F) * CELL_SIZE;
                FluidState fluid = stair.getFluid(laneAt(state, centerX, centerZ));
                if (fluid.isEmpty()) {
                    continue;
                }

                float naturalSurface = Math.max(FACE_INSET, fluid.getOwnHeight() - 0.001F);
                float bottom = topStair ? FACE_INSET : BOTTOM_SEAT_SURFACE;
                float top = topStair
                        ? Math.min(TOP_SEAT_CEILING, naturalSurface)
                        : Math.min(1.0F - FACE_INSET,
                        Math.max(BOTTOM_SEAT_SURFACE + 0.001F, naturalSurface));
                float sampleY = (bottom + top) * 0.5F;
                if (top <= bottom || isSolidAt(collisionBoxes, centerX, sampleY, centerZ)) {
                    continue;
                }
                cells[cellX][cellZ] = new CellVolume(fluid, bottom, top);
            }
        }
        return cells;
    }

    private static void renderCell(
            ZenioniteStairBlockEntity stair,
            BlockState state,
            CellVolume[][] cells,
            int cellX,
            int cellZ,
            CellVolume cell,
            List<AABB> collisionBoxes,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Level level = stair.getLevel();
        if (level == null) {
            return;
        }

        FluidState fluid = cell.fluid();
        TextureAtlasSprite[] sprites = ForgeHooksClient.getFluidSprites(
                level, stair.getBlockPos(), fluid);
        TextureAtlasSprite horizontalSprite = fluid.isSource() ? sprites[0] : sprites[1];
        TextureAtlasSprite sideSprite = sprites[1];
        int tint = IClientFluidTypeExtensions.of(fluid)
                .getTintColor(fluid, level, stair.getBlockPos());
        float alpha = (tint >>> 24 & 255) / 255.0F;
        if (alpha <= 0.0F) {
            alpha = 1.0F;
        }
        float red = (tint >>> 16 & 255) / 255.0F;
        float green = (tint >>> 8 & 255) / 255.0F;
        float blue = (tint & 255) / 255.0F;
        VertexConsumer vertices = buffer.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluid));

        float x0 = cellX == 0 ? FACE_INSET : cellX * CELL_SIZE;
        float x1 = cellX == 1 ? 1.0F - FACE_INSET : (cellX + 1) * CELL_SIZE;
        float z0 = cellZ == 0 ? FACE_INSET : cellZ * CELL_SIZE;
        float z1 = cellZ == 1 ? 1.0F - FACE_INSET : (cellZ + 1) * CELL_SIZE;
        boolean topStair = state.getValue(StairBlock.HALF) == Half.TOP;

        if (topStair) {
            drawBottom(vertices, poseStack.last(), horizontalSprite,
                    x0, x1, z0, z1, cell.bottom(),
                    red * 0.5F, green * 0.5F, blue * 0.5F, alpha,
                    packedLight, packedOverlay);
        } else {
            drawTop(vertices, poseStack.last(), horizontalSprite,
                    x0, x1, z0, z1, cell.top(), red, green, blue, alpha,
                    packedLight, packedOverlay);
        }

        for (Direction side : Direction.Plane.HORIZONTAL) {
            drawCellSide(cells, collisionBoxes, cellX, cellZ, side, cell,
                    vertices, poseStack.last(), sideSprite, x0, x1, z0, z1,
                    red, green, blue, alpha, packedLight, packedOverlay);
        }
    }

    private static Lane laneAt(BlockState state, float x, float z) {
        Direction right = ZenioniteStairBlock.worldSideForLane(state, Lane.RIGHT);
        float projection = (x - 0.5F) * right.getStepX() + (z - 0.5F) * right.getStepZ();
        return projection >= 0.0F ? Lane.RIGHT : Lane.LEFT;
    }

    private static boolean isSolidAt(
            List<AABB> collisionBoxes,
            float x,
            float y,
            float z
    ) {
        for (AABB box : collisionBoxes) {
            if (x > box.minX && x < box.maxX
                    && y > box.minY && y < box.maxY
                    && z > box.minZ && z < box.maxZ) {
                return true;
            }
        }
        return false;
    }

    private static void drawCellSide(
            CellVolume[][] cells,
            List<AABB> collisionBoxes,
            int cellX,
            int cellZ,
            Direction side,
            CellVolume cell,
            VertexConsumer vertices,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            float x0,
            float x1,
            float z0,
            float z1,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay
    ) {
        int neighborX = cellX + side.getStepX();
        int neighborZ = cellZ + side.getStepZ();
        float visibleBottom = cell.bottom();
        if (neighborX >= 0 && neighborX < 2 && neighborZ >= 0 && neighborZ < 2) {
            CellVolume neighbor = cells[neighborX][neighborZ];
            if (neighbor != null && neighbor.fluid().getType().isSame(cell.fluid().getType())) {
                visibleBottom = Math.max(visibleBottom, neighbor.top());
                if (visibleBottom >= cell.top()) {
                    return;
                }
            } else if (neighbor == null) {
                float centerX = (neighborX + 0.5F) * CELL_SIZE;
                float centerZ = (neighborZ + 0.5F) * CELL_SIZE;
                float sampleY = (cell.bottom() + cell.top()) * 0.5F;
                if (isSolidAt(collisionBoxes, centerX, sampleY, centerZ)) {
                    return;
                }
            }
        }

        float shade = side.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;
        drawSide(vertices, pose, sprite, side, x0, x1, z0, z1,
                visibleBottom, cell.top(), red * shade, green * shade, blue * shade,
                alpha, packedLight, packedOverlay);
    }

    private static void drawTop(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            float x0,
            float x1,
            float z0,
            float z1,
            float y,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay
    ) {
        float u0 = sprite.getU(x0 * 16.0F);
        float u1 = sprite.getU(x1 * 16.0F);
        float v0 = sprite.getV(z0 * 16.0F);
        float v1 = sprite.getV(z1 * 16.0F);
        vertex(vertices, pose, x0, y, z0, red, green, blue, alpha,
                u0, v0, packedLight, packedOverlay, 0.0F, 1.0F, 0.0F);
        vertex(vertices, pose, x0, y, z1, red, green, blue, alpha,
                u0, v1, packedLight, packedOverlay, 0.0F, 1.0F, 0.0F);
        vertex(vertices, pose, x1, y, z1, red, green, blue, alpha,
                u1, v1, packedLight, packedOverlay, 0.0F, 1.0F, 0.0F);
        vertex(vertices, pose, x1, y, z0, red, green, blue, alpha,
                u1, v0, packedLight, packedOverlay, 0.0F, 1.0F, 0.0F);
    }

    private static void drawBottom(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            float x0,
            float x1,
            float z0,
            float z1,
            float y,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay
    ) {
        float u0 = sprite.getU(x0 * 16.0F);
        float u1 = sprite.getU(x1 * 16.0F);
        float v0 = sprite.getV(z0 * 16.0F);
        float v1 = sprite.getV(z1 * 16.0F);
        vertex(vertices, pose, x0, y, z0, red, green, blue, alpha,
                u0, v0, packedLight, packedOverlay, 0.0F, -1.0F, 0.0F);
        vertex(vertices, pose, x1, y, z0, red, green, blue, alpha,
                u1, v0, packedLight, packedOverlay, 0.0F, -1.0F, 0.0F);
        vertex(vertices, pose, x1, y, z1, red, green, blue, alpha,
                u1, v1, packedLight, packedOverlay, 0.0F, -1.0F, 0.0F);
        vertex(vertices, pose, x0, y, z1, red, green, blue, alpha,
                u0, v1, packedLight, packedOverlay, 0.0F, -1.0F, 0.0F);
    }

    private static void drawSide(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction side,
            float x0,
            float x1,
            float z0,
            float z1,
            float bottom,
            float top,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay
    ) {
        float vBottom = sprite.getV((1.0F - bottom) * 16.0F);
        float vTop = sprite.getV((1.0F - top) * 16.0F);
        if (side == Direction.NORTH) {
            float u0 = sprite.getU(x0 * 16.0F);
            float u1 = sprite.getU(x1 * 16.0F);
            vertex(vertices, pose, x0, bottom, z0, red, green, blue, alpha,
                    u0, vBottom, packedLight, packedOverlay, 0.0F, 0.0F, -1.0F);
            vertex(vertices, pose, x0, top, z0, red, green, blue, alpha,
                    u0, vTop, packedLight, packedOverlay, 0.0F, 0.0F, -1.0F);
            vertex(vertices, pose, x1, top, z0, red, green, blue, alpha,
                    u1, vTop, packedLight, packedOverlay, 0.0F, 0.0F, -1.0F);
            vertex(vertices, pose, x1, bottom, z0, red, green, blue, alpha,
                    u1, vBottom, packedLight, packedOverlay, 0.0F, 0.0F, -1.0F);
        } else if (side == Direction.SOUTH) {
            float u0 = sprite.getU(x0 * 16.0F);
            float u1 = sprite.getU(x1 * 16.0F);
            vertex(vertices, pose, x0, bottom, z1, red, green, blue, alpha,
                    u0, vBottom, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
            vertex(vertices, pose, x1, bottom, z1, red, green, blue, alpha,
                    u1, vBottom, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
            vertex(vertices, pose, x1, top, z1, red, green, blue, alpha,
                    u1, vTop, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
            vertex(vertices, pose, x0, top, z1, red, green, blue, alpha,
                    u0, vTop, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
        } else if (side == Direction.WEST) {
            float u0 = sprite.getU(z0 * 16.0F);
            float u1 = sprite.getU(z1 * 16.0F);
            vertex(vertices, pose, x0, bottom, z0, red, green, blue, alpha,
                    u0, vBottom, packedLight, packedOverlay, -1.0F, 0.0F, 0.0F);
            vertex(vertices, pose, x0, bottom, z1, red, green, blue, alpha,
                    u1, vBottom, packedLight, packedOverlay, -1.0F, 0.0F, 0.0F);
            vertex(vertices, pose, x0, top, z1, red, green, blue, alpha,
                    u1, vTop, packedLight, packedOverlay, -1.0F, 0.0F, 0.0F);
            vertex(vertices, pose, x0, top, z0, red, green, blue, alpha,
                    u0, vTop, packedLight, packedOverlay, -1.0F, 0.0F, 0.0F);
        } else {
            float u0 = sprite.getU(z0 * 16.0F);
            float u1 = sprite.getU(z1 * 16.0F);
            vertex(vertices, pose, x1, bottom, z1, red, green, blue, alpha,
                    u1, vBottom, packedLight, packedOverlay, 1.0F, 0.0F, 0.0F);
            vertex(vertices, pose, x1, bottom, z0, red, green, blue, alpha,
                    u0, vBottom, packedLight, packedOverlay, 1.0F, 0.0F, 0.0F);
            vertex(vertices, pose, x1, top, z0, red, green, blue, alpha,
                    u0, vTop, packedLight, packedOverlay, 1.0F, 0.0F, 0.0F);
            vertex(vertices, pose, x1, top, z1, red, green, blue, alpha,
                    u1, vTop, packedLight, packedOverlay, 1.0F, 0.0F, 0.0F);
        }
    }

    private static int combineLight(int... packedValues) {
        int block = 0;
        int sky = 0;
        for (int packed : packedValues) {
            block = Math.max(block, LightTexture.block(packed));
            sky = Math.max(sky, LightTexture.sky(packed));
        }
        return LightTexture.pack(block, sky);
    }

    private static void vertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float red,
            float green,
            float blue,
            float alpha,
            float u,
            float v,
            int packedLight,
            int packedOverlay,
            float normalX,
            float normalY,
            float normalZ
    ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay)
                .uv2(packedLight)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    private record CellVolume(FluidState fluid, float bottom, float top) {
    }
}
