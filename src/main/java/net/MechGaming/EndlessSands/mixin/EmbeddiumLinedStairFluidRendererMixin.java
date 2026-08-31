package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.block.custom.LinedStairBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional compatibility for the optimized fluid renderer used by Embeddium. */
@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer", remap = false)
public abstract class EmbeddiumLinedStairFluidRendererMixin {
    @Unique
    private static final float ENDLESS_SANDS$MINIMUM_LINED_STAIR_FLUID_HEIGHT = 0.502F;

    @Unique
    private static final ThreadLocal<Boolean> ENDLESS_SANDS$RENDERING_LINED_STAIR = new ThreadLocal<>();

    @Inject(method = "render", at = @At("HEAD"), require = 0, remap = false)
    private void endlessSands$beginLinedStairFluidRender(
            @Coerce Object levelObject,
            FluidState fluidState,
            BlockPos pos,
            BlockPos origin,
            @Coerce Object buffers,
            CallbackInfo callback
    ) {
        BlockAndTintGetter level = (BlockAndTintGetter) levelObject;
        ENDLESS_SANDS$RENDERING_LINED_STAIR.set(
                level.getBlockState(pos).getBlock() instanceof LinedStairBlock
        );
    }

    @Inject(method = "fluidCornerHeight", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
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

    @Inject(method = "render", at = @At("RETURN"), require = 0, remap = false)
    private void endlessSands$endLinedStairFluidRender(
            @Coerce Object level,
            FluidState fluidState,
            BlockPos pos,
            BlockPos origin,
            @Coerce Object buffers,
            CallbackInfo callback
    ) {
        ENDLESS_SANDS$RENDERING_LINED_STAIR.remove();
    }
}
