package net.MechGaming.EndlessSands.entity.client;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.custom.OldworldGolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class OldworldGolemRenderer extends MobRenderer<OldworldGolemEntity, OldworldGolemModel<OldworldGolemEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "textures/entity/oldworld_golem.png");

    public OldworldGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new OldworldGolemModel<>(context.bakeLayer(ModModelLayers.OLDWORLD_GOLEM_LAYER)), 0.7F);
        this.addLayer(new OldworldGolemHeldItemLayer(this, Minecraft.getInstance().getItemRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(OldworldGolemEntity entity) {
        return TEXTURE;
    }
}
