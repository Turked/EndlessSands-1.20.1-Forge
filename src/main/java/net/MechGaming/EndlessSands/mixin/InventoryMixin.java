package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    @Final
    public NonNullList<ItemStack> items;

    @Shadow
    @Final
    public Player player;

    @Shadow
    public int selected;

    @Shadow
    public abstract int findSlotMatchingItem(ItemStack stack);

    @Shadow
    public abstract int getSuitableHotbarSlot();

    @Shadow
    public abstract int getFreeSlot();

    @Shadow
    public abstract void pickSlot(int index);

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 36))
    private int endlessSands$expandInventorySize(int original) {
        return ExpandedInventoryHelper.EXPANDED_INVENTORY_SIZE;
    }

    @Inject(method = "getSelectionSize", at = @At("HEAD"), cancellable = true)
    private static void endlessSands$getSelectionSize(CallbackInfoReturnable<Integer> callbackInfo) {
        callbackInfo.setReturnValue(ExpandedInventoryHelper.EXPANDED_HOTBAR_SIZE);
    }

    @Inject(method = "isHotbarSlot", at = @At("HEAD"), cancellable = true)
    private static void endlessSands$isHotbarSlot(int index, CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(index >= 0 && index < ExpandedInventoryHelper.EXPANDED_HOTBAR_SIZE);
    }

    @ModifyConstant(method = {"getSuitableHotbarSlot", "swapPaint"}, constant = @Constant(intValue = 9))
    private int endlessSands$expandHotbarSize(int original) {
        return ExpandedInventoryHelper.isUnlocked(this.player)
                ? ExpandedInventoryHelper.EXPANDED_HOTBAR_SIZE
                : ExpandedInventoryHelper.VANILLA_HOTBAR_SIZE;
    }

    @ModifyConstant(method = "getSlotWithRemainingSpace", constant = @Constant(intValue = 40))
    private int endlessSands$moveOffhandCombinedSlot(int original) {
        return ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT;
    }

    @Inject(method = "getSelected", at = @At("HEAD"), cancellable = true)
    private void endlessSands$getSelected(CallbackInfoReturnable<ItemStack> callbackInfo) {
        if (this.selected == ExpandedInventoryHelper.VANILLA_HOTBAR_SIZE
                && !ExpandedInventoryHelper.isUnlocked(this.player)) {
            callbackInfo.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void endlessSands$getFreeSlot(CallbackInfoReturnable<Integer> callbackInfo) {
        if (ExpandedInventoryHelper.isUnlocked(this.player)) {
            return;
        }

        for (int index = 0; index < ExpandedInventoryHelper.VANILLA_INVENTORY_SIZE; index++) {
            if (this.items.get(index).isEmpty()) {
                callbackInfo.setReturnValue(index);
                return;
            }
        }

        callbackInfo.setReturnValue(-1);
    }

    @Inject(method = "setPickedItem", at = @At("HEAD"), cancellable = true)
    private void endlessSands$setPickedItem(ItemStack stack, org.spongepowered.asm.mixin.injection.callback.CallbackInfo callbackInfo) {
        if (ExpandedInventoryHelper.isUnlocked(this.player)) {
            return;
        }

        int matchingSlot = this.findSlotMatchingItem(stack);
        if (matchingSlot >= 0 && matchingSlot < ExpandedInventoryHelper.VANILLA_HOTBAR_SIZE) {
            this.selected = matchingSlot;
        } else {
            if (matchingSlot == -1) {
                this.selected = this.getSuitableHotbarSlot();
                if (!this.items.get(this.selected).isEmpty()) {
                    int freeSlot = this.getFreeSlot();
                    if (freeSlot != -1) {
                        this.items.set(freeSlot, this.items.get(this.selected));
                    }
                }

                this.items.set(this.selected, stack);
            } else {
                this.pickSlot(matchingSlot);
            }
        }

        callbackInfo.cancel();
    }
}
