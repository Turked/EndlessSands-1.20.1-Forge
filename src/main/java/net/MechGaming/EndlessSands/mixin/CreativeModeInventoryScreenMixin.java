package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin
        extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Unique
    private static final ResourceLocation ENDLESS_SANDS$CREATIVE_INVENTORY =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/container/creative_inventory/tab_inventory.png");
    @Unique
    private static final int ENDLESS_SANDS$EXPANDED_WIDTH = 194;
    @Unique
    private static final int ENDLESS_SANDS$EXPANDED_HEIGHT = 166;
    @Unique
    private static final int ENDLESS_SANDS$VANILLA_CREATIVE_WIDTH = 195;
    @Unique
    private static final int ENDLESS_SANDS$VANILLA_CREATIVE_HEIGHT = 136;

    @Shadow
    private static CreativeModeTab selectedTab;
    @Shadow
    private EditBox searchBox;
    @Shadow
    private Slot destroyItemSlot;
    @Shadow(remap = false)
    private CreativeTabsScreenPage currentPage;

    @Unique
    private boolean endlessSands$firstInit = true;

    private CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu,
                                             Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Shadow
    private void renderTabButton(GuiGraphics guiGraphics, CreativeModeTab tab) {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void endlessSands$openExpandedInventoryTab(CallbackInfo callbackInfo) {
        if (!endlessSands$isExpanded()) {
            return;
        }
        if (this.endlessSands$firstInit) {
            CreativeModeTabs.allTabs().stream()
                    .filter(tab -> tab.getType() == CreativeModeTab.Type.INVENTORY)
                    .findFirst()
                    .ifPresent(tab -> selectedTab = tab);
            this.endlessSands$firstInit = false;
        }
        endlessSands$configureDimensions(endlessSands$isInventoryTab(selectedTab));
    }

    @Inject(method = "selectTab", at = @At("HEAD"))
    private void endlessSands$prepareSelectedTab(CreativeModeTab tab, CallbackInfo callbackInfo) {
        if (endlessSands$isExpanded()) {
            endlessSands$configureDimensions(endlessSands$isInventoryTab(tab));
        }
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void endlessSands$positionExpandedInventorySlots(CreativeModeTab tab, CallbackInfo callbackInfo) {
        if (!endlessSands$isExpanded()) {
            return;
        }

        endlessSands$positionAuxiliaryWidgets();
        if (!endlessSands$isInventoryTab(tab) || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        InventoryMenu inventoryMenu = this.minecraft.player.inventoryMenu;
        int wrappedSlots = Math.min(inventoryMenu.slots.size(), this.menu.slots.size());
        for (int index = 0; index < wrappedSlots; index++) {
            Slot source = inventoryMenu.slots.get(index);
            Slot displayed = this.menu.slots.get(index);
            SlotAccessor accessor = (SlotAccessor) (Object) displayed;
            accessor.endlessSands$setX(index == 0 ? 172 : source.x);
            accessor.endlessSands$setY(source.y);
        }

        if (this.destroyItemSlot != null) {
            SlotAccessor trashAccessor = (SlotAccessor) (Object) this.destroyItemSlot;
            trashAccessor.endlessSands$setX(170);
            trashAccessor.endlessSands$setY(62);
        }
    }

    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    private void endlessSands$renderExpandedInventory(GuiGraphics guiGraphics, float partialTick,
                                                       int mouseX, int mouseY, CallbackInfo callbackInfo) {
        if (!endlessSands$isExpanded() || !endlessSands$isInventoryTab(selectedTab)) {
            return;
        }

        for (CreativeModeTab tab : this.currentPage.getVisibleTabs()) {
            if (tab != selectedTab) {
                renderTabButton(guiGraphics, tab);
            }
        }

        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(INVENTORY_LOCATION, x, y, 0, 0, 176, ENDLESS_SANDS$EXPANDED_HEIGHT);
        guiGraphics.blit(INVENTORY_LOCATION, x + 151, y, 133, 0, 43, 83);
        guiGraphics.blit(INVENTORY_LOCATION, x + 169, y + 83, 151, 83, 25, 83);

        endlessSands$blitCraftSlot(guiGraphics, x + 133, y + 17);
        endlessSands$blitCraftSlot(guiGraphics, x + 133, y + 35);
        endlessSands$blitCraftSlot(guiGraphics, x + 97, y + 53);
        endlessSands$blitCraftSlot(guiGraphics, x + 115, y + 53);
        endlessSands$blitCraftSlot(guiGraphics, x + 133, y + 53);

        // Preserve creative mode's destroy slot while using the expanded survival background.
        guiGraphics.blit(ENDLESS_SANDS$CREATIVE_INVENTORY, x + 169, y + 61, 171, 111, 19, 19);

        if (this.currentPage.getVisibleTabs().contains(selectedTab)) {
            renderTabButton(guiGraphics, selectedTab);
        }
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x + 51, y + 75, 30,
                (float) (x + 51 - mouseX), (float) (y + 25 - mouseY), this.minecraft.player);
        callbackInfo.cancel();
    }

    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void endlessSands$renderCraftingLabel(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                                  CallbackInfo callbackInfo) {
        if (endlessSands$isExpanded() && endlessSands$isInventoryTab(selectedTab)) {
            guiGraphics.drawString(this.font, Component.translatable("container.crafting"), 97, 6,
                    0x404040, false);
        }
    }

    @Unique
    private void endlessSands$configureDimensions(boolean inventoryTab) {
        this.imageWidth = inventoryTab ? ENDLESS_SANDS$EXPANDED_WIDTH : ENDLESS_SANDS$VANILLA_CREATIVE_WIDTH;
        this.imageHeight = inventoryTab ? ENDLESS_SANDS$EXPANDED_HEIGHT : ENDLESS_SANDS$VANILLA_CREATIVE_HEIGHT;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Unique
    private void endlessSands$positionAuxiliaryWidgets() {
        if (this.searchBox != null) {
            this.searchBox.setY(this.topPos + 6);
        }
        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget widget && widget.getWidth() == 20 && widget.getHeight() == 20) {
                if (widget.getX() < this.width / 2) {
                    widget.setPosition(this.leftPos, this.topPos - 50);
                } else {
                    widget.setPosition(this.leftPos + this.imageWidth - 20, this.topPos - 50);
                }
            }
        }
    }

    @Unique
    private void endlessSands$blitCraftSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(INVENTORY_LOCATION, x, y, 97, 17, 18, 18);
    }

    @Unique
    private boolean endlessSands$isExpanded() {
        Player player = this.minecraft == null ? null : this.minecraft.player;
        return player != null && ExpandedInventoryHelper.isUnlocked(player);
    }

    @Unique
    private static boolean endlessSands$isInventoryTab(CreativeModeTab tab) {
        return tab != null && tab.getType() == CreativeModeTab.Type.INVENTORY;
    }
}
