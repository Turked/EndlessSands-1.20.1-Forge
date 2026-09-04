package net.MechGaming.EndlessSands.inventory;

import net.MechGaming.EndlessSands.block.entity.ZenioniteBeaconBlockEntity;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class ZenioniteBeaconMenu extends AbstractContainerMenu {
    private static final int VANILLA_COLUMNS = 9;
    private static final int EXPANDED_COLUMNS = 10;
    private static final int PLAYER_INVENTORY_Y = 124;
    private static final int PLAYER_HOTBAR_Y = 182;

    @Nullable
    private final ZenioniteBeaconBlockEntity beacon;
    private final ContainerData data;
    private final BlockPos beaconPos;
    private final boolean expanded;
    private final int mainInventorySlotCount;

    public ZenioniteBeaconMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, readClientPayload(inventory, buffer));
    }

    private ZenioniteBeaconMenu(int containerId, Inventory inventory, ClientPayload payload) {
        this(containerId, inventory, payload.beacon(),
                new SimpleContainerData(ZenioniteBeaconBlockEntity.DATA_COUNT),
                payload.expanded(), payload.pos());
    }

    public ZenioniteBeaconMenu(
            int containerId,
            Inventory inventory,
            ZenioniteBeaconBlockEntity beacon,
            ContainerData data
    ) {
        this(containerId, inventory, beacon, data,
                ExpandedInventoryHelper.isUnlocked(inventory.player), beacon.getBlockPos());
    }

    private ZenioniteBeaconMenu(
            int containerId,
            Inventory inventory,
            @Nullable ZenioniteBeaconBlockEntity beacon,
            ContainerData data,
            boolean expanded,
            BlockPos beaconPos
    ) {
        super(ModMenuTypes.ZENIONITE_BEACON.get(), containerId);
        checkContainerDataCount(data, ZenioniteBeaconBlockEntity.DATA_COUNT);
        this.beacon = beacon;
        this.data = data;
        this.beaconPos = beaconPos.immutable();
        this.expanded = expanded;

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
        if (beacon != null) {
            return beacon.stillValid(player);
        }
        return player.level().isClientSide
                && player.distanceToSqr(
                beaconPos.getX() + 0.5D,
                beaconPos.getY() + 0.5D,
                beaconPos.getZ() + 0.5D
        ) <= 64.0D;
    }

    public int getEnergyStored() {
        return Math.max(0, Math.min(readSplitInt(
                ZenioniteBeaconBlockEntity.DATA_ENERGY_LOW,
                ZenioniteBeaconBlockEntity.DATA_ENERGY_HIGH), getEnergyCapacity()));
    }

    public int getEnergyCapacity() {
        return Math.max(1, readSplitInt(
                ZenioniteBeaconBlockEntity.DATA_CAPACITY_LOW,
                ZenioniteBeaconBlockEntity.DATA_CAPACITY_HIGH));
    }

    public boolean isEnabled() {
        return data.get(ZenioniteBeaconBlockEntity.DATA_ENABLED) != 0;
    }

    public boolean isEntombed() {
        return data.get(ZenioniteBeaconBlockEntity.DATA_ENTOMBED) != 0;
    }

    public boolean isGateControlAvailable() {
        return data.get(ZenioniteBeaconBlockEntity.DATA_GATE_CONTROL_AVAILABLE) != 0;
    }

    public boolean isImprisonmentReady() {
        return data.get(ZenioniteBeaconBlockEntity.DATA_IMPRISONMENT_READY) != 0;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public BlockPos getBeaconPos() {
        return beaconPos;
    }

    public static int screenWidth(boolean expanded) {
        return expanded ? 194 : 176;
    }

    private int readSplitInt(int lowIndex, int highIndex) {
        return ZenioniteBeaconBlockEntity.combineWords(data.get(lowIndex), data.get(highIndex));
    }

    private static ClientPayload readClientPayload(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean expanded = buffer.readBoolean();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        ZenioniteBeaconBlockEntity beacon = blockEntity instanceof ZenioniteBeaconBlockEntity found
                ? found : null;
        return new ClientPayload(pos, expanded, beacon);
    }

    private record ClientPayload(
            BlockPos pos,
            boolean expanded,
            @Nullable ZenioniteBeaconBlockEntity beacon
    ) {
    }
}
