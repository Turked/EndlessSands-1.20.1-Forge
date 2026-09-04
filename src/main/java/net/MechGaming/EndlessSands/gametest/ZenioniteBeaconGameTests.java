package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenioniteBeaconBlock;
import net.MechGaming.EndlessSands.block.custom.ZenionitePortalBlock;
import net.MechGaming.EndlessSands.block.custom.ZenionitePortalFrameBlock;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.ZenioniteBatteryBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteBeaconBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenionitePortalFrameBlockEntity;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.effect.ModEffects;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.entity.custom.PharaohEntity;
import net.MechGaming.EndlessSands.inventory.ModMenuTypes;
import net.MechGaming.EndlessSands.inventory.ZenioniteBeaconMenu;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZenioniteBeaconGameTests {
    private ZenioniteBeaconGameTests() {
    }

    @GameTest(template = "empty")
    public static void customBlockEntityMenuAndRfStorageReplaceVanillaBeacon(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        ZenioniteBeaconBlock block = ModBlocks.ZENIONITE_BEACON.get();
        BlockState state = block.defaultBlockState();

        helper.assertTrue(ModBlockEntities.ZENIONITE_BEACON.isPresent(),
                "The Zenionite Beacon block entity type was not registered");
        helper.assertTrue(ModBlockEntities.ZENIONITE_BEACON.get().isValid(state),
                "The custom block entity type did not accept the Zenionite Beacon");
        helper.assertTrue(ModMenuTypes.ZENIONITE_BEACON.isPresent(),
                "The Zenionite Beacon menu type was not registered");
        helper.assertTrue(ModEffects.CHAINED.isPresent(),
                "The Chained effect was not registered");
        helper.assertTrue(ModItems.ADMIN_ZENIONITE.isPresent()
                        && new ItemStack(ModItems.ADMIN_ZENIONITE.get()).hasFoil(),
                "Admin Zenionite was not registered with its enchanted visual glint");

        helper.setBlock(pos, state);
        ZenioniteBeaconBlockEntity beacon = beacon(helper, pos);
        int capacity = ZenioniteBeaconBlockEntity.ENERGY_CAPACITY
                * EndlessSandsConfig.getRfMultiplier();
        helper.assertTrue(beacon.getEnergyCapacity() == capacity,
                "The Zenionite Beacon did not apply the global RF multiplier");

        IEnergyStorage energy = beacon.getCapability(ForgeCapabilities.ENERGY, Direction.NORTH)
                .orElseThrow(() -> new IllegalStateException("Missing beacon RF capability"));
        helper.assertTrue(energy.canReceive() && !energy.canExtract(),
                "The beacon RF capability did not behave as input-only storage");
        helper.assertTrue(energy.receiveEnergy(7, false) == 7
                        && beacon.getEnergyStored() == 7,
                "The beacon did not store received RF");
        helper.assertTrue(helper.getBlockState(pos).getValue(ZenioniteBeaconBlock.POWERED),
                "The beacon did not synchronize its powered state for the beam renderer");
        helper.assertTrue(helper.getBlockState(pos).getValue(ZenioniteBeaconBlock.BEAM_ACTIVE),
                "A powered, enabled beacon did not activate its chains");
        helper.assertTrue(energy.extractEnergy(7, false) == 0
                        && beacon.getEnergyStored() == 7,
                "The beacon incorrectly exposed RF output");

        Player player = helper.makeMockPlayer();
        ZenioniteBeaconMenu menu = (ZenioniteBeaconMenu) beacon.createMenu(
                1, player.getInventory(), player);
        helper.assertTrue(menu != null && menu.getEnergyStored() == 7
                        && menu.getEnergyCapacity() == capacity && menu.isEnabled(),
                "The custom beacon menu did not expose its RF storage");
        menu.removed(player);

        UUID owner = UUID.randomUUID();
        beacon.setOwner(owner);
        beacon.setEnabled(false);
        helper.assertTrue(beacon.getEnergyStored() == 7
                        && helper.getBlockState(pos).getValue(ZenioniteBeaconBlock.POWERED)
                        && !helper.getBlockState(pos).getValue(ZenioniteBeaconBlock.BEAM_ACTIVE),
                "Turning the beacon off drained RF or left its chains active");

        CompoundTag saved = beacon.saveWithFullMetadata();
        ZenioniteBeaconBlockEntity restored = new ZenioniteBeaconBlockEntity(
                helper.absolutePos(pos), state);
        restored.load(saved);
        helper.assertTrue(restored.getEnergyStored() == 7,
                "The beacon's RF storage did not survive an NBT round trip");
        helper.assertTrue(!restored.isEnabled() && owner.equals(restored.getOwnerUuid()),
                "The beacon's enabled state or owner did not survive an NBT round trip");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chainsTrapMobsDrainAtOneThirdRateAndReleaseWhenDisabled(
            GameTestHelper helper
    ) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ZENIONITE_BEACON.get().defaultBlockState());
        ZenioniteBeaconBlockEntity beacon = beacon(helper, pos);
        IEnergyStorage energy = beacon.getCapability(ForgeCapabilities.ENERGY)
                .orElseThrow(() -> new IllegalStateException("Missing beacon RF capability"));
        energy.receiveEnergy(7, false);

        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        double lockedX = zombie.getX();
        double lockedY = zombie.getY();
        double lockedZ = zombie.getZ();
        tickBeacon(helper, pos, beacon, 1);
        helper.assertTrue(beacon.hasTrappedEntity() && zombie.isNoAi(),
                "The chain column did not trap the mob and disable its AI");

        int drainInterval = EndlessSandsConfig.getRfTransferIntervalTicks() * 3;
        tickBeacon(helper, pos, beacon, drainInterval - 1);
        helper.assertTrue(beacon.getEnergyStored() == 7,
                "The trapped mob drained RF faster than one-third charging rate");
        tickBeacon(helper, pos, beacon, 1);
        helper.assertTrue(beacon.getEnergyStored() == 6,
                "The trapped mob did not drain one RF after three transfer intervals");

        zombie.teleportTo(lockedX + 1.0D, lockedY, lockedZ);
        tickBeacon(helper, pos, beacon, 1);
        helper.assertTrue(zombie.distanceToSqr(lockedX, lockedY, lockedZ) < 0.000001D,
                "The trapped mob was able to leave its locked position");

        beacon.setEnabled(false);
        helper.assertTrue(!beacon.hasTrappedEntity() && !zombie.isNoAi()
                        && !helper.getBlockState(pos)
                        .getValue(ZenioniteBeaconBlock.BEAM_ACTIVE),
                "Turning the beacon off did not release the mob and disable the chains");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void solidBlocksObstructChainsAndRemovingThemRestoresIt(GameTestHelper helper) {
        BlockPos column = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos pos = new BlockPos(column.getX(), helper.getLevel().getHeight(
                Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ()), column.getZ());
        helper.getLevel().setBlock(pos, ModBlocks.ZENIONITE_BEACON.get().defaultBlockState(),
                Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(pos)
                instanceof ZenioniteBeaconBlockEntity beacon)) {
            throw new IllegalStateException("Missing Zenionite Beacon block entity at " + pos);
        }
        fill(beacon);

        helper.getLevel().setBlock(pos.above(2), Blocks.STONE.defaultBlockState(),
                Block.UPDATE_ALL);
        tickBeacon(helper.getLevel(), pos, beacon, 40);
        helper.assertTrue(!helper.getLevel().getBlockState(pos)
                        .getValue(ZenioniteBeaconBlock.BEAM_ACTIVE),
                "An opaque solid block did not obstruct the beacon chains");

        helper.getLevel().destroyBlock(pos.above(2), false);
        tickBeacon(helper.getLevel(), pos, beacon, 40);
        helper.assertTrue(helper.getLevel().getBlockState(pos)
                        .getValue(ZenioniteBeaconBlock.BEAM_ACTIVE),
                "Removing the obstruction did not restore the beacon chains");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400,
            batch = "zenioniteBeaconGateTransfer")
    public static void activeGateAndOppositeBeaconImprisonCapturedMob(GameTestHelper helper) {
        verifyGateImprisonment(helper, false);
    }

    @GameTest(template = "empty", timeoutTicks = 400, batch = "zenioniteLargeBeaconGateTransfer")
    public static void largeGateAndOppositeBeaconImprisonCapturedMob(GameTestHelper helper) {
        verifyGateImprisonment(helper, true);
    }

    private static void verifyGateImprisonment(GameTestHelper helper, boolean large) {
        BlockPos center = large ? new BlockPos(4, 2, 4) : new BlockPos(3, 1, 3);
        PortalFixture portal = large ? placeReadyLargePortal(helper, center) : placeReadyPortal(helper, center);
        BlockPos sourcePos = portal.northBattery().above();
        BlockPos partnerPos = portal.southBattery().above();
        helper.setBlock(sourcePos, ModBlocks.ZENIONITE_BEACON.get().defaultBlockState());
        helper.setBlock(partnerPos, ModBlocks.ZENIONITE_BEACON.get().defaultBlockState());
        ZenioniteBeaconBlockEntity source = beacon(helper, sourcePos);
        ZenioniteBeaconBlockEntity partner = beacon(helper, partnerPos);
        fill(source);
        fill(partner);

        Zombie zombie = helper.spawn(EntityType.ZOMBIE, sourcePos.above(10));
        UUID zombieId = zombie.getUUID();
        double startingY = zombie.getY();
        double startingHorizontalDistance = horizontalDistanceSquared(
                zombie, helper.absolutePos(center));
        tickBeacon(helper, sourcePos, source, 1);
        helper.assertTrue(source.hasTrappedEntity()
                        && horizontalDistanceSquared(zombie, helper.absolutePos(center))
                        < startingHorizontalDistance
                        && Math.abs(partner.getBeamTarget().x - zombie.getX()) < 0.000001D
                        && Math.abs(partner.getBeamTarget().z - zombie.getZ()) < 0.000001D,
                "Capturing a mob did not immediately center it or slant the opposite beam");

        tickBeacon(helper, sourcePos, source, large ? 30 : 20);
        helper.assertTrue(Math.abs(zombie.getY() - startingY) < 0.000001D
                        && horizontalDistanceSquared(zombie, helper.absolutePos(center))
                        < 0.000001D
                        && source.canStartImprisonment()
                        && partner.canStartImprisonment(),
                "The paired beacons did not center the captive and enable either imprison control");

        partner.toggleEntombed();
        helper.assertTrue(source.isEntombed() && partner.isEntombed()
                        && helper.getBlockState(center)
                        .getValue(ZenionitePortalBlock.TRANSFER_ACTIVE),
                "The opposite beacon could not begin the downward imprisonment phase");
        tickBeacon(helper, sourcePos, source, 5);
        helper.assertTrue(zombie.getY() < startingY
                        && horizontalDistanceSquared(zombie, helper.absolutePos(center))
                        < 0.000001D,
                "Imprisonment did not lower the already-centered captive");
        tickBeacon(helper, sourcePos, source, 245);

        ServerLevel prisonRealm = helper.getLevel().getServer()
                .getLevel(ModDimensions.PRISON_REALM_LEVEL);
        BlockPos absoluteCenter = helper.absolutePos(center);
        helper.assertTrue(prisonRealm != null,
                "The prison realm was not registered as a server dimension");
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(prisonRealm.getEntity(zombieId) != null,
                    "The captive was not transferred into the prison realm");
            helper.assertTrue(prisonRealm.getBlockState(new BlockPos(
                            absoluteCenter.getX(), 0, absoluteCenter.getZ()))
                            .is(ModBlocks.PRISON_CONCRETE.get()),
                    "The prison realm did not generate its indestructible concrete floor");
            helper.assertTrue(source.getEnergyStored() == 0 && partner.getEnergyStored() == 0,
                    "The paired beacons did not drain all RF after the transfer");
            helper.assertTrue(!source.isEntombed() && !source.hasTrappedEntity()
                            && !helper.getBlockState(center)
                            .getValue(ZenionitePortalBlock.TRANSFER_ACTIVE),
                    "The completed transfer did not reset the gate and source beacon");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400,
            batch = "zenionitePharaohGate")
    public static void imprisoningPharaohPermanentlyConsumesAndLocksGate(GameTestHelper helper) {
        helper.assertTrue(ModEntities.PHARAOH.isPresent(),
                "The Pharaoh entity type was not registered");
        helper.assertTrue(ModEntities.PHARAOH.get().getDimensions().width
                        == EntityType.GIANT.getDimensions().width
                        && ModEntities.PHARAOH.get().getDimensions().height
                        == EntityType.GIANT.getDimensions().height,
                "The Pharaoh does not use the vanilla Giant's dimensions");

        BlockPos center = new BlockPos(3, 4, 3);
        PortalFixture portal = placeReadyPortal(helper, center);
        ZenioniteBatteryBlockEntity northBattery = battery(helper, portal.northBattery());
        ZenioniteBatteryBlockEntity southBattery = battery(helper, portal.southBattery());
        northBattery.fillEnergyToCapacity();
        southBattery.fillEnergyToCapacity();

        BlockPos[] chargerPositions = {
                portal.northBattery().below(),
                portal.northBattery().below(2),
                portal.northBattery().below(3),
                portal.southBattery().below(),
                portal.southBattery().below(2),
                portal.southBattery().below(3),
                center.offset(-2, 0, -2)
        };
        for (BlockPos chargerPosition : chargerPositions) {
            helper.setBlock(chargerPosition,
                    ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
            charger(helper, chargerPosition).fillEnergyToCapacity();
        }
        ZenioniteChargerBlockEntity fueledCharger = charger(helper, chargerPositions[0]);
        IFluidHandler fluidInput = fueledCharger
                .getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.NORTH)
                .orElseThrow(() -> new IllegalStateException("Missing charger fluid input"));
        fluidInput.fill(new FluidStack(Fluids.WATER, 1_000),
                IFluidHandler.FluidAction.EXECUTE);
        fluidInput.fill(new FluidStack(Fluids.LAVA, 1_000),
                IFluidHandler.FluidAction.EXECUTE);

        BlockPos sourcePos = portal.northBattery().above();
        BlockPos partnerPos = portal.southBattery().above();
        helper.setBlock(sourcePos, ModBlocks.ZENIONITE_BEACON.get().defaultBlockState());
        helper.setBlock(partnerPos, ModBlocks.ZENIONITE_BEACON.get().defaultBlockState());
        ZenioniteBeaconBlockEntity source = beacon(helper, sourcePos);
        ZenioniteBeaconBlockEntity partner = beacon(helper, partnerPos);
        fill(source);
        fill(partner);

        PharaohEntity pharaoh = helper.spawn(ModEntities.PHARAOH.get(), sourcePos.above(10));
        UUID pharaohId = pharaoh.getUUID();
        tickBeacon(helper, sourcePos, source, 24);
        helper.assertTrue(source.hasTrappedEntity() && source.canStartImprisonment(),
                "The powered gate did not capture and center the Pharaoh");
        source.toggleEntombed();
        tickBeacon(helper, sourcePos, source, 125);

        BlockState occupiedPortal = helper.getBlockState(center);
        helper.assertTrue(occupiedPortal.is(ModBlocks.ZENIONITE_PORTAL.get())
                        && occupiedPortal.getValue(ZenionitePortalBlock.PHARAOH_IMPRISONED),
                "Imprisoning the Pharaoh did not switch the portal to Endless Sands");
        helper.assertTrue(!occupiedPortal.getCollisionShape(helper.getLevel(),
                        helper.absolutePos(center), CollisionContext.empty()).isEmpty(),
                "The completed Pharaoh portal was not a solid block");
        helper.assertTrue(!source.isGateControlAvailable()
                        && !partner.isGateControlAvailable()
                        && !source.canStartImprisonment()
                        && !partner.canStartImprisonment(),
                "A completed Pharaoh gate still allowed another imprisonment");

        ZenionitePortalFrameBlockEntity westFrame = frame(
                helper, center.relative(Direction.WEST, 2));
        helper.assertTrue(westFrame.isPharaohGate()
                        && !helper.getBlockState(center.relative(Direction.WEST, 2))
                        .getValue(ZenionitePortalFrameBlock.BEDROCK),
                "The completed gate did not consume its frame inserts");
        helper.assertTrue(northBattery.isPharaohGate() && southBattery.isPharaohGate(),
                "The completed gate did not lock both batteries");
        for (BlockPos chargerPosition : chargerPositions) {
            helper.assertTrue(charger(helper, chargerPosition).isPharaohGate(),
                    "A charger belonging to the completed gate was not locked");
        }
        helper.assertTrue(fueledCharger.getWaterAmount() == 0
                        && fueledCharger.getLavaAmount() == 0
                        && fluidInput.fill(new FluidStack(Fluids.WATER, 1_000),
                        IFluidHandler.FluidAction.EXECUTE) == 0,
                "A completed gate charger retained or accepted fluid");

        int multiplier = Math.max(1, EndlessSandsConfig.getRfMultiplier());
        int frameEnergy = westFrame.getEnergyStored();
        ZenionitePortalFrameBlockEntity.serverTick(helper.getLevel(),
                helper.absolutePos(center.relative(Direction.WEST, 2)),
                helper.getBlockState(center.relative(Direction.WEST, 2)), westFrame);
        helper.assertTrue(westFrame.getEnergyStored() == Math.max(0, frameEnergy - multiplier),
                "A completed portal frame did not rapidly drain one scaled RF per tick");
        int batteryEnergy = northBattery.getEnergyStored();
        ZenioniteBatteryBlockEntity.serverTick(helper.getLevel(),
                helper.absolutePos(portal.northBattery()),
                helper.getBlockState(portal.northBattery()), northBattery);
        helper.assertTrue(northBattery.getEnergyStored()
                        == Math.max(0, batteryEnergy - multiplier),
                "A completed gate battery did not rapidly drain one scaled RF per tick");
        int chargerEnergy = fueledCharger.getEnergyStored();
        ZenioniteChargerBlockEntity.serverTick(helper.getLevel(),
                helper.absolutePos(chargerPositions[0]),
                helper.getBlockState(chargerPositions[0]), fueledCharger);
        helper.assertTrue(fueledCharger.getEnergyStored()
                        == Math.max(0, chargerEnergy - multiplier),
                "A completed gate charger did not rapidly drain one scaled RF per tick");

        ServerLevel prisonRealm = helper.getLevel().getServer()
                .getLevel(ModDimensions.PRISON_REALM_LEVEL);
        helper.assertTrue(prisonRealm != null,
                "The prison realm was not registered as a server dimension");
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(prisonRealm.getEntity(pharaohId) instanceof PharaohEntity,
                    "The Pharaoh was not transferred into the prison realm");
            helper.succeed();
        });
    }

    private static PortalFixture placeReadyLargePortal(GameTestHelper helper, BlockPos center) {
        String[] rows = {"..FFF..", "..F.F..", "FF...FF", "B.....B", "FF...FF", "..F.F..", "..FFF.."};
        for (int z = 0; z < rows.length; z++) {
            for (int x = 0; x < rows[z].length(); x++) {
                BlockPos pos = center.offset(x - 3, 0, z - 3);
                if (rows[z].charAt(x) == 'B') helper.setBlock(pos, ModBlocks.ZENIONITE_BATTERY.get());
                else if (rows[z].charAt(x) == 'F') readyFrame(helper, pos);
            }
        }
        ZenionitePortalFrameBlockEntity.updatePortalState(helper.getLevel(),
                helper.absolutePos(center.relative(Direction.NORTH, 3)));
        return new PortalFixture(center.relative(Direction.WEST, 3), center.relative(Direction.EAST, 3));
    }

    private static PortalFixture placeReadyPortal(GameTestHelper helper, BlockPos center) {
        BlockPos northBattery = center.relative(Direction.NORTH, 2);
        BlockPos southBattery = center.relative(Direction.SOUTH, 2);
        helper.setBlock(northBattery, ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());
        helper.setBlock(southBattery, ModBlocks.ZENIONITE_BATTERY.get().defaultBlockState());

        for (Direction batterySide : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
            BlockPos battery = center.relative(batterySide, 2);
            for (Direction crossSide : new Direction[]{Direction.WEST, Direction.EAST}) {
                readyFrame(helper, battery.relative(crossSide));
                readyFrame(helper, battery.relative(crossSide, 2)
                        .relative(batterySide.getOpposite()));
            }
        }
        readyFrame(helper, center.relative(Direction.WEST, 2));
        readyFrame(helper, center.relative(Direction.EAST, 2));
        ZenionitePortalFrameBlockEntity.updatePortalState(
                helper.getLevel(), helper.absolutePos(center.relative(Direction.WEST, 2)));
        return new PortalFixture(northBattery, southBattery);
    }

    private static void readyFrame(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.ZENIONITE_PORTAL_FRAME.get().defaultBlockState()
                .setValue(ZenionitePortalFrameBlock.BEDROCK, true));
        if (!(helper.getBlockEntity(position)
                instanceof ZenionitePortalFrameBlockEntity frame)) {
            throw new IllegalStateException("Missing portal frame at " + position);
        }
        frame.fillEnergyToCapacity();
    }

    private static void fill(ZenioniteBeaconBlockEntity beacon) {
        beacon.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy ->
                energy.receiveEnergy(beacon.getEnergyCapacity(), false));
    }

    private static double horizontalDistanceSquared(LivingEntity entity, BlockPos position) {
        double xDifference = entity.getX() - (position.getX() + 0.5D);
        double zDifference = entity.getZ() - (position.getZ() + 0.5D);
        return xDifference * xDifference + zDifference * zDifference;
    }

    private static ZenionitePortalFrameBlockEntity frame(
            GameTestHelper helper,
            BlockPos relativePos
    ) {
        if (helper.getBlockEntity(relativePos)
                instanceof ZenionitePortalFrameBlockEntity frame) {
            return frame;
        }
        throw new IllegalStateException("Missing portal frame at " + relativePos);
    }

    private static ZenioniteBatteryBlockEntity battery(
            GameTestHelper helper,
            BlockPos relativePos
    ) {
        if (helper.getBlockEntity(relativePos) instanceof ZenioniteBatteryBlockEntity battery) {
            return battery;
        }
        throw new IllegalStateException("Missing battery at " + relativePos);
    }

    private static ZenioniteChargerBlockEntity charger(
            GameTestHelper helper,
            BlockPos relativePos
    ) {
        if (helper.getBlockEntity(relativePos) instanceof ZenioniteChargerBlockEntity charger) {
            return charger;
        }
        throw new IllegalStateException("Missing charger at " + relativePos);
    }

    private static ZenioniteBeaconBlockEntity beacon(GameTestHelper helper, BlockPos relativePos) {
        if (helper.getBlockEntity(relativePos) instanceof ZenioniteBeaconBlockEntity beacon) {
            return beacon;
        }
        throw new IllegalStateException("Missing Zenionite Beacon block entity at " + relativePos);
    }

    private static void tickBeacon(
            GameTestHelper helper,
            BlockPos relativePos,
            ZenioniteBeaconBlockEntity beacon,
            int count
    ) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenioniteBeaconBlockEntity.serverTick(
                    helper.getLevel(),
                    absolutePos,
                    helper.getLevel().getBlockState(absolutePos),
                    beacon);
        }
    }

    private static void tickBeacon(
            ServerLevel level,
            BlockPos absolutePos,
            ZenioniteBeaconBlockEntity beacon,
            int count
    ) {
        for (int tick = 0; tick < count; tick++) {
            ZenioniteBeaconBlockEntity.serverTick(
                    level, absolutePos, level.getBlockState(absolutePos), beacon);
        }
    }

    private record PortalFixture(BlockPos northBattery, BlockPos southBattery) {
    }
}
