package net.MechGaming.EndlessSands.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.MechGaming.EndlessSands.client.ModKeyMappings;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected abstract void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, ClickType type);

    @ModifyConstant(method = {"checkHotbarMouseClicked", "checkHotbarKeyPressed"}, constant = @Constant(intValue = 40))
    private int endlessSands$moveOffhandSwapSlot(int original) {
        return ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT;
    }

    @Inject(method = "checkHotbarKeyPressed", at = @At("HEAD"), cancellable = true)
    private void endlessSands$checkTenthHotbarKeyPressed(int keyCode, int scanCode,
                                                         CallbackInfoReturnable<Boolean> callbackInfo) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !ExpandedInventoryHelper.isUnlocked(minecraft.player)
                || !this.menu.getCarried().isEmpty()
                || this.hoveredSlot == null) {
            return;
        }

        if (ModKeyMappings.HOTBAR_SLOT_10.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.slotClicked(this.hoveredSlot, this.hoveredSlot.index,
                    ExpandedInventoryHelper.VANILLA_HOTBAR_SIZE, ClickType.SWAP);
            callbackInfo.setReturnValue(true);
        }
    }
}
