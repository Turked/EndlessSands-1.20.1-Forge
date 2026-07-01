package net.MechGaming.EndlessSands.ability;

import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * Holds reusable checks for Dragonite armor set bonuses.
 *
 * This class does not apply effects or boost the player.
 * It only answers: "Is the player wearing the full Dragonite armor set?"
 */
public class DragoniteArmorSetBonus {

    /**
     * Checks the player's armor slots for the exact Dragonite armor items.
     *
     * Armor slot indexes:
     * 0 = boots
     * 1 = leggings
     * 2 = chestplate
     * 3 = helmet
     */
    public static boolean hasFullDragoniteArmor(Player player) {
        return isWearing(player, 0, ModItems.DRAGONITE_BOOTS.get())
                && isWearing(player, 1, ModItems.DRAGONITE_LEGGINGS.get())
                && isWearing(player, 2, ModItems.DRAGONITE_CHESTPLATE.get())
                && isWearing(player, 3, ModItems.DRAGONITE_HELMET.get());
    }

    /**
     * Checks whether a specific armor slot contains a specific item.
     */
    private static boolean isWearing(Player player, int armorSlot, Item expectedItem) {
        return player.getInventory().getArmor(armorSlot).getItem() == expectedItem;
    }
}