package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class GuiHotbarMixin {
    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 9))
    private int endlessSands$renderTenHotbarSlots(int original) {
        return shouldRenderExpandedHotbar() ? ExpandedInventoryHelper.EXPANDED_HOTBAR_SIZE : original;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 90))
    private int endlessSands$centerTenHotbarItems(int original) {
        return shouldRenderExpandedHotbar() ? 100 : original;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 91))
    private int endlessSands$centerTenHotbarFrame(int original) {
        return shouldRenderExpandedHotbar() ? 101 : original;
    }

    @Redirect(method = "renderHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
            ordinal = 0))
    private void endlessSands$renderExpandedHotbarFrame(GuiGraphics guiGraphics, ResourceLocation texture,
                                                         int x, int y, int u, int v, int width, int height) {
        if (!shouldRenderExpandedHotbar()) {
            guiGraphics.blit(texture, x, y, u, v, width, height);
            return;
        }

        guiGraphics.blit(texture, x, y, 0, 0, 181, 22);
        guiGraphics.blit(texture, x + 181, y, 161, 0, 20, 22);
        guiGraphics.blit(texture, x + 201, y, 181, 0, 1, 22);
    }

    private static boolean shouldRenderExpandedHotbar() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && ExpandedInventoryHelper.isUnlocked(player);
    }
}
