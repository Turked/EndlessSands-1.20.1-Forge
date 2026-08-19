package net.MechGaming.EndlessSands.client.model;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LinedStairModelEvents {
    private LinedStairModelEvents() {
    }

    @SubscribeEvent
    public static void replaceLinedStairModels(ModelEvent.ModifyBakingResult event) {
        LinedStairBakedModel.clearCache();
        event.getModels().replaceAll((location, model) ->
                location.getNamespace().equals(EndlessSands.MOD_ID)
                        && location.getPath().equals("lined_stairs")
                        ? new LinedStairBakedModel(model)
                        : model
        );
    }
}
