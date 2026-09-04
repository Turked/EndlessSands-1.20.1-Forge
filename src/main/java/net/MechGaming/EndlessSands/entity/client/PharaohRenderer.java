package net.MechGaming.EndlessSands.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.custom.PharaohEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PharaohRenderer extends MobRenderer<PharaohEntity, PharaohModel<PharaohEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EndlessSands.MOD_ID, "textures/entity/pharaoh.png");
    private static final float GIANT_SCALE = 6.0F;

    public PharaohRenderer(EntityRendererProvider.Context context) {
        super(context, new PharaohModel<>(context.bakeLayer(ModModelLayers.PHARAOH_LAYER)), 0.5F);
    }

    @Override
    protected void scale(PharaohEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(GIANT_SCALE, GIANT_SCALE, GIANT_SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(PharaohEntity entity) {
        return TEXTURE;
    }
}
