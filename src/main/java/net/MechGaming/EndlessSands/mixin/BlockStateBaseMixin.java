package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.Lavalogging;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow
    private FluidState fluidState;

    @Inject(method = "initCache", at = @At("TAIL"))
    private void endlesssands$cacheLavaFluidState(CallbackInfo ci) {
        BlockState state = (BlockState) (Object) this;
        if (Lavalogging.isLavaLogged(state)) {
            this.fluidState = Fluids.LAVA.getSource(false);
        }
    }

    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void endlesssands$emitLavaLight(CallbackInfoReturnable<Integer> cir) {
        if (Lavalogging.isLavaLogged((BlockState) (Object) this)) {
            cir.setReturnValue(15);
        }
    }

    @Inject(method = "updateShape", at = @At("HEAD"))
    private void endlesssands$scheduleLavaTick(
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (Lavalogging.isLavaLogged((BlockState) (Object) this)) {
            level.scheduleTick(currentPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
        }
    }
}
