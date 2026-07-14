package net.MechGaming.EndlessSands.entity.custom;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PocketSandProjectileEntity extends ThrowableItemProjectile {
    public PocketSandProjectileEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public PocketSandProjectileEntity(Level pLevel) {
        super(ModEntities.POCKET_SAND_PROJECTILE.get(), pLevel);
    }
    public PocketSandProjectileEntity(Level pLevel, LivingEntity livingEntity) {
        super(ModEntities.POCKET_SAND_PROJECTILE.get(), livingEntity, pLevel);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CURSED_POCKET_SAND.get();
    }

    //I want to change cursed sand so it's layered like snow
    // Then throwing this projectile will spawn/grow a layer
    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);

        if(!this.level().isClientSide()){
            placeOrGrowLayer(pResult);
        }

    }

    private void placeOrGrowLayer(BlockHitResult result){
        BlockPos hitPos = result.getBlockPos();
        BlockState hitState = this.level().getBlockState(hitPos);

        if (hitState.is(ModBlocks.CURSED_SAND_LAYER.get())){
            growLayer(hitPos, hitState);
            return;
        }

        BlockPos placePos = hitPos.relative(result.getDirection());
        BlockState placeState = this.level().getBlockState(placePos);

        if (placeState.is(ModBlocks.CURSED_SAND_LAYER.get())){
            growLayer(placePos, placeState);
        } else if (placeState.canBeReplaced()) {
            this.level().setBlock(placePos, ModBlocks.CURSED_SAND_LAYER.get().defaultBlockState(), 3);
        }
    }

    private void growLayer(BlockPos pos, BlockState state){
        int layers = state.getValue(CursedSandLayerBlock.LAYERS);

        if (layers >= 3){
            this.level().setBlock(pos, ModBlocks.CURSED_SAND.get().defaultBlockState(), 3);
        }else {
            this.level().setBlock(pos, state.setValue(CursedSandLayerBlock.LAYERS, layers + 1), 3);
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);

        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);

        if (!this.level().isClientSide() && pResult.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0));
        }
    }
}
