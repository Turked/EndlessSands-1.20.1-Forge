package net.MechGaming.EndlessSands.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VultureRenderer extends MobRenderer<VultureEntity, HierarchicalModel<VultureEntity>> {
    private static final float MODEL_SCALE = 5.0F / 12.0F;
    private static final float ARM_ANCHOR_X = 9.125F / 16.0F;
    private static final float ARM_ANCHOR_Y = 5.125F / 16.0F;
    private static final float ARM_ANCHOR_Z = 10.125F / 16.0F;
    private static final float FOOT_MIDPOINT_X = 0.5F / 16.0F;
    private static final float FOOT_MIDPOINT_Y = 24.0F / 16.0F;
    private static final float FOOT_MIDPOINT_Z = 1.5F / 16.0F;
    private static final ResourceLocation VULTURE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "textures/entity/vulture.png");
    private static final ResourceLocation BABY_VULTURE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "textures/entity/baby_vulture.png");

    private final VultureModel<VultureEntity> adultModel;
    private final BabyVultureModel<VultureEntity> babyModel;
    private final VultureHeldItemLayer heldItemLayer;

    public VultureRenderer(EntityRendererProvider.Context context) {
        super(context, new VultureModel<>(context.bakeLayer(ModModelLayers.VULTURE_LAYER)), 0.08F);
        this.adultModel = (VultureModel<VultureEntity>) this.model;
        this.babyModel = new BabyVultureModel<>(context.bakeLayer(ModModelLayers.BABY_VULTURE_LAYER));
        this.heldItemLayer = new VultureHeldItemLayer(this, Minecraft.getInstance().getItemRenderer());
        this.addLayer(this.heldItemLayer);
    }

    @Override
    public void render(VultureEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {
        this.model = entity.isBaby() ? this.babyModel : this.adultModel;
        this.shadowRadius = entity.isBaby() ? 0.0625F : 0.10F;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(VultureEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    @Override
    public boolean shouldRender(VultureEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (entity.isArmPerched()) {
            return false;
        }
        return entity.noCulling || super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
    }

    public void renderArmAttachment(VultureEntity entity, float partialTick, PoseStack poseStack,
                                    MultiBufferSource buffer, int packedLight) {
        if (!entity.isArmPerched() || entity.isBaby()) {
            return;
        }

        this.model = this.adultModel;
        float ageInTicks = entity.tickCount + partialTick;
        this.adultModel.prepareMobModel(entity, 0.0F, 0.0F, partialTick);
        this.adultModel.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        poseStack.pushPose();
        poseStack.translate(ARM_ANCHOR_X, ARM_ANCHOR_Y, ARM_ANCHOR_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(-MODEL_SCALE, -MODEL_SCALE, MODEL_SCALE);
        poseStack.translate(-FOOT_MIDPOINT_X, -FOOT_MIDPOINT_Y, -FOOT_MIDPOINT_Z);

        VertexConsumer vertexConsumer = buffer.getBuffer(this.adultModel.renderType(VULTURE_TEXTURE));
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        this.adultModel.renderToBuffer(poseStack, vertexConsumer, packedLight, overlay,
                1.0F, 1.0F, 1.0F, 1.0F);
        this.heldItemLayer.render(poseStack, buffer, packedLight, entity,
                0.0F, 0.0F, partialTick, ageInTicks, 0.0F, 0.0F);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(VultureEntity entity) {
        return entity.isBaby() ? BABY_VULTURE_TEXTURE : VULTURE_TEXTURE;
    }
}
