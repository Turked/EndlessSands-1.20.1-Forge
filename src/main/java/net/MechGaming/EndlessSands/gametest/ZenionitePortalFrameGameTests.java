package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenionitePortalBlock;
import net.MechGaming.EndlessSands.block.custom.ZenionitePortalFrameBlock;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.ZenioniteBatteryBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenionitePortalFrameBlockEntity;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZenionitePortalFrameGameTests {
    private static final BlockPos PORTAL_CENTER = new BlockPos(3, 1, 3);

    private ZenionitePortalFrameGameTests() {
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void registrationCapacityAndCapabilities(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState state = ModBlocks.ZENIONITE_PORTAL_FRAME.get().defaultBlockState();
        helper.assertTrue(!state.getValue(ZenionitePortalFrameBlock.POWERED),
                "The portal frame started powered without RF");
        helper.assertTrue(!state.getValue(ZenionitePortalFrameBlock.BEDROCK),
                "The portal frame started with Bedrock inserted");
        helper.assertTrue(state.getBlock().getStateDefinition().getPossibleStates().size() == 1296,
                "Frame state definitions did not include the four three-way connection ports");
        helper.assertTrue(ModBlockEntities.ZENIONITE_PORTAL_FRAME.isPresent(),
                "The portal frame block entity type was not registered");

        helper.setBlock(pos, state);
        ZenionitePortalFrameBlockEntity frame = frame(helper, pos);
        helper.assertTrue(frame.getEnergyCapacity()
                        == ZenionitePortalFrameBlockEntity.ENERGY_CAPACITY
                        * EndlessSandsConfig.getRfMultiplier(),
                "Portal frame capacity was not 3 RF times the RF multiplier");
        long lossWindowTicks = (long) EndlessSandsConfig.DEFAULT_RF_TRANSFER_INTERVAL_TICKS
                * ZenionitePortalFrameBlockEntity.LOSS_INTERVAL_MULTIPLIER;
        long twoChargerProduction = lossWindowTicks
                / ZenioniteChargerBlockEntity.PRODUCTION_TICKS * 2L;
        helper.assertTrue(twoChargerProduction
                        > ZenionitePortalFrameBlockEntity.MINIMUM_PORTAL_FRAMES,
                "Two normal chargers did not outproduce a full portal's aggregate passive loss");

        for (Direction direction : Direction.values()) {
            IEnergyStorage energy = energy(frame, direction);
            helper.assertTrue(energy.canReceive() && !energy.canExtract(),
                    direction + " was not exposed as an RF probe face");
            helper.assertTrue(energy.getMaxEnergyStored() == frame.getEnergyCapacity(),
                    direction + " reported the wrong capacity");
        }
        frame.fillEnergyToCapacity();
        helper.assertTrue(frame.getEnergyStored() == frame.getEnergyCapacity(),
                "The admin fill path did not maximize portal-frame RF");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void acceptsBedrockAndRejectsEnderEyes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ZENIONITE_PORTAL_FRAME.get().defaultBlockState());
        BlockPos absolutePos = helper.absolutePos(pos);
        FakePlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolutePos), Direction.UP, absolutePos, false);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ENDER_EYE));
        player.getMainHandItem().useOn(new UseOnContext(
                player, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(!helper.getBlockState(pos)
                        .getValue(ZenionitePortalFrameBlock.BEDROCK),
                "A vanilla Ender Eye filled the Zenionite Portal Frame");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEDROCK));
        BlockState state = helper.getBlockState(pos);
        InteractionResult result = state.getBlock().use(
                state, helper.getLevel(), absolutePos, player,
                InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction(),
                "Bedrock insertion did not consume the interaction");
        helper.assertTrue(helper.getBlockState(pos)
                        .getValue(ZenionitePortalFrameBlock.BEDROCK),
                "Bedrock did not fill the Zenionite Portal Frame");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void invalidPoweredFrameBreaksWithoutDrops(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ZENIONITE_PORTAL_FRAME.get().defaultBlockState());
        ZenionitePortalFrameBlockEntity frame = frame(helper, pos);
        IEnergyStorage energy = energy(frame, Direction.DOWN);

        helper.assertTrue(energy.receiveEnergy(1, true) == 1,
                "A simulated RF probe did not report the frame's available capacity");
        helper.assertTrue(helper.getBlockState(pos).is(ModBlocks.ZENIONITE_PORTAL_FRAME.get()),
                "A simulated RF probe broke the frame");
        helper.assertTrue(energy.receiveEnergy(1, false) == 0,
                "An invalid portal structure accepted RF");
        helper.assertTrue(helper.getBlockState(pos).isAir(),
                "The invalid powered frame did not break");

        BlockPos absolutePos = helper.absolutePos(pos);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(
                ItemEntity.class,
                new AABB(absolutePos).inflate(1.0D),
                item -> item.getItem().is(ModBlocks.ZENIONITE_PORTAL_FRAME.get().asItem())
        );
        helper.assertTrue(drops.isEmpty(),
                "The invalid powered frame dropped an item");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void exactPortalRoutesBatteryPowerThroughF1F2AndF3(GameTestHelper helper) {
        PortalFixture portal = placePortal(helper, PORTAL_CENTER, Direction.Axis.Z);
        Direction batterySide = Direction.NORTH;
        Direction crossSide = Direction.EAST;
        BlockPos batteryPos = PORTAL_CENTER.relative(batterySide, 2);
        BlockPos f1Pos = batteryPos.relative(crossSide);
        BlockPos cornerPos = f1Pos.relative(crossSide);
        BlockPos f2Pos = cornerPos.relative(batterySide.getOpposite());
        BlockPos f3Pos = f2Pos.relative(batterySide.getOpposite());

        helper.assertTrue(portal.frames().size()
                        == ZenionitePortalFrameBlockEntity.MINIMUM_PORTAL_FRAMES,
                "The strict portal fixture did not contain exactly ten frames");
        for (BlockPos framePos : portal.frames()) {
            helper.assertTrue(ZenionitePortalFrameBlockEntity.isValidCircuit(
                            helper.getLevel(), helper.absolutePos(framePos)),
                    "A frame in the exact portal structure was rejected at " + framePos);
        }
        helper.assertTrue(ZenionitePortalFrameBlockEntity.isBatteryInValidCircuit(
                        helper.getLevel(), helper.absolutePos(batteryPos)),
                "The valid portal battery was not exposed to the beacon gate control");

        ZenionitePortalFrameBlockEntity f1 = frame(helper, f1Pos);
        ZenionitePortalFrameBlockEntity f2 = frame(helper, f2Pos);
        ZenionitePortalFrameBlockEntity f3 = frame(helper, f3Pos);
        ZenionitePortalFrameBlockEntity.updatePortalState(helper.getLevel(), helper.absolutePos(f1Pos));
        assertPorts(helper, f1Pos, Direction.WEST, null, Direction.EAST);
        assertPorts(helper, f2Pos, Direction.NORTH, null, Direction.SOUTH);
        assertPorts(helper, f3Pos, Direction.NORTH, Direction.SOUTH, null);
        helper.assertTrue(energy(f1, Direction.DOWN).receiveEnergy(1, false) == 0,
                "F1 accepted current from a side not attached to its battery");
        helper.assertTrue(helper.getBlockState(f1Pos).is(ModBlocks.ZENIONITE_PORTAL_FRAME.get()),
                "A valid F1 frame broke after refusing a wrong-side input");

        ZenioniteBatteryBlockEntity battery = battery(helper, batteryPos);
        helper.assertTrue(energy(battery, Direction.DOWN).receiveEnergy(5, false) == 5,
                "The battery did not accept enough RF to feed both F1 paths");
        int transferInterval = EndlessSandsConfig.getRfTransferIntervalTicks();
        tickBattery(helper, batteryPos, battery, transferInterval * 5);
        helper.assertTrue(f1.getEnergyStored() == 3,
                "Current did not enter the selected F1 frame from its battery");

        tickFrame(helper, f1Pos, f1, transferInterval - 1);
        helper.assertTrue(f1.getEnergyStored() == 3 && f2.getEnergyStored() == 0,
                "F1 transferred RF before the configured interval");
        tickFrame(helper, f1Pos, f1, 1);
        helper.assertTrue(f1.getEnergyStored() == 2 && f2.getEnergyStored() == 1,
                "F1 did not route RF across the unrestricted corner into F2");
        tickFrame(helper, f1Pos, f1, transferInterval);
        helper.assertTrue(f1.getEnergyStored() == 1 && f2.getEnergyStored() == 2,
                "F1 did not continue routing toward F2");

        tickFrame(helper, f2Pos, f2, transferInterval);
        helper.assertTrue(f2.getEnergyStored() == 1 && f3.getEnergyStored() == 1,
                "F2 did not output toward the middle F3 frame");
        tickFrame(helper, f3Pos, f3, transferInterval);
        helper.assertTrue(f3.getEnergyStored() == 1,
                "F3 output RF even though it must be an input-only endpoint");

        tickFrame(helper, f1Pos, f1,
                transferInterval * (ZenionitePortalFrameBlockEntity.LOSS_INTERVAL_MULTIPLIER - 2) - 1);
        helper.assertTrue(f1.getEnergyStored() == 1,
                "F1 lost its remaining RF before one hundred transfer intervals elapsed");
        tickFrame(helper, f1Pos, f1, 1);
        helper.assertTrue(f1.getEnergyStored() == 0,
                "F1 did not lose one RF after one hundred transfer intervals");
        helper.assertTrue(!helper.getBlockState(f1Pos)
                        .getValue(ZenionitePortalFrameBlock.POWERED),
                "An empty F1 frame retained its powered texture state");

        for (int x = PORTAL_CENTER.getX() - 1; x <= PORTAL_CENTER.getX() + 1; x++) {
            for (int z = PORTAL_CENTER.getZ() - 1; z <= PORTAL_CENTER.getZ() + 1; z++) {
                helper.assertTrue(helper.getBlockState(new BlockPos(x, 1, z)).is(Blocks.AIR),
                        "Powering the structure without Bedrock created a portal");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void chargedBedrockFramesOpenAndClosePortal(GameTestHelper helper) {
        PortalFixture portal = placePortal(helper, PORTAL_CENTER, Direction.Axis.Z);

        for (BlockPos framePosition : portal.frames()) {
            helper.setBlock(framePosition, helper.getBlockState(framePosition)
                    .setValue(ZenionitePortalFrameBlock.BEDROCK, true));
            frame(helper, framePosition).fillEnergyToCapacity();
        }
        ZenionitePortalFrameBlockEntity.updatePortalState(
                helper.getLevel(), helper.absolutePos(portal.frames().get(0)));

        for (int x = PORTAL_CENTER.getX() - 1; x <= PORTAL_CENTER.getX() + 1; x++) {
            for (int z = PORTAL_CENTER.getZ() - 1; z <= PORTAL_CENTER.getZ() + 1; z++) {
                helper.assertTrue(helper.getBlockState(new BlockPos(x, 1, z))
                                .is(ModBlocks.ZENIONITE_PORTAL.get()),
                        "A ready gate did not fill its 3x3 interior with portal blocks");
                helper.assertTrue(!helper.getBlockState(new BlockPos(x, 1, z))
                                .getValue(ZenionitePortalBlock.TRANSFER_ACTIVE),
                        "A newly opened gate did not start on its prison-realm animation");
            }
        }

        BlockPos firstBattery = portal.batteries().get(0);
        ZenionitePortalFrameBlockEntity.PortalConnection connection =
                ZenionitePortalFrameBlockEntity.getActivePortalForBattery(
                        helper.getLevel(), helper.absolutePos(firstBattery));
        helper.assertTrue(connection != null
                        && connection.center().equals(helper.absolutePos(PORTAL_CENTER))
                        && connection.oppositeBattery().equals(
                        helper.absolutePos(portal.batteries().get(1))),
                "The active gate did not resolve its center and opposite battery");
        ZenionitePortalFrameBlockEntity.setPortalTransferActive(
                helper.getLevel(), helper.absolutePos(PORTAL_CENTER), true);
        helper.assertTrue(helper.getBlockState(PORTAL_CENTER)
                        .getValue(ZenionitePortalBlock.TRANSFER_ACTIVE),
                "The active gate did not switch to its transfer animation");

        BlockPos disabledFrame = portal.frames().get(0);
        helper.setBlock(disabledFrame, helper.getBlockState(disabledFrame)
                .setValue(ZenionitePortalFrameBlock.BEDROCK, false));
        ZenionitePortalFrameBlockEntity.updatePortalState(
                helper.getLevel(), helper.absolutePos(disabledFrame));

        for (int x = PORTAL_CENTER.getX() - 1; x <= PORTAL_CENTER.getX() + 1; x++) {
            for (int z = PORTAL_CENTER.getZ() - 1; z <= PORTAL_CENTER.getZ() + 1; z++) {
                helper.assertTrue(helper.getBlockState(new BlockPos(x, 1, z)).isAir(),
                        "The portal remained after one frame lost its Bedrock");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void quarterTurnArbitraryFacingAndCornerBatteriesAreValid(
            GameTestHelper helper
    ) {
        PortalFixture portal = placePortal(helper, PORTAL_CENTER, Direction.Axis.X);
        for (BlockPos framePos : portal.frames()) {
            helper.setBlock(framePos, helper.getBlockState(framePos)
                    .setValue(ZenionitePortalFrameBlock.FACING, Direction.NORTH));
        }
        helper.setBlock(new BlockPos(1, 1, 1),
                ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(new BlockPos(5, 1, 1),
                ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 5),
                ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(new BlockPos(5, 1, 5),
                ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(PORTAL_CENTER, Blocks.STONE);

        for (BlockPos framePos : portal.frames()) {
            helper.assertTrue(ZenionitePortalFrameBlockEntity.isValidCircuit(
                            helper.getLevel(), helper.absolutePos(framePos)),
                    "The rotated portal, arbitrary frame facing, or corner batteries were rejected");
        }

        BlockPos middleBatteryPos = PORTAL_CENTER.relative(Direction.WEST, 2);
        ZenioniteBatteryBlockEntity middleBattery = battery(helper, middleBatteryPos);
        helper.assertTrue(energy(middleBattery, Direction.DOWN).receiveEnergy(2, false) == 2,
                "The middle battery did not accept the regression-test RF");
        tickBattery(helper, middleBatteryPos, middleBattery,
                EndlessSandsConfig.getRfTransferIntervalTicks() * 2);
        helper.assertTrue(helper.getBlockState(middleBatteryPos.relative(Direction.NORTH))
                        .is(ModBlocks.ZENIONITE_PORTAL_FRAME.get())
                        && helper.getBlockState(middleBatteryPos.relative(Direction.SOUTH))
                        .is(ModBlocks.ZENIONITE_PORTAL_FRAME.get()),
                "A powered middle battery destroyed one of its valid neighboring F1 frames");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void missingBatteryAndOldLoopsAreInvalid(GameTestHelper helper) {
        placePortal(helper, PORTAL_CENTER, Direction.Axis.Z);
        BlockPos batteryPos = PORTAL_CENTER.relative(Direction.NORTH, 2);
        helper.setBlock(batteryPos, Blocks.AIR);
        BlockPos f1Pos = batteryPos.relative(Direction.EAST);
        helper.assertTrue(!ZenionitePortalFrameBlockEntity.isValidCircuit(
                        helper.getLevel(), helper.absolutePos(f1Pos)),
                "A portal missing one of its two batteries was accepted");

        clearPortalArea(helper);
        List<BlockPos> oldLoop = rectanglePerimeter(1, 1, 1, 4, 3);
        for (BlockPos pos : oldLoop) {
            helper.setBlock(pos, ModBlocks.ZENIONITE_PORTAL_FRAME.get().defaultBlockState());
        }
        helper.assertTrue(!ZenionitePortalFrameBlockEntity.isValidCircuit(
                        helper.getLevel(), helper.absolutePos(oldLoop.get(0))),
                "The old generic ten-frame loop was still accepted");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void rfAndTimersPersistAcrossNbtRoundTrip(GameTestHelper helper) {
        placePortal(helper, PORTAL_CENTER, Direction.Axis.Z);
        BlockPos batteryPos = PORTAL_CENTER.relative(Direction.NORTH, 2);
        BlockPos f1Pos = batteryPos.relative(Direction.WEST);
        ZenionitePortalFrameBlockEntity original = frame(helper, f1Pos);
        energy(original, Direction.EAST).receiveEnergy(1, false);

        CompoundTag saved = original.saveWithoutMetadata();
        ZenionitePortalFrameBlockEntity restored = new ZenionitePortalFrameBlockEntity(
                helper.absolutePos(f1Pos), helper.getBlockState(f1Pos));
        restored.load(saved);

        helper.assertTrue(restored.getEnergyStored() == 1,
                "The portal frame lost RF during an NBT round trip");
        helper.assertTrue(restored.getTransferCooldownTicks()
                        == EndlessSandsConfig.getRfTransferIntervalTicks(),
                "The transfer timer did not persist");
        helper.assertTrue(restored.getLossCooldownTicks()
                        == EndlessSandsConfig.getRfTransferIntervalTicks()
                        * ZenionitePortalFrameBlockEntity.LOSS_INTERVAL_MULTIPLIER,
                "The passive-loss timer did not persist");
        helper.succeed();
    }


    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void largeGateAcceptsAllRotationsAndShowsDirectionalPorts(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 2, 4);
        for (Rotation rotation : Rotation.values()) {
            PortalFixture portal = placeLargePortal(helper, center, rotation);
            helper.assertTrue(portal.frames().size() == 18, "The large fixture must contain 18 frames");
            for (BlockPos position : portal.frames()) {
                helper.assertTrue(ZenionitePortalFrameBlockEntity.isValidCircuit(
                        helper.getLevel(), helper.absolutePos(position)), "Large gate rejected " + rotation + " " + position);
            }
            ZenionitePortalFrameBlockEntity.updatePortalState(
                    helper.getLevel(), helper.absolutePos(portal.frames().get(0)));
            // Independent expected NW path from the user's diagram, then rotate the expectations.
            assertPorts(helper, center.offset(new BlockPos(-3, 0, -1).rotate(rotation)),
                    rotation.rotate(Direction.SOUTH), null, rotation.rotate(Direction.EAST));
            assertPorts(helper, center.offset(new BlockPos(-2, 0, -1).rotate(rotation)),
                    rotation.rotate(Direction.WEST), null, rotation.rotate(Direction.NORTH));
            assertPorts(helper, center.offset(new BlockPos(-1, 0, -2).rotate(rotation)),
                    rotation.rotate(Direction.WEST), null, rotation.rotate(Direction.NORTH));
            assertPorts(helper, center.offset(new BlockPos(-1, 0, -3).rotate(rotation)),
                    rotation.rotate(Direction.SOUTH), null, rotation.rotate(Direction.EAST));
            assertPorts(helper, center.offset(new BlockPos(0, 0, -3).rotate(rotation)),
                    rotation.rotate(Direction.WEST), rotation.rotate(Direction.EAST), null);
            for (BlockPos batteryPos : portal.batteries()) {
                helper.assertTrue(ZenionitePortalFrameBlockEntity.isBatteryInValidCircuit(
                        helper.getLevel(), helper.absolutePos(batteryPos)), "Large gate battery was not recognized");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void largeGateRoutesBatteryPowerToBothInputOnlyEndpoints(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 2, 4);
        PortalFixture portal = placeLargePortal(helper, center, Rotation.NONE);
        // The diagonal relay corner may contain any block, including another battery.
        helper.setBlock(center.offset(-2, 0, -2), ModBlocks.ZENIONITE_BATTERY.get());
        for (BlockPos batteryPos : portal.batteries()) battery(helper, batteryPos).fillEnergyToCapacity();
        int interval = EndlessSandsConfig.getRfTransferIntervalTicks();
        for (int step = 0; step < 70; step++) {
            for (BlockPos batteryPos : portal.batteries()) {
                tickBattery(helper, batteryPos, battery(helper, batteryPos), interval);
            }
            for (BlockPos framePos : portal.frames()) {
                tickFrame(helper, framePos, frame(helper, framePos), interval);
            }
        }
        for (BlockPos framePos : portal.frames()) {
            helper.assertTrue(frame(helper, framePos).getEnergyStored() > 0,
                    "Battery RF did not reach large-gate frame " + framePos);
        }
        for (int z : new int[]{-3, 3}) {
            BlockPos sinkPos = center.offset(0, 0, z);
            ZenionitePortalFrameBlockEntity sink = frame(helper, sinkPos);
            int before = sink.getEnergyStored();
            tickFrame(helper, sinkPos, sink, interval * 2);
            helper.assertTrue(before == sink.getEnergyStored(), "An input-only endpoint exported RF");
            helper.assertTrue(energy(sink, Direction.WEST).receiveEnergy(1, false) == 0,
                    "A sink accepted direct external RF instead of routed frame input");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void largeGateFillsOnlyInteriorAirAndUpdatesEveryPortalCell(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 2, 4);
        PortalFixture portal = placeLargePortal(helper, center, Rotation.CLOCKWISE_90);
        helper.setBlock(center, Blocks.STONE);
        helper.setBlock(center.offset(0, 0, 1), Blocks.GLASS);
        chargeAndFillFrames(helper, portal);
        int portalCount = 0;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos position = center.offset(x, 0, z);
                if (helper.getBlockState(position).is(ModBlocks.ZENIONITE_PORTAL.get())) {
                    portalCount++;
                    helper.assertTrue(Math.abs(x) + Math.abs(z) <= 2, "Portal escaped the shaped interior");
                    helper.assertTrue(ZenionitePortalFrameBlockEntity.isPortalInteriorActive(
                            helper.getLevel(), helper.absolutePos(position)), "A valid outer portal cell was rejected");
                }
            }
        }
        helper.assertTrue(portalCount == 11, "Expected 13 interior cells minus two occupied blocks");
        helper.assertTrue(helper.getBlockState(center).is(Blocks.STONE)
                && helper.getBlockState(center.offset(0, 0, 1)).is(Blocks.GLASS), "Gate overwrote interior blocks");
        for (BlockPos batteryPos : portal.batteries()) {
            var connection = ZenionitePortalFrameBlockEntity.getActivePortalForBattery(
                    helper.getLevel(), helper.absolutePos(batteryPos));
            helper.assertTrue(connection != null && connection.center().equals(helper.absolutePos(center)),
                    "Occupied center disabled the active gate's beacon connection");
        }
        ZenionitePortalFrameBlockEntity.setPortalTransferActive(helper.getLevel(), helper.absolutePos(center), true);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.assertTrue(helper.getBlockState(center.relative(side, 2))
                    .getValue(ZenionitePortalBlock.TRANSFER_ACTIVE), "Transfer state missed an outer portal tip");
        }
        helper.setBlock(center, Blocks.AIR);
        ZenionitePortalFrameBlockEntity.updatePortalState(
                helper.getLevel(), helper.absolutePos(portal.frames().get(0)));
        helper.assertTrue(helper.getBlockState(center).is(ModBlocks.ZENIONITE_PORTAL.get()),
                "Removing the occupied center did not let it fill with portal");
        helper.assertTrue(helper.getBlockState(center).getValue(ZenionitePortalBlock.TRANSFER_ACTIVE),
                "A newly cleared interior cell lost the ongoing transfer state");
        BlockPos broken = portal.frames().get(0);
        helper.setBlock(broken, helper.getBlockState(broken).setValue(ZenionitePortalFrameBlock.BEDROCK, false));
        ZenionitePortalFrameBlockEntity.updatePortalState(helper.getLevel(), helper.absolutePos(broken));
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                helper.assertTrue(!helper.getBlockState(center.offset(x, 0, z)).is(ModBlocks.ZENIONITE_PORTAL.get()),
                        "Gate left behind an active portal after losing Bedrock");
            }
        }
        helper.assertTrue(helper.getBlockState(center.offset(0, 0, 1)).is(Blocks.GLASS),
                "Closing the portal removed an occupied cell");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void largeGateRevalidatesCachedShapeAndRequiresSameHeight(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 2, 4);
        PortalFixture portal = placeLargePortal(helper, center, Rotation.NONE);
        BlockPos probe = center.offset(-3, 0, -1);
        helper.assertTrue(ZenionitePortalFrameBlockEntity.isValidCircuit(
                helper.getLevel(), helper.absolutePos(probe)), "Initial shape was invalid");
        BlockPos missing = center.offset(0, 0, 3);
        BlockState state = helper.getBlockState(missing);
        helper.setBlock(missing, Blocks.AIR);
        helper.setBlock(missing.above(), state);
        helper.assertTrue(!ZenionitePortalFrameBlockEntity.isValidCircuit(
                helper.getLevel(), helper.absolutePos(probe)), "Cached gate accepted a frame on another Y level");
        helper.setBlock(missing.above(), Blocks.AIR);
        helper.setBlock(missing, state);
        helper.assertTrue(ZenionitePortalFrameBlockEntity.isValidCircuit(
                helper.getLevel(), helper.absolutePos(probe)), "Repaired gate was not rediscovered");
        helper.setBlock(portal.batteries().get(0), Blocks.AIR);
        helper.assertTrue(!ZenionitePortalFrameBlockEntity.isValidCircuit(
                helper.getLevel(), helper.absolutePos(probe)), "Cached gate accepted a missing battery");
        helper.assertTrue(energy(frame(helper, probe), Direction.SOUTH).receiveEnergy(1, false) == 0
                        && helper.getBlockState(probe).isAir(),
                "Invalid large-gate frame did not reject RF and break without drops");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zenionitePortalFrames")
    public static void largeGateRetainsPharaohCompletionAndChargerShutdown(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 2, 4);
        PortalFixture portal = placeLargePortal(helper, center, Rotation.NONE);
        BlockPos chargerPos = portal.batteries().get(0).below();
        BlockPos corner = center.offset(-2, 0, -2);
        helper.setBlock(chargerPos, ModBlocks.CREATIVE_ZENIONITE_CHARGER.get());
        helper.setBlock(corner, ModBlocks.ZENIONITE_CHARGER.get());
        chargeAndFillFrames(helper, portal);
        helper.assertTrue(ZenionitePortalFrameBlockEntity.completePortalWithPharaoh(
                helper.getLevel(), helper.absolutePos(center)), "Pharaoh completion rejected the large gate");
        for (BlockPos pos : portal.frames()) {
            helper.assertTrue(frame(helper, pos).isPharaohGate()
                            && !helper.getBlockState(pos).getValue(ZenionitePortalFrameBlock.BEDROCK),
                    "A large-gate frame did not consume Bedrock and enter final shutdown");
        }
        for (BlockPos pos : new BlockPos[]{chargerPos, corner}) {
            ZenioniteChargerBlockEntity charger = (ZenioniteChargerBlockEntity) helper.getBlockEntity(pos);
            helper.assertTrue(charger.isPharaohGate(), "A gate charger was not shut down");
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.assertTrue(helper.getBlockState(center.relative(side, 2))
                    .getValue(ZenionitePortalBlock.PHARAOH_IMPRISONED), "Outer portal tip missed Pharaoh completion");
        }
        helper.assertTrue(ZenionitePortalFrameBlockEntity.getActivePortalForBattery(
                helper.getLevel(), helper.absolutePos(portal.batteries().get(0))) == null,
                "A completed gate still allowed beacon imprisonment");
        helper.succeed();
    }

    private static void chargeAndFillFrames(GameTestHelper helper, PortalFixture portal) {
        for (BlockPos position : portal.frames()) {
            helper.setBlock(position, helper.getBlockState(position).setValue(ZenionitePortalFrameBlock.BEDROCK, true));
            frame(helper, position).fillEnergyToCapacity();
        }
        ZenionitePortalFrameBlockEntity.updatePortalState(
                helper.getLevel(), helper.absolutePos(portal.frames().get(0)));
    }

    private static void assertPorts(GameTestHelper helper, BlockPos pos, Direction input,
                                    Direction secondInput, Direction output) {
        BlockState state = helper.getBlockState(pos);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            var expected = side == input || side == secondInput ? ZenionitePortalFrameBlock.Port.INPUT
                    : side == output ? ZenionitePortalFrameBlock.Port.OUTPUT : ZenionitePortalFrameBlock.Port.NONE;
            helper.assertTrue(state.getValue(ZenionitePortalFrameBlock.portProperty(side)) == expected,
                    "Wrong " + side + " port at " + pos + ": expected " + expected);
        }
    }

    private static PortalFixture placeLargePortal(GameTestHelper helper, BlockPos center, Rotation rotation) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) helper.setBlock(center.offset(x, 0, z), Blocks.AIR);
        }
        String[] rows = {"..FFF..", "..F.F..", "FF...FF", "B.....B", "FF...FF", "..F.F..", "..FFF.."};
        List<BlockPos> frames = new ArrayList<>();
        List<BlockPos> batteries = new ArrayList<>();
        for (int z = 0; z < rows.length; z++) {
            for (int x = 0; x < rows[z].length(); x++) {
                BlockPos pos = center.offset(new BlockPos(x - 3, 0, z - 3).rotate(rotation));
                if (rows[z].charAt(x) == 'F') {
                    // Deliberately face all frames the same way: placement facing must not define wiring.
                    placeFrame(helper, pos, Direction.NORTH);
                    frames.add(pos);
                } else if (rows[z].charAt(x) == 'B') {
                    helper.setBlock(pos, ModBlocks.ZENIONITE_BATTERY.get());
                    batteries.add(pos);
                }
            }
        }
        return new PortalFixture(frames, batteries);
    }

    private static PortalFixture placePortal(
            GameTestHelper helper,
            BlockPos center,
            Direction.Axis batteryAxis
    ) {
        Direction[] batterySides = batteryAxis == Direction.Axis.Z
                ? new Direction[]{Direction.NORTH, Direction.SOUTH}
                : new Direction[]{Direction.WEST, Direction.EAST};
        Direction[] crossSides = batteryAxis == Direction.Axis.Z
                ? new Direction[]{Direction.WEST, Direction.EAST}
                : new Direction[]{Direction.NORTH, Direction.SOUTH};
        List<BlockPos> frames = new ArrayList<>();
        List<BlockPos> batteries = new ArrayList<>();

        for (Direction batterySide : batterySides) {
            BlockPos batteryPos = center.relative(batterySide, 2);
            helper.setBlock(batteryPos, ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
            batteries.add(batteryPos);

            for (Direction crossSide : crossSides) {
                BlockPos f1Pos = batteryPos.relative(crossSide);
                BlockPos cornerPos = f1Pos.relative(crossSide);
                BlockPos f2Pos = cornerPos.relative(batterySide.getOpposite());
                placeFrame(helper, f1Pos, batterySide.getOpposite());
                placeFrame(helper, f2Pos, crossSide.getOpposite());
                frames.add(f1Pos);
                frames.add(f2Pos);
            }
        }

        for (Direction crossSide : crossSides) {
            BlockPos f3Pos = center.relative(crossSide, 2);
            placeFrame(helper, f3Pos, crossSide.getOpposite());
            frames.add(f3Pos);
        }
        return new PortalFixture(frames, batteries);
    }

    private static void placeFrame(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, ModBlocks.ZENIONITE_PORTAL_FRAME.get().defaultBlockState()
                .setValue(ZenionitePortalFrameBlock.FACING, facing));
    }

    private static void clearPortalArea(GameTestHelper helper) {
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.AIR);
            }
        }
    }

    private static List<BlockPos> rectanglePerimeter(
            int minX,
            int y,
            int minZ,
            int maxX,
            int maxZ
    ) {
        List<BlockPos> result = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            result.add(new BlockPos(x, y, minZ));
        }
        for (int z = minZ + 1; z <= maxZ; z++) {
            result.add(new BlockPos(maxX, y, z));
        }
        for (int x = maxX - 1; x >= minX; x--) {
            result.add(new BlockPos(x, y, maxZ));
        }
        for (int z = maxZ - 1; z > minZ; z--) {
            result.add(new BlockPos(minX, y, z));
        }
        return result;
    }

    private static ZenionitePortalFrameBlockEntity frame(
            GameTestHelper helper,
            BlockPos relativePos
    ) {
        if (helper.getBlockEntity(relativePos) instanceof ZenionitePortalFrameBlockEntity frame) {
            return frame;
        }
        throw new IllegalStateException("Missing Zenionite Portal Frame block entity at "
                + relativePos);
    }

    private static ZenioniteBatteryBlockEntity battery(
            GameTestHelper helper,
            BlockPos relativePos
    ) {
        if (helper.getBlockEntity(relativePos) instanceof ZenioniteBatteryBlockEntity battery) {
            return battery;
        }
        throw new IllegalStateException("Missing Zenionite Battery block entity at " + relativePos);
    }

    private static IEnergyStorage energy(
            ZenionitePortalFrameBlockEntity frame,
            Direction side
    ) {
        return frame.getCapability(ForgeCapabilities.ENERGY, side)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing portal-frame energy capability on " + side));
    }

    private static IEnergyStorage energy(
            ZenioniteBatteryBlockEntity battery,
            Direction side
    ) {
        return battery.getCapability(ForgeCapabilities.ENERGY, side)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing battery energy capability on " + side));
    }

    private static void tickFrame(
            GameTestHelper helper,
            BlockPos relativePos,
            ZenionitePortalFrameBlockEntity frame,
            int count
    ) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenionitePortalFrameBlockEntity.serverTick(
                    helper.getLevel(),
                    absolutePos,
                    helper.getLevel().getBlockState(absolutePos),
                    frame
            );
        }
    }

    private static void tickBattery(
            GameTestHelper helper,
            BlockPos relativePos,
            ZenioniteBatteryBlockEntity battery,
            int count
    ) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenioniteBatteryBlockEntity.serverTick(
                    helper.getLevel(),
                    absolutePos,
                    helper.getLevel().getBlockState(absolutePos),
                    battery
            );
        }
    }

    private record PortalFixture(List<BlockPos> frames, List<BlockPos> batteries) {
    }
}
