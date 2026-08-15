package net.MechGaming.EndlessSands.entity.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.VultureNestBlock;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.item.custom.ArmGuardItem;
import net.MechGaming.EndlessSands.item.custom.ModFoods;
import net.MechGaming.EndlessSands.sound.ModSounds;
import net.MechGaming.EndlessSands.util.ModTags;
import net.MechGaming.EndlessSands.worldgen.VultureHomeSavedData;
import net.MechGaming.EndlessSands.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VultureEntity extends TamableAnimal {
    private static final EntityDataAccessor<Integer> ANIMATION = SynchedEntityData.defineId(VultureEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> MOUTH_STACK = SynchedEntityData.defineId(VultureEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> LEFT_TALON_STACK = SynchedEntityData.defineId(VultureEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> RIGHT_TALON_STACK = SynchedEntityData.defineId(VultureEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> ARM_PERCHED = SynchedEntityData.defineId(VultureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> ARM_PERCH_PLAYER = SynchedEntityData.defineId(VultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final String HOME_X = "HomeX";
    private static final String HOME_Y = "HomeY";
    private static final String HOME_Z = "HomeZ";
    private static final String HOME_ID = "HomeId";
    private static final String HOME_SETTLED = "HomeSettled";
    private static final String ADULT_HOME_RESIDENT = "AdultHomeResident";
    private static final String DOMESTICATED = "Domesticated";
    private static final String RETURNING_TWIG = "ReturningTwig";
    private static final String REJECTED_HOME_ID = "RejectedHomeId";
    private static final String ISLAND_X = "IslandX";
    private static final String ISLAND_Y = "IslandY";
    private static final String ISLAND_Z = "IslandZ";
    private static final String CRUD_TARGET_X = "CrudTargetX";
    private static final String CRUD_TARGET_Y = "CrudTargetY";
    private static final String CRUD_TARGET_Z = "CrudTargetZ";
    private static final String BRAIN_STATE = "BrainState";
    private static final String PERCH_X = "PerchX";
    private static final String PERCH_Y = "PerchY";
    private static final String PERCH_Z = "PerchZ";
    private static final String ARM_PLAYER = "ArmPlayer";
    private static final String BABY_PLAYER_TICKS = "BabyPlayerTicks";
    private static final String BABY_FOLLOW_PLAYER = "BabyFollowPlayer";
    private static final String HURT_NIGHT_TICKS = "HurtNightTicks";
    private static final String DAY_SPAWNED = "DaySpawned";
    private static final String COURIER_PLAYER = "CourierPlayer";
    private static final String COURIER_DELAY = "CourierDelay";
    private static final String MOUTH_ITEM = "MouthItem";
    private static final String LEFT_TALON_ITEM = "LeftTalonItem";
    private static final String RIGHT_TALON_ITEM = "RightTalonItem";
    private static final String MEAT_THEFT_PLAYER = "MeatTheftPlayer";
    private static final String MEAT_THEFT_FLEE_TICKS = "MeatTheftFleeTicks";
    private static final String REJECTED_TREE_COOLDOWN = "RejectedTreeCooldown";
    private static final String GUARD_COMMAND = "GuardCommand";
    private static final String GUARD_COMMAND_PLAYER = "GuardCommandPlayer";
    private static final String GUARD_TARGET_X = "GuardTargetX";
    private static final String GUARD_TARGET_Y = "GuardTargetY";
    private static final String GUARD_TARGET_Z = "GuardTargetZ";
    private static final String KNOWN_HOME_X = "KnownHomeX";
    private static final String KNOWN_HOME_Y = "KnownHomeY";
    private static final String KNOWN_HOME_Z = "KnownHomeZ";
    private static final String GUARD_DISMOUNTED = "GuardDismounted";
    private static final String TAMING_PROGRESS = "TamingProgress";

    public static final int BABY_GROW_TICKS = 20 * 60 * 60;
    private static final double MAX_HEALTH = 3.0D;
    private static final int PLAYER_FOLLOW_TAME_TICKS = 20 * 60 * 30;
    private static final int SCAN_INTERVAL = 30;
    private static final int LANDMARK_CHECK_INTERVAL = 100;
    private static final int REJECTED_TREE_COOLDOWN_TICKS = 20 * 60;
    private static final int HOME_RADIUS_CHUNKS = 3;
    private static final int TAKEOFF_PERCHED_FLAP_TICKS = 20;
    private static final int RAISE_LANDING_GEAR_TICKS = 60;
    private static final int DEPLOY_LANDING_GEAR_TICKS = 21;
    private static final int EATING_TICKS = 100;
    private static final int EATING_SOUND_DELAY_TICKS = 20;
    private static final float EATING_SOUND_VOLUME = 2.0F;
    private static final int ARM_IDLE_ANIMATION_TICKS = 46;
    private static final int ARM_IDLE_INTERVAL_TICKS = 80;
    private static final int ARM_CONTINUOUS_IDLE_DELAY_TICKS = 20 * 10;
    private static final int MEAT_THEFT_FLEE_DURATION_TICKS = 20 * 10;
    private static final int ARM_DISMOUNT_COOLDOWN_TICKS = 20 * 5;
    private static final double FOLLOW_TELEPORT_DISTANCE = 32.0D;
    private static final double FLY_SPEED = 0.32D;
    private static final double COURIER_BLOCKS_PER_SECOND = 16.0D;
    private static final double ARM_PERCH_SIDE_OFFSET = 0.34D;
    private static final double ARM_PERCH_FORWARD_OFFSET = 0.62D;
    private static final double ARM_PERCH_TOP_OFFSET = 0.06D;
    private static final double ARM_APPROACH_HITBOX_RADIUS = 0.24D;
    private static final float VULTURE_JERKY_TAME_CHANCE = 1.0F / 6.0F;
    private static final float TAME_CHANCE_PER_SATURATION =
            VULTURE_JERKY_TAME_CHANCE / ModFoods.VULTURE_JERKY_SATURATION;

    public final AnimationState idlePerchedAnimationState = new AnimationState();
    public final AnimationState flapWingsAnimationState = new AnimationState();
    public final AnimationState walkingAnimationState = new AnimationState();
    public final AnimationState eatingOffGroundAnimationState = new AnimationState();
    public final AnimationState raiseLandingGearAnimationState = new AnimationState();
    public final AnimationState soaringAnimationState = new AnimationState();
    public final AnimationState risingAnimationState = new AnimationState();
    public final AnimationState deployLandingGearAnimationState = new AnimationState();
    public final AnimationState fallingAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();

    private final EnumMap<VultureAnimation, AnimationState> animationStates = new EnumMap<>(VultureAnimation.class);

    @Nullable
    private BlockPos homeNestPos;
    @Nullable
    private UUID homeId;
    @Nullable
    private UUID rejectedHomeId;
    @Nullable
    private BlockPos islandTarget;
    @Nullable
    private BlockPos crudTreeTarget;
    @Nullable
    private BlockPos perchPos;
    @Nullable
    private BlockPos guardCommandTarget;
    @Nullable
    private BlockPos knownHomePos;
    @Nullable
    private Vec3 flyingTarget;
    @Nullable
    private UUID courierPlayer;
    @Nullable
    private UUID babyFollowPlayer;
    @Nullable
    private UUID meatTheftPlayer;
    @Nullable
    private UUID guardCommandPlayer;
    @Nullable
    private VultureHomeSavedData.HomeSnapshot cachedHome;

    private BrainState brainState = BrainState.FLYING;
    private int scanCooldown;
    private int actionTicks;
    private int targetItemId = -1;
    private int babyPlayerFollowTicks;
    private int hurtNightTicks;
    private int courierDelayTicks;
    private int descentTransitionTicks;
    private int armIdleAnimationTicks;
    private int armIdleCooldownTicks;
    private int armPlayerStillTicks;
    private int meatTheftFleeTicks;
    private int landmarkCheckCooldown;
    private int islandSearchCooldown;
    private int rejectedTreeCooldown;
    private int armPerchCooldownTicks;
    private long lastLandmarkChunk = Long.MIN_VALUE;
    private boolean daySpawned;
    private boolean homeSettled;
    private boolean adultHomeResident;
    private boolean domesticated;
    private boolean returningTwig;
    private boolean wasBlinded;
    private boolean legacyHomeLink;
    private boolean guardDismounted;
    private float tamingProgress;
    private GuardCommand guardCommand = GuardCommand.NONE;
    private Vec3 takeoffDirection = Vec3.ZERO;
    public VultureEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.animationStates.put(VultureAnimation.IDLE_PERCHED, this.idlePerchedAnimationState);
        this.animationStates.put(VultureAnimation.FLAP_WINGS, this.flapWingsAnimationState);
        this.animationStates.put(VultureAnimation.WALKING, this.walkingAnimationState);
        this.animationStates.put(VultureAnimation.EATING_OFF_GROUND, this.eatingOffGroundAnimationState);
        this.animationStates.put(VultureAnimation.RAISE_LANDING_GEAR, this.raiseLandingGearAnimationState);
        this.animationStates.put(VultureAnimation.SOARING, this.soaringAnimationState);
        this.animationStates.put(VultureAnimation.RISING, this.risingAnimationState);
        this.animationStates.put(VultureAnimation.DEPLOY_LANDING_GEAR, this.deployLandingGearAnimationState);
        this.animationStates.put(VultureAnimation.FALLING, this.fallingAnimationState);
        this.animationStates.put(VultureAnimation.LANDING, this.landingAnimationState);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, FLY_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    public static boolean checkVultureSpawnRules(EntityType<VultureEntity> type, ServerLevelAccessor level,
                                                  MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockPos support = pos.below();
        return level.getBiome(pos).is(ModBiomes.ENDLESS_DESERT)
                && level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)
                && level.getRawBrightness(pos, 0) > 8;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VULTURE_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 80;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VULTURE_HURT.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION, VultureAnimation.SOARING.ordinal());
        this.entityData.define(MOUTH_STACK, ItemStack.EMPTY);
        this.entityData.define(LEFT_TALON_STACK, ItemStack.EMPTY);
        this.entityData.define(RIGHT_TALON_STACK, ItemStack.EMPTY);
        this.entityData.define(ARM_PERCHED, false);
        this.entityData.define(ARM_PERCH_PLAYER, Optional.empty());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ANIMATION.equals(key) && this.level().isClientSide) {
            this.refreshAnimationStates();
        }
    }

    @Override
    public void tick() {
        super.tick();
        UUID armPerchPlayer = getArmPerchPlayerUUID();
        if (!this.level().isClientSide && armPerchPlayer != null && this.isArmPerched()) {
            Player player = findPlayerByUuid(armPerchPlayer);
            if (player != null) {
                lockToArmGuard(player);
            }
        }
        if (this.level().isClientSide) {
            this.refreshAnimationStates();
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.hasEffect(MobEffects.BLINDNESS)) {
            tickBlinded();
            return;
        }
        if (this.wasBlinded) {
            this.wasBlinded = false;
            if (!this.isBaby()) {
                takeOff();
                return;
            }
        }

        if (!this.isBaby() && !this.isTame() && this.babyPlayerFollowTicks >= PLAYER_FOLLOW_TAME_TICKS && this.babyFollowPlayer != null) {
            Player followedPlayer = findPlayerByUuid(this.babyFollowPlayer);
            if (followedPlayer != null) {
                this.tame(followedPlayer);
            }
        }

        if (this.isBaby()) {
            tickBabyBrain();
            return;
        }

        if (this.armPerchCooldownTicks > 0) {
            this.armPerchCooldownTicks--;
        }

        if (this.guardCommand != GuardCommand.NONE && tickGuardCommand()) {
            return;
        }

        if (this.guardDismounted) {
            tickDomesticatedGround();
            return;
        }

        refreshLandmarkStateIfNeeded();

        if (this.courierDelayTicks > 0) {
            this.courierDelayTicks--;
            if (this.courierDelayTicks == 0) {
                placeCourierAtRenderEdge();
            } else {
                setAnimation(VultureAnimation.SOARING);
                return;
            }
        }

        if (this.brainState == BrainState.DEATH_COURIER) {
            tickDeathCourier();
            return;
        }

        if (this.hurtNightTicks > 0) {
            this.hurtNightTicks--;
        }
        if (this.rejectedTreeCooldown > 0) {
            this.rejectedTreeCooldown--;
        }

        if (this.brainState != BrainState.TAKEOFF && getArmPerchPlayerUUID() != null) {
            boolean active = this.brainState == BrainState.LANDING_TO_ARM
                    ? tickLandingToArm()
                    : tickArmPerch();
            if (active) {
                return;
            }
            takeOff();
        }

        if (tickHomePriority()) {
            return;
        }

        if (tickRejectedTreeDeparture()) {
            return;
        }

        if (canScanForImmediateTargets() && --this.scanCooldown <= 0) {
            this.scanCooldown = SCAN_INTERVAL;
            if (scanForImmediateTargets()) {
                return;
            }
        }

        switch (this.brainState) {
            case TAKEOFF -> tickTakeoff();
            case FLYING -> tickFlying();
            case LANDING_TO_LOG -> tickLandingToLog();
            case PERCHED_LOG -> tickPerchedLog();
            case LANDING_TO_ITEM -> tickLandingToItem();
            case WALKING_TO_ITEM -> tickWalkingToItem();
            case EATING_ITEM -> tickEatingItem();
            case FLEEING_NIGHT -> tickNightFleeing();
            case FOLLOWING_OWNER -> tickFollowingOwner();
            case LANDING_TO_MEAT_PLAYER -> tickLandingToMeatPlayer();
            case FLEEING_WITH_MEAT -> tickMeatTheftFleeing();
            case DOMESTICATED_GROUND -> tickDomesticatedGround();
            default -> takeOff();
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isFlyingState()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level().isClientSide) {
                double maxY = getMaximumFlightY(this.getX(), this.getZ());
                if (this.getY() > maxY) {
                    this.setPos(this.getX(), maxY, this.getZ());
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
                }
            }
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92D));
            this.calculateEntityAnimation(false);
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 6);
        }
        if (hurt && !this.level().isDay() && !this.isTame()) {
            this.hurtNightTicks = 20 * 60 * 10;
        }
        if (hurt && this.hasEffect(MobEffects.BLINDNESS)) {
            this.wasBlinded = true;
            releaseTakeoffPerch();
            this.brainState = BrainState.DOMESTICATED_GROUND;
            this.setNoGravity(false);
        } else if (hurt && this.isTame()) {
            this.guardDismounted = false;
            setArmPerchPlayerUUID(null);
            this.entityData.set(ARM_PERCHED, false);
            this.brainState = BrainState.FOLLOWING_OWNER;
            this.setNoGravity(true);
        } else if (hurt && !this.level().isDay()) {
            takeOff();
        }
        return hurt;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        this.fallDistance = 0.0F;
        return false;
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.homeId != null && this.adultHomeResident
                && this.level() instanceof ServerLevel serverLevel) {
            VultureHomeSavedData.get(serverLevel).removeAdultResident(this.homeId, this.getUUID());
        }
        super.die(source);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.homeId == null && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
        if (spawnType == MobSpawnType.STRUCTURE || spawnType == MobSpawnType.NATURAL) {
            this.setNoGravity(true);
            setAnimation(VultureAnimation.SOARING);
        }
        if (spawnType == MobSpawnType.NATURAL) {
            this.daySpawned = this.level().isDay();
        }
        return data;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        VultureEntity baby = ModEntities.VULTURE.get().create(level);
        if (baby != null) {
            baby.setAge(-BABY_GROW_TICKS);
            UUID ownerUuid = this.getOwnerUUID();
            if (this.isTame() && ownerUuid != null) {
                baby.setTame(true);
                baby.setOwnerUUID(ownerUuid);
            }
            if (!baby.isTame()) {
                baby.inheritNaturalHome(this);
                if (baby.homeId == null && otherParent instanceof VultureEntity otherVulture) {
                    baby.inheritNaturalHome(otherVulture);
                }
            }
        }
        return baby;
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        if (this.level().isClientSide || this.isBaby() || this.homeId == null
                || this.adultHomeResident || this.isTame()
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        VultureHomeSavedData homes = VultureHomeSavedData.get(serverLevel);
        Optional<VultureHomeSavedData.HomeSnapshot> home = homes.getHome(serverLevel, this.homeId);
        if (home.isPresent() && home.get().treeIntact()
                && homes.addAdultResident(this.homeId, this.getUUID())) {
            this.adultHomeResident = true;
            this.homeSettled = true;
            this.cachedHome = homes.getHome(serverLevel, this.homeId).orElse(home.get());
            return;
        }

        rejectCurrentHome(homes);
        takeOff();
    }

    @Override
    public void tame(Player player) {
        if (!this.level().isClientSide && this.homeId != null && !this.domesticated
                && this.level() instanceof ServerLevel serverLevel) {
            if (this.homeNestPos != null) {
                this.knownHomePos = this.homeNestPos.immutable();
            }
            rejectCurrentHome(VultureHomeSavedData.get(serverLevel));
            this.rejectedHomeId = null;
            this.rejectedTreeCooldown = 0;
        }
        this.tamingProgress = 0.0F;
        super.tame(player);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return isMeat(stack);
    }

    public static boolean isMeat(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Items.IS_MEAT);
    }

    public static boolean hasAnyArmGuard(Player player) {
        return isArmGuard(player.getOffhandItem()) || isCreativeArmGuard(player.getOffhandItem());
    }

    public static boolean isArmGuard(ItemStack stack) {
        return stack.is(ModItems.ARM_GUARD.get());
    }

    public static boolean isCreativeArmGuard(ItemStack stack) {
        return stack.is(ModItems.CREATIVE_ARM_GUARD.get());
    }

    public VultureAnimation getVultureAnimation() {
        int index = Mth.clamp(this.entityData.get(ANIMATION), 0, VultureAnimation.values().length - 1);
        return VultureAnimation.values()[index];
    }

    public boolean isArmPerched() {
        return this.entityData.get(ARM_PERCHED);
    }

    @Nullable
    public UUID getArmPerchPlayerUUID() {
        return this.entityData.get(ARM_PERCH_PLAYER).orElse(null);
    }

    @Nullable
    public Player getArmPerchPlayer() {
        return findPlayerByUuid(getArmPerchPlayerUUID());
    }

    @Nullable
    public static VultureEntity findArmPerchedVulture(ServerPlayer player, boolean requireTame) {
        UUID playerId = player.getUUID();
        return player.serverLevel().getEntitiesOfClass(VultureEntity.class,
                        player.getBoundingBox().inflate(8.0D),
                        vulture -> vulture.isAlive()
                                && vulture.isArmPerched()
                                && playerId.equals(vulture.getArmPerchPlayerUUID())
                                && (!requireTame || vulture.isTame()))
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    @Nullable
    public static VultureEntity findControlledVulture(ServerPlayer player) {
        VultureEntity perched = findArmPerchedVulture(player, true);
        if (perched != null && perched.canReceiveGuardCommand(player)) {
            ArmGuardItem.linkVulture(player.getOffhandItem(), perched);
            return perched;
        }

        UUID controlledId = ArmGuardItem.getControlledVulture(player.getOffhandItem());
        Entity controlled = controlledId == null ? null : player.serverLevel().getEntity(controlledId);
        if (controlled instanceof VultureEntity vulture && vulture.canReceiveGuardCommand(player)) {
            return vulture;
        }
        return null;
    }

    public boolean canReceiveGuardCommand(Player player) {
        if (!this.isAlive() || this.isBaby() || !this.isTame()
                || !player.getUUID().equals(this.getOwnerUUID())
                || !hasAnyArmGuard(player)
                || !ArmGuardItem.matchesBinding(player.getOffhandItem(), this)) {
            return false;
        }
        UUID controlledId = ArmGuardItem.getControlledVulture(player.getOffhandItem());
        return this.getUUID().equals(controlledId)
                || player.getUUID().equals(getArmPerchPlayerUUID());
    }

    private void setArmPerchPlayerUUID(@Nullable UUID playerUUID) {
        this.entityData.set(ARM_PERCH_PLAYER, Optional.ofNullable(playerUUID));
    }

    public ItemStack getMouthStack() {
        return this.entityData.get(MOUTH_STACK);
    }

    public ItemStack getLeftTalonStack() {
        return this.entityData.get(LEFT_TALON_STACK);
    }

    public ItemStack getRightTalonStack() {
        return this.entityData.get(RIGHT_TALON_STACK);
    }

    public boolean commandEatFrom(Player player) {
        ItemStack foodStack = player.getMainHandItem();
        if (!this.isArmPerched()
                || !player.getUUID().equals(getArmPerchPlayerUUID())
                || !isMeat(foodStack)) {
            return false;
        }
        FoodProperties food = foodStack.getFoodProperties(this);
        if (food == null) {
            return false;
        }

        float saturation = food.getNutrition() * food.getSaturationModifier() * 2.0F;
        this.heal(Math.max(0.5F, food.getNutrition() * 0.5F + saturation));
        this.entityData.set(MOUTH_STACK, foodStack.copyWithCount(1));
        this.actionTicks = EATING_TICKS;
        if (!player.getAbilities().instabuild) {
            foodStack.shrink(1);
        }
        this.playSound(ModSounds.VULTURE_EAT.get(), EATING_SOUND_VOLUME,
                0.95F + this.random.nextFloat() * 0.1F);
        return true;
    }

    public boolean commandFollow(ServerPlayer player) {
        if (!isOwnedBy(player)) {
            return false;
        }
        startGuardCommand(GuardCommand.FOLLOW, player, null);
        return true;
    }

    public boolean commandReturnToGuard(ServerPlayer player) {
        if (!isOwnedBy(player)
                || !hasAnyArmGuard(player)
                || !ArmGuardItem.matchesBinding(player.getOffhandItem(), this)
                || isArmGuardClaimedByAnother(player)) {
            return false;
        }

        clearGuardCommand();
        this.getNavigation().stop();
        this.setNoGravity(true);
        setArmPerchPlayerUUID(player.getUUID());
        this.entityData.set(ARM_PERCHED, true);
        this.brainState = BrainState.PERCHED_ARM;
        this.armPerchCooldownTicks = 0;
        this.guardDismounted = false;
        this.armIdleAnimationTicks = 0;
        this.armIdleCooldownTicks = ARM_IDLE_INTERVAL_TICKS;
        this.armPlayerStillTicks = 0;
        lockToArmGuard(player);
        setAnimation(VultureAnimation.STILL_PERCHED);
        ArmGuardItem.linkVulture(player.getOffhandItem(), this);
        return true;
    }

    public boolean commandSearch(ServerPlayer player, ItemStack locator) {
        if (!isOwnedBy(player)) {
            return false;
        }

        GuardCommand command;
        BlockPos target;
        if (locator.isEmpty()) {
            command = GuardCommand.SEARCH_REMAINS_OF_A_VILLAGE;
            target = player.serverLevel().findNearestMapStructure(
                    ModTags.Structures.REMAINS_OF_A_VILLAGE, this.blockPosition(), 256, false);
        } else if (locator.is(ModBlocks.CRUD_LOG.get().asItem())) {
            command = GuardCommand.SEARCH_CRUD_TREE;
            target = player.serverLevel().findNearestMapStructure(
                    ModTags.Structures.CRUD_TREES, this.blockPosition(), 256, false);
        } else if (locator.is(ModBlocks.TWIG.get().asItem())) {
            command = GuardCommand.SEARCH_FLOATING_ISLAND;
            target = player.serverLevel().findNearestMapStructure(
                    ModTags.Structures.FLOATING_ISLANDS, this.blockPosition(), 256, false);
        } else {
            return false;
        }

        if (target == null) {
            return false;
        }
        startGuardCommand(command, player, target);
        return true;
    }

    public boolean commandFlyHome(ServerPlayer player) {
        if (!isOwnedBy(player)) {
            return false;
        }
        BlockPos target = this.knownHomePos != null ? this.knownHomePos : this.homeNestPos;
        if (target == null) {
            return false;
        }
        startGuardCommand(GuardCommand.FLY_HOME, player, target);
        return true;
    }

    public boolean hasKnownHome() {
        return this.knownHomePos != null || this.homeNestPos != null;
    }

    public void commandDismountFromGuard(ServerPlayer player) {
        if (!player.getUUID().equals(getArmPerchPlayerUUID())) {
            return;
        }

        if (isOwnedBy(player)) {
            ArmGuardItem.linkVulture(player.getOffhandItem(), this);
            this.guardDismounted = true;
        }
        clearGuardCommand();
        releaseTakeoffPerch();
        this.armPerchCooldownTicks = ARM_DISMOUNT_COOLDOWN_TICKS;
        this.brainState = BrainState.DOMESTICATED_GROUND;
        this.setNoGravity(false);
        this.setDeltaMovement(0.0D, -0.12D, 0.0D);
        this.getNavigation().stop();
        setAnimation(VultureAnimation.FALLING);
    }

    private boolean isOwnedBy(Player player) {
        return this.isAlive() && this.isTame() && player.getUUID().equals(this.getOwnerUUID());
    }

    private void startGuardCommand(GuardCommand command, ServerPlayer player, @Nullable BlockPos target) {
        this.guardCommand = command;
        this.guardCommandPlayer = player.getUUID();
        this.guardCommandTarget = target == null ? null : target.immutable();
        this.armPerchCooldownTicks = ARM_DISMOUNT_COOLDOWN_TICKS;
        this.guardDismounted = false;
        ArmGuardItem.linkVulture(player.getOffhandItem(), this);

        if (this.isArmPerched() || this.brainState == BrainState.PERCHED_ARM) {
            takeOff();
        } else {
            releaseTakeoffPerch();
            this.setNoGravity(true);
            this.brainState = BrainState.FLYING;
            setAnimation(VultureAnimation.SOARING);
        }
    }

    private void clearGuardCommand() {
        this.guardCommand = GuardCommand.NONE;
        this.guardCommandPlayer = null;
        this.guardCommandTarget = null;
    }

    private boolean tickGuardCommand() {
        Player player = findPlayerByUuid(this.guardCommandPlayer);
        if (!(player instanceof ServerPlayer serverPlayer) || !isOwnedBy(serverPlayer)) {
            clearGuardCommand();
            return false;
        }

        if (this.brainState == BrainState.TAKEOFF) {
            tickTakeoff();
            return true;
        }

        return switch (this.guardCommand) {
            case FOLLOW -> {
                tickCommandFollow(serverPlayer);
                yield true;
            }
            case SEARCH_CRUD_TREE, SEARCH_FLOATING_ISLAND, SEARCH_REMAINS_OF_A_VILLAGE, FLY_HOME -> {
                tickCommandDestination();
                yield true;
            }
            case NONE -> false;
        };
    }

    private void tickCommandFollow(ServerPlayer player) {
        this.setNoGravity(true);
        this.brainState = BrainState.FLYING;
        if (this.distanceToSqr(player) > FOLLOW_TELEPORT_DISTANCE * FOLLOW_TELEPORT_DISTANCE) {
            tryTeleportNear(player);
        }
        this.flyingTarget = player.position().add(0.0D, 3.0D, 0.0D);
        flyToward(this.flyingTarget);
    }

    private void tickCommandDestination() {
        if (this.guardCommandTarget == null) {
            clearGuardCommand();
            return;
        }

        this.setNoGravity(true);
        this.brainState = BrainState.FLYING;
        ChunkPos currentChunk = new ChunkPos(this.blockPosition());
        ChunkPos targetChunk = new ChunkPos(this.guardCommandTarget);
        if (currentChunk.equals(targetChunk)
                || horizontalDistanceToSqr(Vec3.atCenterOf(this.guardCommandTarget)) < 16.0D * 16.0D) {
            GuardCommand finished = this.guardCommand;
            BlockPos destination = this.guardCommandTarget;
            clearGuardCommand();
            this.flyingTarget = null;

            if (finished == GuardCommand.FLY_HOME || finished == GuardCommand.SEARCH_CRUD_TREE) {
                BlockPos log = findCrudLogNear(destination, 1);
                if (log != null) {
                    this.perchPos = log.above();
                    this.brainState = BrainState.LANDING_TO_LOG;
                    setAnimation(VultureAnimation.LANDING);
                    return;
                }
            }
            chooseRoamingFlightTarget();
            return;
        }
        flyWithWanderingBias(this.guardCommandTarget);
    }

    private boolean tryTeleportNear(ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = origin.getX() + this.random.nextInt(9) - 4;
            int y = origin.getY() + this.random.nextInt(5) - 1;
            int z = origin.getZ() + this.random.nextInt(9) - 4;
            BlockPos candidate = new BlockPos(x, y, z);
            Vec3 destination = Vec3.atBottomCenterOf(candidate);
            if (player.serverLevel().getFluidState(candidate).isEmpty()
                    && player.serverLevel().noCollision(this,
                    this.getBoundingBox().move(destination.subtract(this.position())))) {
                this.teleportTo(destination.x, destination.y, destination.z);
                this.setDeltaMovement(Vec3.ZERO);
                return true;
            }
        }
        return false;
    }

    private double horizontalDistanceToSqr(Vec3 position) {
        double x = this.getX() - position.x;
        double z = this.getZ() - position.z;
        return x * x + z * z;
    }

    public void setHomeNestPos(@Nullable BlockPos homeNestPos) {
        this.homeNestPos = homeNestPos;
    }

    public void setNaturalHome(UUID homeId, BlockPos homeNestPos) {
        this.homeId = homeId;
        this.homeNestPos = homeNestPos.immutable();
        this.knownHomePos = homeNestPos.immutable();
        this.homeSettled = true;
        this.adultHomeResident = true;
        this.domesticated = false;
    }

    private void inheritNaturalHome(VultureEntity parent) {
        if (parent.homeId == null || parent.domesticated || parent.homeNestPos == null) {
            return;
        }
        this.homeId = parent.homeId;
        this.homeNestPos = parent.homeNestPos.immutable();
        this.knownHomePos = parent.homeNestPos.immutable();
        this.homeSettled = true;
        this.adultHomeResident = false;
        this.domesticated = false;
    }

    public void markDaySpawned() {
        this.daySpawned = true;
    }

    public void beginDeathCourier(ServerPlayer owner, List<ItemStack> stacks) {
        this.courierPlayer = owner.getUUID();
        this.brainState = BrainState.DEATH_COURIER;
        this.entityData.set(MOUTH_STACK, stacks.size() > 0 ? stacks.get(0).copy() : ItemStack.EMPTY);
        this.entityData.set(LEFT_TALON_STACK, stacks.size() > 1 ? stacks.get(1).copy() : ItemStack.EMPTY);
        this.entityData.set(RIGHT_TALON_STACK, stacks.size() > 2 ? stacks.get(2).copy() : ItemStack.EMPTY);

        int renderDistance = getServerRenderDistanceBlocks(owner);
        double distance = this.distanceTo(owner);
        int delaySeconds = Math.max(0, Mth.ceil((distance - renderDistance) / COURIER_BLOCKS_PER_SECOND));
        this.courierDelayTicks = delaySeconds * 20;
        if (this.courierDelayTicks <= 0) {
            placeCourierAtRenderEdge();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.homeNestPos != null) {
            tag.putInt(HOME_X, this.homeNestPos.getX());
            tag.putInt(HOME_Y, this.homeNestPos.getY());
            tag.putInt(HOME_Z, this.homeNestPos.getZ());
        }
        if (this.homeId != null) {
            tag.putUUID(HOME_ID, this.homeId);
        }
        if (this.rejectedHomeId != null) {
            tag.putUUID(REJECTED_HOME_ID, this.rejectedHomeId);
        }
        if (this.islandTarget != null) {
            tag.putInt(ISLAND_X, this.islandTarget.getX());
            tag.putInt(ISLAND_Y, this.islandTarget.getY());
            tag.putInt(ISLAND_Z, this.islandTarget.getZ());
        }
        if (this.crudTreeTarget != null) {
            tag.putInt(CRUD_TARGET_X, this.crudTreeTarget.getX());
            tag.putInt(CRUD_TARGET_Y, this.crudTreeTarget.getY());
            tag.putInt(CRUD_TARGET_Z, this.crudTreeTarget.getZ());
        }
        if (this.perchPos != null) {
            tag.putInt(PERCH_X, this.perchPos.getX());
            tag.putInt(PERCH_Y, this.perchPos.getY());
            tag.putInt(PERCH_Z, this.perchPos.getZ());
        }
        UUID armPerchPlayer = getArmPerchPlayerUUID();
        if (armPerchPlayer != null) {
            tag.putUUID(ARM_PLAYER, armPerchPlayer);
        }
        if (this.courierPlayer != null) {
            tag.putUUID(COURIER_PLAYER, this.courierPlayer);
        }
        if (this.babyFollowPlayer != null) {
            tag.putUUID(BABY_FOLLOW_PLAYER, this.babyFollowPlayer);
        }
        if (this.meatTheftPlayer != null) {
            tag.putUUID(MEAT_THEFT_PLAYER, this.meatTheftPlayer);
        }
        if (this.guardCommandPlayer != null) {
            tag.putUUID(GUARD_COMMAND_PLAYER, this.guardCommandPlayer);
        }
        if (this.guardCommandTarget != null) {
            tag.putInt(GUARD_TARGET_X, this.guardCommandTarget.getX());
            tag.putInt(GUARD_TARGET_Y, this.guardCommandTarget.getY());
            tag.putInt(GUARD_TARGET_Z, this.guardCommandTarget.getZ());
        }
        if (this.knownHomePos != null) {
            tag.putInt(KNOWN_HOME_X, this.knownHomePos.getX());
            tag.putInt(KNOWN_HOME_Y, this.knownHomePos.getY());
            tag.putInt(KNOWN_HOME_Z, this.knownHomePos.getZ());
        }
        tag.putInt(BRAIN_STATE, this.brainState.ordinal());
        tag.putInt(BABY_PLAYER_TICKS, this.babyPlayerFollowTicks);
        tag.putInt(HURT_NIGHT_TICKS, this.hurtNightTicks);
        tag.putInt(COURIER_DELAY, this.courierDelayTicks);
        tag.putInt(MEAT_THEFT_FLEE_TICKS, this.meatTheftFleeTicks);
        tag.putInt(REJECTED_TREE_COOLDOWN, this.rejectedTreeCooldown);
        tag.putInt(GUARD_COMMAND, this.guardCommand.ordinal());
        tag.putBoolean(DAY_SPAWNED, this.daySpawned);
        tag.putBoolean(HOME_SETTLED, this.homeSettled);
        tag.putBoolean(ADULT_HOME_RESIDENT, this.adultHomeResident);
        tag.putBoolean(DOMESTICATED, this.domesticated);
        tag.putBoolean(RETURNING_TWIG, this.returningTwig);
        tag.putBoolean(GUARD_DISMOUNTED, this.guardDismounted);
        tag.putFloat(TAMING_PROGRESS, this.tamingProgress);
        tag.put(MOUTH_ITEM, this.getMouthStack().save(new CompoundTag()));
        tag.put(LEFT_TALON_ITEM, this.getLeftTalonStack().save(new CompoundTag()));
        tag.put(RIGHT_TALON_ITEM, this.getRightTalonStack().save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HEALTH);
            this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));
        }
        if (tag.contains(HOME_X)) {
            this.homeNestPos = new BlockPos(tag.getInt(HOME_X), tag.getInt(HOME_Y), tag.getInt(HOME_Z));
        }
        this.homeId = tag.hasUUID(HOME_ID) ? tag.getUUID(HOME_ID) : null;
        this.rejectedHomeId = tag.hasUUID(REJECTED_HOME_ID) ? tag.getUUID(REJECTED_HOME_ID) : null;
        if (tag.contains(ISLAND_X)) {
            this.islandTarget = new BlockPos(tag.getInt(ISLAND_X), tag.getInt(ISLAND_Y), tag.getInt(ISLAND_Z));
        }
        if (tag.contains(CRUD_TARGET_X)) {
            this.crudTreeTarget = new BlockPos(tag.getInt(CRUD_TARGET_X), tag.getInt(CRUD_TARGET_Y),
                    tag.getInt(CRUD_TARGET_Z));
        }
        if (tag.contains(PERCH_X)) {
            this.perchPos = new BlockPos(tag.getInt(PERCH_X), tag.getInt(PERCH_Y), tag.getInt(PERCH_Z));
        }
        if (tag.hasUUID(ARM_PLAYER)) {
            setArmPerchPlayerUUID(tag.getUUID(ARM_PLAYER));
        }
        if (tag.hasUUID(COURIER_PLAYER)) {
            this.courierPlayer = tag.getUUID(COURIER_PLAYER);
        }
        if (tag.hasUUID(BABY_FOLLOW_PLAYER)) {
            this.babyFollowPlayer = tag.getUUID(BABY_FOLLOW_PLAYER);
        }
        if (tag.hasUUID(MEAT_THEFT_PLAYER)) {
            this.meatTheftPlayer = tag.getUUID(MEAT_THEFT_PLAYER);
        }
        if (tag.hasUUID(GUARD_COMMAND_PLAYER)) {
            this.guardCommandPlayer = tag.getUUID(GUARD_COMMAND_PLAYER);
        }
        if (tag.contains(GUARD_TARGET_X)) {
            this.guardCommandTarget = new BlockPos(tag.getInt(GUARD_TARGET_X), tag.getInt(GUARD_TARGET_Y),
                    tag.getInt(GUARD_TARGET_Z));
        }
        if (tag.contains(KNOWN_HOME_X)) {
            this.knownHomePos = new BlockPos(tag.getInt(KNOWN_HOME_X), tag.getInt(KNOWN_HOME_Y),
                    tag.getInt(KNOWN_HOME_Z));
        }
        this.brainState = BrainState.byId(tag.getInt(BRAIN_STATE));
        this.babyPlayerFollowTicks = tag.getInt(BABY_PLAYER_TICKS);
        this.hurtNightTicks = tag.getInt(HURT_NIGHT_TICKS);
        this.courierDelayTicks = tag.getInt(COURIER_DELAY);
        this.meatTheftFleeTicks = tag.getInt(MEAT_THEFT_FLEE_TICKS);
        this.rejectedTreeCooldown = tag.getInt(REJECTED_TREE_COOLDOWN);
        this.guardCommand = GuardCommand.byId(tag.getInt(GUARD_COMMAND));
        this.daySpawned = tag.getBoolean(DAY_SPAWNED);
        this.homeSettled = tag.getBoolean(HOME_SETTLED);
        this.legacyHomeLink = this.homeId != null && !tag.contains(ADULT_HOME_RESIDENT);
        this.adultHomeResident = tag.contains(ADULT_HOME_RESIDENT)
                ? tag.getBoolean(ADULT_HOME_RESIDENT)
                : this.homeId != null && !this.isBaby();
        this.domesticated = tag.getBoolean(DOMESTICATED);
        this.returningTwig = tag.getBoolean(RETURNING_TWIG);
        this.guardDismounted = tag.getBoolean(GUARD_DISMOUNTED);
        this.tamingProgress = Mth.clamp(tag.getFloat(TAMING_PROGRESS), 0.0F, 1.0F);
        this.entityData.set(MOUTH_STACK, ItemStack.of(tag.getCompound(MOUTH_ITEM)));
        this.entityData.set(LEFT_TALON_STACK, ItemStack.of(tag.getCompound(LEFT_TALON_ITEM)));
        this.entityData.set(RIGHT_TALON_STACK, ItemStack.of(tag.getCompound(RIGHT_TALON_ITEM)));
    }

    private void refreshLandmarkStateIfNeeded() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long currentChunk = new ChunkPos(this.blockPosition()).toLong();
        if (currentChunk == this.lastLandmarkChunk && --this.landmarkCheckCooldown > 0) {
            return;
        }
        this.lastLandmarkChunk = currentChunk;
        this.landmarkCheckCooldown = LANDMARK_CHECK_INTERVAL;

        VultureHomeSavedData homes = VultureHomeSavedData.get(serverLevel);
        if (this.homeId != null) {
            boolean wasDomesticated = this.domesticated;
            Optional<VultureHomeSavedData.HomeSnapshot> existing = homes.getHome(serverLevel, this.homeId);
            if (existing.isEmpty()) {
                this.homeId = null;
                this.homeNestPos = null;
                this.cachedHome = null;
                this.homeSettled = false;
            } else {
                this.cachedHome = existing.get();
                this.homeNestPos = this.cachedHome.nestPos() == null
                        ? this.cachedHome.originalNestPos()
                        : this.cachedHome.nestPos();
                this.domesticated = this.cachedHome.domesticated();
                if (this.legacyHomeLink) {
                    this.legacyHomeLink = false;
                    if (this.cachedHome.natural() && !this.homeSettled) {
                        homes.removeAdultResident(this.homeId, this.getUUID());
                        this.adultHomeResident = false;
                    }
                }
                if (!this.isBaby() && !homes.addAdultResident(this.homeId, this.getUUID())) {
                    rejectCurrentHome(homes);
                    takeOff();
                    return;
                }
                this.adultHomeResident = !this.isBaby();
                if (this.cachedHome.natural() && !this.isBaby()) {
                    this.homeSettled = true;
                }

                if (wasDomesticated && this.cachedHome.natural()
                        && !this.cachedHome.domesticated() && !this.cachedHome.hasNest()) {
                    this.setTame(false);
                    this.setOwnerUUID(null);
                }

                if (this.cachedHome.hasNest() && this.returningTwig) {
                    this.returningTwig = false;
                    this.islandTarget = null;
                    this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);
                }

                if (this.domesticated && this.cachedHome.ownerId() != null) {
                    this.setTame(true);
                    this.setOwnerUUID(this.cachedHome.ownerId());
                } else if (!this.homeSettled && !this.cachedHome.treeIntact()) {
                    rejectCurrentHome(homes);
                }
            }
        }

        if (this.domesticated && (this.cachedHome == null || !this.cachedHome.hasNest())) {
            homes.findNearbyDomesticHome(serverLevel, this.blockPosition(), this.getOwnerUUID())
                    .ifPresent(home -> assignHome(homes, home));
        } else if (this.homeId == null && !this.isTame() && !this.domesticated) {
            Optional<VultureHomeSavedData.HomeSnapshot> nearest = homes.findNearestAvailableNaturalHome(
                    serverLevel, this.blockPosition(), this.rejectedHomeId);
            if (nearest.isPresent()) {
                assignHome(homes, nearest.get());
            } else if (this.rejectedHomeId == null && this.rejectedTreeCooldown <= 0
                    && this.crudTreeTarget == null
                    && (this.tickCount < 5 || this.tickCount % 600 == 0)) {
                this.crudTreeTarget = serverLevel.findNearestMapStructure(
                        ModTags.Structures.CRUD_TREES, this.blockPosition(), 256, false);
            }
        }
    }

    private void assignHome(VultureHomeSavedData homes, VultureHomeSavedData.HomeSnapshot home) {
        if (this.homeId != null && !this.homeId.equals(home.id())) {
            homes.removeAdultResident(this.homeId, this.getUUID());
        }
        if (this.isBaby() || !homes.addAdultResident(home.id(), this.getUUID())) {
            this.rejectedHomeId = home.id();
            return;
        }
        this.homeId = home.id();
        this.cachedHome = homes.getHome((ServerLevel) this.level(), home.id()).orElse(home);
        this.homeNestPos = home.nestPos() == null ? home.originalNestPos() : home.nestPos();
        this.homeSettled = true;
        this.adultHomeResident = true;
        this.rejectedHomeId = null;
        this.domesticated = home.domesticated();
        this.flyingTarget = null;
        this.crudTreeTarget = null;
    }

    private void rejectCurrentHome(VultureHomeSavedData homes) {
        if (this.homeId != null) {
            if (this.adultHomeResident) {
                homes.removeAdultResident(this.homeId, this.getUUID());
            }
            this.rejectedHomeId = this.homeId;
        }
        this.homeId = null;
        this.homeNestPos = null;
        this.cachedHome = null;
        this.homeSettled = false;
        this.adultHomeResident = false;
        this.flyingTarget = null;
    }

    private boolean tickHomePriority() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        refreshCachedHomeAfterNestChange(serverLevel);
        if (this.cachedHome == null) {
            return false;
        }

        if (this.cachedHome.natural() && !this.cachedHome.treeIntact()) {
            rejectCurrentHome(VultureHomeSavedData.get(serverLevel));
            takeOff();
            return true;
        }

        if (this.cachedHome.domesticated()) {
            Player carrier = findNestCarrier(this.cachedHome);
            if (carrier != null) {
                if (this.brainState == BrainState.DOMESTICATED_GROUND) {
                    takeOff();
                    return true;
                }
                if (this.brainState == BrainState.TAKEOFF) {
                    return false;
                }
                this.setNoGravity(true);
                this.brainState = BrainState.FOLLOWING_OWNER;
                this.flyingTarget = carrier.position().add(0.0D, 8.0D, 0.0D);
                flyToward(this.flyingTarget);
                return true;
            }

            if (!this.cachedHome.hasNest()) {
                tickDomesticatedGround();
                return true;
            }

            if (this.brainState == BrainState.DOMESTICATED_GROUND) {
                takeOff();
                return true;
            }
            return false;
        }

        if (!this.cachedHome.hasNest()) {
            if (!this.level().isDay()) {
                if (this.hurtNightTicks <= 0) {
                    tickMissingNestAtNight(this.cachedHome);
                    return true;
                }
                return false;
            }
            tickNestRepair(serverLevel, this.cachedHome);
            return true;
        }
        return false;
    }

    private void refreshCachedHomeAfterNestChange(ServerLevel level) {
        if (this.homeId == null || this.cachedHome == null) {
            return;
        }

        boolean missingNestBlock = this.cachedHome.hasNest()
                && this.homeNestPos != null
                && level.hasChunkAt(this.homeNestPos)
                && !level.getBlockState(this.homeNestPos).is(ModBlocks.VULTURE_NEST.get());
        boolean legacyDomesticatedHome = this.cachedHome.natural()
                && this.cachedHome.domesticated()
                && !this.cachedHome.hasNest();
        if (!missingNestBlock && !legacyDomesticatedHome) {
            return;
        }

        boolean wasDomesticated = this.domesticated;
        VultureHomeSavedData homes = VultureHomeSavedData.get(level);
        if (missingNestBlock) {
            homes.markNestMissing(this.homeId);
        }
        homes.getHome(level, this.homeId).ifPresent(home -> {
            this.cachedHome = home;
            this.homeNestPos = home.nestPos() == null ? home.originalNestPos() : home.nestPos();
            this.domesticated = home.domesticated();
            if (wasDomesticated && home.natural() && !home.domesticated() && !home.hasNest()) {
                this.setTame(false);
                this.setOwnerUUID(null);
            }
        });
    }

    private void tickMissingNestAtNight(VultureHomeSavedData.HomeSnapshot home) {
        if (this.brainState == BrainState.TAKEOFF) {
            tickTakeoff();
            return;
        }
        if (this.brainState == BrainState.LANDING_TO_LOG) {
            tickLandingToLog();
            return;
        }
        if (this.brainState == BrainState.PERCHED_LOG) {
            tickPerchedLog();
            return;
        }

        if (new ChunkPos(this.blockPosition()).equals(home.homeChunk())) {
            BlockPos log = findCrudLogNear(home.originalNestPos(), 0);
            if (log != null) {
                this.perchPos = log.above();
                this.brainState = BrainState.LANDING_TO_LOG;
                setAnimation(VultureAnimation.LANDING);
                return;
            }
        }

        if (!this.isNoGravity() && this.onGround()) {
            takeOff();
            return;
        }
        this.setNoGravity(true);
        this.brainState = BrainState.FLYING;
        flyWithWanderingBias(home.originalNestPos());
    }

    private void tickNestRepair(ServerLevel level, VultureHomeSavedData.HomeSnapshot home) {
        if (this.brainState == BrainState.TAKEOFF) {
            tickTakeoff();
            return;
        }
        if (!this.isNoGravity() || this.brainState == BrainState.PERCHED_LOG
                || this.brainState == BrainState.LANDING_TO_LOG) {
            takeOff();
            return;
        }

        this.setNoGravity(true);
        this.brainState = BrainState.FLYING;

        if (this.returningTwig) {
            if (isHorizontallyInside(home.bounds(), this.blockPosition())) {
                this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);
                this.returningTwig = false;
                this.islandTarget = null;
                this.flyingTarget = null;
                VultureHomeSavedData homes = VultureHomeSavedData.get(level);
                homes.depositTwig(level, home.id());
                this.cachedHome = homes.getHome(level, home.id()).orElse(home);
                return;
            }
            flyWithWanderingBias(home.originalNestPos());
            return;
        }

        if (this.islandTarget == null && --this.islandSearchCooldown <= 0) {
            this.islandSearchCooldown = LANDMARK_CHECK_INTERVAL;
            BlockPos registeredIsland = VultureHomeSavedData.get(level)
                    .findNearestIsland(this.blockPosition())
                    .map(VultureHomeSavedData.IslandSnapshot::origin)
                    .orElse(null);
            BlockPos locatedIsland = level.findNearestMapStructure(
                    ModTags.Structures.FLOATING_ISLANDS, this.blockPosition(), 256, false);
            if (registeredIsland == null || locatedIsland != null
                    && locatedIsland.distSqr(this.blockPosition()) < registeredIsland.distSqr(this.blockPosition())) {
                this.islandTarget = locatedIsland;
            } else {
                this.islandTarget = registeredIsland;
            }
        }

        if (this.islandTarget == null) {
            if (this.flyingTarget == null || this.position().distanceToSqr(this.flyingTarget) < 9.0D
                    || this.random.nextInt(80) == 0) {
                chooseRoamingFlightTarget();
            }
            flyToward(this.flyingTarget);
            return;
        }

        ChunkPos current = new ChunkPos(this.blockPosition());
        ChunkPos island = new ChunkPos(this.islandTarget);
        if (current.equals(island)) {
            this.entityData.set(MOUTH_STACK, new ItemStack(ModBlocks.TWIG.get()));
            this.returningTwig = true;
            this.flyingTarget = null;
            return;
        }
        flyWithWanderingBias(this.islandTarget);
    }

    private void tickDomesticatedGround() {
        this.brainState = BrainState.DOMESTICATED_GROUND;
        this.setNoGravity(false);
        this.flyingTarget = null;
        this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);

        if (!this.onGround()) {
            setAnimation(VultureAnimation.FALLING);
            return;
        }

        if (this.guardDismounted) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.25D, 1.0D, 0.25D));
            setAnimation(VultureAnimation.IDLE_PERCHED);
            return;
        }

        if (this.getNavigation().isDone() && this.random.nextInt(40) == 0) {
            int x = this.getBlockX() + this.random.nextInt(13) - 6;
            int z = this.getBlockZ() + this.random.nextInt(13) - 6;
            int y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            this.getNavigation().moveTo(x + 0.5D, y, z + 0.5D, 0.9D);
        }
        setAnimation(this.getNavigation().isDone() ? VultureAnimation.IDLE_PERCHED : VultureAnimation.WALKING);
    }

    private void tickBlinded() {
        this.wasBlinded = true;
        releaseTakeoffPerch();
        this.brainState = BrainState.DOMESTICATED_GROUND;
        this.setNoGravity(false);
        this.flyingTarget = null;
        this.fallDistance = 0.0F;

        if (!this.onGround()) {
            this.getNavigation().stop();
            setAnimation(VultureAnimation.FALLING);
            return;
        }

        LivingEntity attacker = this.getLastHurtByMob();
        if (attacker != null && attacker.isAlive() && this.distanceToSqr(attacker) < 32.0D * 32.0D
                && (this.getNavigation().isDone() || this.tickCount % 10 == 0)) {
            Vec3 away = this.position().subtract(attacker.position());
            away = new Vec3(away.x, 0.0D, away.z);
            if (away.lengthSqr() < 0.01D) {
                away = randomHorizontalDirection();
            } else {
                away = away.normalize();
            }
            int x = Mth.floor(this.getX() + away.x * 10.0D);
            int z = Mth.floor(this.getZ() + away.z * 10.0D);
            int y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            this.getNavigation().moveTo(x + 0.5D, y, z + 0.5D, 1.25D);
        }

        setAnimation(this.getNavigation().isDone()
                ? VultureAnimation.IDLE_PERCHED
                : VultureAnimation.WALKING);
    }

    @Nullable
    private Player findNestCarrier(VultureHomeSavedData.HomeSnapshot home) {
        if (home.ownerId() == null) {
            return null;
        }
        Player owner = findPlayerByUuid(home.ownerId());
        if (owner == null) {
            return null;
        }
        ItemStack helmet = owner.getItemBySlot(EquipmentSlot.HEAD);
        UUID carriedHome = VultureNestBlock.getHomeId(helmet);
        return home.id().equals(carriedHome) ? owner : null;
    }

    private void flyWithWanderingBias(BlockPos destination) {
        if (this.flyingTarget == null || this.position().distanceToSqr(this.flyingTarget) < 9.0D
                || this.random.nextInt(80) == 0) {
            chooseBiasedFlightTarget(destination);
        }
        flyToward(this.flyingTarget);
    }

    private void chooseBiasedFlightTarget(BlockPos destination) {
        Vec3 toward = Vec3.atCenterOf(destination).subtract(this.position());
        Vec3 horizontal = new Vec3(toward.x, 0.0D, toward.z);
        if (horizontal.horizontalDistanceSqr() < 0.01D) {
            horizontal = randomHorizontalDirection();
        }

        Vec3 randomDirection = randomHorizontalDirection();
        Vec3 biasedDirection = horizontal.normalize().scale(0.78D)
                .add(randomDirection.scale(0.22D)).normalize();
        double distance = 18.0D + this.random.nextDouble() * 14.0D;
        int x = Mth.floor(this.getX() + biasedDirection.x * distance);
        int z = Mth.floor(this.getZ() + biasedDirection.z * distance);
        int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int y = Math.max(groundY + 10, Mth.floor(this.getY()) + this.random.nextInt(7) - 3);
        this.flyingTarget = new Vec3(x + 0.5D, Math.min(y, getMaximumFlightY(x, z)), z + 0.5D);
    }

    private static boolean isHorizontallyInside(net.minecraft.world.level.levelgen.structure.BoundingBox box,
                                                BlockPos pos) {
        return pos.getX() >= box.minX() && pos.getX() <= box.maxX()
                && pos.getZ() >= box.minZ() && pos.getZ() <= box.maxZ();
    }

    private void tickBabyBrain() {
        this.setNoGravity(false);
        setAnimation(this.getDeltaMovement().horizontalDistanceSqr() > 0.0004D ? VultureAnimation.WALKING : VultureAnimation.IDLE_PERCHED);
        attractHostilesToBaby();

        AABB adultSearchArea = new AABB(
                this.getX() - 10.0D, this.level().getMinBuildHeight(), this.getZ() - 10.0D,
                this.getX() + 10.0D, this.level().getMaxBuildHeight(), this.getZ() + 10.0D
        );
        Optional<VultureEntity> adult = this.level().getEntitiesOfClass(VultureEntity.class, adultSearchArea,
                        vulture -> vulture != this && !vulture.isBaby() && horizontalDistanceToSqr(vulture) <= 100.0D)
                .stream()
                .min(Comparator.comparingDouble(this::horizontalDistanceToSqr));

        if (adult.isPresent()) {
            VultureEntity followedAdult = adult.get();
            this.getNavigation().moveTo(followedAdult.getX(), this.getY(), followedAdult.getZ(), 1.15D);
            return;
        }

        Player player = this.level().getNearestPlayer(this, -1.0D);
        if (player != null) {
            this.getNavigation().moveTo(player, 1.1D);
            this.babyFollowPlayer = player.getUUID();
            this.babyPlayerFollowTicks++;
            if (this.getAge() >= 0 && this.babyPlayerFollowTicks >= PLAYER_FOLLOW_TAME_TICKS) {
                this.tame(player);
            }
        }
    }

    private void attractHostilesToBaby() {
        List<Monster> monsters = this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(16.0D),
                monster -> monster.getTarget() == null);
        for (Monster monster : monsters) {
            monster.setTarget(this);
        }
    }

    private boolean scanForImmediateTargets() {
        Player armPlayer = findArmGuardPlayer();
        if (armPlayer != null) {
            if (isMeat(armPlayer.getMainHandItem())) {
                playDetectionSound();
            }
            setArmPerchPlayerUUID(armPlayer.getUUID());
            this.entityData.set(ARM_PERCHED, false);
            this.actionTicks = 0;
            this.armIdleAnimationTicks = 0;
            this.armIdleCooldownTicks = 0;
            this.armPlayerStillTicks = 0;
            this.brainState = BrainState.LANDING_TO_ARM;
            setAnimation(VultureAnimation.FALLING);
            return true;
        }

        Player meatPlayer = findUnguardedMeatPlayer();
        if (meatPlayer != null) {
            playDetectionSound();
            this.meatTheftPlayer = meatPlayer.getUUID();
            this.brainState = BrainState.LANDING_TO_MEAT_PLAYER;
            setAnimation(VultureAnimation.FALLING);
            return true;
        }

        ItemEntity meat = findMeatItemInSameChunk();
        if (meat != null) {
            playDetectionSound();
            this.targetItemId = meat.getId();
            this.flyingTarget = meat.position().add(0.0D, 2.0D, 0.0D);
            this.brainState = BrainState.LANDING_TO_ITEM;
            setAnimation(VultureAnimation.LANDING);
            return true;
        }

        Player watchedPlayer = findWatchedPlayerBelow();
        if (watchedPlayer != null) {
            BlockPos log = findCrudLogNear(this.blockPosition(), 0);
            if (log != null) {
                this.perchPos = log.above();
                this.brainState = BrainState.LANDING_TO_LOG;
                setAnimation(VultureAnimation.LANDING);
                return true;
            }
        }

        return false;
    }

    private boolean canScanForImmediateTargets() {
        return this.brainState == BrainState.FLYING
                || this.brainState == BrainState.PERCHED_LOG
                || this.brainState == BrainState.FOLLOWING_OWNER;
    }

    @Nullable
    private Player findArmGuardPlayer() {
        if (!this.level().isDay() && this.hurtNightTicks <= 0 && !this.isTame()) {
            return null;
        }

        AABB chunkBox = sameChunkBox().inflate(0.0D, 96.0D, 0.0D);
        return this.level().getEntitiesOfClass(Player.class, chunkBox, player ->
                        player.getY() <= this.getY() + 2.0D
                                && hasAnyArmGuard(player)
                                && ArmGuardItem.matchesBinding(player.getOffhandItem(), this)
                                && !isArmGuardClaimedByAnother(player)
                                && (isCreativeArmGuard(player.getOffhandItem())
                                || isMeat(player.getMainHandItem())
                                || (this.isTame() && player.getUUID().equals(this.getOwnerUUID()))))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Nullable
    private Player findUnguardedMeatPlayer() {
        if (this.isTame() || (!this.level().isDay() && this.hurtNightTicks <= 0)) {
            return null;
        }

        AABB chunkBox = sameChunkBox().inflate(0.0D, 96.0D, 0.0D);
        return this.level().getEntitiesOfClass(Player.class, chunkBox, player ->
                        player.getY() <= this.getY() + 2.0D
                                && !hasAnyArmGuard(player)
                                && isMeat(player.getMainHandItem()))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private void playDetectionSound() {
        this.playSound(ModSounds.VULTURE_DETECTS.get(), 6.0F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    @Nullable
    private Player findWatchedPlayerBelow() {
        if (!this.level().isDay()) {
            return null;
        }
        return this.level().getEntitiesOfClass(Player.class, sameChunkBox().inflate(0.0D, 96.0D, 0.0D),
                        player -> player.getY() < this.getY() - 2.0D)
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Nullable
    private ItemEntity findMeatItemInSameChunk() {
        return this.level().getEntitiesOfClass(ItemEntity.class, sameChunkBox().inflate(0.0D, 96.0D, 0.0D),
                        item -> item.isAlive() && isMeat(item.getItem()))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private AABB sameChunkBox() {
        int minX = (Mth.floor(this.getX()) >> 4) << 4;
        int minZ = (Mth.floor(this.getZ()) >> 4) << 4;
        return new AABB(minX, this.level().getMinBuildHeight(), minZ, minX + 16, this.level().getMaxBuildHeight(), minZ + 16);
    }

    private void tickFlying() {
        this.setNoGravity(true);

        if (tickHomeFlightConstraints()) {
            return;
        }

        if (this.homeId == null && this.crudTreeTarget != null
                && new ChunkPos(this.blockPosition()).equals(new ChunkPos(this.crudTreeTarget))) {
            if (this.level() instanceof ServerLevel serverLevel) {
                VultureHomeSavedData.get(serverLevel)
                        .findNearestNaturalHome(serverLevel, this.crudTreeTarget, 64)
                        .ifPresent(home -> this.rejectedHomeId = home.id());
            }
            BlockPos rejectedTarget = this.crudTreeTarget;
            this.crudTreeTarget = null;
            chooseDepartureFrom(rejectedTarget);
            this.rejectedTreeCooldown = REJECTED_TREE_COOLDOWN_TICKS;
        }

        if (!this.level().isDay() && this.daySpawned && !this.isTame() && this.homeId == null) {
            this.brainState = BrainState.FLEEING_NIGHT;
            chooseNightFleeTarget();
            return;
        }

        if (!this.level().isDay() && this.hurtNightTicks <= 0) {
            BlockPos log = findCrudLogNear(this.blockPosition(), 1);
            if (log != null) {
                this.perchPos = log.above();
                this.brainState = BrainState.LANDING_TO_LOG;
                setAnimation(VultureAnimation.LANDING);
                return;
            }
        }

        if (this.isTame() && shouldFollowOwner()) {
            this.brainState = BrainState.FOLLOWING_OWNER;
            return;
        }

        if (this.flyingTarget == null || this.position().distanceToSqr(this.flyingTarget) < 9.0D || this.random.nextInt(80) == 0) {
            chooseRoamingFlightTarget();
        }
        flyToward(this.flyingTarget);
    }

    private boolean tickHomeFlightConstraints() {
        if (this.cachedHome == null || !this.cachedHome.hasNest()) {
            return false;
        }

        ChunkPos current = new ChunkPos(this.blockPosition());
        ChunkPos homeChunk = this.cachedHome.homeChunk();
        if (!this.homeSettled) {
            if (current.equals(homeChunk)) {
                if (this.cachedHome.treeIntact() || this.cachedHome.domesticated()) {
                    this.homeSettled = true;
                    this.rejectedHomeId = null;
                    this.flyingTarget = null;
                    return false;
                }
                if (this.level() instanceof ServerLevel serverLevel) {
                    VultureHomeSavedData homes = VultureHomeSavedData.get(serverLevel);
                    rejectCurrentHome(homes);
                    homes.findNearestAvailableNaturalHome(serverLevel, this.blockPosition(), this.rejectedHomeId)
                            .ifPresent(home -> assignHome(homes, home));
                }
                return true;
            }

            flyWithWanderingBias(this.cachedHome.nestPos() == null
                    ? this.cachedHome.originalNestPos()
                    : this.cachedHome.nestPos());
            return true;
        }

        int radius = this.cachedHome.domesticated() || !this.cachedHome.treeIntact()
                ? 0
                : HOME_RADIUS_CHUNKS;
        int deltaX = Math.abs(current.x - homeChunk.x);
        int deltaZ = Math.abs(current.z - homeChunk.z);
        boolean atBoundary = radius == 0 ? deltaX != 0 || deltaZ != 0
                : deltaX >= radius || deltaZ >= radius;
        if (atBoundary) {
            BlockPos nestPos = this.cachedHome.nestPos() == null
                    ? this.cachedHome.originalNestPos()
                    : this.cachedHome.nestPos();
            flyWithWanderingBias(nestPos);
            return true;
        }
        return false;
    }

    private void tickTakeoff() {
        this.setNoGravity(true);
        if (this.actionTicks > RAISE_LANDING_GEAR_TICKS) {
            this.setDeltaMovement(Vec3.ZERO);
            Player armPlayer = findPlayerByUuid(getArmPerchPlayerUUID());
            if (armPlayer != null && this.isArmPerched()) {
                lockToArmGuard(armPlayer);
            }
            setAnimation(VultureAnimation.FLAP_WINGS);
            this.actionTicks--;
            return;
        }

        releaseTakeoffPerch();
        Vec3 launchVelocity = this.takeoffDirection.scale(2.0D)
                .add(0.0D, 1.0D, 0.0D)
                .normalize()
                .scale(FLY_SPEED);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.55D).add(launchVelocity.scale(0.45D)));
        this.setYRot((float) (Mth.atan2(this.takeoffDirection.z, this.takeoffDirection.x) * Mth.RAD_TO_DEG) - 90.0F);
        this.yBodyRot = this.getYRot();
        this.setYHeadRot(this.getYRot());
        this.yHeadRotO = this.getYRot();
        setAnimation(VultureAnimation.RAISE_LANDING_GEAR);
        if (--this.actionTicks <= 0) {
            this.brainState = BrainState.FLYING;
            this.flyingTarget = this.position().add(this.takeoffDirection.scale(16.0D)).add(0.0D, 8.0D, 0.0D);
        }
    }

    private void tickFollowingOwner() {
        if (this.domesticated) {
            this.brainState = BrainState.FLYING;
            this.flyingTarget = null;
            return;
        }
        LivingEntity ownerEntity = this.getOwner();
        if (!(ownerEntity instanceof Player owner)) {
            takeOff();
            return;
        }

        this.setNoGravity(true);
        this.flyingTarget = owner.position().add(0.0D, 8.0D, 0.0D);
        flyToward(this.flyingTarget);
        if (this.distanceToSqr(owner) < 64.0D) {
            takeOff();
        }
    }

    private boolean shouldFollowOwner() {
        if (!this.isTame() || this.domesticated) {
            return false;
        }
        LivingEntity ownerEntity = this.getOwner();
        if (!(ownerEntity instanceof Player owner)) {
            return false;
        }
        if (this.homeNestPos == null) {
            return this.distanceToSqr(owner) > 48.0D * 48.0D;
        }
        return owner.blockPosition().distSqr(this.homeNestPos) > 48.0D * 48.0D;
    }

    private void tickLandingToLog() {
        if (this.perchPos == null || !isGoodPerch(this.perchPos.below())) {
            takeOff();
            return;
        }

        this.setNoGravity(true);
        flyToward(Vec3.atBottomCenterOf(this.perchPos));
        setAnimation(VultureAnimation.LANDING);

        if (this.blockPosition().distSqr(this.perchPos) < 2.0D) {
            this.setPos(this.perchPos.getX() + 0.5D, this.perchPos.getY(), this.perchPos.getZ() + 0.5D);
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(false);
            this.brainState = BrainState.PERCHED_LOG;
            this.actionTicks = 80 + this.random.nextInt(120);
            setAnimation(VultureAnimation.IDLE_PERCHED);
        }
    }

    private void tickPerchedLog() {
        this.setNoGravity(false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.4D, 1.0D, 0.4D));
        setAnimation(this.getDeltaMovement().horizontalDistanceSqr() > 0.0004D ? VultureAnimation.WALKING : VultureAnimation.IDLE_PERCHED);

        Player watchedPlayer = findWatchedPlayerBelow();
        if (watchedPlayer != null) {
            this.getLookControl().setLookAt(watchedPlayer, 80.0F, 80.0F);
        }

        if (this.level().isDay() && watchedPlayer == null && --this.actionTicks <= 0) {
            takeOff();
        } else if (!this.level().isDay() && this.random.nextInt(160) == 0) {
            tryWalkOnCrudLog();
        }
    }

    private void tryWalkOnCrudLog() {
        if (this.perchPos == null) {
            return;
        }
        for (int i = 0; i < 8; i++) {
            BlockPos target = this.perchPos.offset(this.random.nextInt(5) - 2, 0, this.random.nextInt(5) - 2);
            if (this.level().getBlockState(target.below()).is(ModBlocks.CRUD_LOG.get())
                    && !isPerchOccupied(target)
                    && this.level().noCollision(this, this.getBoundingBox().move(Vec3.atBottomCenterOf(target).subtract(this.position())))) {
                this.perchPos = target;
                this.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 0.8D);
                setAnimation(VultureAnimation.WALKING);
                return;
            }
        }
    }

    private void tickLandingToItem() {
        Entity entity = this.level().getEntity(this.targetItemId);
        if (!(entity instanceof ItemEntity item) || !item.isAlive() || !isMeat(item.getItem())) {
            takeOff();
            return;
        }

        this.setNoGravity(true);
        Vec3 target = item.position().add(0.0D, 0.4D, 0.0D);
        flyToward(target);
        setAnimation(VultureAnimation.LANDING);

        if (this.distanceToSqr(item) < 9.0D) {
            this.setNoGravity(false);
            this.brainState = BrainState.WALKING_TO_ITEM;
        }
    }

    private void tickWalkingToItem() {
        Entity entity = this.level().getEntity(this.targetItemId);
        if (!(entity instanceof ItemEntity item) || !item.isAlive() || !isMeat(item.getItem())) {
            takeOff();
            return;
        }

        this.setNoGravity(false);
        this.getNavigation().moveTo(item, 1.1D);
        setAnimation(VultureAnimation.WALKING);

        if (this.distanceToSqr(item) < 1.8D) {
            this.brainState = BrainState.EATING_ITEM;
            this.actionTicks = EATING_TICKS;
            ItemStack itemStack = item.getItem();
            this.entityData.set(MOUTH_STACK, itemStack.copyWithCount(1));
            itemStack.shrink(1);
            if (itemStack.isEmpty()) {
                item.discard();
            } else {
                item.setItem(itemStack);
            }
            setAnimation(VultureAnimation.EATING_OFF_GROUND);
        }
    }

    private void tickEatingItem() {
        setAnimation(VultureAnimation.EATING_OFF_GROUND);
        this.actionTicks--;
        playEatingSoundIfDue();
        if (this.actionTicks > 0) {
            return;
        }

        this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);
        takeOff();
    }

    private boolean tickArmPerch() {
        Player player = findPlayerByUuid(getArmPerchPlayerUUID());
        if (player == null || !canUseArmPerch(player)) {
            return false;
        }

        boolean creativeGuard = isCreativeArmGuard(player.getOffhandItem());
        this.setNoGravity(true);
        this.entityData.set(ARM_PERCHED, true);
        lockToArmGuard(player);

        if (creativeGuard && !this.isTame()) {
            this.tame(player);
        }

        if (this.isTame() && this.actionTicks > 0) {
            this.actionTicks--;
            playEatingSoundIfDue();
            if (this.actionTicks <= 0) {
                this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);
            }
        }

        if (!creativeGuard && !this.isTame() && this.actionTicks <= 0) {
            ItemStack meat = player.getMainHandItem();
            this.entityData.set(MOUTH_STACK, meat.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                meat.shrink(1);
            }
            this.actionTicks = EATING_TICKS;
        } else if (!creativeGuard && !this.isTame()) {
            this.actionTicks--;
            playEatingSoundIfDue();
            if (this.actionTicks <= 0) {
                tryTameFromFood(player, this.entityData.get(MOUTH_STACK));
                this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);
                if (!this.isTame()) {
                    takeOff();
                    return true;
                }
            }
        }

        tickArmPerchAnimation(player);

        return true;
    }

    private void tickArmPerchAnimation(Player player) {
        if (this.actionTicks > 0) {
            this.armIdleAnimationTicks = 0;
            this.armIdleCooldownTicks = ARM_IDLE_INTERVAL_TICKS;
            this.armPlayerStillTicks = 0;
            setAnimation(VultureAnimation.EATING_OFF_GROUND);
            return;
        }

        if (isPlayerStandingStill(player)) {
            this.armPlayerStillTicks++;
            if (this.armIdleAnimationTicks > 0) {
                this.armIdleAnimationTicks--;
                setAnimation(VultureAnimation.IDLE_PERCHED);
            } else if (this.armPlayerStillTicks >= ARM_CONTINUOUS_IDLE_DELAY_TICKS) {
                setAnimation(VultureAnimation.IDLE_PERCHED);
            } else {
                setAnimation(VultureAnimation.STILL_PERCHED);
            }
            return;
        }

        boolean wasContinuouslyIdling = this.armPlayerStillTicks >= ARM_CONTINUOUS_IDLE_DELAY_TICKS;
        this.armPlayerStillTicks = 0;
        if (wasContinuouslyIdling) {
            this.armIdleAnimationTicks = 0;
            this.armIdleCooldownTicks = ARM_IDLE_INTERVAL_TICKS;
            setAnimation(VultureAnimation.STILL_PERCHED);
            return;
        }

        if (this.armIdleAnimationTicks > 0) {
            this.armIdleAnimationTicks--;
            this.armIdleCooldownTicks--;
            setAnimation(VultureAnimation.IDLE_PERCHED);
            return;
        }

        if (--this.armIdleCooldownTicks <= 0) {
            this.armIdleAnimationTicks = ARM_IDLE_ANIMATION_TICKS;
            this.armIdleCooldownTicks = ARM_IDLE_INTERVAL_TICKS;
            setAnimation(VultureAnimation.IDLE_PERCHED);
            return;
        }
        setAnimation(VultureAnimation.STILL_PERCHED);
    }

    private void playEatingSoundIfDue() {
        if (this.actionTicks == EATING_TICKS - EATING_SOUND_DELAY_TICKS) {
            this.playSound(ModSounds.VULTURE_EAT.get(), EATING_SOUND_VOLUME,
                    0.95F + this.random.nextFloat() * 0.1F);
        }
    }

    private double horizontalDistanceToSqr(Entity entity) {
        double xDistance = this.getX() - entity.getX();
        double zDistance = this.getZ() - entity.getZ();
        return xDistance * xDistance + zDistance * zDistance;
    }

    private boolean isPlayerStandingStill(Player player) {
        double xMovement = player.getX() - player.xo;
        double yMovement = player.getY() - player.yo;
        double zMovement = player.getZ() - player.zo;
        return xMovement * xMovement + yMovement * yMovement + zMovement * zMovement < 0.0001D;
    }

    private void stopDetectionSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundStopSoundPacket(
                    ModSounds.VULTURE_DETECTS.get().getLocation(), SoundSource.NEUTRAL));
        }
    }

    private boolean tickLandingToArm() {
        Player player = findPlayerByUuid(getArmPerchPlayerUUID());
        if (player == null || !canUseArmPerch(player)) {
            return false;
        }

        this.setNoGravity(true);
        this.entityData.set(ARM_PERCHED, false);
        this.getNavigation().stop();
        Vec3 armPosition = getArmPerchPosition(player, 1.0F);
        flyToward(armPosition);
        setAnimation(VultureAnimation.FALLING);

        AABB armHitbox = new AABB(
                armPosition.x - ARM_APPROACH_HITBOX_RADIUS,
                armPosition.y - ARM_APPROACH_HITBOX_RADIUS,
                armPosition.z - ARM_APPROACH_HITBOX_RADIUS,
                armPosition.x + ARM_APPROACH_HITBOX_RADIUS,
                armPosition.y + ARM_APPROACH_HITBOX_RADIUS,
                armPosition.z + ARM_APPROACH_HITBOX_RADIUS
        );
        if (this.getBoundingBox().intersects(armHitbox)) {
            stopDetectionSound(player);
            this.brainState = BrainState.PERCHED_ARM;
            this.entityData.set(ARM_PERCHED, true);
            this.armIdleAnimationTicks = 0;
            this.armIdleCooldownTicks = ARM_IDLE_INTERVAL_TICKS;
            this.armPlayerStillTicks = 0;
            lockToArmGuard(player);
            setAnimation(VultureAnimation.STILL_PERCHED);
        }
        return true;
    }

    private void tickLandingToMeatPlayer() {
        Player player = findPlayerByUuid(this.meatTheftPlayer);
        if (player == null || !player.isAlive() || hasAnyArmGuard(player) || !isMeat(player.getMainHandItem())) {
            this.meatTheftPlayer = null;
            takeOff();
            return;
        }

        this.setNoGravity(true);
        this.getNavigation().stop();
        Vec3 target = player.position().add(0.0D, player.getBbHeight() * 0.65D, 0.0D);
        flyToward(target);
        setAnimation(VultureAnimation.FALLING);

        if (!this.getBoundingBox().intersects(player.getBoundingBox().inflate(0.3D))) {
            return;
        }

        ItemStack meat = player.getMainHandItem();
        ItemStack stolenMeat = meat.copyWithCount(1);
        meat.shrink(1);
        this.entityData.set(MOUTH_STACK, stolenMeat);
        player.hurt(this.damageSources().mobAttack(this), 1.0F);
        beginMeatTheftFlee(player);
    }

    private void beginMeatTheftFlee(Player player) {
        Vec3 away = this.position().subtract(player.position());
        away = new Vec3(away.x, 0.0D, away.z);
        if (away.lengthSqr() < 0.1D) {
            away = new Vec3(this.random.nextDouble() - 0.5D, 0.0D, this.random.nextDouble() - 0.5D);
        }
        away = away.normalize();
        this.flyingTarget = this.position().add(away.scale(48.0D)).add(0.0D, 8.0D, 0.0D);
        this.setDeltaMovement(away.scale(0.38D).add(0.0D, 0.2D, 0.0D));
        this.meatTheftFleeTicks = MEAT_THEFT_FLEE_DURATION_TICKS;
        this.brainState = BrainState.FLEEING_WITH_MEAT;
        this.playSound(ModSounds.VULTURE_HURT.get(), 1.2F, 0.95F + this.random.nextFloat() * 0.1F);
        this.level().broadcastEntityEvent(this, (byte) 6);
        setAnimation(VultureAnimation.FLAP_WINGS);
    }

    private void tickMeatTheftFleeing() {
        this.setNoGravity(true);
        if (--this.meatTheftFleeTicks <= 0 || this.flyingTarget == null) {
            this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);
            this.meatTheftPlayer = null;
            this.brainState = BrainState.FLYING;
            chooseRoamingFlightTarget();
            return;
        }

        flyToward(this.flyingTarget);
    }

    private boolean canUseArmPerch(Player player) {
        if (!player.isAlive() || this.guardDismounted || this.armPerchCooldownTicks > 0
                || !hasAnyArmGuard(player) || isArmGuardClaimedByAnother(player)
                || !ArmGuardItem.matchesBinding(player.getOffhandItem(), this)) {
            return false;
        }
        boolean creativeGuard = isCreativeArmGuard(player.getOffhandItem());
        boolean ownerPerch = this.isTame() && player.getUUID().equals(this.getOwnerUUID());
        return creativeGuard || ownerPerch || isMeat(player.getMainHandItem());
    }

    private boolean isArmGuardClaimedByAnother(Player player) {
        UUID playerId = player.getUUID();
        AABB searchArea = player.getBoundingBox().inflate(128.0D);
        return !this.level().getEntitiesOfClass(VultureEntity.class, searchArea, vulture ->
                vulture != this
                        && vulture.isAlive()
                        && playerId.equals(vulture.getArmPerchPlayerUUID())
                        && (vulture.isArmPerched()
                        || vulture.brainState == BrainState.PERCHED_ARM
                        || vulture.brainState == BrainState.LANDING_TO_ARM)
        ).isEmpty();
    }

    private void lockToArmGuard(Player player) {
        Vec3 armPos = getArmPerchPosition(player, 1.0F);
        this.setPos(armPos.x, armPos.y, armPos.z);
        this.setDeltaMovement(Vec3.ZERO);
        float perchYaw = player.yBodyRot + (player.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F);
        this.setYRot(perchYaw);
        this.setYHeadRot(perchYaw);
        this.yBodyRot = perchYaw;
        this.yRotO = perchYaw;
        this.yHeadRotO = perchYaw;
        this.yBodyRotO = perchYaw;
        this.setXRot(0.0F);
        this.xRotO = 0.0F;
    }

    public static Vec3 getArmPerchPosition(Player player, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot()) * Mth.DEG_TO_RAD;
        Vec3 horizontalForward = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
        Vec3 armDirection = horizontalForward.scale(Mth.cos(pitch)).add(0.0D, -Mth.sin(pitch), 0.0D);
        Vec3 armTop = horizontalForward.scale(Mth.sin(pitch)).add(0.0D, Mth.cos(pitch), 0.0D);
        Vec3 left = new Vec3(horizontalForward.z, 0.0D, -horizontalForward.x);
        double offhandSide = player.getMainArm() == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        Vec3 playerPosition = new Vec3(
                Mth.lerp(partialTick, player.xo, player.getX()),
                Mth.lerp(partialTick, player.yo, player.getY()),
                Mth.lerp(partialTick, player.zo, player.getZ())
        );
        return playerPosition
                .add(0.0D, player.isCrouching() ? 1.18D : 1.42D, 0.0D)
                .add(left.scale(ARM_PERCH_SIDE_OFFSET * offhandSide))
                .add(armDirection.scale(ARM_PERCH_FORWARD_OFFSET))
                .add(armTop.scale(ARM_PERCH_TOP_OFFSET));
    }

    private void tryTameFromFood(Player player, ItemStack foodStack) {
        if (this.isTame()) {
            return;
        }
        FoodProperties food = foodStack.getFoodProperties(this);
        float saturation = food == null ? 0.0F : food.getNutrition() * food.getSaturationModifier() * 2.0F;
        spawnTamingHearts(saturation);
        float chance = Mth.clamp(saturation * TAME_CHANCE_PER_SATURATION, 0.04F, 1.0F / 3.0F);
        this.tamingProgress = Mth.clamp(this.tamingProgress + chance, 0.0F, 1.0F);
        if (this.tamingProgress >= 1.0F - 1.0E-5F || this.random.nextFloat() < chance) {
            this.tame(player);
        }
    }

    private void spawnTamingHearts(float saturation) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float saturationRatio = Mth.clamp(saturation / 12.8F, 0.0F, 1.0F);
        int particleCount = Mth.clamp(1 + Mth.floor(saturationRatio * 6.0F), 1, 7);
        serverLevel.sendParticles(ParticleTypes.HEART,
                this.getX(), this.getY() + this.getBbHeight() * 0.8D, this.getZ(),
                particleCount, 0.12D, 0.08D, 0.12D, 0.01D);
    }

    private void tickNightFleeing() {
        this.setNoGravity(true);
        if (this.flyingTarget == null || this.position().distanceToSqr(this.flyingTarget) < 25.0D) {
            chooseNightFleeTarget();
        }
        flyToward(this.flyingTarget);

        Player nearest = this.level().getNearestPlayer(this, 256.0D);
        if (nearest == null || this.distanceToSqr(nearest) > getServerRenderDistanceBlocks(nearest) * getServerRenderDistanceBlocks(nearest)) {
            this.discard();
        }
    }

    private void tickDeathCourier() {
        Player player = findPlayerByUuid(this.courierPlayer);
        if (player == null) {
            takeOff();
            return;
        }

        this.setNoGravity(true);
        Vec3 target = player.position().add(0.0D, 1.6D, 0.0D);
        flyToward(target);
        if (this.distanceToSqr(player) < 6.0D) {
            giveCourierStack(player, this.getMouthStack());
            giveCourierStack(player, this.getLeftTalonStack());
            giveCourierStack(player, this.getRightTalonStack());
            this.entityData.set(MOUTH_STACK, ItemStack.EMPTY);
            this.entityData.set(LEFT_TALON_STACK, ItemStack.EMPTY);
            this.entityData.set(RIGHT_TALON_STACK, ItemStack.EMPTY);
            this.courierPlayer = null;
            takeOff();
        }
    }

    private void giveCourierStack(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack copy = stack.copy();
        if (!player.addItem(copy)) {
            player.drop(copy, false);
        }
    }

    private void placeCourierAtRenderEdge() {
        Player player = findPlayerByUuid(this.courierPlayer);
        if (player == null) {
            return;
        }
        int renderDistance = getServerRenderDistanceBlocks(player);
        Vec3 direction = this.position().subtract(player.position());
        if (direction.lengthSqr() < 1.0D) {
            direction = new Vec3(this.random.nextDouble() - 0.5D, 0.0D, this.random.nextDouble() - 0.5D);
        }
        direction = direction.normalize();
        BlockPos pos = BlockPos.containing(player.getX() + direction.x * renderDistance, player.getY() + 20.0D, player.getZ() + direction.z * renderDistance);
        this.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        this.brainState = BrainState.DEATH_COURIER;
    }

    private int getServerRenderDistanceBlocks(Player player) {
        if (player.level().getServer() == null) {
            return 160;
        }
        return Math.max(64, player.level().getServer().getPlayerList().getViewDistance() * 16);
    }

    private void chooseRoamingFlightTarget() {
        if (this.cachedHome != null && !this.homeSettled && this.cachedHome.hasNest()) {
            chooseBiasedFlightTarget(this.cachedHome.nestPos() == null
                    ? this.cachedHome.originalNestPos()
                    : this.cachedHome.nestPos());
            return;
        }
        if (this.cachedHome == null && this.crudTreeTarget != null && !this.isTame() && !this.domesticated) {
            chooseBiasedFlightTarget(this.crudTreeTarget);
            return;
        }

        int x = Mth.floor(this.getX()) + this.random.nextInt(33) - 16;
        int z = Mth.floor(this.getZ()) + this.random.nextInt(33) - 16;
        if (this.cachedHome != null && this.homeSettled && this.cachedHome.hasNest()) {
            ChunkPos homeChunk = this.cachedHome.homeChunk();
            int radius = this.cachedHome.domesticated() || !this.cachedHome.treeIntact()
                    ? 0
                    : HOME_RADIUS_CHUNKS;
            x = Mth.clamp(x, (homeChunk.x - radius) << 4, ((homeChunk.x + radius + 1) << 4) - 1);
            z = Mth.clamp(z, (homeChunk.z - radius) << 4, ((homeChunk.z + radius + 1) << 4) - 1);
        }
        int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int minY = groundY + 10;
        int maxY = Mth.floor(getMaximumFlightY(x, z));
        int currentY = Mth.clamp(Mth.floor(this.getY()), minY, maxY);
        int verticalChoice = this.random.nextInt(10);
        int y;
        if (verticalChoice < 7) {
            y = currentY;
        } else if (verticalChoice < 9) {
            y = Math.min(maxY, currentY + 2 + this.random.nextInt(4));
        } else {
            y = Math.max(minY, currentY - 2 - this.random.nextInt(4));
        }
        this.flyingTarget = new Vec3(x + 0.5D, y, z + 0.5D);
    }

    private void chooseNightFleeTarget() {
        Player nearest = this.level().getNearestPlayer(this, 128.0D);
        Vec3 away = nearest == null ? new Vec3(this.random.nextDouble() - 0.5D, 0.0D, this.random.nextDouble() - 0.5D) : this.position().subtract(nearest.position());
        if (away.horizontalDistanceSqr() < 0.1D) {
            away = new Vec3(this.random.nextDouble() - 0.5D, 0.0D, this.random.nextDouble() - 0.5D);
        }
        away = new Vec3(away.x, 0.0D, away.z).normalize();
        this.flyingTarget = this.position().add(away.scale(48.0D)).add(0.0D, 8.0D, 0.0D);
    }

    private boolean tickRejectedTreeDeparture() {
        if (this.rejectedHomeId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        Optional<VultureHomeSavedData.HomeSnapshot> rejectedHome =
                VultureHomeSavedData.get(serverLevel).getHome(serverLevel, this.rejectedHomeId);
        if (rejectedHome.isEmpty()) {
            this.rejectedHomeId = null;
            return false;
        }

        BlockPos rejectedTree = rejectedHome.get().basePos();
        double x = this.getX() - (rejectedTree.getX() + 0.5D);
        double z = this.getZ() - (rejectedTree.getZ() + 0.5D);
        if (x * x + z * z > 128.0D * 128.0D) {
            return false;
        }

        Vec3 away = new Vec3(x, 0.0D, z);
        Vec3 towardTarget = this.flyingTarget == null
                ? Vec3.ZERO
                : this.flyingTarget.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        boolean targetPointsAway = away.horizontalDistanceSqr() > 0.01D
                && towardTarget.horizontalDistanceSqr() > 0.01D
                && away.normalize().dot(towardTarget.normalize()) > 0.5D;
        if (!targetPointsAway || this.position().distanceToSqr(this.flyingTarget) < 9.0D
                || this.random.nextInt(100) == 0) {
            chooseDepartureFrom(rejectedTree);
        }

        this.setNoGravity(true);
        this.brainState = BrainState.FLYING;
        flyToward(this.flyingTarget);
        return true;
    }

    private void chooseDepartureFrom(BlockPos rejectedTarget) {
        Vec3 away = this.position().subtract(Vec3.atCenterOf(rejectedTarget));
        if (away.horizontalDistanceSqr() < 0.1D) {
            away = new Vec3(this.random.nextDouble() - 0.5D, 0.0D,
                    this.random.nextDouble() - 0.5D);
        }
        Vec3 randomDirection = randomHorizontalDirection();
        away = new Vec3(away.x, 0.0D, away.z).normalize().scale(0.85D)
                .add(randomDirection.scale(0.15D)).normalize();
        this.flyingTarget = this.position().add(away.scale(96.0D)).add(0.0D, 8.0D, 0.0D);
    }

    private void flyToward(@Nullable Vec3 target) {
        if (target == null) {
            chooseRoamingFlightTarget();
            target = this.flyingTarget;
        }
        target = limitFlightTargetAltitude(target);
        if (this.flyingTarget != null) {
            this.flyingTarget = limitFlightTargetAltitude(this.flyingTarget);
        }
        Vec3 delta = target.subtract(this.position());
        if (delta.lengthSqr() < 0.1D) {
            return;
        }

        Vec3 movement = delta.normalize().scale(FLY_SPEED);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.65D).add(movement.scale(0.35D)));
        this.setYRot((float) (Mth.atan2(this.getDeltaMovement().z, this.getDeltaMovement().x) * Mth.RAD_TO_DEG) - 90.0F);
        this.yBodyRot = this.getYRot();

        if (delta.y > 1.0D) {
            this.descentTransitionTicks = 0;
            setAnimation(VultureAnimation.RISING);
        } else if (delta.y < -1.0D) {
            if (this.descentTransitionTicks <= 0
                    && this.getVultureAnimation() != VultureAnimation.DEPLOY_LANDING_GEAR
                    && this.getVultureAnimation() != VultureAnimation.FALLING) {
                this.descentTransitionTicks = DEPLOY_LANDING_GEAR_TICKS;
            }
            if (this.descentTransitionTicks > 0) {
                this.descentTransitionTicks--;
                setAnimation(VultureAnimation.DEPLOY_LANDING_GEAR);
            } else {
                setAnimation(VultureAnimation.FALLING);
            }
        } else {
            this.descentTransitionTicks = 0;
            setAnimation(VultureAnimation.SOARING);
        }
    }

    private Vec3 limitFlightTargetAltitude(Vec3 target) {
        double maxY = getMaximumFlightY(target.x, target.z);
        return target.y > maxY ? new Vec3(target.x, maxY, target.z) : target;
    }

    private double getMaximumFlightY(double x, double z) {
        int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mth.floor(x), Mth.floor(z));
        int maximumVisibleHeight = Math.max(10, this.getType().clientTrackingRange() * 16 - 1);
        return Math.min(this.level().getMaxBuildHeight() - 1, groundY + maximumVisibleHeight);
    }

    private void takeOff() {
        this.guardDismounted = false;
        boolean perchedTakeoff = this.isArmPerched()
                || this.brainState == BrainState.PERCHED_ARM
                || this.brainState == BrainState.PERCHED_LOG
                || this.brainState == BrainState.EATING_ITEM
                || (!this.isNoGravity() && this.onGround());
        this.setNoGravity(true);
        this.meatTheftPlayer = null;
        this.meatTheftFleeTicks = 0;
        this.armIdleAnimationTicks = 0;
        this.armIdleCooldownTicks = 0;
        this.armPlayerStillTicks = 0;
        this.targetItemId = -1;
        this.getNavigation().stop();
        this.brainState = BrainState.TAKEOFF;
        this.takeoffDirection = randomHorizontalDirection();
        this.actionTicks = RAISE_LANDING_GEAR_TICKS + (perchedTakeoff ? TAKEOFF_PERCHED_FLAP_TICKS : 0);
        this.flyingTarget = this.position().add(this.takeoffDirection.scale(12.0D)).add(0.0D, 6.0D, 0.0D);
        if (perchedTakeoff) {
            this.setDeltaMovement(Vec3.ZERO);
            this.playSound(ModSounds.VULTURE_TAKE_OFF.get(), 1.2F, 0.95F + this.random.nextFloat() * 0.1F);
            setAnimation(VultureAnimation.FLAP_WINGS);
        } else {
            releaseTakeoffPerch();
            setAnimation(VultureAnimation.RAISE_LANDING_GEAR);
        }
    }

    private Vec3 randomHorizontalDirection() {
        double angle = this.random.nextDouble() * Mth.TWO_PI;
        return new Vec3(Mth.cos((float) angle), 0.0D, Mth.sin((float) angle));
    }

    private void releaseTakeoffPerch() {
        setArmPerchPlayerUUID(null);
        this.entityData.set(ARM_PERCHED, false);
        this.perchPos = null;
    }

    @Nullable
    private Player findPlayerByUuid(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(uuid);
        }
        return this.level().getPlayerByUUID(uuid);
    }

    @Nullable
    private BlockPos findCrudLogNear(BlockPos origin, int radiusChunks) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        int minY = Math.max(this.level().getMinBuildHeight(), origin.getY() - 40);
        int maxY = Math.min(this.level().getMaxBuildHeight() - 1, origin.getY() + 40);

        for (int chunkX = originChunkX - radiusChunks; chunkX <= originChunkX + radiusChunks; chunkX++) {
            for (int chunkZ = originChunkZ - radiusChunks; chunkZ <= originChunkZ + radiusChunks; chunkZ++) {
                for (int y = maxY; y >= minY; y--) {
                    for (int x = chunkX << 4; x < (chunkX << 4) + 16; x++) {
                        for (int z = chunkZ << 4; z < (chunkZ << 4) + 16; z++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (isGoodPerch(pos)) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isGoodPerch(BlockPos logPos) {
        return this.level().getBlockState(logPos).is(ModBlocks.CRUD_LOG.get())
                && this.level().getBlockState(logPos.above()).getCollisionShape(this.level(), logPos.above()).isEmpty()
                && !isPerchOccupied(logPos.above());
    }

    private boolean isPerchOccupied(BlockPos perch) {
        AABB searchArea = new AABB(perch).inflate(96.0D);
        return !this.level().getEntitiesOfClass(VultureEntity.class, searchArea, vulture ->
                vulture != this
                        && vulture.isAlive()
                        && ((perch.equals(vulture.perchPos)
                        && (vulture.brainState == BrainState.LANDING_TO_LOG
                        || vulture.brainState == BrainState.PERCHED_LOG))
                        || (vulture.brainState == BrainState.PERCHED_LOG
                        && perch.equals(vulture.blockPosition())))
        ).isEmpty();
    }

    private boolean isFlyingState() {
        if (this.isBaby()) {
            return false;
        }
        return this.isNoGravity() || this.brainState == BrainState.FLYING || this.brainState == BrainState.LANDING_TO_LOG
                || this.brainState == BrainState.LANDING_TO_ITEM || this.brainState == BrainState.FLEEING_NIGHT
                || this.brainState == BrainState.FOLLOWING_OWNER || this.brainState == BrainState.DEATH_COURIER
                || this.brainState == BrainState.TAKEOFF || this.brainState == BrainState.LANDING_TO_ARM
                || this.brainState == BrainState.LANDING_TO_MEAT_PLAYER
                || this.brainState == BrainState.FLEEING_WITH_MEAT;
    }

    private void setAnimation(VultureAnimation animation) {
        this.entityData.set(ANIMATION, animation.ordinal());
    }

    private void refreshAnimationStates() {
        VultureAnimation current = getVultureAnimation();
        for (VultureAnimation animation : VultureAnimation.values()) {
            AnimationState state = this.animationStates.get(animation);
            if (state == null) {
                continue;
            }
            if (animation == current) {
                state.startIfStopped(this.tickCount);
            } else {
                state.stop();
            }
        }
    }

    private enum BrainState {
        TAKEOFF,
        FLYING,
        LANDING_TO_LOG,
        PERCHED_LOG,
        LANDING_TO_ITEM,
        WALKING_TO_ITEM,
        EATING_ITEM,
        PERCHED_ARM,
        FLEEING_NIGHT,
        FOLLOWING_OWNER,
        DEATH_COURIER,
        LANDING_TO_ARM,
        LANDING_TO_MEAT_PLAYER,
        FLEEING_WITH_MEAT,
        DOMESTICATED_GROUND;

        private static BrainState byId(int id) {
            BrainState[] values = values();
            return values[Mth.clamp(id, 0, values.length - 1)];
        }
    }

    private enum GuardCommand {
        NONE,
        FOLLOW,
        SEARCH_CRUD_TREE,
        SEARCH_FLOATING_ISLAND,
        FLY_HOME,
        SEARCH_REMAINS_OF_A_VILLAGE;

        private static GuardCommand byId(int id) {
            GuardCommand[] values = values();
            return values[Mth.clamp(id, 0, values.length - 1)];
        }
    }
}
