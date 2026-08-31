package net.MechGaming.EndlessSands.inventory;

import net.MechGaming.EndlessSands.block.custom.ZenioniteBatteryBlock;
import net.MechGaming.EndlessSands.block.entity.ZenioniteBatteryBlockEntity;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ZenioniteBatteryMenu extends AbstractContainerMenu {
    private static final int VANILLA_COLUMNS = 9;
    private static final int EXPANDED_COLUMNS = 10;
    private static final int PLAYER_INVENTORY_Y = 124;
    private static final int PLAYER_HOTBAR_Y = 182;

    @Nullable
    private final ZenioniteBatteryBlockEntity battery;
    private final ContainerData data;
    private final BlockPos batteryPos;
    private final boolean expanded;
    private final Level menuLevel;
    private final int mainInventorySlotCount;

    public ZenioniteBatteryMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, readClientPayload(inventory, buffer));
    }

    private ZenioniteBatteryMenu(int containerId, Inventory inventory, ClientPayload payload) {
        this(containerId, inventory, payload.battery(),
                new SimpleContainerData(ZenioniteBatteryBlockEntity.DATA_COUNT),
                payload.expanded(), payload.pos());
    }

    public ZenioniteBatteryMenu(
            int containerId,
            Inventory inventory,
            ZenioniteBatteryBlockEntity battery,
            ContainerData data
    ) {
        this(containerId, inventory, battery, data,
                ExpandedInventoryHelper.isUnlocked(inventory.player), battery.getBlockPos());
    }

    private ZenioniteBatteryMenu(
            int containerId,
            Inventory inventory,
            @Nullable ZenioniteBatteryBlockEntity battery,
            ContainerData data,
            boolean expanded,
            BlockPos batteryPos
    ) {
        super(ModMenuTypes.ZENIONITE_BATTERY.get(), containerId);
        checkContainerDataCount(data, ZenioniteBatteryBlockEntity.DATA_COUNT);
        this.battery = battery;
        this.data = data;
        this.batteryPos = batteryPos.immutable();
        this.expanded = expanded;
        this.menuLevel = inventory.player.level();

        int columns = expanded ? EXPANDED_COLUMNS : VANILLA_COLUMNS;
        int inventoryStart = expanded ? ExpandedInventoryHelper.EXPANDED_HOTBAR_SIZE
                : ExpandedInventoryHelper.VANILLA_HOTBAR_SIZE;
        this.mainInventorySlotCount = columns * 3;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < columns; column++) {
                addSlot(new Slot(inventory, inventoryStart + column + row * columns,
                        8 + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < columns; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, PLAYER_HOTBAR_Y));
        }

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < mainInventorySlotCount
                ? moveItemStackTo(stack, mainInventorySlotCount, slots.size(), false)
                : moveItemStackTo(stack, 0, mainInventorySlotCount, false);
        if (!moved) {
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
        if (battery != null) {
            return battery.stillValid(player);
        }
        return player.level().isClientSide
                && player.distanceToSqr(
                batteryPos.getX() + 0.5D,
                batteryPos.getY() + 0.5D,
                batteryPos.getZ() + 0.5D
        ) <= 64.0D;
    }

    public int getFrontEnergy() {
        return getEnergy(getFacing());
    }

    public int getLeftEnergy() {
        return getEnergy(getFacing().getCounterClockWise());
    }

    public int getRightEnergy() {
        return getEnergy(getFacing().getClockWise());
    }

    public int getBackEnergy() {
        return getEnergy(getFacing().getOpposite());
    }

    public int getSideCapacity() {
        return Math.max(1, readSplitInt(
                ZenioniteBatteryBlockEntity.DATA_SIDE_CAPACITY_LOW,
                ZenioniteBatteryBlockEntity.DATA_SIDE_CAPACITY_HIGH));
    }

    public boolean isExpanded() {
        return expanded;
    }

    public BlockPos getBatteryPos() {
        return batteryPos;
    }

    public static int screenWidth(boolean expanded) {
        return expanded ? 194 : 176;
    }

    private int getEnergy(Direction direction) {
        int index = ZenioniteBatteryBlockEntity.directionIndex(direction);
        int energy = readSplitInt(index,
                ZenioniteBatteryBlockEntity.DATA_SIDE_HIGH_START + index);
        return Math.max(0, Math.min(energy, getSideCapacity()));
    }

    private int readSplitInt(int lowIndex, int highIndex) {
        return ZenioniteBatteryBlockEntity.combineWords(
                data.get(lowIndex), data.get(highIndex));
    }

    private Direction getFacing() {
        BlockState state = menuLevel.getBlockState(batteryPos);
        return state.hasProperty(ZenioniteBatteryBlock.FACING)
                ? state.getValue(ZenioniteBatteryBlock.FACING) : Direction.NORTH;
    }

    private static ClientPayload readClientPayload(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean expanded = buffer.readBoolean();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        ZenioniteBatteryBlockEntity battery = blockEntity instanceof ZenioniteBatteryBlockEntity found
                ? found : null;
        return new ClientPayload(pos, expanded, battery);
    }

    private record ClientPayload(
            BlockPos pos,
            boolean expanded,
            @Nullable ZenioniteBatteryBlockEntity battery
    ) {
    }
}
