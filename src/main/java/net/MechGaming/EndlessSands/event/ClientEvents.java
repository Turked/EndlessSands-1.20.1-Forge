package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
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
}
