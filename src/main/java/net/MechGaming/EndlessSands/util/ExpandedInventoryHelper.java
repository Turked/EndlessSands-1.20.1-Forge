package net.MechGaming.EndlessSands.util;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ExpandedInventoryHelper {
    public static final int VANILLA_INVENTORY_SIZE = 36;
    public static final int EXPANDED_INVENTORY_SIZE = 40;
    public static final int VANILLA_HOTBAR_SIZE = 9;
    public static final int EXPANDED_HOTBAR_SIZE = 10;
    public static final int EXPANDED_OFFHAND_SLOT = 44;
    public static final String UNLOCKED_KEY = EndlessSands.MOD_ID + ".expanded_inventory_unlocked";

    private ExpandedInventoryHelper() {
    }

    public static boolean isUnlocked(Player player) {
        return player.getPersistentData().getBoolean(UNLOCKED_KEY)
                || player.level().dimension().equals(ModDimensions.ENDLESS_SANDS_LEVEL);
    }

    public static void unlock(Player player) {
        if (player.getPersistentData().getBoolean(UNLOCKED_KEY)) {
            return;
        }

        migrateVanillaInventory(player.getInventory());
        player.getPersistentData().putBoolean(UNLOCKED_KEY, true);
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    public static void copyUnlock(Player original, Player replacement) {
        if (original.getPersistentData().getBoolean(UNLOCKED_KEY)) {
            replacement.getPersistentData().putBoolean(UNLOCKED_KEY, true);
        }
    }

    private static void migrateVanillaInventory(Inventory inventory) {
        if (inventory.items.size() < EXPANDED_INVENTORY_SIZE) {
            return;
        }

        for (int index = VANILLA_INVENTORY_SIZE - 1; index >= VANILLA_HOTBAR_SIZE; index--) {
            inventory.items.set(index + 1, inventory.items.get(index));
        }

        inventory.items.set(VANILLA_HOTBAR_SIZE, ItemStack.EMPTY);
    }
}
