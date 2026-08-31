package net.MechGaming.EndlessSands.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.MechGaming.EndlessSands.block.custom.LinedStairBlock;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlockRenderer.class)
public abstract class LinedStairLiquidBlockRendererMixin {
    /*
     * Both vanilla and Embeddium lower the finished top face by 0.001 blocks.
     * Keeping the calculated corner at 0.502 leaves the visible face one
     * thousandth above the half-block seat and avoids z-fighting with it.
    */
    @Unique
    private static final float ENDLESS_SANDS$MINIMUM_LINED_STAIR_FLUID_HEIGHT = 0.502F;

    /* LiquidBlockRenderer is shared by client chunk-build threads. */
    @Unique
    private static final ThreadLocal<Boolean> ENDLESS_SANDS$RENDERING_LINED_STAIR = new ThreadLocal<>();

    @Inject(method = "tesselate", at = @At("HEAD"))
    private void endlessSands$beginLinedStairFluidRender(
            BlockAndTintGetter level,
            BlockPos pos,
            VertexConsumer vertexConsumer,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo callback
    ) {
        ENDLESS_SANDS$RENDERING_LINED_STAIR.set(blockState.getBlock() instanceof LinedStairBlock);
    }

    @Inject(method = "calculateAverageHeight", at = @At("RETURN"), cancellable = true)
    private void endlessSands$keepFluidAboveLinedStairSeat(
            BlockAndTintGetter level,
            Fluid fluid,
            float centerHeight,
            float adjacentHeightA,
            float adjacentHeightB,
            BlockPos cornerPos,
            CallbackInfoReturnable<Float> callback
    ) {
        if (Boolean.TRUE.equals(ENDLESS_SANDS$RENDERING_LINED_STAIR.get())) {
            callback.setReturnValue(Math.max(
                    callback.getReturnValue(),
                    ENDLESS_SANDS$MINIMUM_LINED_STAIR_FLUID_HEIGHT
            ));
        }
    }

    @Inject(method = "tesselate", at = @At("RETURN"))
    private void endlessSands$endLinedStairFluidRender(
            BlockAndTintGetter level,
            BlockPos pos,
            VertexConsumer vertexConsumer,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo callback
    ) {
        ENDLESS_SANDS$RENDERING_LINED_STAIR.remove();
    }
}
