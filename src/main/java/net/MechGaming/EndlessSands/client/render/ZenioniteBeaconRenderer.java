package net.MechGaming.EndlessSands.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.ZenioniteBeaconBlock;
import net.MechGaming.EndlessSands.block.entity.ZenioniteBeaconBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ZenioniteBeaconRenderer
        implements BlockEntityRenderer<ZenioniteBeaconBlockEntity> {
    public static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath(
            EndlessSands.MOD_ID, "block/beacon_laser");

    private static final float BLOCKS_PER_TICK = 0.05F;
    private static final float MODEL_HEIGHT = 20.0F / 16.0F;
    private static final int VIEW_DISTANCE = 256;

    private final ModelBlockRenderer modelRenderer;

    public ZenioniteBeaconRenderer(BlockEntityRendererProvider.Context context) {
        modelRenderer = context.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(
            ZenioniteBeaconBlockEntity beacon,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Level level = beacon.getLevel();
        if (level == null
                || !beacon.getBlockState().getValue(ZenioniteBeaconBlock.BEAM_ACTIVE)) {
            return;
        }

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(MODEL_LOCATION);
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            return;
        }

        Vec3 start = new Vec3(
                beacon.getBlockPos().getX() + 0.5D,
                beacon.getBlockPos().getY(),
                beacon.getBlockPos().getZ() + 0.5D
        );
        Vec3 target = beacon.getBeamTarget();
        Vec3 difference = target.subtract(start);
        double beamLength = difference.length();
        if (beamLength < 0.001D) {
            return;
        }

        Vec3 direction = difference.scale(1.0D / beamLength);
        Quaternionf rotation = new Quaternionf().rotationTo(
                new Vector3f(0.0F, 1.0F, 0.0F),
                new Vector3f((float) direction.x, (float) direction.y, (float) direction.z)
        );
        float movementOffset = Mth.frac(
                (level.getGameTime() + partialTick) * BLOCKS_PER_TICK);
        VertexConsumer vertices = buffer.getBuffer(Sheets.cutoutBlockSheet());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(rotation);
        poseStack.translate(-0.5D, movementOffset - 1.0D, -0.5D);
        double linkStart = movementOffset - 1.0D;
        while (linkStart < beamLength) {
            double remainingLength = beamLength - linkStart;
            boolean finalLink = linkStart + MODEL_HEIGHT >= beamLength;
            float verticalScale = finalLink
                    ? (float) Math.max(0.0D, remainingLength / MODEL_HEIGHT)
                    : 1.0F;

            poseStack.pushPose();
            poseStack.scale(1.0F, verticalScale, 1.0F);
            modelRenderer.renderModel(
                    poseStack.last(),
                    vertices,
                    null,
                    model,
                    1.0F,
                    1.0F,
                    1.0F,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );
            poseStack.popPose();
            if (finalLink) {
                break;
            }
            poseStack.translate(0.0D, 1.0D, 0.0D);
            linkStart += 1.0D;
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ZenioniteBeaconBlockEntity beacon) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public boolean shouldRender(ZenioniteBeaconBlockEntity beacon, Vec3 cameraPos) {
        Vec3 beaconCenter = Vec3.atCenterOf(beacon.getBlockPos())
                .multiply(1.0D, 0.0D, 1.0D);
        Vec3 horizontalCamera = cameraPos.multiply(1.0D, 0.0D, 1.0D);
        return beaconCenter.closerThan(horizontalCamera, VIEW_DISTANCE);
    }
}
