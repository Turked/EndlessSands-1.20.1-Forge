package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenioniteChargerBlock;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.inventory.ModMenuTypes;
import net.MechGaming.EndlessSands.inventory.ZenioniteChargerMenu;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZenioniteChargerGameTests {
    private ZenioniteChargerGameTests() {
    }

    @GameTest(template = "empty")
    public static void registrationAndDefaultState(GameTestHelper helper) {
        ZenioniteChargerBlock block = ModBlocks.ZENIONITE_CHARGER.get();
        BlockState state = block.defaultBlockState();
        helper.assertTrue(state.getValue(ZenioniteChargerBlock.WATER) == 0,
                "The charger did not start with an empty water gauge");
        helper.assertTrue(state.getValue(ZenioniteChargerBlock.LAVA) == 0,
                "The charger did not start with an empty lava gauge");
        helper.assertTrue(state.getValue(ZenioniteChargerBlock.POWER) == 0,
                "The charger did not start with an empty power gauge");
        helper.assertTrue(!state.getValue(ZenioniteChargerBlock.POWER_DRAINING),
                "The charger incorrectly started in upward-draining display mode");
        helper.assertTrue(block.getStateDefinition().getPossibleStates().size() == 1_458,
                "The gauges and power-flow direction did not produce 1,458 combined states");
        helper.assertTrue(ModBlockEntities.ZENIONITE_CHARGER.isPresent(),
                "The charger block entity type was not registered");
        helper.assertTrue(ModMenuTypes.ZENIONITE_CHARGER.isPresent(),
                "The charger menu type was not registered");

        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, state);
        ZenioniteChargerBlockEntity charger = charger(helper, pos);
        int multiplier = Math.max(1, EndlessSandsConfig.RF_MULTIPLIER.get());
        helper.assertTrue(charger.getEnergyCapacity()
                        == ZenioniteChargerBlockEntity.ENERGY_CAPACITY * multiplier,
                "RF capacity was not the configured Charger capacity times the RF multiplier");
        helper.assertTrue(EndlessSandsConfig.DEFAULT_RF_TRANSFER_INTERVAL_TICKS == 10,
                "The default RF transfer interval was not half a second");
        helper.assertTrue(ZenioniteChargerBlockEntity.TANK_CAPACITY == 8_000,
                "A charger fluid tank did not hold eight buckets");
        charger.fillEnergyToCapacity();
        helper.assertTrue(charger.getEnergyStored() == charger.getEnergyCapacity(),
                "The admin fill path did not maximize charger RF");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void directBucketsFillAndReturnEmpties(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        ZenioniteChargerBlock block = ModBlocks.ZENIONITE_CHARGER.get();
        helper.setBlock(relativePos, block.defaultBlockState());
        ZenioniteChargerBlockEntity charger = charger(helper, relativePos);
        Player player = helper.makeMockPlayer();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH,
                absolutePos, false);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        block.use(helper.getBlockState(relativePos), helper.getLevel(), absolutePos,
                player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(charger.getWaterAmount() == 1_000,
                "Right-clicking with a water bucket did not add one bucket");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.BUCKET)),
                "The water bucket did not return an empty bucket to the player");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
        block.use(helper.getBlockState(relativePos), helper.getLevel(), absolutePos,
                player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(charger.getLavaAmount() == 1_000,
                "Right-clicking with a lava bucket did not add one bucket");
        helper.assertTrue(helper.getBlockState(relativePos).getValue(ZenioniteChargerBlock.WATER) == 1
                        && helper.getBlockState(relativePos).getValue(ZenioniteChargerBlock.LAVA) == 1,
                "The block gauges did not follow direct bucket insertion");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void offhandBucketUseDefersMainHandAndFillsCharger(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        ZenioniteChargerBlock block = ModBlocks.ZENIONITE_CHARGER.get();
        helper.setBlock(relativePos, block.defaultBlockState());
        ZenioniteChargerBlockEntity charger = charger(helper, relativePos);
        Player player = helper.makeMockPlayer();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH,
                absolutePos, false);

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.WATER_BUCKET));
        InteractionResult mainHandResult = block.use(helper.getBlockState(relativePos),
                helper.getLevel(), absolutePos, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(mainHandResult == InteractionResult.PASS,
                "An empty main hand did not defer to a filled offhand bucket");
        helper.assertTrue(charger.getWaterAmount() == 0,
                "The main-hand pass inserted the offhand bucket prematurely");

        InteractionResult offhandResult = block.use(helper.getBlockState(relativePos),
                helper.getLevel(), absolutePos, player, InteractionHand.OFF_HAND, hit);
        helper.assertTrue(offhandResult.consumesAction(),
                "The offhand water bucket interaction was not consumed");
        helper.assertTrue(charger.getWaterAmount() == 1_000,
                "Right-clicking with an offhand water bucket did not fill the charger");
        helper.assertTrue(player.getOffhandItem().isEmpty(),
                "The filled bucket remained in the player's offhand");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.BUCKET)),
                "The offhand interaction did not return an empty bucket to the player");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void menuOffhandSwapUsesCombinedSlotFortyFour(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        ZenioniteChargerBlockEntity charger = charger(helper, pos);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.LAVA_BUCKET));
        helper.assertTrue(player.getInventory().getItem(
                        ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT).is(Items.LAVA_BUCKET),
                "Combined inventory slot 44 did not resolve to the player's offhand");

        ZenioniteChargerMenu menu = new ZenioniteChargerMenu(
                1, player.getInventory(), charger, charger.getContainerData());
        menu.clicked(ZenioniteChargerBlockEntity.LAVA_SLOT,
                ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT, ClickType.SWAP, player);

        helper.assertTrue(charger.getLavaAmount() == 1_000,
                "Swapping the offhand lava bucket onto the GUI input did not fill the charger");
        helper.assertTrue(player.getOffhandItem().isEmpty(),
                "The filled bucket remained in the player's offhand after the GUI swap");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.BUCKET)),
                "The GUI offhand swap did not return an empty bucket to the player");
        helper.assertTrue(charger.getAutomationItemHandler()
                        .getStackInSlot(ZenioniteChargerBlockEntity.LAVA_SLOT).isEmpty(),
                "Manual GUI insertion incorrectly retained a bucket in the automation slot");
        menu.removed(player);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fuelConversionUsesTwentyTicksAndOneRf(GameTestHelper helper) {
        int originalMultiplier = EndlessSandsConfig.RF_MULTIPLIER.get();
        int testMultiplier = originalMultiplier == 3 ? 4 : 3;
        try {
            EndlessSandsConfig.RF_MULTIPLIER.set(testMultiplier);
            BlockPos pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
            ZenioniteChargerBlockEntity charger = charger(helper, pos);
            IFluidHandler fluidInput = fluidInput(charger, Direction.NORTH);
            helper.assertTrue(charger.getEnergyCapacity() == 8 * testMultiplier,
                    "The regression test did not exercise a multiplied RF capacity");
            helper.assertTrue(fluidInput.fill(new FluidStack(Fluids.WATER, 1_000),
                    IFluidHandler.FluidAction.EXECUTE) == 1_000, "The water tank refused a bucket");
            helper.assertTrue(fluidInput.fill(new FluidStack(Fluids.LAVA, 1_000),
                    IFluidHandler.FluidAction.EXECUTE) == 1_000, "The lava tank refused a bucket");

            tick(helper, pos, charger, 19);
            helper.assertTrue(charger.getEnergyStored() == 0,
                    "The charger generated RF before twenty valid ticks");
            helper.assertTrue(charger.getWaterAmount() == 1_000 && charger.getLavaAmount() == 1_000,
                    "The charger consumed fuel before generation completed");

            tick(helper, pos, charger, 1);
            helper.assertTrue(charger.getEnergyStored() == 1,
                    "A fuel pair did not generate exactly one RF when capacity was multiplied");
            helper.assertTrue(charger.getWaterAmount() == 0 && charger.getLavaAmount() == 0,
                    "A completed cycle did not consume one bucket of each fluid");
            helper.assertTrue(helper.getBlockState(pos).getValue(ZenioniteChargerBlock.POWER) == 1,
                    "The first RF did not show as one power segment");
            helper.assertTrue(!helper.getBlockState(pos)
                            .getValue(ZenioniteChargerBlock.POWER_DRAINING),
                    "Generated power incorrectly used the upward-draining texture direction");
        } finally {
            EndlessSandsConfig.RF_MULTIPLIER.set(originalMultiplier);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sourceAndFlowingCollectionUseExactTimers(GameTestHelper helper) {
        BlockPos sourceChargerPos = new BlockPos(1, 1, 1);
        BlockPos flowingChargerPos = new BlockPos(1, 1, 3);
        helper.setBlock(sourceChargerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        helper.setBlock(flowingChargerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        helper.setBlock(sourceChargerPos.east(), Blocks.WATER);
        helper.setBlock(flowingChargerPos.east(), Blocks.LAVA.defaultBlockState()
                .setValue(LiquidBlock.LEVEL, 3));
        ZenioniteChargerBlockEntity sourceCharger = charger(helper, sourceChargerPos);
        ZenioniteChargerBlockEntity flowingCharger = charger(helper, flowingChargerPos);

        tick(helper, sourceChargerPos, sourceCharger, 1_199);
        helper.assertTrue(sourceCharger.getWaterAmount() == 0,
                "A source fluid was harvested before 1,200 ticks");
        tick(helper, sourceChargerPos, sourceCharger, 1);
        helper.assertTrue(sourceCharger.getWaterAmount() == 1_000,
                "A source fluid was not harvested on tick 1,200");

        tick(helper, flowingChargerPos, flowingCharger, 11_999);
        helper.assertTrue(flowingCharger.getLavaAmount() == 0,
                "A flowing fluid was harvested before 12,000 ticks");
        tick(helper, flowingChargerPos, flowingCharger, 1);
        helper.assertTrue(flowingCharger.getLavaAmount() == 1_000,
                "A flowing fluid was not harvested on tick 12,000");
        helper.assertTrue(helper.getLevel().getFluidState(helper.absolutePos(sourceChargerPos.east())).isSource(),
                "Environmental collection consumed the water source");
        helper.assertTrue(!helper.getLevel().getFluidState(helper.absolutePos(flowingChargerPos.east())).isSource(),
                "Environmental collection replaced flowing lava with a source");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sidedCapabilitiesAndAutomationObeyDirections(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        ZenioniteChargerBlockEntity charger = charger(helper, pos);

        helper.assertTrue(charger.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.NORTH).isPresent(),
                "A horizontal side did not expose fluid input");
        helper.assertTrue(!charger.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).isPresent()
                        && !charger.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).isPresent(),
                "The top or bottom incorrectly exposed fluid input");
        helper.assertTrue(charger.getCapability(ForgeCapabilities.ENERGY, Direction.DOWN).isPresent(),
                "The bottom did not expose RF input");
        helper.assertTrue(charger.getCapability(ForgeCapabilities.ENERGY, Direction.UP).isPresent(),
                "The top did not expose RF output");
        helper.assertTrue(!charger.getCapability(ForgeCapabilities.ENERGY, Direction.NORTH).isPresent(),
                "A horizontal side incorrectly exposed RF");

        IItemHandler sideItems = itemHandler(charger, Direction.WEST);
        helper.assertTrue(sideItems.insertItem(ZenioniteChargerBlockEntity.WATER_SLOT,
                new ItemStack(Items.WATER_BUCKET), false).isEmpty(),
                "A side hopper could not insert a water bucket");
        tick(helper, pos, charger, 1);
        helper.assertTrue(charger.getWaterAmount() == 1_000,
                "An automated water bucket did not fill the tank");
        helper.assertTrue(charger.getAutomationItemHandler()
                        .getStackInSlot(ZenioniteChargerBlockEntity.WATER_SLOT).is(Items.BUCKET),
                "Automation did not retain the empty bucket");
        ItemStack extracted = itemHandler(charger, Direction.DOWN)
                .extractItem(ZenioniteChargerBlockEntity.WATER_SLOT, 1, false);
        helper.assertTrue(extracted.is(Items.BUCKET),
                "A hopper below could not extract the retained empty bucket");

        IEnergyStorage input = energy(charger, Direction.DOWN);
        IEnergyStorage output = energy(charger, Direction.UP);
        helper.assertTrue(input.receiveEnergy(4, false) == 4,
                "The bottom RF input did not accept energy");
        int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
        tick(helper, pos, charger, transferInterval - 1);
        helper.assertTrue(output.extractEnergy(1, true) == 0,
                "The top output RF before the configured transfer interval elapsed");
        tick(helper, pos, charger, 1);
        helper.assertTrue(output.extractEnergy(1, true) == 1,
                "Simulating top output did not report one RF after the transfer interval");
        helper.assertTrue(output.extractEnergy(1, false) == 1,
                "The top did not output one RF after the transfer interval");
        helper.assertTrue(output.extractEnergy(1, false) == 0,
                "The top output again before another transfer interval elapsed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void stackedChargersWaitOneTransferIntervalPerHop(GameTestHelper helper) {
        BlockPos lowerPos = new BlockPos(1, 1, 1);
        BlockPos middlePos = lowerPos.above();
        BlockPos upperPos = middlePos.above();
        helper.setBlock(lowerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        helper.setBlock(middlePos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        helper.setBlock(upperPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        ZenioniteChargerBlockEntity lower = charger(helper, lowerPos);
        ZenioniteChargerBlockEntity middle = charger(helper, middlePos);
        ZenioniteChargerBlockEntity upper = charger(helper, upperPos);

        helper.assertTrue(energy(lower, Direction.DOWN).receiveEnergy(2, false) == 2,
                "The bottom charger could not receive test power");
        helper.assertTrue(helper.getBlockState(lowerPos).getValue(ZenioniteChargerBlock.POWER)
                        == lower.getPowerLevel(),
                "The lower charger's power texture did not match its stored RF before transfer");
        int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
        tick(helper, lowerPos, lower, transferInterval - 1);
        helper.assertTrue(lower.getEnergyStored() == 2 && middle.getEnergyStored() == 0
                        && upper.getEnergyStored() == 0,
                "A stacked charger transferred RF before the first half-second hop completed");

        tick(helper, lowerPos, lower, 1);
        helper.assertTrue(lower.getEnergyStored() == 1 && middle.getEnergyStored() == 1
                        && upper.getEnergyStored() == 0,
                "The first charger-to-charger hop did not transfer exactly one RF on time");

        tick(helper, middlePos, middle, transferInterval - 1);
        helper.assertTrue(middle.getEnergyStored() == 1 && upper.getEnergyStored() == 0,
                "The middle charger forwarded newly received RF without its own interval");
        tick(helper, middlePos, middle, 1);
        helper.assertTrue(lower.getEnergyStored() == 1 && middle.getEnergyStored() == 0
                        && upper.getEnergyStored() == 1,
                "A stacked chain did not complete its second timed hop without loss");
        helper.assertTrue(helper.getBlockState(lowerPos).getValue(ZenioniteChargerBlock.POWER)
                        == lower.getPowerLevel()
                        && helper.getBlockState(upperPos).getValue(ZenioniteChargerBlock.POWER)
                        == upper.getPowerLevel(),
                "The stacked chargers' power textures did not follow upward RF transfer");
        helper.assertTrue(helper.getBlockState(lowerPos)
                        .getValue(ZenioniteChargerBlock.POWER_DRAINING)
                        && !helper.getBlockState(upperPos)
                        .getValue(ZenioniteChargerBlock.POWER_DRAINING),
                "Upward transfer did not drain the sender from the bottom while filling the receiver from below");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void storagePersistsAcrossNbtRoundTrip(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState state = ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState();
        helper.setBlock(pos, state);
        ZenioniteChargerBlockEntity original = charger(helper, pos);
        IFluidHandler fluids = fluidInput(original, Direction.SOUTH);
        fluids.fill(new FluidStack(Fluids.WATER, 2_500), IFluidHandler.FluidAction.EXECUTE);
        fluids.fill(new FluidStack(Fluids.LAVA, 3_500), IFluidHandler.FluidAction.EXECUTE);
        energy(original, Direction.DOWN).receiveEnergy(3, false);
        original.getAutomationItemHandler().setStackInSlot(
                ZenioniteChargerBlockEntity.WATER_SLOT, new ItemStack(Items.WATER_BUCKET));
        tick(helper, pos, original, 7);

        CompoundTag saved = original.saveWithFullMetadata();
        ZenioniteChargerBlockEntity restored = new ZenioniteChargerBlockEntity(
                helper.absolutePos(pos), helper.getBlockState(pos));
        restored.load(saved);
        helper.assertTrue(restored.getWaterAmount() == original.getWaterAmount()
                        && restored.getLavaAmount() == original.getLavaAmount(),
                "Fluid storage did not survive an NBT round trip");
        helper.assertTrue(restored.getEnergyStored() == original.getEnergyStored(),
                "RF storage did not survive an NBT round trip");
        helper.assertTrue(ItemStack.matches(
                        restored.getAutomationItemHandler().getStackInSlot(
                                ZenioniteChargerBlockEntity.WATER_SLOT),
                        original.getAutomationItemHandler().getStackInSlot(
                                ZenioniteChargerBlockEntity.WATER_SLOT)),
                "Automation inventory did not survive an NBT round trip");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "creativeZenioniteCharger")
    public static void creativeChargerStaysFullAfterInfiniteExtraction(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        ZenioniteChargerBlock block = ModBlocks.CREATIVE_ZENIONITE_CHARGER.get();
        BlockState state = block.defaultBlockState();
        helper.assertTrue(state.getValue(ZenioniteChargerBlock.WATER) == 8
                        && state.getValue(ZenioniteChargerBlock.LAVA) == 8
                        && state.getValue(ZenioniteChargerBlock.POWER) == 8,
                "The Creative Zenionite Charger did not place with every gauge full");
        helper.assertTrue(!state.getValue(ZenioniteChargerBlock.POWER_DRAINING),
                "The creative charger incorrectly placed with a draining texture");
        helper.assertTrue(ModBlockEntities.ZENIONITE_CHARGER.get().isValid(state),
                "The charger block entity type did not accept the creative charger");

        helper.setBlock(pos, state);
        ZenioniteChargerBlockEntity charger = charger(helper, pos);
        IFluidHandler fluids = fluidInput(charger, Direction.NORTH);
        IEnergyStorage input = energy(charger, Direction.DOWN);
        IEnergyStorage output = energy(charger, Direction.UP);
        int capacity = charger.getEnergyCapacity();

        helper.assertTrue(charger.getWaterAmount() == ZenioniteChargerBlockEntity.TANK_CAPACITY
                        && charger.getLavaAmount() == ZenioniteChargerBlockEntity.TANK_CAPACITY
                        && charger.getEnergyStored() == capacity,
                "The creative charger did not expose full fluid and RF storage");
        helper.assertTrue(input.receiveEnergy(capacity, false) == 0,
                "The already-full creative charger accepted RF input");
        helper.assertTrue(output.extractEnergy(1, false) == 1,
                "The creative charger did not provide its normal timed RF output");
        helper.assertTrue(charger.getEnergyStored() == capacity,
                "Extracting RF reduced the creative charger's stored power");

        FluidStack lava = fluids.drain(new FluidStack(Fluids.LAVA, 1_250),
                IFluidHandler.FluidAction.EXECUTE);
        FluidStack water = fluids.drain(new FluidStack(Fluids.WATER, 750),
                IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(lava.getFluid() == Fluids.LAVA && lava.getAmount() == 1_250
                        && water.getFluid() == Fluids.WATER && water.getAmount() == 750,
                "The creative charger did not provide infinite water and lava");
        helper.assertTrue(charger.getWaterAmount() == ZenioniteChargerBlockEntity.TANK_CAPACITY
                        && charger.getLavaAmount() == ZenioniteChargerBlockEntity.TANK_CAPACITY,
                "Extracting fluid reduced the creative charger's displayed storage");
        helper.assertTrue(helper.getBlockState(pos).equals(state),
                "Creative extraction changed the charger's permanently-full block texture");
        helper.succeed();
    }

    private static ZenioniteChargerBlockEntity charger(GameTestHelper helper, BlockPos relativePos) {
        if (helper.getBlockEntity(relativePos) instanceof ZenioniteChargerBlockEntity charger) {
            return charger;
        }
        throw new IllegalStateException("Missing Zenionite Charger block entity at " + relativePos);
    }

    private static void tick(GameTestHelper helper, BlockPos relativePos,
                             ZenioniteChargerBlockEntity charger, int count) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenioniteChargerBlockEntity.serverTick(helper.getLevel(), absolutePos,
                    helper.getLevel().getBlockState(absolutePos), charger);
        }
    }

    private static IFluidHandler fluidInput(ZenioniteChargerBlockEntity charger, Direction side) {
        return charger.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
                .orElseThrow(() -> new IllegalStateException("Missing fluid capability on " + side));
    }

    private static IItemHandler itemHandler(ZenioniteChargerBlockEntity charger, Direction side) {
        return charger.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                .orElseThrow(() -> new IllegalStateException("Missing item capability on " + side));
    }

    private static IEnergyStorage energy(ZenioniteChargerBlockEntity charger, Direction side) {
        return charger.getCapability(ForgeCapabilities.ENERGY, side)
                .orElseThrow(() -> new IllegalStateException("Missing energy capability on " + side));
    }
}
