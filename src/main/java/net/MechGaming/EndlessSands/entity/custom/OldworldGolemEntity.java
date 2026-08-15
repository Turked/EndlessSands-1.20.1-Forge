package net.MechGaming.EndlessSands.entity.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class OldworldGolemEntity extends IronGolem {
    private static final EntityDataAccessor<Integer> ANIMATION =
            SynchedEntityData.defineId(OldworldGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIMATION_SERIAL =
            SynchedEntityData.defineId(OldworldGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> HELD_ROSE =
            SynchedEntityData.defineId(OldworldGolemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> SAPLING_REMOVED =
            SynchedEntityData.defineId(OldworldGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double WARDEN_HEALTH = 500.0D;
    private static final double HALF_WARDEN_ATTACK_DAMAGE = 15.0D;
    private static final int ATTACK_TICKS = 60;
    private static final int INSPECT_ROSE_TICKS = 30;
    private static final int REMOVE_SAPLING_TICKS = 100;
    private static final int SAPLING_REMOVAL_TICK = 80;
    private static final double FOLLOW_ROSE_RANGE = 16.0D;
    private static final double FOLLOW_ROSE_STOP_DISTANCE_SQR = 6.25D;

    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState inspectRoseAnimationState = new AnimationState();
    public final AnimationState removeSaplingAnimationState = new AnimationState();

    private final EnumMap<OldworldGolemAnimation, AnimationState> animationStates =
            new EnumMap<>(OldworldGolemAnimation.class);
    private int animationTicks;
    private int lastAnimationSerial = -1;
    @Nullable
    private LivingEntity delayedAttackTarget;
    @Nullable
    private UUID saplingRemovalLookPlayer;

    public OldworldGolemEntity(EntityType<? extends OldworldGolemEntity> entityType, Level level) {
        super(entityType, level);
        this.animationStates.put(OldworldGolemAnimation.WALK, this.walkAnimationState);
        this.animationStates.put(OldworldGolemAnimation.ATTACK, this.attackAnimationState);
        this.animationStates.put(OldworldGolemAnimation.INSPECT_ROSE, this.inspectRoseAnimationState);
        this.animationStates.put(OldworldGolemAnimation.REMOVE_SAPLING, this.removeSaplingAnimationState);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            public boolean canUse() {
                return !OldworldGolemEntity.this.isBusyAnimationActive() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !OldworldGolemEntity.this.isBusyAnimationActive() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F) {
            @Override
            public boolean canUse() {
                return !OldworldGolemEntity.this.isBusyAnimationActive() && super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new FollowRoseHolderGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D) {
            @Override
            public boolean canUse() {
                return !OldworldGolemEntity.this.isBusyAnimationActive() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false, entity -> entity instanceof Enemy));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false,
                entity -> entity instanceof Player player && this.shouldAttackPlayer(player)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, WARDEN_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, HALF_WARDEN_ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION, OldworldGolemAnimation.IDLE.ordinal());
        this.entityData.define(ANIMATION_SERIAL, 0);
        this.entityData.define(HELD_ROSE, ItemStack.EMPTY);
        this.entityData.define(SAPLING_REMOVED, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if ((ANIMATION.equals(key) || ANIMATION_SERIAL.equals(key)) && this.level().isClientSide) {
            this.refreshAnimationStates();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.refreshAnimationStates();
            return;
        }

        this.clearInvalidPlayerTarget();
        this.tickServerAnimation();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.isBusyAnimationActive() || !(target instanceof LivingEntity livingTarget)) {
            return false;
        }
        this.delayedAttackTarget = livingTarget;
        this.startTimedAnimation(OldworldGolemAnimation.ATTACK, ATTACK_TICKS);
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        return true;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4 && this.level().isClientSide) {
            this.startClientAnimation(OldworldGolemAnimation.ATTACK);
        }
        super.handleEntityEvent(id);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player player && !this.shouldAttackPlayer(player)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        if (type == EntityType.CREEPER || type == EntityType.PLAYER) {
            return true;
        }
        return super.canAttackType(type);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isRose(stack)) {
            return super.mobInteract(player, hand);
        }

        if (this.hasRose()) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.level().isClientSide) {
            this.acceptRose(player, stack);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ItemStack heldRose = this.getHeldRoseStack();
        if (!heldRose.isEmpty()) {
            tag.put("HeldRose", heldRose.save(new CompoundTag()));
        }
        tag.putBoolean("SaplingRemoved", this.isSaplingRemoved());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(HELD_ROSE, ItemStack.of(tag.getCompound("HeldRose")));
        this.entityData.set(SAPLING_REMOVED, tag.getBoolean("SaplingRemoved"));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    public OldworldGolemAnimation getGolemAnimation() {
        return OldworldGolemAnimation.byId(this.entityData.get(ANIMATION));
    }

    public ItemStack getHeldRoseStack() {
        return this.entityData.get(HELD_ROSE);
    }

    public boolean hasRose() {
        return !this.getHeldRoseStack().isEmpty();
    }

    public boolean isSaplingRemoved() {
        return this.entityData.get(SAPLING_REMOVED);
    }

    public static boolean isRose(ItemStack stack) {
        return stack.is(ModItems.OLDWORLD_POPPY.get())
                || stack.is(Items.POPPY)
                || stack.is(Items.ROSE_BUSH)
                || stack.is(Items.WITHER_ROSE);
    }

    private void acceptRose(Player player, ItemStack stack) {
        ItemStack rose = stack.copyWithCount(1);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.entityData.set(HELD_ROSE, rose);
        this.saplingRemovalLookPlayer = player.getUUID();
        this.setTarget(null);
        this.getNavigation().stop();
        this.startTimedAnimation(OldworldGolemAnimation.INSPECT_ROSE, INSPECT_ROSE_TICKS);
    }

    private void tickServerAnimation() {
        OldworldGolemAnimation animation = this.getGolemAnimation();
        if (this.animationTicks > 0) {
            if (this.isBusyAnimationActive()) {
                this.getNavigation().stop();
            }
            if (this.isInteractionAnimationActive()) {
                this.setTarget(null);
            }
            if (animation == OldworldGolemAnimation.ATTACK && this.delayedAttackTarget != null) {
                this.getLookControl().setLookAt(this.delayedAttackTarget, 30.0F, 30.0F);
            } else if (animation == OldworldGolemAnimation.REMOVE_SAPLING) {
                this.lookAtSaplingRemovalPlayer();
                int elapsedTicks = REMOVE_SAPLING_TICKS - this.animationTicks + 1;
                if (elapsedTicks >= SAPLING_REMOVAL_TICK) {
                    this.removeEmbeddedSapling();
                }
            }

            this.animationTicks--;
            if (this.animationTicks <= 0) {
                this.finishTimedAnimation(animation);
            }
            return;
        }

        boolean walking = this.getDeltaMovement().horizontalDistanceSqr() > 0.0004D;
        this.switchAnimation(walking ? OldworldGolemAnimation.WALK : OldworldGolemAnimation.IDLE);
    }

    private void finishTimedAnimation(OldworldGolemAnimation animation) {
        if (animation == OldworldGolemAnimation.ATTACK) {
            this.performDelayedAttack();
            this.delayedAttackTarget = null;
            this.switchAnimation(OldworldGolemAnimation.IDLE);
            return;
        }
        if (animation == OldworldGolemAnimation.INSPECT_ROSE) {
            this.startTimedAnimation(OldworldGolemAnimation.REMOVE_SAPLING, REMOVE_SAPLING_TICKS);
            return;
        }
        if (animation == OldworldGolemAnimation.REMOVE_SAPLING) {
            this.removeEmbeddedSapling();
            this.saplingRemovalLookPlayer = null;
        }
        this.switchAnimation(OldworldGolemAnimation.IDLE);
    }

    private void removeEmbeddedSapling() {
        if (this.isSaplingRemoved()) {
            return;
        }
        this.entityData.set(SAPLING_REMOVED, true);
        this.spawnAtLocation(new ItemStack(ModBlocks.OLDWORLD_SAPLING.get()), 1.5F);
    }

    private void performDelayedAttack() {
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        LivingEntity target = this.delayedAttackTarget;
        if (target == null || !target.isAlive() || !this.canAttack(target)
                || this.distanceToSqr(target) > getDelayedAttackReachSqr(target)) {
            return;
        }

        boolean hurt = target.hurt(this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            double knockbackResistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            double knockbackMultiplier = Math.max(0.0D, 1.0D - knockbackResistance);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.4D * knockbackMultiplier, 0.0D));
            this.doEnchantDamageEffects(this, target);
        }
    }

    private double getDelayedAttackReachSqr(LivingEntity target) {
        double reach = this.getBbWidth() * 2.5D + target.getBbWidth();
        return reach * reach;
    }

    private void lookAtSaplingRemovalPlayer() {
        if (this.saplingRemovalLookPlayer == null) {
            return;
        }
        Player player = this.level().getPlayerByUUID(this.saplingRemovalLookPlayer);
        if (player != null) {
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
    }

    private void clearInvalidPlayerTarget() {
        LivingEntity target = this.getTarget();
        if (target instanceof Player player && !this.shouldAttackPlayer(player)) {
            this.setTarget(null);
            this.getNavigation().stop();
        }
    }

    private boolean shouldAttackPlayer(Player player) {
        if (this.hasRose()) {
            return false;
        }
        return !isProtectedByRose(player);
    }

    private static boolean isProtectedByRose(Player player) {
        return player.isShiftKeyDown()
                && (isRose(player.getMainHandItem()) || isRose(player.getOffhandItem()));
    }

    private boolean isInteractionAnimationActive() {
        OldworldGolemAnimation animation = this.getGolemAnimation();
        return this.animationTicks > 0
                && (animation == OldworldGolemAnimation.INSPECT_ROSE
                || animation == OldworldGolemAnimation.REMOVE_SAPLING);
    }

    private boolean isBusyAnimationActive() {
        OldworldGolemAnimation animation = this.getGolemAnimation();
        return this.animationTicks > 0
                && (animation == OldworldGolemAnimation.ATTACK
                || animation == OldworldGolemAnimation.INSPECT_ROSE
                || animation == OldworldGolemAnimation.REMOVE_SAPLING);
    }

    private void startTimedAnimation(OldworldGolemAnimation animation, int ticks) {
        this.animationTicks = ticks;
        this.setAnimationData(animation, true);
    }

    private void switchAnimation(OldworldGolemAnimation animation) {
        if (this.getGolemAnimation() != animation) {
            this.setAnimationData(animation, false);
        }
    }

    private void setAnimationData(OldworldGolemAnimation animation, boolean forceRestart) {
        boolean changed = this.entityData.get(ANIMATION) != animation.ordinal();
        if (changed) {
            this.entityData.set(ANIMATION, animation.ordinal());
        }
        if (changed || forceRestart) {
            this.entityData.set(ANIMATION_SERIAL, this.entityData.get(ANIMATION_SERIAL) + 1);
        }
    }

    private void startClientAnimation(OldworldGolemAnimation animation) {
        this.entityData.set(ANIMATION, animation.ordinal());
        this.entityData.set(ANIMATION_SERIAL, this.entityData.get(ANIMATION_SERIAL) + 1);
        this.refreshAnimationStates();
    }

    private void refreshAnimationStates() {
        OldworldGolemAnimation current = this.getGolemAnimation();
        int serial = this.entityData.get(ANIMATION_SERIAL);
        boolean restart = serial != this.lastAnimationSerial;
        this.lastAnimationSerial = serial;

        for (OldworldGolemAnimation animation : OldworldGolemAnimation.values()) {
            AnimationState state = this.animationStates.get(animation);
            if (state == null) {
                continue;
            }
            if (animation == current) {
                if (restart) {
                    state.stop();
                    state.start(this.tickCount);
                } else {
                    state.startIfStopped(this.tickCount);
                }
            } else {
                state.stop();
            }
        }
    }

    private static class FollowRoseHolderGoal extends Goal {
        private final OldworldGolemEntity golem;
        private final double speedModifier;
        @Nullable
        private Player targetPlayer;
        private int pathRecalculationTicks;

        private FollowRoseHolderGoal(OldworldGolemEntity golem, double speedModifier) {
            this.golem = golem;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.golem.hasRose() || this.golem.isBusyAnimationActive() || this.golem.getTarget() != null) {
                return false;
            }
            this.targetPlayer = this.findRoseHolder();
            return this.targetPlayer != null
                    && this.golem.distanceToSqr(this.targetPlayer) > FOLLOW_ROSE_STOP_DISTANCE_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPlayer != null
                    && this.targetPlayer.isAlive()
                    && this.golem.hasRose()
                    && !this.golem.isBusyAnimationActive()
                    && this.golem.getTarget() == null
                    && isHoldingRose(this.targetPlayer)
                    && this.golem.distanceToSqr(this.targetPlayer) > FOLLOW_ROSE_STOP_DISTANCE_SQR;
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }
            this.golem.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
            if (--this.pathRecalculationTicks <= 0) {
                this.pathRecalculationTicks = 10;
                this.golem.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
            }
        }

        @Nullable
        private Player findRoseHolder() {
            List<Player> players = this.golem.level().getEntitiesOfClass(
                    Player.class,
                    this.golem.getBoundingBox().inflate(FOLLOW_ROSE_RANGE),
                    FollowRoseHolderGoal::isHoldingRose);
            Player closest = null;
            double closestDistance = Double.MAX_VALUE;
            for (Player player : players) {
                double distance = this.golem.distanceToSqr(player);
                if (distance < closestDistance) {
                    closest = player;
                    closestDistance = distance;
                }
            }
            return closest;
        }

        private static boolean isHoldingRose(Player player) {
            return OldworldGolemEntity.isRose(player.getMainHandItem())
                    || OldworldGolemEntity.isRose(player.getOffhandItem());
        }
    }
}
