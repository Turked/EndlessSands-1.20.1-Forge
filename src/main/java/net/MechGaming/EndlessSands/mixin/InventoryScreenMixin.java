package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    @Unique
    private static final int ENDLESS_SANDS$VANILLA_WIDTH = 176;
    @Unique
    private static final int ENDLESS_SANDS$EXPANDED_WIDTH = 194;
    @Unique
    private static final int ENDLESS_SANDS$VANILLA_RESULT_X = 154;
    @Unique
    private static final int ENDLESS_SANDS$EXPANDED_RESULT_X = 172;

    @Shadow
    private float xMouse;
    @Shadow
    private float yMouse;

    @Unique
    private ImageButton endlessSands$recipeBookButton;

    private InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void endlessSands$configureInventoryWidth(CallbackInfo callbackInfo) {
        this.imageWidth = endlessSands$isExpanded()
                ? ENDLESS_SANDS$EXPANDED_WIDTH
                : ENDLESS_SANDS$VANILLA_WIDTH;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void endlessSands$configureExpandedWidgets(CallbackInfo callbackInfo) {
        ((SlotAccessor) (Object) this.menu.getSlot(0)).endlessSands$setX(
                endlessSands$isExpanded() ? ENDLESS_SANDS$EXPANDED_RESULT_X : ENDLESS_SANDS$VANILLA_RESULT_X);

        this.endlessSands$recipeBookButton = null;
        for (GuiEventListener child : this.children()) {
            if (child instanceof ImageButton imageButton
                    && imageButton.getWidth() == 20
                    && imageButton.getHeight() == 18) {
                this.endlessSands$recipeBookButton = imageButton;
                break;
            }
        }

        endlessSands$positionRecipeBookButton();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void endlessSands$keepExpandedWidgetsPositioned(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                                             float partialTick, CallbackInfo callbackInfo) {
        if (endlessSands$isExpanded()) {
            ((SlotAccessor) (Object) this.menu.getSlot(0)).endlessSands$setX(ENDLESS_SANDS$EXPANDED_RESULT_X);
            endlessSands$positionRecipeBookButton();
        }
    }

    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    private void endlessSands$renderExpandedBackground(GuiGraphics guiGraphics, float partialTick,
                                                        int mouseX, int mouseY, CallbackInfo callbackInfo) {
        if (!endlessSands$isExpanded()) {
            return;
        }

        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.blit(INVENTORY_LOCATION, x, y, 0, 0,
                ENDLESS_SANDS$VANILLA_WIDTH, this.imageHeight);

        // Insert room for the third crafting column before the vanilla arrow and output slot.
        guiGraphics.blit(INVENTORY_LOCATION, x + 151, y, 133, 0, 43, 83);

        // Repeat the final inventory cell and then retain the vanilla right border.
        guiGraphics.blit(INVENTORY_LOCATION, x + 169, y + 83, 151, 83, 25, 83);

        // Complete the 3x3 crafting frame using the vanilla crafting-slot tile.
        endlessSands$blitCraftSlot(guiGraphics, x + 133, y + 17);
        endlessSands$blitCraftSlot(guiGraphics, x + 133, y + 35);
        endlessSands$blitCraftSlot(guiGraphics, x + 97, y + 53);
        endlessSands$blitCraftSlot(guiGraphics, x + 115, y + 53);
        endlessSands$blitCraftSlot(guiGraphics, x + 133, y + 53);

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x + 51, y + 75, 30,
                (float) (x + 51) - this.xMouse,
                (float) (y + 75 - 50) - this.yMouse,
                this.minecraft.player);
        callbackInfo.cancel();
    }

    @Unique
    private void endlessSands$blitCraftSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(INVENTORY_LOCATION, x, y, 97, 17, 18, 18);
    }

    @Unique
    private void endlessSands$positionRecipeBookButton() {
        if (endlessSands$isExpanded() && this.endlessSands$recipeBookButton != null) {
            this.endlessSands$recipeBookButton.setPosition(this.leftPos + 76, this.topPos + 18);
        }
    }

    @Unique
    private boolean endlessSands$isExpanded() {
        Player player = this.minecraft == null ? null : this.minecraft.player;
        return player != null && ExpandedInventoryHelper.isUnlocked(player);
    }
}
