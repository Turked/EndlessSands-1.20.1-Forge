package net.MechGaming.EndlessSands.client.render;

import com.mojang.math.Axis;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.client.BuriedInSandClientState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = EndlessSands.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class BuriedInSandRenderer {
    private BuriedInSandRenderer() {}

    @SubscribeEvent
    public static void beforePlayerRender(
            RenderPlayerEvent.Pre event
    ) {
        if (!BuriedInSandClientState.isBuried(
                event.getEntity().getId()
        )) {
            return;
        }

        event.getPoseStack().pushPose();

        // Rotate the upright player model onto its back.
        event.getPoseStack().translate(
                0.0D,
                0.15D,
                0.0D
        );
        event.getPoseStack().mulPose(
                Axis.XP.rotationDegrees(90.0F)
        );
        event.getPoseStack().translate(
                0.0D,
                -1.0D,
                0.0D
        );
    }

    @SubscribeEvent
    public static void afterPlayerRender(
            RenderPlayerEvent.Post event
    ) {
        if (BuriedInSandClientState.isBuried(
                event.getEntity().getId()
        )) {
            event.getPoseStack().popPose();
        }
    }
}