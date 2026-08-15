package net.MechGaming.EndlessSands.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.MechGaming.EndlessSands.entity.custom.OldworldGolemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class OldworldGolemHeldItemLayer extends RenderLayer<OldworldGolemEntity, OldworldGolemModel<OldworldGolemEntity>> {
    private final ItemRenderer itemRenderer;

    public OldworldGolemHeldItemLayer(RenderLayerParent<OldworldGolemEntity, OldworldGolemModel<OldworldGolemEntity>> renderer,
                                      ItemRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, OldworldGolemEntity golem,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack stack = golem.getHeldRoseStack();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().translateToRightHand(poseStack);
        poseStack.translate(0.0F, -0.0925F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0F));
        poseStack.translate(0.0F, 1.5F / 16.0F, 0.0F);
        poseStack.scale(0.85F, 0.85F, 0.85F);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.NONE, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, golem.level(), golem.getId());
        poseStack.popPose();
    }
}
