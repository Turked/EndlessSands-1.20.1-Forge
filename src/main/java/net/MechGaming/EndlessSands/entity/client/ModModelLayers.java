package net.MechGaming.EndlessSands.entity.client;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation RHINO_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "rhino_layer"),
            "main"
    );
}