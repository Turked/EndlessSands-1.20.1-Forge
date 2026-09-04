package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.fluid.AncientOceanWaterFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowingFluid.class)
public abstract class AncientWaterloggingMixin {
    // Vanilla waterlogging stores only a boolean, not a fluid identity. Allow the
    // usual water behavior; the contained fluid becomes vanilla water in that state.
    @Redirect(method = "canHoldFluid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/LiquidBlockContainer;canPlaceLiquid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z"))
    private boolean endlessSands$allowAncientWater(LiquidBlockContainer container, BlockGetter level,
                                                  BlockPos pos, BlockState state, Fluid fluid) {
        return container.canPlaceLiquid(level, pos, state,
                container instanceof SimpleWaterloggedBlock && fluid instanceof AncientOceanWaterFluid
                        ? Fluids.WATER : fluid);
    }

    @Redirect(method = "spreadTo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/LiquidBlockContainer;placeLiquid(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z"))
    private boolean endlessSands$waterloggedState(LiquidBlockContainer container, LevelAccessor level,
                                                BlockPos pos, BlockState block, FluidState state) {
        return container.placeLiquid(level, pos, block,
                container instanceof SimpleWaterloggedBlock && state.getType() instanceof AncientOceanWaterFluid
                        ? Fluids.WATER.defaultFluidState() : state);
    }
}
