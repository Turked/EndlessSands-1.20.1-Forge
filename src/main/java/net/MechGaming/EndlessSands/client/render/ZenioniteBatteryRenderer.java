package net.MechGaming.EndlessSands.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.ZenioniteBatteryBlock;
import net.MechGaming.EndlessSands.block.entity.ZenioniteBatteryBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ZenioniteBatteryRenderer implements BlockEntityRenderer<ZenioniteBatteryBlockEntity> {
    private static final ResourceLocation PORT_POWER_TEXTURE = texture(
            "zenionite_battery_side_no_connections_power_iso");
    private static final ResourceLocation SMOOTH_POWER_TEXTURE = texture(
            "zenionite_battery_smooth_side_power_iso");
    private static final float SURFACE_OFFSET = 0.001F;

    public ZenioniteBatteryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            ZenioniteBatteryBlockEntity battery,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        int sideCapacity = battery.getSideCapacity();
        if (sideCapacity <= 0) {
            return;
        }

        BlockState state = battery.getBlockState();
        boolean hasConnection = state.hasProperty(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION)
                && state.getValue(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int stored = battery.getSideEnergy(direction);
            if (stored <= 0) {
                continue;
            }

            float ratio = Math.max(0.0F, Math.min(1.0F, stored / (float) sideCapacity));
            float textureMin = 0.5F - ratio * 0.5F;
            float textureMax = 0.5F + ratio * 0.5F;
            boolean portFace = !hasConnection || ZenioniteBatteryBlock.isConnected(state, direction);
            ResourceLocation texture = portFace ? PORT_POWER_TEXTURE : SMOOTH_POWER_TEXTURE;
            VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
            int faceLight = battery.getLevel() == null ? packedLight : LevelRenderer.getLightColor(
                    battery.getLevel(), battery.getBlockPos().relative(direction));
            drawFace(vertices, poseStack.last(), direction, textureMin, textureMax,
                    faceLight, packedOverlay);
        }
    }

    private static void drawFace(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            Direction direction,
            float minimum,
            float maximum,
            int packedLight,
            int packedOverlay
    ) {
        switch (direction) {
            case NORTH -> {
                float z = -SURFACE_OFFSET;
                vertex(vertices, pose, minimum, 0.0F, z, maximum, 1.0F,
                        0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
                vertex(vertices, pose, maximum, 0.0F, z, minimum, 1.0F,
                        0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
                vertex(vertices, pose, maximum, 1.0F, z, minimum, 0.0F,
                        0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
                vertex(vertices, pose, minimum, 1.0F, z, maximum, 0.0F,
                        0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
            }
            case SOUTH -> {
                float z = 1.0F + SURFACE_OFFSET;
                vertex(vertices, pose, maximum, 0.0F, z, maximum, 1.0F,
                        0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
                vertex(vertices, pose, minimum, 0.0F, z, minimum, 1.0F,
                        0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
                vertex(vertices, pose, minimum, 1.0F, z, minimum, 0.0F,
                        0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
                vertex(vertices, pose, maximum, 1.0F, z, maximum, 0.0F,
                        0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
            }
            case WEST -> {
                float x = -SURFACE_OFFSET;
                vertex(vertices, pose, x, 0.0F, maximum, maximum, 1.0F,
                        -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
                vertex(vertices, pose, x, 0.0F, minimum, minimum, 1.0F,
                        -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
                vertex(vertices, pose, x, 1.0F, minimum, minimum, 0.0F,
                        -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
                vertex(vertices, pose, x, 1.0F, maximum, maximum, 0.0F,
                        -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
            }
            case EAST -> {
                float x = 1.0F + SURFACE_OFFSET;
                vertex(vertices, pose, x, 0.0F, minimum, maximum, 1.0F,
                        1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
                vertex(vertices, pose, x, 0.0F, maximum, minimum, 1.0F,
                        1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
                vertex(vertices, pose, x, 1.0F, maximum, minimum, 0.0F,
                        1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
                vertex(vertices, pose, x, 1.0F, minimum, maximum, 0.0F,
                        1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
            }
            default -> {
            }
        }
    }

    private static void vertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float normalX,
            float normalY,
            float normalZ,
            int packedLight,
            int packedOverlay
    ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                EndlessSands.MOD_ID, "textures/block/" + name + ".png");
    }
}
