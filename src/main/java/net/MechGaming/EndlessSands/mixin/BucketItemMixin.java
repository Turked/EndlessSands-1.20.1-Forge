package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.Lavalogging;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Shadow
    @Final
    private Fluid content;

    @Shadow
    protected abstract void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos);

    @Inject(
            method = "emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void endlesssands$lavalogBlock(
            @Nullable Player player,
            Level level,
            BlockPos pos,
            @Nullable BlockHitResult hitResult,
            @Nullable ItemStack container,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.content != Fluids.LAVA) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LiquidBlockContainer liquidContainer)
                || !Lavalogging.isSupported(state)
                || !liquidContainer.canPlaceLiquid(level, pos, state, Fluids.LAVA)) {
            return;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, Lavalogging.withFluid(state, Fluids.LAVA), 3);
            level.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
        }
        this.playEmptySound(player, level, pos);
        cir.setReturnValue(true);
    }
}
