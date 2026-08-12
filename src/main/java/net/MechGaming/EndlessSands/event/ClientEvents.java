package net.MechGaming.EndlessSands.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.client.BuriedInSandClientState;
import net.MechGaming.EndlessSands.client.HeatstrokeWarningClientState;
import net.MechGaming.EndlessSands.client.ModKeyMappings;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.MechGaming.EndlessSands.util.SunGearHelper;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HeatstrokeWarningClientState.tick();
            selectTenthHotbarSlot();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        HeatstrokeWarningClientState.render(
                event.getGuiGraphics(),
                event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight()
        );
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        ItemStack helmet = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
        if (!SunGearHelper.hasTwigVisor(helmet)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        event.getRenderer().getModel().head.translateAndRotate(poseStack);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                new ItemStack(ModItems.TWIG_VISOR.get()),
                ItemDisplayContext.HEAD,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                poseStack,
                event.getMultiBufferSource(),
                event.getEntity().level(),
                event.getEntity().getId()
        );
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void  onItemTooltip(ItemTooltipEvent event){
        if (SunGearHelper.hasTwigVisor(event.getItemStack())) {
            event.getToolTip().add(
                    Component.translatable("tooltip.endlesssands.twig_visor_attached.tooltip")
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        if(event.getItemStack().getItem() == ModItems.SCROLL_OF_YEARNING.get()){
            event.getToolTip().add(
                    Component.translatable("tooltip.endlesssands.scroll_of_yearning.tooltip")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
        }

        if(event.getItemStack().getItem() == ModItems.SCROLL_OF_WISDOM.get()){
            event.getToolTip().add(
                    Component.translatable("tooltip.endlesssands.scroll_of_wisdom_one.tooltip")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                            .append(Component.translatable("tooltip.endlesssands.scroll_of_wisdom_two.tooltip")
                                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC, ChatFormatting.BOLD))
                                    .append(Component.translatable("tooltip.endlesssands.scroll_of_wisdom_three.tooltip")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
            );

        }

        if(event.getItemStack().getItem() == ModItems.SCROLL_OF_LORE.get()){
            event.getToolTip().add(
                    Component.translatable("tooltip.endlesssands.scroll_of_lore.tooltip")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
        }

        if(event.getItemStack().getItem() == ModItems.SCROLL_OF_MEDIOCRITY.get()){
            event.getToolTip().add(
                    Component.translatable("tooltip.endlesssands.scroll_of_mediocrity.tooltip")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
        }

    }

    private static void selectTenthHotbarSlot() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        while (ModKeyMappings.HOTBAR_SLOT_10.consumeClick()) {
            if (ExpandedInventoryHelper.isUnlocked(minecraft.player)) {
                minecraft.player.getInventory().selected = 9;
            }
        }
    }

    @SubscribeEvent
    public static void onClientPlayerRespawn(
            ClientPlayerNetworkEvent.Clone event
    ) {
        boolean diedInEndlessSands =
                event.getOldPlayer()
                        .level()
                        .dimension()
                        .equals(ModDimensions.ENDLESS_SANDS_LEVEL);

        boolean respawnedInEndlessSands =
                event.getNewPlayer()
                        .level()
                        .dimension()
                        .equals(ModDimensions.ENDLESS_SANDS_LEVEL);

        if (diedInEndlessSands || respawnedInEndlessSands) {
            BuriedInSandClientState.preview(
                    event.getNewPlayer().getId()
            );
        }
    }

    @SubscribeEvent
    public static void onClientPlayerLogin(
            ClientPlayerNetworkEvent.LoggingIn event
    ) {
        if (event.getPlayer()
                .level()
                .dimension()
                .equals(ModDimensions.ENDLESS_SANDS_LEVEL)) {
            BuriedInSandClientState.preview(
                    event.getPlayer().getId()
            );
        }
    }
}
