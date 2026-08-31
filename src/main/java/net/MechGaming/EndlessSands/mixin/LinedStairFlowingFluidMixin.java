package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.block.custom.LinedStairBlock;
import net.MechGaming.EndlessSands.block.custom.ZenioniteStairBlock;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity.FlowContext;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity.Lane;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class LinedStairFlowingFluidMixin {
    @Shadow
    protected abstract FluidState getNewLiquid(Level level, BlockPos pos, BlockState state);

    @Shadow
    protected abstract int getSpreadDelay(Level level, BlockPos pos, FluidState oldState, FluidState newState);

    @Shadow
    protected abstract void spread(Level level, BlockPos pos, FluidState state);

    @Shadow
    protected abstract int getDropOff(LevelReader level);

    @Inject(method = "spreadTo", at = @At("HEAD"), cancellable = true)
    private void endlessSands$placeIntoZenioniteStairLane(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            Direction movement,
            FluidState incoming,
            CallbackInfo callback
    ) {
        if (state.getBlock() instanceof ZenioniteStairBlock stair) {
            stair.placeLiquidFromFlow(level, pos, state, movement, incoming);
            callback.cancel();
        }
    }

    @Inject(method = "canSpreadTo", at = @At("HEAD"), cancellable = true)
    private void endlessSands$keepZenioniteFlowInItsLane(
            BlockGetter level,
            BlockPos fromPos,
            BlockState fromState,
            Direction movement,
            BlockPos toPos,
            BlockState toState,
            FluidState toFluid,
            Fluid fluid,
            CallbackInfoReturnable<Boolean> callback
    ) {
        FlowContext context = ZenioniteStairBlockEntity.activeFlow();
        if (context == null || !context.pos().equals(fromPos) || !movement.getAxis().isHorizontal()
                || !(fromState.getBlock() instanceof ZenioniteStairBlock)) {
            return;
        }

        if (!ZenioniteStairBlock.canLaneSpreadToward(fromState, context.lane(), movement)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "getNewLiquid", at = @At("RETURN"), cancellable = true)
    private void endlessSands$includeActiveZenioniteLaneAsNeighbor(
            Level level,
            BlockPos pos,
            BlockState state,
            CallbackInfoReturnable<FluidState> callback
    ) {
        FlowContext context = ZenioniteStairBlockEntity.activeFlow();
        if (context == null || !context.fluidState().getType().isSame((Fluid) (Object) this)) {
            return;
        }

        BlockPos sourcePos = context.pos();
        FluidState candidate;
        if (pos.equals(sourcePos.below())) {
            candidate = ((FlowingFluid) (Object) this).getFlowing(8, true);
        } else {
            int horizontalDistance = Math.abs(pos.getX() - sourcePos.getX())
                    + Math.abs(pos.getZ() - sourcePos.getZ());
            if (pos.getY() != sourcePos.getY() || horizontalDistance != 1) {
                return;
            }

            FluidState sourceFluid = context.fluidState();
            int amount = sourceFluid.hasProperty(FlowingFluid.FALLING)
                    && sourceFluid.getValue(FlowingFluid.FALLING)
                    ? 7
                    : sourceFluid.getAmount() - getDropOff(level);
            if (amount <= 0) {
                return;
            }
            candidate = ((FlowingFluid) (Object) this).getFlowing(Mth.clamp(amount, 1, 8), false);
        }

        if (fluidStrength(candidate) > fluidStrength(callback.getReturnValue())) {
            callback.setReturnValue(candidate);
        }
    }

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

    private static int fluidStrength(FluidState state) {
        return state.isEmpty() ? 0 : state.isSource() ? 9 : state.getAmount();
    }
}
