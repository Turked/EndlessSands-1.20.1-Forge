package net.MechGaming.EndlessSands.client.screen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.inventory.ZenioniteBeaconMenu;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.ZenioniteBeaconControlC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ZenioniteBeaconScreen extends AbstractContainerScreen<ZenioniteBeaconMenu> {
    private static final int SCREEN_HEIGHT = 206;
    private static final int BAR_SCALE = 3;
    private static final int BAR_WIDTH = 6 * BAR_SCALE;
    private static final int BAR_HEIGHT = 16 * BAR_SCALE;
    private static final int BAR_Y = 38;
    private static final int CONTROL_Y = 61;
    private static final int CONTROL_WIDTH = 50;
    private static final int CONTROL_HEIGHT = 20;

    private static final int OUTER_COLOR = 0xFF221813;
    private static final int PANEL_COLOR = 0xFF3A2A1F;
    private static final int SLOT_DARK_COLOR = 0xFF5A4129;
    private static final int SLOT_MID_COLOR = 0xFF7A5833;
    private static final int SLOT_LIGHT_COLOR = 0xFF9A7240;
    private static final int BORDER_COLOR = 0xFFC2914F;
    private static final int HIGHLIGHT_COLOR = 0xFFE0B866;
    private static final int TEXT_COLOR = 0xFFFFF0A3;

    private static final ResourceLocation EMPTY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EndlessSands.MOD_ID, "textures/block/zenionite_charger_side_empty.png");
    private static final ResourceLocation POWER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EndlessSands.MOD_ID, "textures/block/zenionite_charger_side_power_iso.png");
    private static final ResourceLocation GLASS_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "textures/block/glass.png");
    private static final Component POWER_LABEL =
            Component.translatable("gui.endlesssands.zenionite_beacon.power");

    private ZenioniteButton enabledButton;
    private ZenioniteButton entombButton;

    public ZenioniteBeaconScreen(ZenioniteBeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ZenioniteBeaconMenu.screenWidth(menu.isExpanded());
        this.imageHeight = SCREEN_HEIGHT;
        this.inventoryLabelY = 106;
    }

    @Override
    protected void init() {
        super.init();
        enabledButton = addRenderableWidget(new ZenioniteButton(
                leftPos + 9,
                topPos + CONTROL_Y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                enabledText(),
                false,
                () -> ModMessages.sendToServer(new ZenioniteBeaconControlC2SPacket(
                        menu.getBeaconPos(),
                        ZenioniteBeaconControlC2SPacket.Action.TOGGLE_ENABLED))));

        entombButton = addRenderableWidget(new ZenioniteButton(
                leftPos + imageWidth - CONTROL_WIDTH - 9,
                topPos + CONTROL_Y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                entombText(),
                true,
                () -> ModMessages.sendToServer(new ZenioniteBeaconControlC2SPacket(
                        menu.getBeaconPos(),
                        ZenioniteBeaconControlC2SPacket.Action.TOGGLE_ENTOMBED))));
        updateControlButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateControlButtons();
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

        drawPowerBar(graphics, left + barX(), top + BAR_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 7, TEXT_COLOR);
        graphics.drawCenteredString(font, POWER_LABEL, imageWidth / 2, 26, TEXT_COLOR);
        graphics.drawCenteredString(font,
                Component.translatable("gui.endlesssands.zenionite_beacon.on_off"),
                9 + CONTROL_WIDTH / 2, CONTROL_Y - 11, TEXT_COLOR);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                TEXT_COLOR, false);
    }

    private void updateControlButtons() {
        if (enabledButton != null) {
            enabledButton.setMessage(enabledText());
        }
        if (entombButton != null) {
            entombButton.setMessage(entombText());
            entombButton.visible = menu.isGateControlAvailable();
            entombButton.active = entombButton.visible
                    && (menu.isImprisonmentReady() || menu.isEntombed());
            if (!entombButton.visible) {
                entombButton.closeSafetyCover();
            }
        }
    }

    private Component enabledText() {
        return Component.translatable(menu.isEnabled()
                ? "gui.endlesssands.zenionite_beacon.on"
                : "gui.endlesssands.zenionite_beacon.off");
    }

    private Component entombText() {
        return Component.translatable(menu.isEntombed()
                ? "gui.endlesssands.zenionite_beacon.release"
                : "gui.endlesssands.zenionite_beacon.imprison");
    }

    private void drawPowerBar(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, SLOT_DARK_COLOR);
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, HIGHLIGHT_COLOR);
        graphics.blit(EMPTY_TEXTURE, x, y, BAR_WIDTH, BAR_HEIGHT,
                5.0F, 0.0F, 6, 16, 16, 16);

        float fillRatio = menu.getEnergyStored() / (float) menu.getEnergyCapacity();
        int fillHeight = Math.round(BAR_HEIGHT * Math.max(0.0F, Math.min(1.0F, fillRatio)));
        if (fillHeight <= 0) {
            return;
        }

        int clipTop = y + BAR_HEIGHT - fillHeight;
        graphics.enableScissor(x, clipTop, x + BAR_WIDTH, y + BAR_HEIGHT);
        graphics.blit(POWER_TEXTURE, x, y, BAR_WIDTH, BAR_HEIGHT,
                5.0F, 0.0F, 6, 16, 16, 16);
        graphics.disableScissor();
    }

    private void renderPowerTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int relativeX = mouseX - leftPos;
        int relativeY = mouseY - topPos;
        if (relativeX >= barX() - 2 && relativeX < barX() + BAR_WIDTH + 2
                && relativeY >= BAR_Y - 2 && relativeY < BAR_Y + BAR_HEIGHT + 2) {
            Component tooltip = Component.empty().append(POWER_LABEL)
                    .append(": " + menu.getEnergyStored() + " / "
                            + menu.getEnergyCapacity() + " RF");
            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private int barX() {
        return (imageWidth - BAR_WIDTH) / 2;
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

    private static final class ZenioniteButton extends AbstractButton {
        private final boolean safetyCover;
        private final Runnable action;
        private boolean safetyCoverOpen;

        private ZenioniteButton(
                int x,
                int y,
                int width,
                int height,
                Component message,
                boolean safetyCover,
                Runnable action
        ) {
            super(x, y, width, height, message);
            this.safetyCover = safetyCover;
            this.action = action;
        }

        @Override
        public void onPress() {
            if (safetyCover && !safetyCoverOpen) {
                safetyCoverOpen = true;
                return;
            }
            action.run();
            if (safetyCover) {
                safetyCoverOpen = false;
            }
        }

        @Override
        protected void renderWidget(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            int x = getX();
            int y = getY();
            boolean highlightButton = isHoveredOrFocused()
                    && (!safetyCover || safetyCoverOpen);
            int base = highlightButton ? SLOT_LIGHT_COLOR : SLOT_MID_COLOR;
            graphics.fill(x, y, x + width, y + height, SLOT_DARK_COLOR);
            graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BORDER_COLOR);
            graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, base);
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    getMessage(),
                    x + width / 2,
                    y + (height - 8) / 2,
                    TEXT_COLOR);

            if (!safetyCover) {
                return;
            }
            if (safetyCoverOpen) {
                int raisedY = y - 9;
                graphics.fill(x, raisedY, x + width, raisedY + 8, SLOT_DARK_COLOR);
                graphics.blit(GLASS_TEXTURE, x + 1, raisedY + 1,
                        width - 2, 6, 0.0F, 0.0F, 16, 16, 16, 16);
                graphics.fill(x, y - 1, x + width, y + 1, HIGHLIGHT_COLOR);
            } else {
                graphics.blit(GLASS_TEXTURE, x + 1, y + 1,
                        width - 2, height - 2, 0.0F, 0.0F, 16, 16, 16, 16);
                graphics.fill(x, y, x + width, y + 1, HIGHLIGHT_COLOR);
                graphics.fill(x, y, x + 1, y + height, BORDER_COLOR);
            }
        }

        private void closeSafetyCover() {
            safetyCoverOpen = false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }
}
