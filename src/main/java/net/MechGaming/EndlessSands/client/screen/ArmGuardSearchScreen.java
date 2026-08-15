package net.MechGaming.EndlessSands.client.screen;

import net.MechGaming.EndlessSands.inventory.ArmGuardSearchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ArmGuardSearchScreen extends AbstractContainerScreen<ArmGuardSearchMenu> {
    private static final int SCREEN_HEIGHT = 166;
    private boolean submitted;

    public ArmGuardSearchScreen(ArmGuardSearchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ArmGuardSearchMenu.screenWidth(menu.isExpanded() ? 10 : 9);
        this.imageHeight = SCREEN_HEIGHT;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> submit())
                .bounds(this.leftPos + (this.imageWidth - 60) / 2, this.topPos + 50, 60, 20)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!this.menu.getSearchStack().isEmpty()) {
            submit();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF1E1E1E);
        graphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + this.imageHeight - 1,
                0xFFFFFFFF);
        graphics.fill(left + 3, top + 3, left + this.imageWidth - 3, top + this.imageHeight - 3,
                0xFFC6C6C6);

        for (Slot slot : this.menu.slots) {
            drawSlotBackground(graphics, left + slot.x, top + slot.y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                0xFF404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX,
                this.inventoryLabelY, 0xFF404040, false);
    }

    private static void drawSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        graphics.fill(x, y, x + 17, y + 17, 0xFFFFFFFF);
        graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }

    private void submit() {
        if (!this.submitted && this.minecraft != null && this.minecraft.gameMode != null) {
            this.submitted = true;
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }
}
