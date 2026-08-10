package net.MechGaming.EndlessSands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class HeatstrokeWarningClientState {
    // Change this to resize the warning. It will still shrink as needed to fit the screen.
    public static final float MAX_TEXT_SCALE = 1.25F;

    private static final int FADE_IN_TICKS = 10;
    private static final int STAY_TICKS = 60;
    private static final int FADE_OUT_TICKS = 20;
    private static final int TOTAL_TICKS = FADE_IN_TICKS + STAY_TICKS + FADE_OUT_TICKS;

    private static Component message = Component.empty();
    private static int age = TOTAL_TICKS;

    private HeatstrokeWarningClientState() {}

    public static void show(int tier) {
        String translationKey = switch (tier) {
            case 0 -> "message.endlesssands.heatstroke.tier_i";
            case 1 -> "message.endlesssands.heatstroke.tier_ii";
            case 2 -> "message.endlesssands.heatstroke.tier_iii";
            case 3 -> "message.endlesssands.heatstroke.tier_iv";
            default -> null;
        };

        if (translationKey != null) {
            message = Component.translatable(translationKey);
            age = 0;
        }
    }

    public static void tick() {
        if (age < TOTAL_TICKS) {
            age++;
        }
    }

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (age >= TOTAL_TICKS || minecraft.options.hideGui) {
            return;
        }

        float alpha = alpha();
        if (alpha <= 0.0F) {
            return;
        }

        Font font = minecraft.font;
        int textWidth = font.width(message);
        float availableWidth = Math.max(1.0F, screenWidth - 32.0F);
        float scale = Math.min(MAX_TEXT_SCALE, availableWidth / Math.max(1, textWidth));
        int color = Mth.ceil(alpha * 255.0F) << 24 | 0xFFFFFF;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(screenWidth / 2.0F, screenHeight / 2.0F, 0.0F);
        poseStack.scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, message, -textWidth / 2, -font.lineHeight / 2, color, true);
        poseStack.popPose();
    }

    private static float alpha() {
        if (age < FADE_IN_TICKS) {
            return age / (float) FADE_IN_TICKS;
        }

        int fadeOutStart = FADE_IN_TICKS + STAY_TICKS;
        if (age >= fadeOutStart) {
            return (TOTAL_TICKS - age) / (float) FADE_OUT_TICKS;
        }

        return 1.0F;
    }
}
