package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.Lavalogging;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    private void endlesssands$retainLavaWhenPlaced(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        BlockState state = cir.getReturnValue();
        if (state == null || !Lavalogging.isSupported(state)) {
            return;
        }

        if (!context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.LAVA)) {
            cir.setReturnValue(state.setValue(Lavalogging.LAVA_LOGGED, false));
            return;
        }

        boolean canRetainLava = state.getBlock() instanceof LiquidBlockContainer liquidContainer
                && liquidContainer.canPlaceLiquid(
                context.getLevel(),
                context.getClickedPos(),
                Lavalogging.withFluid(state, Fluids.EMPTY),
                Fluids.LAVA
        );
        cir.setReturnValue(Lavalogging.withFluid(state, canRetainLava ? Fluids.LAVA : Fluids.EMPTY));
    }

    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void endlesssands$schedulePlacedLava(
            BlockPlaceContext context,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue() && Lavalogging.isLavaLogged(state)) {
            context.getLevel().scheduleTick(
                    context.getClickedPos(),
                    Fluids.LAVA,
                    Fluids.LAVA.getTickDelay(context.getLevel())
            );
        }
    }
}
