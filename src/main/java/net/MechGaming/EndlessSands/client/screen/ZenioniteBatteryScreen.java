package net.MechGaming.EndlessSands.client.screen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.inventory.ZenioniteBatteryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ZenioniteBatteryScreen extends AbstractContainerScreen<ZenioniteBatteryMenu> {
    private static final int SCREEN_HEIGHT = 206;
    private static final int BAR_SCALE = 5;
    private static final int BAR_WIDTH = 16 * BAR_SCALE;
    private static final int BAR_HEIGHT = 2 * BAR_SCALE;
    private static final int FIRST_BAR_Y = 28;
    private static final int BAR_SPACING = 18;

    private static final int OUTER_COLOR = 0xFF221813;
    private static final int PANEL_COLOR = 0xFF3A2A1F;
    private static final int SLOT_DARK_COLOR = 0xFF5A4129;
    private static final int SLOT_MID_COLOR = 0xFF7A5833;
    private static final int SLOT_LIGHT_COLOR = 0xFF9A7240;
    private static final int BORDER_COLOR = 0xFFC2914F;
    private static final int HIGHLIGHT_COLOR = 0xFFE0B866;
    private static final int TEXT_COLOR = 0xFFFFF0A3;

    private static final ResourceLocation POWER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EndlessSands.MOD_ID,
            "textures/block/zenionite_battery_smooth_side_power_iso.png"
    );

    private static final Component FRONT_LABEL =
            Component.translatable("gui.endlesssands.zenionite_battery.front");
    private static final Component LEFT_LABEL =
            Component.translatable("gui.endlesssands.zenionite_battery.left");
    private static final Component RIGHT_LABEL =
            Component.translatable("gui.endlesssands.zenionite_battery.right");
    private static final Component BACK_LABEL =
            Component.translatable("gui.endlesssands.zenionite_battery.back");

    public ZenioniteBatteryScreen(ZenioniteBatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ZenioniteBatteryMenu.screenWidth(menu.isExpanded());
        this.imageHeight = SCREEN_HEIGHT;
        this.inventoryLabelY = 106;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderPowerTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;

        graphics.fill(left, top, left + imageWidth, top + imageHeight, OUTER_COLOR);
        graphics.fill(left + 1, top + 1, left + imageWidth - 1, top + 2, HIGHLIGHT_COLOR);
        graphics.fill(left + 1, top + 2, left + 2, top + imageHeight - 1, BORDER_COLOR);
        graphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2,
                PANEL_COLOR);

        drawPanel(graphics, left + 6, top + 19, imageWidth - 12, 84);
        drawPanel(graphics, left + 5, top + 116, imageWidth - 10, 84);

        for (Slot slot : menu.slots) {
            drawSlotBackground(graphics, left + slot.x, top + slot.y);
        }

        drawPowerBar(graphics, 0, menu.getFrontEnergy());
        drawPowerBar(graphics, 1, menu.getLeftEnergy());
        drawPowerBar(graphics, 2, menu.getRightEnergy());
        drawPowerBar(graphics, 3, menu.getBackEnergy());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 7, TEXT_COLOR);
        graphics.drawString(font, FRONT_LABEL, 12, FIRST_BAR_Y + 1, TEXT_COLOR, false);
        graphics.drawString(font, LEFT_LABEL, 12, FIRST_BAR_Y + BAR_SPACING + 1, TEXT_COLOR, false);
        graphics.drawString(font, RIGHT_LABEL, 12, FIRST_BAR_Y + BAR_SPACING * 2 + 1,
                TEXT_COLOR, false);
        graphics.drawString(font, BACK_LABEL, 12, FIRST_BAR_Y + BAR_SPACING * 3 + 1,
                TEXT_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                TEXT_COLOR, false);
    }

    private void drawPowerBar(GuiGraphics graphics, int row, int amount) {
        int x = leftPos + barX();
        int y = topPos + FIRST_BAR_Y + row * BAR_SPACING;
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, SLOT_DARK_COLOR);
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, HIGHLIGHT_COLOR);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, PANEL_COLOR);

        int capacity = menu.getSideCapacity();
        int revealWidth = Math.round(BAR_WIDTH * Math.max(0.0F,
                Math.min(1.0F, amount / (float) capacity)));
        if ((revealWidth & 1) != 0) {
            revealWidth++;
        }
        revealWidth = Math.min(BAR_WIDTH, revealWidth);
        if (revealWidth <= 0) {
            return;
        }

        int clipLeft = x + (BAR_WIDTH - revealWidth) / 2;
        int clipRight = clipLeft + revealWidth;
        graphics.enableScissor(clipLeft, y, clipRight, y + BAR_HEIGHT);
        graphics.blit(POWER_TEXTURE, x, y, BAR_WIDTH, BAR_HEIGHT,
                0.0F, 7.0F, 16, 2, 16, 16);
        graphics.disableScissor();
    }

    private void renderPowerTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int relativeX = mouseX - leftPos;
        int relativeY = mouseY - topPos;
        if (relativeX < barX() - 2 || relativeX >= barX() + BAR_WIDTH + 2) {
            return;
        }

        Component[] labels = {FRONT_LABEL, LEFT_LABEL, RIGHT_LABEL, BACK_LABEL};
        int[] amounts = {
                menu.getFrontEnergy(), menu.getLeftEnergy(),
                menu.getRightEnergy(), menu.getBackEnergy()
        };
        for (int row = 0; row < labels.length; row++) {
            int rowY = FIRST_BAR_Y + row * BAR_SPACING;
            if (relativeY >= rowY - 2 && relativeY < rowY + BAR_HEIGHT + 2) {
                Component tooltip = Component.empty().append(labels[row])
                        .append(": " + amounts[row] + " / " + menu.getSideCapacity() + " RF");
                graphics.renderTooltip(font, tooltip, mouseX, mouseY);
                return;
            }
        }
    }

    private int barX() {
        return (imageWidth - BAR_WIDTH) / 2 + 12;
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
}
