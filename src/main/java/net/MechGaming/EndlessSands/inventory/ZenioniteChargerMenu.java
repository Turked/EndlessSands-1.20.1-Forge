package net.MechGaming.EndlessSands.inventory;

import net.MechGaming.EndlessSands.block.custom.ZenioniteChargerBlock;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public class ZenioniteChargerMenu extends AbstractContainerMenu {
    public static final int WATER_DATA = 0;
    public static final int LAVA_DATA = 1;
    public static final int POWER_DATA = 2;
    public static final int DATA_COUNT = 3;

    private static final int WATER_SLOT = 0;
    private static final int LAVA_SLOT = 1;
    private static final int MACHINE_SLOT_COUNT = 2;
    private static final int VANILLA_COLUMNS = 9;
    private static final int EXPANDED_COLUMNS = 10;
    private static final int PLAYER_INVENTORY_Y = 124;
    private static final int PLAYER_HOTBAR_Y = 182;

    @Nullable
    private final ZenioniteChargerBlockEntity charger;
    private final ContainerData data;
    private final BlockPos chargerPos;
    private final boolean expanded;
    private final Level menuLevel;

    public ZenioniteChargerMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, readClientPayload(inventory, buffer));
    }

    private ZenioniteChargerMenu(int containerId, Inventory inventory, ClientPayload payload) {
        this(containerId, inventory, payload.charger(), new SimpleContainerData(DATA_COUNT),
                payload.expanded(), payload.pos());
    }

    public ZenioniteChargerMenu(int containerId, Inventory inventory,
                                ZenioniteChargerBlockEntity charger, ContainerData data) {
        this(containerId, inventory, charger, data, ExpandedInventoryHelper.isUnlocked(inventory.player),
                charger.getBlockPos());
    }

    private ZenioniteChargerMenu(int containerId, Inventory inventory,
                                 @Nullable ZenioniteChargerBlockEntity charger, ContainerData data,
                                 boolean expanded, BlockPos chargerPos) {
        super(ModMenuTypes.ZENIONITE_CHARGER.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.charger = charger;
        this.data = data;
        this.chargerPos = chargerPos.immutable();
        this.expanded = expanded;
        this.menuLevel = inventory.player.level();

        IItemHandler itemHandler = charger != null ? charger.getItemHandler() : new ItemStackHandler(2);
        int width = screenWidth(expanded);

        this.addSlot(new ChargerInputSlot(itemHandler, WATER_SLOT, 18, 53, Items.WATER_BUCKET));
        this.addSlot(new ChargerInputSlot(itemHandler, LAVA_SLOT, width - 34, 53, Items.LAVA_BUCKET));

        int columns = expanded ? EXPANDED_COLUMNS : VANILLA_COLUMNS;
        int inventoryStart = expanded ? ExpandedInventoryHelper.EXPANDED_HOTBAR_SIZE
                : ExpandedInventoryHelper.VANILLA_HOTBAR_SIZE;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < columns; column++) {
                this.addSlot(new Slot(inventory, inventoryStart + column + row * columns,
                        8 + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }

        for (int column = 0; column < columns; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, PLAYER_HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isMachineSlot(slotId) && clickType == ClickType.PICKUP) {
            ItemStack carried = this.getCarried();
            if (isBucketForSlot(slotId, carried)) {
                tryManualInput(player, carried);
                return;
            }
        }

        boolean playerSwapSlot = button >= 0 && button < player.getInventory().items.size();
        boolean offhandSwapSlot = button == ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT;
        if (isMachineSlot(slotId) && clickType == ClickType.SWAP
                && (playerSwapSlot || offhandSwapSlot)) {
            ItemStack hotbarStack = player.getInventory().getItem(button);
            if (isBucketForSlot(slotId, hotbarStack)) {
                tryManualInput(player, hotbarStack);
                return;
            }
        }

        super.clicked(slotId, button, clickType, player);
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
        if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, MACHINE_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (isFilledBucket(stack)) {
            if (player.level().isClientSide || this.charger == null
                    || !this.charger.tryManualBucket(player, stack)) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
            // tryManualBucket may put the empty bucket back into this just-vacated
            // inventory slot, so leave its post-insertion contents untouched.
            return player.getAbilities().instabuild ? ItemStack.EMPTY : original;
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
    public boolean canDragTo(Slot slot) {
        return !(slot instanceof ChargerInputSlot);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.charger != null) {
            return this.charger.stillValid(player);
        }

        return player.level().isClientSide
                && player.distanceToSqr(this.chargerPos.getX() + 0.5D,
                this.chargerPos.getY() + 0.5D, this.chargerPos.getZ() + 0.5D) <= 64.0D;
    }

    public int getWaterLevel() {
        return getDisplayedLevel(ZenioniteChargerBlock.WATER, WATER_DATA);
    }

    public int getLavaLevel() {
        return getDisplayedLevel(ZenioniteChargerBlock.LAVA, LAVA_DATA);
    }

    public int getPowerLevel() {
        return getDisplayedLevel(ZenioniteChargerBlock.POWER, POWER_DATA);
    }

    public boolean isPowerDraining() {
        BlockState state = this.menuLevel.getBlockState(this.chargerPos);
        return state.hasProperty(ZenioniteChargerBlock.POWER_DRAINING)
                && state.getValue(ZenioniteChargerBlock.POWER_DRAINING);
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public BlockPos getChargerPos() {
        return this.chargerPos;
    }

    public static int screenWidth(boolean expanded) {
        return expanded ? 194 : 176;
    }

    private int getDisplayedLevel(IntegerProperty property, int dataIndex) {
        BlockState state = this.menuLevel.getBlockState(this.chargerPos);
        if (state.hasProperty(property)) {
            return state.getValue(property);
        }
        return Mth.clamp(this.data.get(dataIndex), 0, 8);
    }

    private void tryManualInput(Player player, ItemStack stack) {
        if (!player.level().isClientSide && this.charger != null
                && this.charger.tryManualBucket(player, stack)) {
            this.broadcastChanges();
        }
    }

    private static boolean isMachineSlot(int slotId) {
        return slotId == WATER_SLOT || slotId == LAVA_SLOT;
    }

    private static boolean isBucketForSlot(int slotId, ItemStack stack) {
        return slotId == WATER_SLOT && stack.is(Items.WATER_BUCKET)
                || slotId == LAVA_SLOT && stack.is(Items.LAVA_BUCKET);
    }

    private static boolean isFilledBucket(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET);
    }

    private static ClientPayload readClientPayload(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean expanded = buffer.readBoolean();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        ZenioniteChargerBlockEntity charger = blockEntity instanceof ZenioniteChargerBlockEntity found
                ? found : null;
        return new ClientPayload(pos, expanded, charger);
    }

    private record ClientPayload(BlockPos pos, boolean expanded,
                                 @Nullable ZenioniteChargerBlockEntity charger) {
    }

    private static final class ChargerInputSlot extends SlotItemHandler {
        private final net.minecraft.world.item.Item acceptedBucket;

        private ChargerInputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition,
                                 net.minecraft.world.item.Item acceptedBucket) {
            super(itemHandler, index, xPosition, yPosition);
            this.acceptedBucket = acceptedBucket;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(this.acceptedBucket);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
