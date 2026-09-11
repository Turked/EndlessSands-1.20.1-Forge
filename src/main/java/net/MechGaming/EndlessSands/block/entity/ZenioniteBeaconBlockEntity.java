package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.block.custom.ZenioniteBeaconBlock;
import net.MechGaming.EndlessSands.event.DragonEggSacrificeEvents;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.effect.ModEffects;
import net.MechGaming.EndlessSands.entity.custom.PharaohEntity;
import net.MechGaming.EndlessSands.event.ModEvents;
import net.MechGaming.EndlessSands.inventory.ZenioniteBeaconMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ZenioniteBeaconBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_CAPACITY = 80;

    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_ENABLED = 4;
    public static final int DATA_ENTOMBED = 5;
    public static final int DATA_GATE_CONTROL_AVAILABLE = 6;
    public static final int DATA_IMPRISONMENT_READY = 7;
    public static final int DATA_COUNT = 8;

    private static final String ENERGY_TAG = "Energy";
    private static final String ENABLED_TAG = "Enabled";
    private static final String ENTOMBED_TAG = "Entombed";
    private static final String OWNER_TAG = "Owner";
    private static final String TRAPPED_ENTITY_TAG = "TrappedEntity";
    private static final String TRAPPED_X_TAG = "TrappedX";
    private static final String TRAPPED_Y_TAG = "TrappedY";
    private static final String TRAPPED_Z_TAG = "TrappedZ";
    private static final String TRAPPED_BEAM_END_Y_TAG = "TrappedBeamEndY";
    private static final String TRAPPED_MOB_WAS_NO_AI_TAG = "TrappedMobWasNoAi";
    private static final String DRAIN_COOLDOWN_TAG = "DrainCooldown";
    private static final String IMPRISONMENT_PHASE_TAG = "ImprisonmentPhase";
    private static final String PORTAL_CENTER_TAG = "PortalCenter";
    private static final String PARTNER_BEACON_TAG = "PartnerBeacon";
    private static final String LINKED_SOURCE_BEACON_TAG = "LinkedSourceBeacon";
    private static final String CUSTOM_BEAM_TARGET_TAG = "CustomBeamTarget";
    private static final String BEAM_TARGET_X_TAG = "BeamTargetX";
    private static final String BEAM_TARGET_Y_TAG = "BeamTargetY";
    private static final String BEAM_TARGET_Z_TAG = "BeamTargetZ";
    private static final String IMPRISONMENT_DRAIN_TICKS_TAG = "ImprisonmentDrainTicks";
    private static final String PHARAOH_GATE_LINK_TAG = "PharaohGateLink";

    private static final double CHAIN_HALF_WIDTH = 5.0D / 16.0D;
    private static final double IMPRISONMENT_MOVE_SPEED = 0.12D;
    private static final int IMPRISONMENT_DRAIN_DURATION_TICKS = 20;
    private static final int TARGET_MISSING_GRACE_TICKS = 100;

    private final IEnergyStorage energyHandler = new EnergyHandler();
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyHandler);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> lowWord(getEnergyStored());
                case DATA_ENERGY_HIGH -> highWord(getEnergyStored());
                case DATA_CAPACITY_LOW -> lowWord(getEnergyCapacity());
                case DATA_CAPACITY_HIGH -> highWord(getEnergyCapacity());
                case DATA_ENABLED -> enabled ? 1 : 0;
                case DATA_ENTOMBED -> isEntombedForControl() ? 1 : 0;
                case DATA_GATE_CONTROL_AVAILABLE -> isGateControlAvailable() ? 1 : 0;
                case DATA_IMPRISONMENT_READY -> canStartImprisonment() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_ENERGY_LOW -> energyStored = withLowWord(energyStored, value);
                case DATA_ENERGY_HIGH -> energyStored = withHighWord(energyStored, value);
                case DATA_CAPACITY_LOW -> syncedCapacity = withLowWord(syncedCapacity, value);
                case DATA_CAPACITY_HIGH -> syncedCapacity = withHighWord(syncedCapacity, value);
                case DATA_ENABLED -> enabled = value != 0;
                case DATA_ENTOMBED -> entombed = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private int energyStored;
    private int syncedCapacity;
    private boolean enabled = true;
    private boolean entombed;
    @Nullable
    private UUID ownerUuid;
    @Nullable
    private UUID trappedEntityUuid;
    private double trappedX;
    private double trappedY;
    private double trappedZ;
    private double trappedBeamEndY;
    private boolean trappedMobWasNoAi;
    private int drainCooldownTicks;
    private int targetMissingTicks;
    private ImprisonmentPhase imprisonmentPhase = ImprisonmentPhase.NONE;
    @Nullable
    private BlockPos portalCenter;
    @Nullable
    private BlockPos partnerBeaconPos;
    @Nullable
    private BlockPos linkedSourceBeaconPos;
    private boolean hasCustomBeamTarget;
    private double beamTargetX;
    private double beamTargetY;
    private double beamTargetZ;
    private int imprisonmentDrainTicks;
    private boolean pharaohGateLink;
    private boolean beamPathClear = true;
    private boolean checkingVerticalBeamClear = true;
    private int lastBeamCheckY = Integer.MIN_VALUE;

    public ZenioniteBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZENIONITE_BEACON.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ZenioniteBeaconBlockEntity beacon) {
        beacon.clampEnergyToCapacity();
        boolean completedGateLink = beacon.updatePharaohGateLink();
        beacon.updateBeamPathClear();
        beacon.syncRenderState();

        if (completedGateLink) {
            return;
        }

        if (beacon.linkedSourceBeaconPos != null) {
            if (!beacon.hasValidLinkedSource()) {
                beacon.clearExternalBeamLink();
            }
            return;
        }
        if (beacon.imprisonmentPhase != ImprisonmentPhase.NONE) {
            beacon.tickImprisonment();
            return;
        }
        if (beacon.entombed) {
            beacon.entombed = false;
            beacon.notifyClients();
        }
        if (!beacon.isBeamActive()) {
            beacon.releaseTrappedEntity();
            return;
        }

        LivingEntity trapped = beacon.findTrappedEntity();
        if (trapped != null) {
            beacon.targetMissingTicks = 0;
            if (!trapped.isAlive() || trapped.isRemoved()) {
                beacon.clearTrap();
                return;
            }
            if (trapped instanceof Player player && beacon.isOwner(player.getUUID())
                    && player.isShiftKeyDown()) {
                beacon.releaseTrappedEntity();
                return;
            }
            beacon.tickGateCapture(trapped);
            beacon.drainForTrappedEntity();
            return;
        }
        if (beacon.trappedEntityUuid != null) {
            if (++beacon.targetMissingTicks > TARGET_MISSING_GRACE_TICKS) {
                beacon.clearTrap();
            }
            return;
        }

        LivingEntity target = beacon.findFirstChainTarget(level, pos);
        if (target != null) {
            beacon.capture(target);
            beacon.tickGateCapture(target);
        }
    }

    public int getEnergyStored() {
        return Math.max(0, Math.min(energyStored, getEnergyCapacity()));
    }

    public int getEnergyCapacity() {
        if (level != null && level.isClientSide && syncedCapacity > 0) {
            return syncedCapacity;
        }
        return ENERGY_CAPACITY * EndlessSandsConfig.getRfMultiplier();
    }

    public boolean isPowered() {
        return getEnergyStored() > 0;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEntombed() {
        return isEntombedForControl();
    }

    public boolean isBeamActive() {
        return (pharaohGateLink || enabled && isPowered()) && beamPathClear;
    }

    public boolean isPharaohGateLinkActive() {
        return pharaohGateLink;
    }

    public boolean hasTrappedEntity() {
        return trappedEntityUuid != null;
    }

    public Vec3 getBeamTarget() {
        if (hasCustomBeamTarget) {
            return new Vec3(beamTargetX, beamTargetY, beamTargetZ);
        }
        if (trappedEntityUuid != null) {
            return new Vec3(trappedX, trappedBeamEndY, trappedZ);
        }
        double maximumY = level == null
                ? worldPosition.getY() + 1.0D : level.getMaxBuildHeight();
        return new Vec3(worldPosition.getX() + 0.5D, maximumY,
                worldPosition.getZ() + 0.5D);
    }

    public double getBeamEndY() {
        return getBeamTarget().y;
    }

    public boolean isGateControlAvailable() {
        return level != null && ZenionitePortalFrameBlockEntity.isBatteryInValidCircuit(
                level, worldPosition.below());
    }

    public boolean canStartImprisonment() {
        if (linkedSourceBeaconPos != null) {
            ZenioniteBeaconBlockEntity source = getLinkedSourceBeacon();
            return source != null && source.canStartOwnedImprisonment();
        }
        return canStartOwnedImprisonment();
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (!enabled) {
            if (imprisonmentPhase != ImprisonmentPhase.NONE) {
                abortImprisonment(true);
            } else {
                releaseTrappedEntity();
            }
        }
        syncRenderState();
        notifyClients();
    }

    public void toggleEnabled() {
        setEnabled(!enabled);
    }

    public void toggleEntombed() {
        if (linkedSourceBeaconPos != null) {
            ZenioniteBeaconBlockEntity source = getLinkedSourceBeacon();
            if (source != null) source.toggleEntombed();
            return;
        }
        if (imprisonmentPhase != ImprisonmentPhase.NONE) {
            abortImprisonment(true);
        } else {
            startImprisonment();
        }
    }

    public void setOwner(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        notifyClients();
    }

    public void setOwnerIfAbsent(UUID ownerUuid) {
        if (this.ownerUuid == null) {
            setOwner(ownerUuid);
        }
    }

    public boolean isOwner(UUID playerUuid) {
        return ownerUuid != null && ownerUuid.equals(playerUuid);
    }

    @Nullable
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ZenioniteBeaconMenu(containerId, inventory, this, data);
    }

    @Override
    public AABB getRenderBoundingBox() {
        Vec3 start = new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY(),
                worldPosition.getZ() + 0.5D);
        return new AABB(start, getBeamTarget()).inflate(1.0D);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energyHandler);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncRenderState();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            if (imprisonmentPhase != ImprisonmentPhase.NONE) {
                abortImprisonment(true);
            } else {
                releaseTrappedEntity();
            }
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(ENERGY_TAG, getEnergyStored());
        tag.putBoolean(ENABLED_TAG, enabled);
        tag.putBoolean(ENTOMBED_TAG, entombed);
        if (ownerUuid != null) tag.putUUID(OWNER_TAG, ownerUuid);
        if (trappedEntityUuid != null) {
            tag.putUUID(TRAPPED_ENTITY_TAG, trappedEntityUuid);
            tag.putDouble(TRAPPED_X_TAG, trappedX);
            tag.putDouble(TRAPPED_Y_TAG, trappedY);
            tag.putDouble(TRAPPED_Z_TAG, trappedZ);
            tag.putDouble(TRAPPED_BEAM_END_Y_TAG, trappedBeamEndY);
            tag.putBoolean(TRAPPED_MOB_WAS_NO_AI_TAG, trappedMobWasNoAi);
            tag.putInt(DRAIN_COOLDOWN_TAG, drainCooldownTicks);
        }
        tag.putInt(IMPRISONMENT_PHASE_TAG, imprisonmentPhase.ordinal());
        if (portalCenter != null) tag.putLong(PORTAL_CENTER_TAG, portalCenter.asLong());
        if (partnerBeaconPos != null) tag.putLong(PARTNER_BEACON_TAG, partnerBeaconPos.asLong());
        if (linkedSourceBeaconPos != null) {
            tag.putLong(LINKED_SOURCE_BEACON_TAG, linkedSourceBeaconPos.asLong());
        }
        tag.putBoolean(CUSTOM_BEAM_TARGET_TAG, hasCustomBeamTarget);
        if (hasCustomBeamTarget) {
            tag.putDouble(BEAM_TARGET_X_TAG, beamTargetX);
            tag.putDouble(BEAM_TARGET_Y_TAG, beamTargetY);
            tag.putDouble(BEAM_TARGET_Z_TAG, beamTargetZ);
        }
        tag.putInt(IMPRISONMENT_DRAIN_TICKS_TAG, imprisonmentDrainTicks);
        tag.putBoolean(PHARAOH_GATE_LINK_TAG, pharaohGateLink);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStored = Math.max(0, Math.min(tag.getInt(ENERGY_TAG), getEnergyCapacity()));
        enabled = !tag.contains(ENABLED_TAG) || tag.getBoolean(ENABLED_TAG);
        ownerUuid = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
        trappedEntityUuid = tag.hasUUID(TRAPPED_ENTITY_TAG)
                ? tag.getUUID(TRAPPED_ENTITY_TAG) : null;
        trappedX = tag.getDouble(TRAPPED_X_TAG);
        trappedY = tag.getDouble(TRAPPED_Y_TAG);
        trappedZ = tag.getDouble(TRAPPED_Z_TAG);
        trappedBeamEndY = tag.getDouble(TRAPPED_BEAM_END_Y_TAG);
        trappedMobWasNoAi = tag.getBoolean(TRAPPED_MOB_WAS_NO_AI_TAG);
        drainCooldownTicks = Math.max(0, Math.min(tag.getInt(DRAIN_COOLDOWN_TAG),
                trapDrainIntervalTicks()));

        int phaseIndex = tag.getInt(IMPRISONMENT_PHASE_TAG);
        imprisonmentPhase = phaseIndex >= 0 && phaseIndex < ImprisonmentPhase.values().length
                ? ImprisonmentPhase.values()[phaseIndex] : ImprisonmentPhase.NONE;
        entombed = imprisonmentPhase != ImprisonmentPhase.NONE
                || tag.getBoolean(ENTOMBED_TAG);
        portalCenter = tag.contains(PORTAL_CENTER_TAG)
                ? BlockPos.of(tag.getLong(PORTAL_CENTER_TAG)) : null;
        partnerBeaconPos = tag.contains(PARTNER_BEACON_TAG)
                ? BlockPos.of(tag.getLong(PARTNER_BEACON_TAG)) : null;
        linkedSourceBeaconPos = tag.contains(LINKED_SOURCE_BEACON_TAG)
                ? BlockPos.of(tag.getLong(LINKED_SOURCE_BEACON_TAG)) : null;
        hasCustomBeamTarget = tag.getBoolean(CUSTOM_BEAM_TARGET_TAG);
        beamTargetX = tag.getDouble(BEAM_TARGET_X_TAG);
        beamTargetY = tag.getDouble(BEAM_TARGET_Y_TAG);
        beamTargetZ = tag.getDouble(BEAM_TARGET_Z_TAG);
        imprisonmentDrainTicks = Math.max(0, Math.min(IMPRISONMENT_DRAIN_DURATION_TICKS,
                tag.getInt(IMPRISONMENT_DRAIN_TICKS_TAG)));
        pharaohGateLink = tag.getBoolean(PHARAOH_GATE_LINK_TAG);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    private void startImprisonment() {
        if (!(level instanceof ServerLevel) || !canStartImprisonment()) return;
        ZenioniteBeaconBlockEntity partner = getPartnerBeacon();
        LivingEntity target = findTrappedEntity();
        if (partner == null || target == null) return;

        imprisonmentPhase = ImprisonmentPhase.LOWER_TO_TRANSFER_HEIGHT;
        entombed = true;
        imprisonmentDrainTicks = 0;
        hasCustomBeamTarget = false;
        ZenionitePortalFrameBlockEntity.setPortalTransferActive(level, portalCenter, true);
        updateTargetBeam(target, partner);
        notifyClients();
    }

    private void tickImprisonment() {
        if (!(level instanceof ServerLevel)) return;
        ZenioniteBeaconBlockEntity partner = getPartnerBeacon();
        if (partner == null) {
            abortImprisonment(true);
            return;
        }
        if (imprisonmentPhase == ImprisonmentPhase.DRAIN) {
            tickPostTransferDrain(partner);
            return;
        }
        if (!isImprisonmentConnectionValid(partner)) {
            abortImprisonment(true);
            return;
        }
        if (!isBeamActive() || !partner.isBeamActive()) {
            abortImprisonment(true);
            return;
        }

        LivingEntity target = findTrappedEntity();
        if (target == null || !target.isAlive() || target.isRemoved()) {
            abortImprisonment(false);
            return;
        }
        if (target instanceof Player player && isOwner(player.getUUID())
                && player.isShiftKeyDown()) {
            abortImprisonment(true);
            return;
        }

        switch (imprisonmentPhase) {
            case LOWER_TO_TRANSFER_HEIGHT -> {
                if (moveLockedTarget(target, new Vec3(portalCenter.getX() + 0.5D,
                        worldPosition.getY() + 5.0D, portalCenter.getZ() + 0.5D))) {
                    imprisonmentPhase = ImprisonmentPhase.DESCEND_INTO_PORTAL;
                }
            }
            case CENTER_OVER_PORTAL -> {
                // Compatibility for worlds saved mid-imprisonment before capture-time centering was added.
                if (moveLockedTarget(target, new Vec3(portalCenter.getX() + 0.5D,
                        trappedY, portalCenter.getZ() + 0.5D))) {
                    imprisonmentPhase = ImprisonmentPhase.LOWER_TO_TRANSFER_HEIGHT;
                }
            }
            case DESCEND_INTO_PORTAL -> {
                if (moveLockedTarget(target, new Vec3(portalCenter.getX() + 0.5D,
                        worldPosition.getY() - 1.0D, portalCenter.getZ() + 0.5D))) {
                    finishTransfer(target, partner);
                    return;
                }
            }
            default -> {
                abortImprisonment(true);
                return;
            }
        }

        updateTargetBeam(target, partner);
        drainForTrappedEntity();
        if (!isPowered()) {
            abortImprisonment(true);
            return;
        }
        notifyClients();
    }

    private boolean moveLockedTarget(LivingEntity target, Vec3 destination) {
        Vec3 current = new Vec3(trappedX, trappedY, trappedZ);
        Vec3 difference = destination.subtract(current);
        double distance = difference.length();
        if (distance <= IMPRISONMENT_MOVE_SPEED) {
            trappedX = destination.x;
            trappedY = destination.y;
            trappedZ = destination.z;
            holdEntity(target);
            return true;
        }
        Vec3 next = current.add(difference.scale(IMPRISONMENT_MOVE_SPEED / distance));
        trappedX = next.x;
        trappedY = next.y;
        trappedZ = next.z;
        holdEntity(target);
        return false;
    }

    private void updateTargetBeam(LivingEntity target, ZenioniteBeaconBlockEntity partner) {
        trappedBeamEndY = trappedY + target.getBbHeight() * 0.5D;
        partner.setExternalBeamTarget(worldPosition,
                new Vec3(trappedX, trappedBeamEndY, trappedZ));
    }

    private void tickGateCapture(LivingEntity target) {
        ZenioniteBeaconBlockEntity partner = getPartnerBeacon();
        if (partner == null) {
            establishGateCapture(target);
            partner = getPartnerBeacon();
        }
        if (partner == null || !isStagedConnectionValid(partner)) {
            clearGateCaptureLink();
            holdEntity(target);
            return;
        }

        moveLockedTarget(target, new Vec3(
                portalCenter.getX() + 0.5D,
                trappedY,
                portalCenter.getZ() + 0.5D));
        updateTargetBeam(target, partner);
        notifyClients();
    }

    private void establishGateCapture(LivingEntity target) {
        if (level == null || imprisonmentPhase != ImprisonmentPhase.NONE) return;
        ZenionitePortalFrameBlockEntity.PortalConnection connection =
                ZenionitePortalFrameBlockEntity.getActivePortalForBattery(
                        level, worldPosition.below());
        if (connection == null) return;
        ZenioniteBeaconBlockEntity partner = findAvailablePartner(
                connection.oppositeBattery().above());
        if (partner == null) return;

        portalCenter = connection.center().immutable();
        partnerBeaconPos = partner.worldPosition.immutable();
        updateTargetBeam(target, partner);
    }

    private boolean canStartOwnedImprisonment() {
        if (level == null || level.isClientSide || !isBeamActive()
                || trappedEntityUuid == null
                || imprisonmentPhase != ImprisonmentPhase.NONE
                || portalCenter == null || partnerBeaconPos == null
                || !isHorizontallyCentered()) {
            return false;
        }
        ZenioniteBeaconBlockEntity partner = getPartnerBeacon();
        return partner != null && partner.isBeamActive() && isStagedConnectionValid(partner);
    }

    private boolean isHorizontallyCentered() {
        return portalCenter != null
                && Math.abs(trappedX - (portalCenter.getX() + 0.5D)) < 0.000001D
                && Math.abs(trappedZ - (portalCenter.getZ() + 0.5D)) < 0.000001D;
    }

    private boolean isEntombedForControl() {
        if (entombed) return true;
        ZenioniteBeaconBlockEntity source = getLinkedSourceBeacon();
        return source != null && source.entombed;
    }

    private void finishTransfer(LivingEntity target, ZenioniteBeaconBlockEntity partner) {
        Vec3 portalTarget = new Vec3(portalCenter.getX() + 0.5D,
                portalCenter.getY() + 0.35D, portalCenter.getZ() + 0.5D);
        setCustomBeamTarget(portalTarget);
        partner.setExternalBeamTarget(worldPosition, portalTarget);
        restoreTargetControl(target);
        if (!ModEvents.enterPrisonRealm(target, portalCenter)) {
            abortImprisonment(true);
            return;
        }
        if (target instanceof PharaohEntity) {
            ZenionitePortalFrameBlockEntity.completePortalWithPharaoh(level, portalCenter);
        }

        trappedEntityUuid = null;
        trappedMobWasNoAi = false;
        drainCooldownTicks = 0;
        targetMissingTicks = 0;
        imprisonmentPhase = ImprisonmentPhase.DRAIN;
        imprisonmentDrainTicks = IMPRISONMENT_DRAIN_DURATION_TICKS;
        notifyClients();
    }

    private void tickPostTransferDrain(ZenioniteBeaconBlockEntity partner) {
        if (imprisonmentDrainTicks <= 0) {
            completeImprisonment(partner);
            return;
        }
        drainTowardZero(imprisonmentDrainTicks);
        partner.drainTowardZero(imprisonmentDrainTicks);
        imprisonmentDrainTicks--;
        if (imprisonmentDrainTicks <= 0) completeImprisonment(partner);
        else notifyClients();
    }

    private void drainTowardZero(int ticksRemaining) {
        if (energyStored <= 0) return;
        int amount = Math.max(1, (energyStored + ticksRemaining - 1) / ticksRemaining);
        energyStored = Math.max(0, energyStored - amount);
        syncRenderState();
        notifyClients();
    }

    private void completeImprisonment(ZenioniteBeaconBlockEntity partner) {
        if (level != null && portalCenter != null) {
            ZenionitePortalFrameBlockEntity.setPortalTransferActive(level, portalCenter, false);
        }
        partner.clearExternalBeamLink();
        clearImprisonmentState();
        clearTrap();
        syncRenderState();
        notifyClients();
    }

    private void abortImprisonment(boolean releaseTarget) {
        if (level != null && portalCenter != null) {
            ZenionitePortalFrameBlockEntity.setPortalTransferActive(level, portalCenter, false);
        }
        ZenioniteBeaconBlockEntity partner = getPartnerBeacon();
        if (partner != null) partner.clearExternalBeamLink();
        if (releaseTarget) releaseTrappedEntity();
        else clearTrap();
        clearImprisonmentState();
        syncRenderState();
        notifyClients();
    }

    private void clearImprisonmentState() {
        imprisonmentPhase = ImprisonmentPhase.NONE;
        entombed = false;
        portalCenter = null;
        partnerBeaconPos = null;
        imprisonmentDrainTicks = 0;
        clearCustomBeamTarget();
    }

    private boolean isImprisonmentConnectionValid(ZenioniteBeaconBlockEntity partner) {
        if (level == null || portalCenter == null || partnerBeaconPos == null
                || !partner.worldPosition.equals(partnerBeaconPos)
                || !worldPosition.equals(partner.linkedSourceBeaconPos)) return false;
        ZenionitePortalFrameBlockEntity.PortalConnection connection =
                ZenionitePortalFrameBlockEntity.getActivePortalForBattery(
                        level, worldPosition.below());
        return connection != null && connection.center().equals(portalCenter)
                && connection.oppositeBattery().above().equals(partnerBeaconPos);
    }

    private boolean isStagedConnectionValid(ZenioniteBeaconBlockEntity partner) {
        if (level == null || portalCenter == null || partnerBeaconPos == null
                || !partner.worldPosition.equals(partnerBeaconPos)
                || !worldPosition.equals(partner.linkedSourceBeaconPos)
                || !partner.isBeamActive()) return false;
        ZenionitePortalFrameBlockEntity.PortalConnection connection =
                ZenionitePortalFrameBlockEntity.getActivePortalForBattery(
                        level, worldPosition.below());
        return connection != null && connection.center().equals(portalCenter)
                && connection.oppositeBattery().above().equals(partnerBeaconPos);
    }

    @Nullable
    private ZenioniteBeaconBlockEntity findAvailablePartner(BlockPos position) {
        if (level == null || !(level.getBlockEntity(position)
                instanceof ZenioniteBeaconBlockEntity partner)
                || partner == this || !partner.isBeamActive()
                || partner.trappedEntityUuid != null
                || partner.imprisonmentPhase != ImprisonmentPhase.NONE
                || partner.linkedSourceBeaconPos != null) return null;
        return partner;
    }

    @Nullable
    private ZenioniteBeaconBlockEntity getPartnerBeacon() {
        if (level == null || partnerBeaconPos == null) return null;
        return level.getBlockEntity(partnerBeaconPos)
                instanceof ZenioniteBeaconBlockEntity partner ? partner : null;
    }

    @Nullable
    private ZenioniteBeaconBlockEntity getLinkedSourceBeacon() {
        if (level == null || linkedSourceBeaconPos == null) return null;
        return level.getBlockEntity(linkedSourceBeaconPos)
                instanceof ZenioniteBeaconBlockEntity source ? source : null;
    }

    private void setExternalBeamTarget(BlockPos sourceBeacon, Vec3 target) {
        linkedSourceBeaconPos = sourceBeacon.immutable();
        setCustomBeamTarget(target);
        syncRenderState();
        notifyClients();
    }

    private void clearExternalBeamLink() {
        if (linkedSourceBeaconPos == null && !hasCustomBeamTarget) return;
        linkedSourceBeaconPos = null;
        clearCustomBeamTarget();
        syncRenderState();
        notifyClients();
    }

    private boolean hasValidLinkedSource() {
        ZenioniteBeaconBlockEntity source = getLinkedSourceBeacon();
        return source != null && source.trappedEntityUuid != null
                && worldPosition.equals(source.partnerBeaconPos)
                && (source.imprisonmentPhase != ImprisonmentPhase.NONE
                || source.isStagedConnectionValid(this));
    }

    private void clearGateCaptureLink() {
        ZenioniteBeaconBlockEntity partner = getPartnerBeacon();
        if (partner != null && worldPosition.equals(partner.linkedSourceBeaconPos)) {
            partner.clearExternalBeamLink();
        }
        if (imprisonmentPhase == ImprisonmentPhase.NONE) {
            portalCenter = null;
            partnerBeaconPos = null;
        }
    }

    private void setCustomBeamTarget(Vec3 target) {
        hasCustomBeamTarget = true;
        beamTargetX = target.x;
        beamTargetY = target.y;
        beamTargetZ = target.z;
    }

    private void clearCustomBeamTarget() {
        hasCustomBeamTarget = false;
        beamTargetX = 0.0D;
        beamTargetY = 0.0D;
        beamTargetZ = 0.0D;
    }

    private boolean updatePharaohGateLink() {
        if (level == null || level.isClientSide
                || imprisonmentPhase != ImprisonmentPhase.NONE
                || trappedEntityUuid != null || linkedSourceBeaconPos != null) {
            setPharaohGateLink(false, null);
            return false;
        }

        ZenionitePortalFrameBlockEntity.PortalConnection connection =
                ZenionitePortalFrameBlockEntity.getPharaohGateForBattery(
                        level, worldPosition.below());
        if (connection == null) {
            setPharaohGateLink(false, null);
            return false;
        }

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && (DragonEggSacrificeEvents.isInProgress(serverLevel, connection.center())
                || ZenionitePortalFrameBlockEntity.isSacrificeComplete(level, connection.center()))) {
            setPharaohGateLink(false, null);
            return false;
        }

        BlockPos oppositeBeaconPos = connection.oppositeBattery().above();
        if (!(level.getBlockEntity(oppositeBeaconPos)
                instanceof ZenioniteBeaconBlockEntity oppositeBeacon)) {
            setPharaohGateLink(false, null);
            return false;
        }

        Vec3 target = new Vec3(
                oppositeBeacon.worldPosition.getX() + 0.5D,
                oppositeBeacon.worldPosition.getY(),
                oppositeBeacon.worldPosition.getZ() + 0.5D);
        setPharaohGateLink(true, target);
        return true;
    }

    private void setPharaohGateLink(boolean active, @Nullable Vec3 target) {
        boolean wasActive = pharaohGateLink;
        boolean changed = wasActive != active;
        pharaohGateLink = active;
        if (active && target != null) {
            changed |= !hasCustomBeamTarget
                    || Math.abs(beamTargetX - target.x) > 0.000001D
                    || Math.abs(beamTargetY - target.y) > 0.000001D
                    || Math.abs(beamTargetZ - target.z) > 0.000001D;
            setCustomBeamTarget(target);
        } else if (wasActive) {
            clearCustomBeamTarget();
        }
        if (changed) notifyClients();
    }

    private void updateBeamPathClear() {
        if (level == null || level.isClientSide) return;
        if (pharaohGateLink) {
            beamPathClear = true;
            resetVerticalBeamCheck();
            return;
        }
        if (!enabled || !isPowered()) {
            beamPathClear = true;
            resetVerticalBeamCheck();
            return;
        }

        if (hasCustomBeamTarget || trappedEntityUuid != null) {
            beamPathClear = hasClearBeamPath(getBeamTarget());
            resetVerticalBeamCheck();
            return;
        }

        updateVerticalBeamCheck();
    }

    private void updateVerticalBeamCheck() {
        int topY = level.getMaxBuildHeight() - 1;
        if (lastBeamCheckY < worldPosition.getY() || lastBeamCheckY > topY) {
            lastBeamCheckY = worldPosition.getY();
            checkingVerticalBeamClear = true;
        }

        for (int checked = 0; checked < 10 && lastBeamCheckY < topY; checked++) {
            BlockPos checkPos = new BlockPos(
                    worldPosition.getX(), ++lastBeamCheckY, worldPosition.getZ());
            if (blocksBeaconBeam(level.getBlockState(checkPos), checkPos)) {
                checkingVerticalBeamClear = false;
                lastBeamCheckY = topY;
                break;
            }
        }

        if (lastBeamCheckY >= topY) {
            beamPathClear = checkingVerticalBeamClear;
            lastBeamCheckY = worldPosition.getY();
            checkingVerticalBeamClear = true;
        }
    }

    private boolean hasClearBeamPath(Vec3 target) {
        Vec3 start = new Vec3(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.0D,
                worldPosition.getZ() + 0.5D);
        BlockPos targetPosition = BlockPos.containing(target);
        return BlockGetter.traverseBlocks(
                start,
                target,
                level,
                (checkedLevel, checkPos) -> {
                    if (checkPos.equals(worldPosition) || checkPos.equals(targetPosition)) {
                        return null;
                    }
                    return blocksBeaconBeam(checkedLevel.getBlockState(checkPos), checkPos)
                            ? Boolean.FALSE : null;
                },
                ignored -> Boolean.TRUE);
    }

    private boolean blocksBeaconBeam(BlockState state, BlockPos position) {
        return state.getLightBlock(level, position) >= 15 && !state.is(Blocks.BEDROCK);
    }

    private void resetVerticalBeamCheck() {
        lastBeamCheckY = Integer.MIN_VALUE;
        checkingVerticalBeamClear = true;
    }

    private LivingEntity findFirstChainTarget(Level level, BlockPos pos) {
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        AABB beamColumn = new AABB(centerX - CHAIN_HALF_WIDTH, pos.getY() + 0.75D,
                centerZ - CHAIN_HALF_WIDTH, centerX + CHAIN_HALF_WIDTH,
                level.getMaxBuildHeight(), centerZ + CHAIN_HALF_WIDTH);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class,
                beamColumn, entity -> entity.isAlive() && !entity.isSpectator()
                        && !(entity instanceof Player player
                        && isOwner(player.getUUID()) && player.isShiftKeyDown()));
        return candidates.stream().min(Comparator.comparingDouble(Entity::getY)).orElse(null);
    }

    @Nullable
    private LivingEntity findTrappedEntity() {
        if (trappedEntityUuid == null || !(level instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(trappedEntityUuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void capture(LivingEntity target) {
        trappedEntityUuid = target.getUUID();
        trappedX = target.getX();
        trappedY = target.getY();
        trappedZ = target.getZ();
        trappedBeamEndY = target.getY() + target.getBbHeight() * 0.5D;
        trappedMobWasNoAi = target instanceof Mob mob && mob.isNoAi();
        drainCooldownTicks = trapDrainIntervalTicks();
        targetMissingTicks = 0;
        disableTargetControl(target);
        notifyClients();
    }

    private void holdEntity(LivingEntity target) {
        disableTargetControl(target);
        if (target.distanceToSqr(trappedX, trappedY, trappedZ) > 0.000001D) {
            target.teleportTo(trappedX, trappedY, trappedZ);
        }
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0.0F;
    }

    private void disableTargetControl(LivingEntity target) {
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setTarget(null);
            mob.setNoAi(true);
        }
        if (target instanceof Player player) applyChainedEffect(player);
    }

    private void restoreTargetControl(LivingEntity target) {
        if (target instanceof Mob mob) mob.setNoAi(trappedMobWasNoAi);
        if (target instanceof Player player) player.removeEffect(ModEffects.CHAINED.get());
    }

    private void releaseTrappedEntity() {
        LivingEntity target = findTrappedEntity();
        if (target != null) restoreTargetControl(target);
        clearTrap();
    }

    private void clearTrap() {
        if (imprisonmentPhase == ImprisonmentPhase.NONE) clearGateCaptureLink();
        if (trappedEntityUuid == null) {
            drainCooldownTicks = 0;
            targetMissingTicks = 0;
            return;
        }
        trappedEntityUuid = null;
        trappedMobWasNoAi = false;
        trappedBeamEndY = 0.0D;
        drainCooldownTicks = 0;
        targetMissingTicks = 0;
        notifyClients();
    }

    private void drainForTrappedEntity() {
        if (energyStored <= 0) {
            releaseTrappedEntity();
            return;
        }
        if (drainCooldownTicks <= 0) drainCooldownTicks = trapDrainIntervalTicks();
        if (--drainCooldownTicks > 0) {
            setChanged();
            return;
        }
        energyStored--;
        drainCooldownTicks = trapDrainIntervalTicks();
        syncRenderState();
        notifyClients();
        if (energyStored <= 0 && imprisonmentPhase == ImprisonmentPhase.NONE) {
            releaseTrappedEntity();
        }
    }

    private void applyChainedEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.CHAINED.get(),
                10, 0, false, false, true));
    }

    private int trapDrainIntervalTicks() {
        long interval = (long) EndlessSandsConfig.getRfTransferIntervalTicks() * 3L;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, interval));
    }

    private void clampEnergyToCapacity() {
        int clamped = Math.max(0, Math.min(energyStored, getEnergyCapacity()));
        if (clamped != energyStored) {
            energyStored = clamped;
            syncRenderState();
            notifyClients();
        }
    }

    private void syncRenderState() {
        if (level == null || level.isClientSide || isRemoved()) return;
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(ZenioniteBeaconBlock.POWERED)
                || !state.hasProperty(ZenioniteBeaconBlock.BEAM_ACTIVE)) return;
        BlockState updated = state.setValue(ZenioniteBeaconBlock.POWERED, isPowered())
                .setValue(ZenioniteBeaconBlock.BEAM_ACTIVE, isBeamActive());
        if (!updated.equals(state)) level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
    }

    private void notifyClients() {
        setChanged();
        if (level != null && !level.isClientSide && !isRemoved()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static int combineWords(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }

    private static int lowWord(int value) { return value & 0xFFFF; }
    private static int highWord(int value) { return value >>> 16 & 0xFFFF; }
    private static int withLowWord(int current, int low) {
        return current & 0xFFFF0000 | low & 0xFFFF;
    }
    private static int withHighWord(int current, int high) {
        return current & 0x0000FFFF | (high & 0xFFFF) << 16;
    }

    private enum ImprisonmentPhase {
        NONE, LOWER_TO_TRANSFER_HEIGHT, CENTER_OVER_PORTAL, DESCEND_INTO_PORTAL, DRAIN
    }

    private final class EnergyHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(Math.max(0, maxReceive),
                    Math.max(0, getEnergyCapacity() - energyStored));
            if (!simulate && received > 0) {
                energyStored += received;
                syncRenderState();
                notifyClients();
            }
            return received;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() {
            return ZenioniteBeaconBlockEntity.this.getEnergyStored();
        }
        @Override public int getMaxEnergyStored() { return getEnergyCapacity(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}
