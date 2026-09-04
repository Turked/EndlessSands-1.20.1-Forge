package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.client.ModKeyMappings;
import net.MechGaming.EndlessSands.client.screen.ArmGuardSearchScreen;
import net.MechGaming.EndlessSands.client.screen.ZenioniteBatteryScreen;
import net.MechGaming.EndlessSands.client.screen.ZenioniteBeaconScreen;
import net.MechGaming.EndlessSands.client.screen.ZenioniteChargerScreen;
import net.MechGaming.EndlessSands.client.render.ZenioniteBatteryRenderer;
import net.MechGaming.EndlessSands.client.render.ZenioniteBeaconRenderer;
import net.MechGaming.EndlessSands.client.render.ZenioniteStairRenderer;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.MechGaming.EndlessSands.entity.client.BabyVultureModel;
import net.MechGaming.EndlessSands.entity.client.ModModelLayers;
import net.MechGaming.EndlessSands.entity.client.OldworldGolemModel;
import net.MechGaming.EndlessSands.entity.client.OldworldGolemRenderer;
import net.MechGaming.EndlessSands.entity.client.PharaohModel;
import net.MechGaming.EndlessSands.entity.client.PharaohRenderer;
import net.MechGaming.EndlessSands.entity.client.RhinoModel;
import net.MechGaming.EndlessSands.entity.client.VultureModel;
import net.MechGaming.EndlessSands.entity.client.VultureRenderer;
import net.MechGaming.EndlessSands.inventory.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.phys.Vec3;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModFluids.ANCIENT_OCEAN_WATER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ANCIENT_OCEAN_WATER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.STAR_TOUCHED_LAVA.get(), RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_STAR_TOUCHED_LAVA.get(), RenderType.solid());
            MenuScreens.register(ModMenuTypes.ARM_GUARD_SEARCH.get(), ArmGuardSearchScreen::new);
            MenuScreens.register(ModMenuTypes.ZENIONITE_CHARGER.get(), ZenioniteChargerScreen::new);
            MenuScreens.register(ModMenuTypes.ZENIONITE_BATTERY.get(), ZenioniteBatteryScreen::new);
            MenuScreens.register(ModMenuTypes.ZENIONITE_BEACON.get(), ZenioniteBeaconScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.RHINO_LAYER, RhinoModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.VULTURE_LAYER, VultureModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.BABY_VULTURE_LAYER, BabyVultureModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.OLDWORLD_GOLEM_LAYER, OldworldGolemModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.PHARAOH_LAYER, PharaohModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.ZENIONITE_BATTERY.get(), ZenioniteBatteryRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.ZENIONITE_BEACON.get(), ZenioniteBeaconRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.ZENIONITE_STAIRS.get(), ZenioniteStairRenderer::new);
        event.registerEntityRenderer(ModEntities.POCKET_SAND_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.VULTURE_EGG_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.VULTURE.get(), VultureRenderer::new);
        event.registerEntityRenderer(ModEntities.OLDWORLD_GOLEM.get(), OldworldGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.PHARAOH.get(), PharaohRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ZenioniteBeaconRenderer.MODEL_LOCATION);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.HOTBAR_SLOT_10);
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ModDimensions.PRISON_REALM_EFFECTS,
                new DimensionSpecialEffects(
                        Float.NaN,
                        true,
                        DimensionSpecialEffects.SkyType.NONE,
                        true,
                        true
                ) {
                    @Override
                    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
                        return Vec3.ZERO;
                    }

                    @Override
                    public boolean isFoggyAt(int x, int y) {
                        return false;
                    }

                    @Override
                    public float[] getSunriseColor(float timeOfDay, float partialTick) {
                        return null;
                    }
                });
    }
}
