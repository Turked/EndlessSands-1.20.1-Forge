package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.inventory.ExpandedInventorySlot;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingContainer> {
    private static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_END = 5;
    private static final int ARMOR_SLOT_START = 5;
    private static final int ARMOR_SLOT_END = 9;
    private static final int VANILLA_MAIN_SLOT_START = 9;
    private static final int VANILLA_MAIN_SLOT_END = 36;
    private static final int HOTBAR_SLOT_START = 36;
    private static final int HOTBAR_SLOT_END = 45;
    private static final int OFFHAND_SLOT = 45;
    private static final int EXTRA_CRAFT_SLOT_START = 46;
    private static final int EXTRA_CRAFT_SLOT_END = 51;
    private static final int EXPANDED_MAIN_SLOT_START = 51;
    private static final int EXPANDED_MAIN_SLOT_END = 81;
    private static final int EXPANDED_HOTBAR_SLOT_10 = 81;

    @Shadow
    @Final
    private Player owner;

    private InventoryMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 2, ordinal = 0))
    private int endlessSands$useThreeWideBackingCraftGrid(int original) {
        return 3;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 2, ordinal = 1))
    private int endlessSands$useThreeHighBackingCraftGrid(int original) {
        return 3;
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/inventory/Slot"))
    private Slot endlessSands$createGatedVanillaSlot(Container container, int slot, int x, int y) {
        if (container instanceof CraftingContainer) {
            return new ExpandedInventorySlot(container, mapVanillaCraftSlot(slot), x, y,
                    ExpandedInventorySlot.Visibility.ALWAYS, this.owner);
        }

        if (container instanceof Inventory && slot >= VANILLA_MAIN_SLOT_START && slot < VANILLA_MAIN_SLOT_END) {
            return new ExpandedInventorySlot(container, slot, x, y,
                    ExpandedInventorySlot.Visibility.BEFORE_UNLOCK);
        }

        return new ExpandedInventorySlot(container, slot, x, y,
                ExpandedInventorySlot.Visibility.ALWAYS);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int endlessSands$moveArmorCombinedSlot(int original) {
        return 43;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int endlessSands$moveOffhandCombinedSlot(int original) {
        return ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void endlessSands$addExpandedSlots(Inventory inventory, boolean active, Player owner, CallbackInfo callbackInfo) {
        CraftingContainer craftSlots = ((InventoryMenu) (Object) this).getCraftSlots();

        this.addSlot(new ExpandedInventorySlot(craftSlots, 2, 134, 18,
                ExpandedInventorySlot.Visibility.AFTER_UNLOCK, owner));
        this.addSlot(new ExpandedInventorySlot(craftSlots, 5, 134, 36,
                ExpandedInventorySlot.Visibility.AFTER_UNLOCK, owner));
        this.addSlot(new ExpandedInventorySlot(craftSlots, 6, 98, 54,
                ExpandedInventorySlot.Visibility.AFTER_UNLOCK, owner));
        this.addSlot(new ExpandedInventorySlot(craftSlots, 7, 116, 54,
                ExpandedInventorySlot.Visibility.AFTER_UNLOCK, owner));
        this.addSlot(new ExpandedInventorySlot(craftSlots, 8, 134, 54,
                ExpandedInventorySlot.Visibility.AFTER_UNLOCK, owner));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 10; column++) {
                int inventoryIndex = 10 + column + row * 10;
                this.addSlot(new ExpandedInventorySlot(inventory, inventoryIndex,
                        8 + column * 18, 84 + row * 18,
                        ExpandedInventorySlot.Visibility.AFTER_UNLOCK));
            }
        }

        this.addSlot(new ExpandedInventorySlot(inventory, 9, 170, 142,
                ExpandedInventorySlot.Visibility.AFTER_UNLOCK));
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void endlessSands$quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> callbackInfo) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem() || !slot.isActive()) {
            callbackInfo.setReturnValue(movedStack);
            return;
        }

        boolean unlocked = ExpandedInventoryHelper.isUnlocked(player);
        ItemStack slotStack = slot.getItem();
        movedStack = slotStack.copy();
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(movedStack);

        if (index == RESULT_SLOT) {
            if (!moveToIndexes(slotStack, true, getInventoryAndHotbarSlots(unlocked))) {
                callbackInfo.setReturnValue(ItemStack.EMPTY);
                return;
            }

            slot.onQuickCraft(slotStack, movedStack);
        } else if (isCraftSlot(index) || isArmorSlot(index) || index == OFFHAND_SLOT) {
            if (!moveToIndexes(slotStack, false, getInventoryAndHotbarSlots(unlocked))) {
                callbackInfo.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR
                && !this.slots.get(8 - equipmentSlot.getIndex()).hasItem()) {
            int targetSlot = 8 - equipmentSlot.getIndex();
            if (!moveToIndexes(slotStack, false, targetSlot)) {
                callbackInfo.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (equipmentSlot == EquipmentSlot.OFFHAND && !this.slots.get(OFFHAND_SLOT).hasItem()) {
            if (!moveToIndexes(slotStack, false, OFFHAND_SLOT)) {
                callbackInfo.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (isMainInventorySlot(index, unlocked)) {
            if (!moveToIndexes(slotStack, false, getHotbarSlots(unlocked))) {
                callbackInfo.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (isHotbarSlotIndex(index, unlocked)) {
            if (!moveToIndexes(slotStack, false, getMainInventorySlots(unlocked))) {
                callbackInfo.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (!moveToIndexes(slotStack, false, getInventoryAndHotbarSlots(unlocked))) {
            callbackInfo.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == movedStack.getCount()) {
            callbackInfo.setReturnValue(ItemStack.EMPTY);
            return;
        }

        slot.onTake(player, slotStack);
        if (index == RESULT_SLOT) {
            player.drop(slotStack, false);
        }

        callbackInfo.setReturnValue(movedStack);
    }

    @Inject(method = "isHotbarSlot", at = @At("HEAD"), cancellable = true)
    private static void endlessSands$isHotbarSlot(int index, CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(index >= HOTBAR_SLOT_START && index < HOTBAR_SLOT_END
                || index == OFFHAND_SLOT
                || index == EXPANDED_HOTBAR_SLOT_10);
    }

    @Inject(method = "getGridWidth", at = @At("HEAD"), cancellable = true)
    private void endlessSands$getGridWidth(CallbackInfoReturnable<Integer> callbackInfo) {
        callbackInfo.setReturnValue(ExpandedInventoryHelper.isUnlocked(this.owner) ? 3 : 2);
    }

    @Inject(method = "getGridHeight", at = @At("HEAD"), cancellable = true)
    private void endlessSands$getGridHeight(CallbackInfoReturnable<Integer> callbackInfo) {
        callbackInfo.setReturnValue(ExpandedInventoryHelper.isUnlocked(this.owner) ? 3 : 2);
    }

    @Inject(method = "getSize", at = @At("HEAD"), cancellable = true)
    private void endlessSands$getSize(CallbackInfoReturnable<Integer> callbackInfo) {
        callbackInfo.setReturnValue(ExpandedInventoryHelper.isUnlocked(this.owner) ? 10 : 5);
    }

    private boolean moveToIndexes(ItemStack stack, boolean reverse, int... indexes) {
        boolean moved = false;

        if (stack.isStackable()) {
            int current = reverse ? indexes.length - 1 : 0;
            while (!stack.isEmpty() && current >= 0 && current < indexes.length) {
                Slot slot = this.slots.get(indexes[current]);
                ItemStack slotStack = slot.getItem();

                if (slot.isActive() && !slotStack.isEmpty() && ItemStack.isSameItemSameTags(stack, slotStack)) {
                    int combinedCount = slotStack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                    if (combinedCount <= maxSize) {
                        stack.setCount(0);
                        slotStack.setCount(combinedCount);
                        slot.setChanged();
                        moved = true;
                    } else if (slotStack.getCount() < maxSize) {
                        stack.shrink(maxSize - slotStack.getCount());
                        slotStack.setCount(maxSize);
                        slot.setChanged();
                        moved = true;
                    }
                }

                current += reverse ? -1 : 1;
            }
        }

        if (!stack.isEmpty()) {
            int current = reverse ? indexes.length - 1 : 0;
            while (current >= 0 && current < indexes.length) {
                Slot slot = this.slots.get(indexes[current]);
                if (slot.isActive() && slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.setByPlayer(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.setByPlayer(stack.split(stack.getCount()));
                    }

                    slot.setChanged();
                    moved = true;
                    break;
                }

                current += reverse ? -1 : 1;
            }
        }

        return moved;
    }

    private static int mapVanillaCraftSlot(int slot) {
        return switch (slot) {
            case 2 -> 3;
            case 3 -> 4;
            default -> slot;
        };
    }

    private static boolean isCraftSlot(int index) {
        return index >= CRAFT_SLOT_START && index < CRAFT_SLOT_END
                || index >= EXTRA_CRAFT_SLOT_START && index < EXTRA_CRAFT_SLOT_END;
    }

    private static boolean isArmorSlot(int index) {
        return index >= ARMOR_SLOT_START && index < ARMOR_SLOT_END;
    }

    private static boolean isMainInventorySlot(int index, boolean unlocked) {
        if (unlocked) {
            return index >= EXPANDED_MAIN_SLOT_START && index < EXPANDED_MAIN_SLOT_END;
        }

        return index >= VANILLA_MAIN_SLOT_START && index < VANILLA_MAIN_SLOT_END;
    }

    private static boolean isHotbarSlotIndex(int index, boolean unlocked) {
        return index >= HOTBAR_SLOT_START && index < HOTBAR_SLOT_END
                || unlocked && index == EXPANDED_HOTBAR_SLOT_10;
    }

    private static int[] getInventoryAndHotbarSlots(boolean unlocked) {
        return unlocked
                ? concat(getMainInventorySlots(true), getHotbarSlots(true))
                : range(VANILLA_MAIN_SLOT_START, HOTBAR_SLOT_END);
    }

    private static int[] getMainInventorySlots(boolean unlocked) {
        return unlocked
                ? range(EXPANDED_MAIN_SLOT_START, EXPANDED_MAIN_SLOT_END)
                : range(VANILLA_MAIN_SLOT_START, VANILLA_MAIN_SLOT_END);
    }

    private static int[] getHotbarSlots(boolean unlocked) {
        return unlocked
                ? concat(range(HOTBAR_SLOT_START, HOTBAR_SLOT_END), new int[]{EXPANDED_HOTBAR_SLOT_10})
                : range(HOTBAR_SLOT_START, HOTBAR_SLOT_END);
    }

    private static int[] range(int startInclusive, int endExclusive) {
        int[] values = new int[endExclusive - startInclusive];
        for (int index = 0; index < values.length; index++) {
            values[index] = startInclusive + index;
        }

        return values;
    }

    private static int[] concat(int[] first, int[] second) {
        int[] values = new int[first.length + second.length];
        System.arraycopy(first, 0, values, 0, first.length);
        System.arraycopy(second, 0, values, first.length, second.length);
        return values;
    }
}
