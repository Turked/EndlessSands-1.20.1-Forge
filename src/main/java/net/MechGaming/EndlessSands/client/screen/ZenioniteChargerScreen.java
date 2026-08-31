package net.MechGaming.EndlessSands.client.screen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.inventory.ZenioniteChargerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ZenioniteChargerScreen extends AbstractContainerScreen<ZenioniteChargerMenu> {
    private static final int SCREEN_HEIGHT = 206;
    private static final int BAR_SCALE = 3;
    private static final int BAR_HEIGHT = 16 * BAR_SCALE;

    private static final int OUTER_COLOR = 0xFF221813;
    private static final int PANEL_COLOR = 0xFF3A2A1F;
    private static final int SLOT_DARK_COLOR = 0xFF5A4129;
    private static final int SLOT_MID_COLOR = 0xFF7A5833;
    private static final int SLOT_LIGHT_COLOR = 0xFF9A7240;
    private static final int BORDER_COLOR = 0xFFC2914F;
    private static final int HIGHLIGHT_COLOR = 0xFFE0B866;
    private static final int TEXT_COLOR = 0xFFFFF0A3;

    private static final ResourceLocation EMPTY_TEXTURE = blockTexture("zenionite_charger_side_empty");
    private static final ResourceLocation WATER_TEXTURE = guiTexture("water_bar");
    private static final ResourceLocation POWER_TEXTURE = blockTexture("zenionite_charger_side_power_iso");
    private static final ResourceLocation LAVA_TEXTURE = guiTexture("lava_bar");

    private static final Component WATER_LABEL =
            Component.translatable("gui.endlesssands.zenionite_charger.water");
    private static final Component POWER_LABEL =
            Component.translatable("gui.endlesssands.zenionite_charger.power");
    private static final Component LAVA_LABEL =
            Component.translatable("gui.endlesssands.zenionite_charger.lava");

    public ZenioniteChargerScreen(ZenioniteChargerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ZenioniteChargerMenu.screenWidth(menu.isExpanded());
        this.imageHeight = SCREEN_HEIGHT;
        this.inventoryLabelY = 106;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderBarTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, OUTER_COLOR);
        graphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + 2, HIGHLIGHT_COLOR);
        graphics.fill(left + 1, top + 2, left + 2, top + this.imageHeight - 1, BORDER_COLOR);
        graphics.fill(left + 2, top + 2, left + this.imageWidth - 2, top + this.imageHeight - 2,
                PANEL_COLOR);

        drawPanel(graphics, left + 6, top + 19, this.imageWidth - 12, 84);
        drawPanel(graphics, left + 5, top + 116, this.imageWidth - 10, 84);

        for (Slot slot : this.menu.slots) {
            drawSlotBackground(graphics, left + slot.x, top + slot.y);
        }

        int barY = top + 38;
        drawBar(graphics, left + waterBarX(), barY, 0, 5, WATER_TEXTURE,
                this.menu.getWaterLevel(), true, false);
        drawBar(graphics, left + powerBarX(), barY, 5, 6, POWER_TEXTURE,
                this.menu.getPowerLevel(), false, this.menu.isPowerDraining());
        drawBar(graphics, left + lavaBarX(), barY, 11, 5, LAVA_TEXTURE,
                this.menu.getLavaLevel(), true, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, this.imageWidth / 2, 7, TEXT_COLOR);
        graphics.drawCenteredString(this.font, WATER_LABEL, waterBarX() + 7, 26, TEXT_COLOR);
        graphics.drawCenteredString(this.font, POWER_LABEL, powerBarX() + 9, 26, TEXT_COLOR);
        graphics.drawCenteredString(this.font, LAVA_LABEL, lavaBarX() + 7, 26, TEXT_COLOR);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX,
                this.inventoryLabelY, TEXT_COLOR, false);
    }

    private void renderBarTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int relativeX = mouseX - this.leftPos;
        int relativeY = mouseY - this.topPos;
        if (relativeY < 36 || relativeY >= 88) {
            return;
        }

        if (relativeX >= waterBarX() - 2 && relativeX < waterBarX() + 17) {
            renderLevelTooltip(graphics, WATER_LABEL, this.menu.getWaterLevel(), mouseX, mouseY);
        } else if (relativeX >= powerBarX() - 2 && relativeX < powerBarX() + 20) {
            renderLevelTooltip(graphics, POWER_LABEL, this.menu.getPowerLevel(), mouseX, mouseY);
        } else if (relativeX >= lavaBarX() - 2 && relativeX < lavaBarX() + 17) {
            renderLevelTooltip(graphics, LAVA_LABEL, this.menu.getLavaLevel(), mouseX, mouseY);
        }
    }

    private void renderLevelTooltip(GuiGraphics graphics, Component label, int level, int mouseX, int mouseY) {
        Component tooltip = Component.empty().append(label).append(": " + level + " / 8");
        graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, SLOT_DARK_COLOR);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BORDER_COLOR);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_COLOR);
    }

    private static void drawSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 2, y - 2, x + 18, y + 18, SLOT_DARK_COLOR);
        graphics.fill(x - 1, y - 1, x + 17, y + 17, BORDER_COLOR);
        graphics.fill(x, y, x + 16, y + 16, SLOT_MID_COLOR);
        graphics.fill(x + 1, y + 1, x + 15, y + 15, SLOT_LIGHT_COLOR);
        graphics.fill(x + 2, y + 2, x + 15, y + 15, PANEL_COLOR);
    }

    private static void drawBar(GuiGraphics graphics, int x, int y, int sourceX, int sourceWidth,
                                ResourceLocation fillTexture, int level, boolean plainBackground,
                                boolean revealFromTop) {
        int displayWidth = sourceWidth * BAR_SCALE;
        graphics.fill(x - 2, y - 2, x + displayWidth + 2, y + BAR_HEIGHT + 2, SLOT_DARK_COLOR);
        graphics.fill(x - 1, y - 1, x + displayWidth + 1, y + BAR_HEIGHT + 1, HIGHLIGHT_COLOR);
        if (plainBackground) {
            graphics.fill(x, y, x + displayWidth, y + BAR_HEIGHT, PANEL_COLOR);
        } else {
            graphics.blit(EMPTY_TEXTURE, x, y, displayWidth, BAR_HEIGHT,
                    (float) sourceX, 0.0F, sourceWidth, 16, 16, 16);
        }

        int displayFillHeight = Math.max(0, Math.min(8, level)) * 2 * BAR_SCALE;
        if (displayFillHeight <= 0) {
            return;
        }

        int clipTop = revealFromTop ? y : y + BAR_HEIGHT - displayFillHeight;
        int clipBottom = revealFromTop ? y + displayFillHeight : y + BAR_HEIGHT;
        graphics.enableScissor(x, clipTop, x + displayWidth, clipBottom);
        graphics.blit(fillTexture, x, y, displayWidth, BAR_HEIGHT,
                (float) sourceX, 0.0F, sourceWidth, 16, 16, 16);
        graphics.disableScissor();
    }

    private int waterBarX() {
        return 44;
    }

    private int powerBarX() {
        return (this.imageWidth - 18) / 2;
    }

    private int lavaBarX() {
        return this.imageWidth - 59;
    }

    private static ResourceLocation blockTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                EndlessSands.MOD_ID, "textures/block/" + name + ".png");
    }

    private static ResourceLocation guiTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                EndlessSands.MOD_ID, "textures/gui/" + name + ".png");
    }
}
