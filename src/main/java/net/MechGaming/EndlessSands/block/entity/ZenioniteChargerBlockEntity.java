package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenioniteChargerBlock;
import net.MechGaming.EndlessSands.block.custom.ZenioniteStairBlock;
import net.MechGaming.EndlessSands.config.EndlessSandsConfig;
import net.MechGaming.EndlessSands.inventory.ZenioniteChargerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZenioniteChargerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TANK_CAPACITY = 8_000;
    public static final int BUCKET_AMOUNT = 1_000;
    public static final int COLLECTION_TARGET = 12_000;
    public static final int ZENIONITE_STAIR_COLLECTION_TICKS = 20;
    public static final int PRODUCTION_TICKS = 20;
    public static final int OUTPUT_PER_TRANSFER = 1;
    public static final int ENERGY_CAPACITY = 8;

    public static final int WATER_SLOT = 0;
    public static final int LAVA_SLOT = 1;

    public static final int DATA_WATER = 0;
    public static final int DATA_LAVA = 1;
    public static final int DATA_POWER = 2;
    public static final int DATA_COUNT = 3;

    private static final String WATER_TANK_TAG = "WaterTank";
    private static final String LAVA_TANK_TAG = "LavaTank";
    private static final String INVENTORY_TAG = "Inventory";
    private static final String ENERGY_TAG = "Energy";
    private static final String PRODUCTION_PROGRESS_TAG = "ProductionProgress";
    private static final String WATER_COLLECTION_PROGRESS_TAG = "WaterCollectionProgress";
    private static final String LAVA_COLLECTION_PROGRESS_TAG = "LavaCollectionProgress";
    private static final String WATER_STAIR_COLLECTION_PROGRESS_TAG = "WaterStairCollectionProgress";
    private static final String LAVA_STAIR_COLLECTION_PROGRESS_TAG = "LavaStairCollectionProgress";
    private static final String TRANSFER_COOLDOWN_TAG = "TransferCooldown";
    private static final String PHARAOH_GATE_TAG = "PharaohGate";

    private final FluidTank waterTank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }

        @Override
        protected void onContentsChanged() {
            onStorageChanged();
        }
    };

    private final FluidTank lavaTank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.LAVA;
        }

        @Override
        protected void onContentsChanged() {
            onStorageChanged();
        }
    };

    private final ItemStackHandler automationItems = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !isCreativeSource() && !pharaohGate
                    && (slot == WATER_SLOT && stack.is(Items.WATER_BUCKET)
                    || slot == LAVA_SLOT && stack.is(Items.LAVA_BUCKET));
        }

        @Override
        protected void onContentsChanged(int slot) {
            onStorageChanged();
        }
    };

    private final IFluidHandler horizontalFluidInput = new FluidInputHandler();
    private final IItemHandler horizontalBucketInput = new BucketInputHandler();
    private final IItemHandler downwardBucketOutput = new EmptyBucketOutputHandler();
    private final IEnergyStorage downwardEnergyInput = new EnergyInputHandler();
    private final IEnergyStorage upwardEnergyOutput = new EnergyOutputHandler();

    private LazyOptional<IFluidHandler> fluidInputCapability =
            LazyOptional.of(() -> horizontalFluidInput);
    private LazyOptional<IItemHandler> bucketInputCapability =
            LazyOptional.of(() -> horizontalBucketInput);
    private LazyOptional<IItemHandler> bucketOutputCapability =
            LazyOptional.of(() -> downwardBucketOutput);
    private LazyOptional<IEnergyStorage> energyInputCapability =
            LazyOptional.of(() -> downwardEnergyInput);
    private LazyOptional<IEnergyStorage> energyOutputCapability =
            LazyOptional.of(() -> upwardEnergyOutput);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            boolean clientView = level != null && level.isClientSide;
            return switch (index) {
                case DATA_WATER -> clientView ? syncedWaterLevel : getWaterLevel();
                case DATA_LAVA -> clientView ? syncedLavaLevel : getLavaLevel();
                case DATA_POWER -> clientView ? syncedPowerLevel : getPowerLevel();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            int stage = Math.max(0, Math.min(8, value));
            switch (index) {
                case DATA_WATER -> syncedWaterLevel = stage;
                case DATA_LAVA -> syncedLavaLevel = stage;
                case DATA_POWER -> syncedPowerLevel = stage;
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
    private int productionProgress;
    private int waterCollectionProgress;
    private int lavaCollectionProgress;
    private int waterStairCollectionProgress;
    private int lavaStairCollectionProgress;
    private int syncedWaterLevel;
    private int syncedLavaLevel;
    private int syncedPowerLevel;
    private boolean powerDraining;
    private boolean pharaohGate;

    private int transferCooldownTicks;
    private boolean loading;

    public ZenioniteChargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZENIONITE_CHARGER.get(), pos, state);
        this.powerDraining = state.hasProperty(ZenioniteChargerBlock.POWER_DRAINING)
                && state.getValue(ZenioniteChargerBlock.POWER_DRAINING);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ZenioniteChargerBlockEntity charger
    ) {
        if (charger.pharaohGate) {
            charger.drainPharaohGateEnergy();
            charger.updateBlockState();
            return;
        }
        if (charger.isCreativeSource()) {
            charger.tickTransferCooldown();
            charger.pushEnergyUpward(level, pos);
            return;
        }
        charger.clampEnergyToCapacity();
        charger.tickTransferCooldown();
        charger.processAutomationBuckets();
        charger.collectNearbyFluids(level, pos);
        charger.runProductionCycle();
        charger.pushEnergyUpward(level, pos);
        charger.updateBlockState();
    }

    private void processAutomationBuckets() {
        processAutomationBucket(WATER_SLOT, Items.WATER_BUCKET, Fluids.WATER, waterTank);
        processAutomationBucket(LAVA_SLOT, Items.LAVA_BUCKET, Fluids.LAVA, lavaTank);
    }

    private void processAutomationBucket(int slot, net.minecraft.world.item.Item filledBucket,
                                         Fluid fluid, FluidTank tank) {
        ItemStack stack = automationItems.getStackInSlot(slot);
        if (!stack.is(filledBucket) || stack.getCount() != 1
                || tank.fill(new FluidStack(fluid, BUCKET_AMOUNT), IFluidHandler.FluidAction.SIMULATE)
                != BUCKET_AMOUNT) {
            return;
        }

        tank.fill(new FluidStack(fluid, BUCKET_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
        automationItems.setStackInSlot(slot, new ItemStack(Items.BUCKET));
    }

    private void collectNearbyFluids(Level level, BlockPos pos) {
        NearbyFluidInputs inputs = nearbyFluidInputs(level, pos);

        waterCollectionProgress = advanceCollection(waterTank, Fluids.WATER,
                waterCollectionProgress, inputs.waterEnvironmentalRate());
        lavaCollectionProgress = advanceCollection(lavaTank, Fluids.LAVA,
                lavaCollectionProgress, inputs.lavaEnvironmentalRate());
        waterStairCollectionProgress = advanceZenioniteStairCollection(
                waterTank, Fluids.WATER, waterStairCollectionProgress, inputs.waterStairHalves());
        lavaStairCollectionProgress = advanceZenioniteStairCollection(
                lavaTank, Fluids.LAVA, lavaStairCollectionProgress, inputs.lavaStairHalves());
    }

    private int advanceCollection(FluidTank tank, Fluid fluid, int progress, int rate) {
        if (rate <= 0 || tank.getCapacity() - tank.getFluidAmount() < BUCKET_AMOUNT) {
            if (progress != 0) {
                setChanged();
            }
            return 0;
        }

        int nextProgress = Math.min(COLLECTION_TARGET, progress + rate);
        if (nextProgress >= COLLECTION_TARGET) {
            tank.fill(new FluidStack(fluid, BUCKET_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
            return 0;
        }

        if (nextProgress != progress) {
            setChanged();
        }
        return nextProgress;
    }

    private int advanceZenioniteStairCollection(
            FluidTank tank,
            Fluid fluid,
            int progress,
            int loggedHalves
    ) {
        int wholeBucketSpace = (tank.getCapacity() - tank.getFluidAmount()) / BUCKET_AMOUNT;
        if (loggedHalves <= 0 || wholeBucketSpace <= 0) {
            if (progress != 0) {
                setChanged();
            }
            return 0;
        }

        int nextProgress = progress + 1;
        if (nextProgress >= ZENIONITE_STAIR_COLLECTION_TICKS) {
            int buckets = Math.min(loggedHalves, wholeBucketSpace);
            tank.fill(new FluidStack(fluid, buckets * BUCKET_AMOUNT),
                    IFluidHandler.FluidAction.EXECUTE);
            return 0;
        }
        if (nextProgress != progress) {
            setChanged();
        }
        return nextProgress;
    }

    private static NearbyFluidInputs nearbyFluidInputs(Level level, BlockPos pos) {
        boolean waterSource = false;
        boolean lavaSource = false;
        boolean waterFlowing = false;
        boolean lavaFlowing = false;
        int waterStairHalves = 0;
        int lavaStairHalves = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof ZenioniteStairBlock
                    && level.getBlockEntity(neighborPos) instanceof ZenioniteStairBlockEntity stair) {
                waterStairHalves += stair.countLoggedHalves(FluidTags.WATER);
                lavaStairHalves += stair.countLoggedHalves(FluidTags.LAVA);
                continue;
            }

            FluidState fluidState = neighborState.getFluidState();
            if (fluidState.is(FluidTags.WATER)) {
                waterSource |= fluidState.isSource();
                waterFlowing |= !fluidState.isSource();
            } else if (fluidState.is(FluidTags.LAVA)) {
                lavaSource |= fluidState.isSource();
                lavaFlowing |= !fluidState.isSource();
            }
        }
        return new NearbyFluidInputs(
                waterSource ? 10 : waterFlowing ? 1 : 0,
                lavaSource ? 10 : lavaFlowing ? 1 : 0,
                waterStairHalves,
                lavaStairHalves
        );
    }

    private record NearbyFluidInputs(
            int waterEnvironmentalRate,
            int lavaEnvironmentalRate,
            int waterStairHalves,
            int lavaStairHalves
    ) {
    }

    private void runProductionCycle() {
        boolean canProduce = waterTank.getFluidAmount() >= BUCKET_AMOUNT
                && lavaTank.getFluidAmount() >= BUCKET_AMOUNT
                && energyStored < getEnergyCapacity();
        if (!canProduce) {
            if (productionProgress != 0) {
                productionProgress = 0;
                setChanged();
            }
            return;
        }

        productionProgress++;
        setChanged();
        if (productionProgress < PRODUCTION_TICKS) {
            return;
        }

        productionProgress = 0;
        waterTank.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
        lavaTank.drain(BUCKET_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
        powerDraining = false;
        boolean wasEmpty = energyStored == 0;
        energyStored++;
        if (wasEmpty) {
            restartTransferCooldown();
        }
        onStorageChanged();
    }

    private void pushEnergyUpward(Level level, BlockPos pos) {
        int available = getOutputAllowance();
        if (available <= 0) {
            return;
        }

        BlockEntity above = level.getBlockEntity(pos.above());
        if (above == null) {
            return;
        }

        above.getCapability(ForgeCapabilities.ENERGY, Direction.DOWN).ifPresent(receiver -> {
            int requested = Math.min(available, Math.max(0, receiver.receiveEnergy(available, true)));
            int sent = upwardEnergyOutput.extractEnergy(requested, false);
            if (sent <= 0) {
                return;
            }

            int accepted = Math.min(sent, Math.max(0, receiver.receiveEnergy(sent, false)));
            int refund = sent - accepted;
            if (refund > 0) {
                powerDraining = false;
                if (!isCreativeSource()) {
                    energyStored += refund;
                }
                if (accepted <= 0) {
                    transferCooldownTicks = 0;
                }
                onStorageChanged();
            }
        });
    }

    public boolean canAcceptManualBucket(ItemStack stack) {
        if (isCreativeSource() || pharaohGate) {
            return false;
        }
        FluidTank tank = tankForBucket(stack);
        Fluid fluid = fluidForBucket(stack);
        return tank != null && fluid != null
                && tank.fill(new FluidStack(fluid, BUCKET_AMOUNT), IFluidHandler.FluidAction.SIMULATE)
                == BUCKET_AMOUNT;
    }

    public boolean tryManualBucket(Player player, ItemStack stack) {
        if (isCreativeSource() || pharaohGate) {
            return false;
        }
        FluidTank tank = tankForBucket(stack);
        Fluid fluid = fluidForBucket(stack);
        if (tank == null || fluid == null
                || tank.fill(new FluidStack(fluid, BUCKET_AMOUNT), IFluidHandler.FluidAction.SIMULATE)
                != BUCKET_AMOUNT) {
            return false;
        }

        tank.fill(new FluidStack(fluid, BUCKET_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            player.getInventory().placeItemBackInInventory(new ItemStack(Items.BUCKET));
        }
        return true;
    }

    public boolean insertManualBucket(Player player, ItemStack stack) {
        return tryManualBucket(player, stack);
    }

    @Nullable
    private FluidTank tankForBucket(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            return waterTank;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return lavaTank;
        }
        return null;
    }

    @Nullable
    private Fluid fluidForBucket(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            return Fluids.WATER;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return Fluids.LAVA;
        }
        return null;
    }

    public int getWaterAmount() {
        return isCreativeSource() && !pharaohGate
                ? TANK_CAPACITY : waterTank.getFluidAmount();
    }

    public int getLavaAmount() {
        return isCreativeSource() && !pharaohGate
                ? TANK_CAPACITY : lavaTank.getFluidAmount();
    }

    public int getEnergyStored() {
        if (isCreativeSource() && !pharaohGate) {
            return getEnergyCapacity();
        }
        return Math.min(energyStored, getEnergyCapacity());
    }

    public boolean isCreativeSource() {
        return getBlockState().getBlock() instanceof ZenioniteChargerBlock chargerBlock
                && chargerBlock.isCreative();
    }

    public int getEnergyCapacity() {
        return ENERGY_CAPACITY * EndlessSandsConfig.getRfMultiplier();
    }

    public boolean isPharaohGate() {
        return pharaohGate;
    }

    public void beginPharaohGateDrain() {
        if (pharaohGate) {
            return;
        }
        pharaohGate = true;
        waterTank.setFluid(FluidStack.EMPTY);
        lavaTank.setFluid(FluidStack.EMPTY);
        productionProgress = 0;
        waterCollectionProgress = 0;
        lavaCollectionProgress = 0;
        waterStairCollectionProgress = 0;
        lavaStairCollectionProgress = 0;
        transferCooldownTicks = 0;
        powerDraining = energyStored > 0;
        onStorageChanged();
    }

    public void fillEnergyToCapacity() {
        if (pharaohGate) {
            return;
        }
        if (isCreativeSource()) {
            setChanged();
            return;
        }
        energyStored = getEnergyCapacity();
        powerDraining = false;
        restartTransferCooldown();
        onStorageChanged();
    }

    public int getWaterLevel() {
        return displayLevel(getWaterAmount(), BUCKET_AMOUNT);
    }

    public int getLavaLevel() {
        return displayLevel(getLavaAmount(), BUCKET_AMOUNT);
    }

    public int getPowerLevel() {
        return displayLevel(getEnergyStored(), EndlessSandsConfig.getRfMultiplier());
    }

    public int getWaterDisplayLevel() {
        return getWaterLevel();
    }

    public int getLavaDisplayLevel() {
        return getLavaLevel();
    }

    public int getEnergyDisplayLevel() {
        return getPowerLevel();
    }

    public int getProductionProgress() {
        return productionProgress;
    }

    public int getWaterCollectionProgress() {
        return waterCollectionProgress;
    }

    public int getLavaCollectionProgress() {
        return lavaCollectionProgress;
    }

    public int getWaterStairCollectionProgress() {
        return waterStairCollectionProgress;
    }

    public int getLavaStairCollectionProgress() {
        return lavaStairCollectionProgress;
    }

    public ItemStackHandler getAutomationItemHandler() {
        return automationItems;
    }

    public ItemStackHandler getItemHandler() {
        return automationItems;
    }

    public ContainerData getData() {
        return data;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    public void dropInventory() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < automationItems.getSlots(); slot++) {
            ItemStack stack = automationItems.extractItem(slot, Integer.MAX_VALUE, false);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D,
                        stack);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ZenioniteChargerMenu(containerId, inventory, this, data);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (side != null && side.getAxis().isHorizontal()) {
            if (capability == ForgeCapabilities.FLUID_HANDLER) {
                return fluidInputCapability.cast();
            }
            if (capability == ForgeCapabilities.ITEM_HANDLER) {
                return bucketInputCapability.cast();
            }
        }

        if (side == Direction.DOWN) {
            if (capability == ForgeCapabilities.ENERGY) {
                return energyInputCapability.cast();
            }
            if (capability == ForgeCapabilities.ITEM_HANDLER) {
                return bucketOutputCapability.cast();
            }
        }

        if (side == Direction.UP && capability == ForgeCapabilities.ENERGY) {
            return energyOutputCapability.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidInputCapability.invalidate();
        bucketInputCapability.invalidate();
        bucketOutputCapability.invalidate();
        energyInputCapability.invalidate();
        energyOutputCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidInputCapability = LazyOptional.of(() -> horizontalFluidInput);
        bucketInputCapability = LazyOptional.of(() -> horizontalBucketInput);
        bucketOutputCapability = LazyOptional.of(() -> downwardBucketOutput);
        energyInputCapability = LazyOptional.of(() -> downwardEnergyInput);
        energyOutputCapability = LazyOptional.of(() -> upwardEnergyOutput);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(WATER_TANK_TAG, waterTank.writeToNBT(new CompoundTag()));
        tag.put(LAVA_TANK_TAG, lavaTank.writeToNBT(new CompoundTag()));
        tag.put(INVENTORY_TAG, automationItems.serializeNBT());
        tag.putInt(ENERGY_TAG, getEnergyStored());
        tag.putInt(PRODUCTION_PROGRESS_TAG, productionProgress);
        tag.putInt(WATER_COLLECTION_PROGRESS_TAG, waterCollectionProgress);
        tag.putInt(LAVA_COLLECTION_PROGRESS_TAG, lavaCollectionProgress);
        tag.putInt(WATER_STAIR_COLLECTION_PROGRESS_TAG, waterStairCollectionProgress);
        tag.putInt(LAVA_STAIR_COLLECTION_PROGRESS_TAG, lavaStairCollectionProgress);
        tag.putInt(TRANSFER_COOLDOWN_TAG, transferCooldownTicks);
        tag.putBoolean(PHARAOH_GATE_TAG, pharaohGate);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        try {
            loadAndClampTank(waterTank, tag.getCompound(WATER_TANK_TAG), Fluids.WATER);
            loadAndClampTank(lavaTank, tag.getCompound(LAVA_TANK_TAG), Fluids.LAVA);
            automationItems.deserializeNBT(tag.getCompound(INVENTORY_TAG));
            clampAutomationSlot(WATER_SLOT, Items.WATER_BUCKET);
            clampAutomationSlot(LAVA_SLOT, Items.LAVA_BUCKET);
            energyStored = Math.max(0, Math.min(tag.getInt(ENERGY_TAG), getEnergyCapacity()));
            productionProgress = clampProgress(tag.getInt(PRODUCTION_PROGRESS_TAG), PRODUCTION_TICKS);
            waterCollectionProgress = clampProgress(
                    tag.getInt(WATER_COLLECTION_PROGRESS_TAG), COLLECTION_TARGET);
            lavaCollectionProgress = clampProgress(
                    tag.getInt(LAVA_COLLECTION_PROGRESS_TAG), COLLECTION_TARGET);
            waterStairCollectionProgress = clampProgress(
                    tag.getInt(WATER_STAIR_COLLECTION_PROGRESS_TAG), ZENIONITE_STAIR_COLLECTION_TICKS);
            lavaStairCollectionProgress = clampProgress(
                    tag.getInt(LAVA_STAIR_COLLECTION_PROGRESS_TAG), ZENIONITE_STAIR_COLLECTION_TICKS);
            int defaultCooldown = energyStored > 0
                    ? EndlessSandsConfig.getRfTransferIntervalTicks() : 0;
            transferCooldownTicks = tag.contains(TRANSFER_COOLDOWN_TAG)
                    ? Math.max(0, Math.min(tag.getInt(TRANSFER_COOLDOWN_TAG),
                    EndlessSandsConfig.getRfTransferIntervalTicks()))
                    : defaultCooldown;
            pharaohGate = tag.getBoolean(PHARAOH_GATE_TAG);
        } finally {
            loading = false;
        }
    }

    private void onStorageChanged() {
        if (loading) {
            return;
        }
        setChanged();
        updateBlockState();
    }

    private void updateBlockState() {
        if (level == null || level.isClientSide || isRemoved() || isCreativeSource()) {
            return;
        }

        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof ZenioniteChargerBlock)) {
            return;
        }

        BlockState updated = current
                .setValue(ZenioniteChargerBlock.WATER, getWaterLevel())
                .setValue(ZenioniteChargerBlock.LAVA, getLavaLevel())
                .setValue(ZenioniteChargerBlock.POWER, getPowerLevel())
                .setValue(ZenioniteChargerBlock.POWER_DRAINING, powerDraining);
        if (updated != current) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
    }

    private void clampEnergyToCapacity() {
        int clamped = Math.max(0, Math.min(energyStored, getEnergyCapacity()));
        if (clamped != energyStored) {
            powerDraining = false;
            energyStored = clamped;
            onStorageChanged();
        }
    }

    private int getOutputAllowance() {
        if (pharaohGate || level == null || level.isClientSide || transferCooldownTicks > 0) {
            return 0;
        }
        return Math.min(getEnergyStored(), OUTPUT_PER_TRANSFER);
    }

    private void restartTransferCooldown() {
        transferCooldownTicks = getEnergyStored() > 0
                ? EndlessSandsConfig.getRfTransferIntervalTicks() : 0;
    }

    private void tickTransferCooldown() {
        int clamped = Math.min(transferCooldownTicks,
                EndlessSandsConfig.getRfTransferIntervalTicks());
        int next = Math.max(0, clamped - 1);
        if (next != transferCooldownTicks) {
            transferCooldownTicks = next;
            setChanged();
        }
    }

    private void drainPharaohGateEnergy() {
        if (energyStored <= 0) {
            if (powerDraining) {
                powerDraining = false;
                onStorageChanged();
            }
            return;
        }
        energyStored = Math.max(0,
                energyStored - Math.max(1, EndlessSandsConfig.getRfMultiplier()));
        powerDraining = energyStored > 0;
        onStorageChanged();
    }

    private static int displayLevel(int amount, int unitsPerStage) {
        if (amount <= 0) {
            return 0;
        }
        return Math.min(8, 1 + (amount - 1) / Math.max(1, unitsPerStage));
    }

    private static int clampProgress(int value, int target) {
        return Math.max(0, Math.min(value, target - 1));
    }

    private static void loadAndClampTank(FluidTank tank, CompoundTag tag, Fluid expectedFluid) {
        tank.readFromNBT(tag);
        FluidStack loaded = tank.getFluid();
        if (loaded.isEmpty() || loaded.getFluid() != expectedFluid) {
            tank.setFluid(FluidStack.EMPTY);
            return;
        }
        tank.setFluid(new FluidStack(loaded, Math.min(TANK_CAPACITY, Math.max(0, loaded.getAmount()))));
    }

    private void clampAutomationSlot(int slot, net.minecraft.world.item.Item expectedBucket) {
        ItemStack stack = automationItems.getStackInSlot(slot);
        if (!stack.is(expectedBucket) && !stack.is(Items.BUCKET)) {
            automationItems.setStackInSlot(slot, ItemStack.EMPTY);
            return;
        }
        stack.setCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
    }

    private final class FluidInputHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (isCreativeSource()) {
                return switch (tank) {
                    case WATER_SLOT -> new FluidStack(Fluids.WATER, TANK_CAPACITY);
                    case LAVA_SLOT -> new FluidStack(Fluids.LAVA, TANK_CAPACITY);
                    default -> FluidStack.EMPTY;
                };
            }
            return switch (tank) {
                case WATER_SLOT -> waterTank.getFluid().copy();
                case LAVA_SLOT -> lavaTank.getFluid().copy();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == WATER_SLOT || tank == LAVA_SLOT ? TANK_CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return !pharaohGate && (tank == WATER_SLOT && waterTank.isFluidValid(stack)
                    || tank == LAVA_SLOT && lavaTank.isFluidValid(stack));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || isCreativeSource() || pharaohGate) {
                return 0;
            }
            if (resource.getFluid() == Fluids.WATER) {
                return waterTank.fill(resource, action);
            }
            if (resource.getFluid() == Fluids.LAVA) {
                return lavaTank.fill(resource, action);
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (isCreativeSource() && !resource.isEmpty()
                    && (resource.getFluid() == Fluids.WATER
                    || resource.getFluid() == Fluids.LAVA)) {
                return new FluidStack(resource.getFluid(),
                        Math.min(resource.getAmount(), TANK_CAPACITY));
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (isCreativeSource() && maxDrain > 0) {
                return new FluidStack(Fluids.WATER, Math.min(maxDrain, TANK_CAPACITY));
            }
            return FluidStack.EMPTY;
        }
    }

    private final class BucketInputHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return automationItems.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return automationItems.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return automationItems.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return automationItems.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return automationItems.isItemValid(slot, stack);
        }
    }

    private final class EmptyBucketOutputHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return automationItems.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return automationItems.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stored = automationItems.getStackInSlot(slot);
            if (!stored.is(Items.BUCKET)) {
                return ItemStack.EMPTY;
            }
            return automationItems.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return automationItems.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }

    private final class EnergyInputHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (isCreativeSource() || pharaohGate) {
                return 0;
            }
            int availableSpace = Math.max(0, getEnergyCapacity() - energyStored);
            int received = Math.min(Math.max(0, maxReceive), availableSpace);
            if (!simulate && received > 0) {
                powerDraining = false;
                boolean wasEmpty = energyStored == 0;
                energyStored += received;
                if (wasEmpty) {
                    restartTransferCooldown();
                }
                onStorageChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return ZenioniteChargerBlockEntity.this.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return getEnergyCapacity();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return !pharaohGate;
        }
    }

    private final class EnergyOutputHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (pharaohGate) {
                return 0;
            }
            int extracted = Math.min(Math.max(0, maxExtract), getOutputAllowance());
            if (!simulate && extracted > 0) {
                if (!isCreativeSource()) {
                    powerDraining = true;
                    energyStored -= extracted;
                }
                restartTransferCooldown();
                if (isCreativeSource()) {
                    setChanged();
                } else {
                    onStorageChanged();
                }
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return ZenioniteChargerBlockEntity.this.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return getEnergyCapacity();
        }

        @Override
        public boolean canExtract() {
            return !pharaohGate;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
