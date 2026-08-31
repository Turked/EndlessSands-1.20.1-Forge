package net.MechGaming.EndlessSands.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FlowingFluid.class)
public interface FlowingFluidAccessor {
    @Invoker("spread")
    void endlessSands$spread(Level level, BlockPos pos, FluidState state);

    @Invoker("getDropOff")
    int endlessSands$getDropOff(LevelReader level);

    @Invoker("canPassThroughWall")
    boolean endlessSands$canPassThroughWall(
            Direction direction,
            BlockGetter level,
            BlockPos fromPos,
            BlockState fromState,
            BlockPos toPos,
            BlockState toState
    );
}
