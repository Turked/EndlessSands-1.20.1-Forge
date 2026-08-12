package net.MechGaming.EndlessSands.entity.custom;

import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.worldgen.biome.ModBiomes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class VultureEggProjectileEntity extends ThrowableItemProjectile {
    public VultureEggProjectileEntity(EntityType<? extends VultureEggProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public VultureEggProjectileEntity(Level level, LivingEntity owner) {
        super(ModEntities.VULTURE_EGG_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.VULTURE_EGG.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextFloat() - 0.5D) * 0.08D,
                        (this.random.nextFloat() - 0.5D) * 0.08D,
                        (this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }

        if (this.level().isDay() && this.level().getBiome(this.blockPosition()).is(ModBiomes.ENDLESS_DESERT)) {
            this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(),
                    new ItemStack(ModItems.HANDFUL_OF_SCRAMBLED_EGGS.get())));
        }
        this.level().broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }
}
