package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.block.custom.LinedStairBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public abstract class LinedStairFlowingFluidMixin {
    @Shadow
    protected abstract FluidState getNewLiquid(Level level, BlockPos pos, BlockState state);

    @Shadow
    protected abstract int getSpreadDelay(Level level, BlockPos pos, FluidState oldState, FluidState newState);

    @Shadow
    protected abstract void spread(Level level, BlockPos pos, FluidState state);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void endlessSands$tickInsideLinedStair(
            Level level,
            BlockPos pos,
            FluidState scheduledState,
            CallbackInfo callback
    ) {
        BlockState hostState = level.getBlockState(pos);
        if (!(hostState.getBlock() instanceof LinedStairBlock)) {
            return;
        }

        FluidState currentState = scheduledState;
        if (!scheduledState.isSource()) {
            FluidState nextState = getNewLiquid(level, pos, hostState);
            int delay = getSpreadDelay(level, pos, scheduledState, nextState);

            if (nextState.isEmpty()) {
                currentState = nextState;
                level.setBlock(pos, LinedStairBlock.withFluidState(hostState, nextState), 3);
            } else if (!nextState.equals(scheduledState)) {
                currentState = nextState;
                BlockState updatedHost = LinedStairBlock.withFluidState(hostState, nextState);
                level.setBlock(pos, updatedHost, 2);
                level.scheduleTick(pos, nextState.getType(), delay);
                level.updateNeighborsAt(pos, updatedHost.getBlock());
            }
        }

        spread(level, pos, currentState);
        callback.cancel();
    }
}
