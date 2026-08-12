package net.MechGaming.EndlessSands.inventory;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.item.custom.ArmGuardItem;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public class ArmGuardSearchMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOT = 0;
    private static final int PLAYER_SLOT_START = 1;
    private static final int VANILLA_COLUMNS = 9;
    private static final int EXPANDED_COLUMNS = 10;

    private final Container searchInput = new SimpleContainer(1);
    private final int vultureId;
    private final boolean expanded;

    public ArmGuardSearchMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readVarInt(), data.readBoolean());
    }

    public ArmGuardSearchMenu(int containerId, Inventory inventory, int vultureId, boolean expanded) {
        super(ModMenuTypes.ARM_GUARD_SEARCH.get(), containerId);
        this.vultureId = vultureId;
        this.expanded = expanded;

        int columns = expanded ? EXPANDED_COLUMNS : VANILLA_COLUMNS;
        int inputX = (screenWidth(columns) - 16) / 2;
        this.addSlot(new Slot(this.searchInput, 0, inputX, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSearchToken(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        int inventoryStart = expanded ? 10 : 9;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < columns; column++) {
                this.addSlot(new Slot(inventory, inventoryStart + column + row * columns,
                        8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < columns; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public static void open(ServerPlayer player, VultureEntity vulture) {
        boolean expanded = ExpandedInventoryHelper.isUnlocked(player);
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new ArmGuardSearchMenu(containerId, inventory, vulture.getId(), expanded),
                        Component.translatable("screen.endlesssands.search")),
                data -> {
                    data.writeVarInt(vulture.getId());
                    data.writeBoolean(expanded);
                });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        ItemStack locator = this.searchInput.removeItemNoUpdate(INPUT_SLOT);
        if (locator.isEmpty()) {
            return false;
        }
        if (!isSearchToken(locator)) {
            serverPlayer.getInventory().placeItemBackInInventory(locator);
            return false;
        }

        Entity entity = serverPlayer.serverLevel().getEntity(this.vultureId);
        boolean succeeded = entity instanceof VultureEntity vulture
                && vulture.canReceiveGuardCommand(serverPlayer)
                && vulture.commandSearch(serverPlayer, locator);

        serverPlayer.getInventory().placeItemBackInInventory(locator);
        if (!succeeded) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.endlesssands.arm_guard.command_failed"), true);
        }
        serverPlayer.closeContainer();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == INPUT_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (isSearchToken(stack)) {
            if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getOffhandItem().getItem() instanceof ArmGuardItem;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            this.clearContainer(player, this.searchInput);
        }
    }

    public ItemStack getSearchStack() {
        return this.searchInput.getItem(INPUT_SLOT);
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public static int screenWidth(int columns) {
        return columns == EXPANDED_COLUMNS ? 194 : 176;
    }

    private static boolean isSearchToken(ItemStack stack) {
        return stack.is(ModBlocks.CRUD_LOG.get().asItem())
                || stack.is(ModBlocks.TWIG.get().asItem());
    }
}
