package net.MechGaming.EndlessSands.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.client.ModModelLayers;
import net.MechGaming.EndlessSands.entity.client.VultureModel;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class VultureAdvancementIconRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "textures/entity/vulture.png");

    private VultureModel<VultureEntity> model;

    public VultureAdvancementIconRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (this.model == null) {
            this.model = new VultureModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.VULTURE_LAYER)
            );
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.1F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(205.0F));
        poseStack.scale(-0.62F, -0.62F, 0.62F);

        VertexConsumer vertices = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(
                poseStack,
                vertices,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
        poseStack.popPose();
    }
}
