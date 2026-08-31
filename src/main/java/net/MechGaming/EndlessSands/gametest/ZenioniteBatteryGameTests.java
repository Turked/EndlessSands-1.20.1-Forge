package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenioniteBatteryBlock;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.ZenioniteBatteryBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.inventory.ModMenuTypes;
import net.MechGaming.EndlessSands.inventory.ZenioniteBatteryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZenioniteBatteryGameTests {
    private ZenioniteBatteryGameTests() {
    }

    @GameTest(template = "empty")
    public static void registrationCapacityCapabilitiesAndMenu(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        ZenioniteBatteryBlock block = ModBlocks.ZENIONITE_BATTERY.get();
        BlockState state = block.defaultBlockState();
        helper.assertTrue(state.getValue(ZenioniteBatteryBlock.FACING) == Direction.NORTH,
                "The battery did not default to a north-facing front");
        helper.assertTrue(!state.getValue(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION),
                "The battery incorrectly started with a horizontal output connection");
        helper.assertTrue(block.getStateDefinition().getPossibleStates().size() == 128,
                "The facing and five connection properties did not create 128 battery states");
        helper.assertTrue(ModBlockEntities.ZENIONITE_BATTERY.isPresent(),
                "The battery block entity type was not registered");
        helper.assertTrue(ModMenuTypes.ZENIONITE_BATTERY.isPresent(),
                "The battery menu type was not registered");

        helper.setBlock(pos, state);
        ZenioniteBatteryBlockEntity battery = battery(helper, pos);
        int multiplier = EndlessSandsConfig.getRfMultiplier();
        helper.assertTrue(battery.getEnergyCapacity()
                        == ZenioniteBatteryBlockEntity.ENERGY_CAPACITY * multiplier,
                "Battery capacity was not 80 RF times the configured RF multiplier");
        helper.assertTrue(battery.getEnergyCapacity()
                        == ZenioniteChargerBlockEntity.ENERGY_CAPACITY * multiplier * 10,
                "The battery did not store exactly ten times as much RF as a charger");
        int largeGuiValue = 40000;
        int packetLowWord = (short) ZenioniteBatteryBlockEntity.lowWord(largeGuiValue);
        int packetHighWord = (short) ZenioniteBatteryBlockEntity.highWord(largeGuiValue);
        helper.assertTrue(ZenioniteBatteryBlockEntity.combineWords(
                        packetLowWord, packetHighWord) == largeGuiValue,
                "Battery GUI synchronization did not preserve RF values above a signed short");

        IEnergyStorage bottom = energy(battery, Direction.DOWN);
        helper.assertTrue(bottom.canReceive() && !bottom.canExtract(),
                "The bottom face was not input-only");
        for (Direction outputDirection : new Direction[]{
                Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
        }) {
            IEnergyStorage output = energy(battery, outputDirection);
            helper.assertTrue(output.canExtract() && !output.canReceive(),
                    outputDirection + " was not output-only");
        }

        Player player = helper.makeMockPlayer();
        ZenioniteBatteryMenu menu = new ZenioniteBatteryMenu(
                1, player.getInventory(), battery, battery.getContainerData());
        int expectedPlayerSlots = menu.isExpanded() ? 40 : 36;
        helper.assertTrue(menu.slots.size() == expectedPlayerSlots,
                "The battery GUI added a machine item slot");
        menu.removed(player);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disconnectedBatteryFillsFrontLeftRightBack(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState state = ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState()
                .setValue(ZenioniteBatteryBlock.FACING, Direction.NORTH);
        helper.setBlock(pos, state);
        ZenioniteBatteryBlockEntity battery = battery(helper, pos);
        int sideCapacity = battery.getSideCapacity();

        int accepted = energy(battery, Direction.DOWN)
                .receiveEnergy(sideCapacity * 3 + 1, false);
        helper.assertTrue(accepted == sideCapacity * 3 + 1,
                "The battery did not accept the requested disconnected fill amount");
        helper.assertTrue(battery.getSideEnergy(Direction.NORTH) == sideCapacity,
                "The player-facing side did not fill first");
        helper.assertTrue(battery.getSideEnergy(Direction.WEST) == sideCapacity,
                "The left side did not fill second");
        helper.assertTrue(battery.getSideEnergy(Direction.EAST) == sideCapacity,
                "The right side did not fill third");
        helper.assertTrue(battery.getSideEnergy(Direction.SOUTH) == 1,
                "The back side did not fill last");
        helper.assertTrue(Arrays.equals(
                        battery.getFillOrderSnapshot(),
                        new int[]{
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.NORTH),
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.WEST),
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.EAST),
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.SOUTH)
                        }),
                "The battery did not retain its front-left-right-back visual stack");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void connectedBatteryUsesConnectedContiguousPriority(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState connectedState = ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState()
                .setValue(ZenioniteBatteryBlock.FACING, Direction.NORTH)
                .setValue(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION, true)
                .setValue(ZenioniteBatteryBlock.EAST_CONNECTED, true)
                .setValue(ZenioniteBatteryBlock.SOUTH_CONNECTED, true);
        helper.setBlock(pos, connectedState);
        ZenioniteBatteryBlockEntity battery = battery(helper, pos);
        int sideCapacity = battery.getSideCapacity();

        energy(battery, Direction.DOWN).receiveEnergy(sideCapacity * 3 + 1, false);
        helper.assertTrue(battery.getSideEnergy(Direction.EAST) == sideCapacity,
                "The connected right side did not start the charge path");
        helper.assertTrue(battery.getSideEnergy(Direction.SOUTH) == sideCapacity,
                "The next connected and adjacent side did not fill second");
        helper.assertTrue(battery.getSideEnergy(Direction.NORTH) == sideCapacity,
                "The right-preferred contiguous fallback did not fill third");
        helper.assertTrue(battery.getSideEnergy(Direction.WEST) == 1,
                "The final remaining contiguous side did not fill last");
        helper.assertTrue(Arrays.equals(
                        battery.getFillOrderSnapshot(),
                        new int[]{
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.EAST),
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.SOUTH),
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.NORTH),
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.WEST)
                        }),
                "The connected charge path was not saved in visual stack order");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void batteryToChargerWaitsForConfiguredTransferInterval(GameTestHelper helper) {
        BlockPos batteryPos = new BlockPos(1, 1, 1);
        BlockPos chargerPos = batteryPos.above();
        helper.setBlock(batteryPos, ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(chargerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        ZenioniteBatteryBlockEntity battery = battery(helper, batteryPos);
        ZenioniteChargerBlockEntity charger = charger(helper, chargerPos);
        int sideCapacity = battery.getSideCapacity();

        energy(battery, Direction.DOWN).receiveEnergy(sideCapacity * 2 + 3, false);
        helper.assertTrue(battery.getSideEnergy(Direction.EAST) == 3,
                "The setup did not leave the right side as the top of the charge stack");

        int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
        tick(helper, batteryPos, battery, transferInterval - 1);
        helper.assertTrue(charger.getEnergyStored() == 0,
                "The battery transferred RF before the configured interval elapsed");
        helper.assertTrue(battery.getSideEnergy(Direction.EAST) == 3,
                "The battery drained its last-filled side before the transfer interval elapsed");

        tick(helper, batteryPos, battery, 1);
        helper.assertTrue(charger.getEnergyStored() == 1,
                "The battery did not transfer one RF after the configured interval");
        helper.assertTrue(battery.getSideEnergy(Direction.EAST) == 2,
                "Upward transfer did not crop the last-filled side back toward the center");
        helper.assertTrue(!helper.getBlockState(batteryPos)
                        .getValue(ZenioniteBatteryBlock.HAS_HORIZONTAL_CONNECTION),
                "A top receiver incorrectly changed the battery's horizontal base textures");

        tick(helper, batteryPos, battery, 1);
        helper.assertTrue(charger.getEnergyStored() == 1,
                "The battery transferred again before another interval elapsed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chargerToBatteryWaitsForConfiguredTransferInterval(GameTestHelper helper) {
        BlockPos chargerPos = new BlockPos(1, 1, 1);
        BlockPos batteryPos = chargerPos.above();
        helper.setBlock(chargerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        helper.setBlock(batteryPos, ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        ZenioniteChargerBlockEntity charger = charger(helper, chargerPos);
        ZenioniteBatteryBlockEntity battery = battery(helper, batteryPos);

        energy(charger, Direction.DOWN).receiveEnergy(2, false);
        int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
        tickCharger(helper, chargerPos, charger, transferInterval - 1);
        helper.assertTrue(battery.getEnergyStored() == 0,
                "The charger transferred RF into the battery before the interval elapsed");

        tickCharger(helper, chargerPos, charger, 1);
        helper.assertTrue(charger.getEnergyStored() == 1 && battery.getEnergyStored() == 1,
                "Charger-to-battery transfer did not move exactly one RF on time");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void batteryToBatteryRequiresASeparateIntervalForEachHop(GameTestHelper helper) {
        BlockPos lowerPos = new BlockPos(1, 1, 1);
        BlockPos upperPos = lowerPos.above();
        BlockPos chargerPos = upperPos.above();
        helper.setBlock(lowerPos, ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(upperPos, ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(chargerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        ZenioniteBatteryBlockEntity lower = battery(helper, lowerPos);
        ZenioniteBatteryBlockEntity upper = battery(helper, upperPos);
        ZenioniteChargerBlockEntity charger = charger(helper, chargerPos);

        energy(lower, Direction.DOWN).receiveEnergy(2, false);
        int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
        tick(helper, lowerPos, lower, transferInterval - 1);
        helper.assertTrue(upper.getEnergyStored() == 0 && charger.getEnergyStored() == 0,
                "The lower battery transferred before its interval elapsed");

        tick(helper, lowerPos, lower, 1);
        helper.assertTrue(lower.getEnergyStored() == 1 && upper.getEnergyStored() == 1
                        && charger.getEnergyStored() == 0,
                "Battery-to-battery transfer did not move exactly one RF on time");

        tick(helper, upperPos, upper, transferInterval - 1);
        helper.assertTrue(upper.getEnergyStored() == 1 && charger.getEnergyStored() == 0,
                "The receiving battery forwarded RF without waiting its own interval");
        tick(helper, upperPos, upper, 1);
        helper.assertTrue(upper.getEnergyStored() == 0 && charger.getEnergyStored() == 1,
                "The second battery did not complete its separately timed output hop");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void itemNbtKeepsEnergySidesOrderAndRelativeFacing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState northFacing = ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState()
                .setValue(ZenioniteBatteryBlock.FACING, Direction.NORTH);
        helper.setBlock(pos, northFacing);
        ZenioniteBatteryBlockEntity original = battery(helper, pos);
        int sideCapacity = original.getSideCapacity();
        energy(original, Direction.DOWN).receiveEnergy(sideCapacity + 7, false);

        BlockPos absolutePos = helper.absolutePos(pos);
        ItemStack preserved = ModBlocks.ZENIONITE_BATTERY.get().getCloneItemStack(
                helper.getLevel(), absolutePos, helper.getLevel().getBlockState(absolutePos));
        CompoundTag blockEntityTag = BlockItem.getBlockEntityData(preserved);
        helper.assertTrue(blockEntityTag != null,
                "The retained battery item did not contain block entity data");

        BlockState eastFacing = northFacing.setValue(ZenioniteBatteryBlock.FACING, Direction.EAST);
        ZenioniteBatteryBlockEntity restored = new ZenioniteBatteryBlockEntity(absolutePos, eastFacing);
        restored.load(blockEntityTag);
        helper.assertTrue(restored.getEnergyStored() == sideCapacity + 7,
                "The retained battery item lost RF");
        helper.assertTrue(restored.getSideEnergy(Direction.EAST) == sideCapacity,
                "The old front-side charge did not remain on the new front face");
        helper.assertTrue(restored.getSideEnergy(Direction.NORTH) == 7,
                "The old left-side charge did not remain on the new left face");
        helper.assertTrue(Arrays.equals(
                        restored.getFillOrderSnapshot(),
                        new int[]{
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.EAST),
                                ZenioniteBatteryBlockEntity.directionIndex(Direction.NORTH)
                        }),
                "The retained item did not preserve and rotate its LIFO charge order");
        helper.succeed();
    }

    private static ZenioniteBatteryBlockEntity battery(GameTestHelper helper, BlockPos relativePos) {
        if (helper.getBlockEntity(relativePos) instanceof ZenioniteBatteryBlockEntity battery) {
            return battery;
        }
        throw new IllegalStateException("Missing Zenionite Battery block entity at " + relativePos);
    }

    private static ZenioniteChargerBlockEntity charger(GameTestHelper helper, BlockPos relativePos) {
        if (helper.getBlockEntity(relativePos) instanceof ZenioniteChargerBlockEntity charger) {
            return charger;
        }
        throw new IllegalStateException("Missing Zenionite Charger block entity at " + relativePos);
    }

    private static IEnergyStorage energy(ZenioniteBatteryBlockEntity battery, Direction side) {
        return battery.getCapability(ForgeCapabilities.ENERGY, side)
                .orElseThrow(() -> new IllegalStateException("Missing battery energy capability on " + side));
    }

    private static IEnergyStorage energy(ZenioniteChargerBlockEntity charger, Direction side) {
        return charger.getCapability(ForgeCapabilities.ENERGY, side)
                .orElseThrow(() -> new IllegalStateException("Missing charger energy capability on " + side));
    }

    private static void tick(
            GameTestHelper helper,
            BlockPos relativePos,
            ZenioniteBatteryBlockEntity battery,
            int count
    ) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenioniteBatteryBlockEntity.serverTick(helper.getLevel(), absolutePos,
                    helper.getLevel().getBlockState(absolutePos), battery);
        }
    }

    private static void tickCharger(
            GameTestHelper helper,
            BlockPos relativePos,
            ZenioniteChargerBlockEntity charger,
            int count
    ) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenioniteChargerBlockEntity.serverTick(helper.getLevel(), absolutePos,
                    helper.getLevel().getBlockState(absolutePos), charger);
        }
    }
}
