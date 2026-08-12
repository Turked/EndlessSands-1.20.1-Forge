package net.MechGaming.EndlessSands.inventory;

import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ExpandedInventorySlot extends Slot {
    @Nullable
    private final Player owner;
    private final Visibility visibility;

    public ExpandedInventorySlot(Container container, int slot, int x, int y, Visibility visibility) {
        this(container, slot, x, y, visibility, null);
    }

    public ExpandedInventorySlot(Container container, int slot, int x, int y,
                                 Visibility visibility, @Nullable Player owner) {
        super(container, slot, x, y);
        this.owner = owner;
        this.visibility = visibility;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.isActive() && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.isActive() && super.mayPickup(player);
    }

    @Override
    public boolean isActive() {
        Player player = getPlayer();
        if (player == null) {
            return this.visibility == Visibility.ALWAYS;
        }

        boolean unlocked = ExpandedInventoryHelper.isUnlocked(player);
        return switch (this.visibility) {
            case ALWAYS -> true;
            case BEFORE_UNLOCK -> !unlocked;
            case AFTER_UNLOCK -> unlocked;
        };
    }

    private Player getPlayer() {
        if (this.owner != null) {
            return this.owner;
        }

        if (this.container instanceof Inventory inventory) {
            return inventory.player;
        }

        return null;
    }

    public enum Visibility {
        ALWAYS,
        BEFORE_UNLOCK,
        AFTER_UNLOCK
    }
}
