package net.MechGaming.EndlessSands.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class VultureHeldItemLayer extends RenderLayer<VultureEntity, HierarchicalModel<VultureEntity>> {
    private final ItemRenderer itemRenderer;

    public VultureHeldItemLayer(RenderLayerParent<VultureEntity, HierarchicalModel<VultureEntity>> renderer, ItemRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VultureEntity vulture, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(this.getParentModel() instanceof VultureModel<?> model)) {
            return;
        }

        renderAtMouth(model, vulture.getMouthStack(), vulture, poseStack, buffer, packedLight);
        renderAtLeftTalon(model, vulture.getLeftTalonStack(), vulture, poseStack, buffer, packedLight);
        renderAtRightTalon(model, vulture.getRightTalonStack(), vulture, poseStack, buffer, packedLight);
    }

    private void renderAtMouth(VultureModel<?> model, ItemStack stack, VultureEntity vulture, PoseStack poseStack,
                               MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        model.translateToMouth(poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.56F, 0.56F, 0.56F);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, vulture.level(), vulture.getId());
        poseStack.popPose();
    }

    private void renderAtLeftTalon(VultureModel<?> model, ItemStack stack, VultureEntity vulture, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        model.translateToLeftTalon(poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.28F, 0.28F, 0.28F);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, vulture.level(), vulture.getId());
        poseStack.popPose();
    }

    private void renderAtRightTalon(VultureModel<?> model, ItemStack stack, VultureEntity vulture, PoseStack poseStack,
                                    MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        model.translateToRightTalon(poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.28F, 0.28F, 0.28F);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, vulture.level(), vulture.getId());
        poseStack.popPose();
    }
}
