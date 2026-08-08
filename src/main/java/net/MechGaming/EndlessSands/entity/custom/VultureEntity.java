package net.MechGaming.EndlessSands.entity.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.sound.ModSounds;
import net.MechGaming.EndlessSands.util.ModTags;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.ServerLevelAccessor;
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

    public static final int BABY_GROW_TICKS = 20 * 60 * 60;
    private static final int PLAYER_FOLLOW_TAME_TICKS = 20 * 60 * 30;
    private static final int SCAN_INTERVAL = 30;
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
    private static final double FLY_SPEED = 0.32D;
    private static final double COURIER_BLOCKS_PER_SECOND = 16.0D;
    private static final double ARM_PERCH_SIDE_OFFSET = 0.34D;
    private static final double ARM_PERCH_FORWARD_OFFSET = 0.62D;
    private static final double ARM_PERCH_TOP_OFFSET = 0.06D;
    private static final double ARM_APPROACH_HITBOX_RADIUS = 0.24D;

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
    private BlockPos perchPos;
    @Nullable
    private Vec3 flyingTarget;
    @Nullable
    private UUID courierPlayer;
    @Nullable
    private UUID babyFollowPlayer;
    @Nullable
    private UUID meatTheftPlayer;

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
    private boolean daySpawned;
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
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, FLY_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
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

        if (this.brainState != BrainState.TAKEOFF && getArmPerchPlayerUUID() != null) {
            boolean active = this.brainState == BrainState.LANDING_TO_ARM
                    ? tickLandingToArm()
                    : tickArmPerch();
            if (active) {
                return;
            }
            takeOff();
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
        if (hurt && this.isTame()) {
            setArmPerchPlayerUUID(null);
            this.entityData.set(ARM_PERCHED, false);
            this.brainState = BrainState.FOLLOWING_OWNER;
            this.setNoGravity(true);
        }
        if (hurt && !this.level().isDay() && !this.isTame()) {
            this.hurtNightTicks = 20 * 60 * 10;
            takeOff();
        }
        return hurt;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
        if (spawnType == MobSpawnType.STRUCTURE || spawnType == MobSpawnType.NATURAL) {
            this.setNoGravity(true);
            setAnimation(VultureAnimation.SOARING);
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
        }
        return baby;
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

    public void setHomeNestPos(@Nullable BlockPos homeNestPos) {
        this.homeNestPos = homeNestPos;
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
        tag.putInt(BRAIN_STATE, this.brainState.ordinal());
        tag.putInt(BABY_PLAYER_TICKS, this.babyPlayerFollowTicks);
        tag.putInt(HURT_NIGHT_TICKS, this.hurtNightTicks);
        tag.putInt(COURIER_DELAY, this.courierDelayTicks);
        tag.putInt(MEAT_THEFT_FLEE_TICKS, this.meatTheftFleeTicks);
        tag.putBoolean(DAY_SPAWNED, this.daySpawned);
        tag.put(MOUTH_ITEM, this.getMouthStack().save(new CompoundTag()));
        tag.put(LEFT_TALON_ITEM, this.getLeftTalonStack().save(new CompoundTag()));
        tag.put(RIGHT_TALON_ITEM, this.getRightTalonStack().save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(HOME_X)) {
            this.homeNestPos = new BlockPos(tag.getInt(HOME_X), tag.getInt(HOME_Y), tag.getInt(HOME_Z));
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
        this.brainState = BrainState.byId(tag.getInt(BRAIN_STATE));
        this.babyPlayerFollowTicks = tag.getInt(BABY_PLAYER_TICKS);
        this.hurtNightTicks = tag.getInt(HURT_NIGHT_TICKS);
        this.courierDelayTicks = tag.getInt(COURIER_DELAY);
        this.meatTheftFleeTicks = tag.getInt(MEAT_THEFT_FLEE_TICKS);
        this.daySpawned = tag.getBoolean(DAY_SPAWNED);
        this.entityData.set(MOUTH_STACK, ItemStack.of(tag.getCompound(MOUTH_ITEM)));
        this.entityData.set(LEFT_TALON_STACK, ItemStack.of(tag.getCompound(LEFT_TALON_ITEM)));
        this.entityData.set(RIGHT_TALON_STACK, ItemStack.of(tag.getCompound(RIGHT_TALON_ITEM)));
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

        if (!this.level().isDay() && this.daySpawned && !this.isTame()) {
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
        if (!this.isTame()) {
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
            if (this.level().getBlockState(target.below()).is(ModBlocks.CRUD_LOG.get()) && this.level().noCollision(this, this.getBoundingBox().move(Vec3.atBottomCenterOf(target).subtract(this.position())))) {
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
            this.entityData.set(MOUTH_STACK, item.getItem().copyWithCount(1));
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

        Entity entity = this.level().getEntity(this.targetItemId);
        if (entity instanceof ItemEntity item && item.isAlive() && isMeat(item.getItem())) {
            item.getItem().shrink(1);
            if (item.getItem().isEmpty()) {
                item.discard();
            }
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

        if (!creativeGuard && !this.isTame() && this.actionTicks <= 0) {
            ItemStack meat = player.getMainHandItem();
            this.entityData.set(MOUTH_STACK, meat.copyWithCount(1));
            this.actionTicks = EATING_TICKS;
        } else if (!creativeGuard && !this.isTame()) {
            this.actionTicks--;
            playEatingSoundIfDue();
            if (this.actionTicks <= 0) {
                ItemStack meat = player.getMainHandItem();
                if (!meat.isEmpty() && isMeat(meat)) {
                    if (!player.getAbilities().instabuild) {
                        meat.shrink(1);
                    }
                    tryTameFromFood(player, this.entityData.get(MOUTH_STACK));
                }
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
        if (!player.isAlive() || !hasAnyArmGuard(player)) {
            return false;
        }
        boolean creativeGuard = isCreativeArmGuard(player.getOffhandItem());
        boolean ownerPerch = this.isTame() && player.getUUID().equals(this.getOwnerUUID());
        return creativeGuard || ownerPerch || isMeat(player.getMainHandItem());
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
        float chance = Mth.clamp((saturation / 12.8F) / 3.0F, 0.04F, 1.0F / 3.0F);
        if (this.random.nextFloat() < chance) {
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
        int x = Mth.floor(this.getX()) + this.random.nextInt(33) - 16;
        int z = Mth.floor(this.getZ()) + this.random.nextInt(33) - 16;
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
                && this.level().getBlockState(logPos.above()).getCollisionShape(this.level(), logPos.above()).isEmpty();
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
        FLEEING_WITH_MEAT;

        private static BrainState byId(int id) {
            BrainState[] values = values();
            return values[Mth.clamp(id, 0, values.length - 1)];
        }
    }
}
