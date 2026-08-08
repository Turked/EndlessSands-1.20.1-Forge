package net.MechGaming.EndlessSands.item.custom;

import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class VultureSpawnEggItem extends Item {
    public VultureSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        VultureEntity vulture = ModEntities.VULTURE.get().create(level);
        if (vulture == null) {
            return InteractionResult.FAIL;
        }

        vulture.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );

        if (level instanceof ServerLevel serverLevel) {
            vulture.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWN_EGG, null, null);
        }

        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            vulture.setAge(-VultureEntity.BABY_GROW_TICKS);
        }

        boolean added = level.addFreshEntity(vulture);
        if (!added) {
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
