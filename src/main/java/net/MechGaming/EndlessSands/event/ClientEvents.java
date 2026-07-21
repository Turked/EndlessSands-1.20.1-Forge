package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.client.BuriedInSandClientState;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;


@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void  onItemTooltip(ItemTooltipEvent event){

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
