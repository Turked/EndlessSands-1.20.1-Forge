package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.Lavalogging;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void endlesssands$burnLavaloggedHost(
            Level level,
            BlockPos pos,
            FluidState fluidState,
            RandomSource random,
            CallbackInfo ci
    ) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (!Lavalogging.isLavaLogged(state) || !endlesssands$canBurn(state, level, pos)) {
            return;
        }

        // Lava only attempts to start nearby fires on a random fluid tick. Use the
        // same two-in-three gate before consuming a flammable block holding lava.
        if (random.nextInt(3) == 0) {
            return;
        }

        state.onCaughtFire(level, pos, Direction.UP, null);
        BlockState stateAfterFireCallback = level.getBlockState(pos);
        if (Lavalogging.isLavaLogged(stateAfterFireCallback)) {
            level.destroyBlock(pos, false);
        }

        if (level.getBlockState(pos).isAir()) {
            level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        }
    }

    private static boolean endlesssands$canBurn(BlockState state, Level level, BlockPos pos) {
        if (state.ignitedByLava()) {
            return true;
        }

        for (Direction direction : Direction.values()) {
            if (state.isFlammable(level, pos, direction)) {
                return true;
            }
        }
        return false;
    }
}
