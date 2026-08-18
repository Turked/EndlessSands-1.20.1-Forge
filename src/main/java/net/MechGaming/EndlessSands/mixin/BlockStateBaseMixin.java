package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.Lavalogging;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void endlesssands$useVanillaGeometryStateForShape(
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState state = (BlockState) (Object) this;
        if (Lavalogging.isLavaLogged(state)) {
            BlockState geometryState = Lavalogging.geometryState(state);
            cir.setReturnValue(state.getBlock().getShape(geometryState, level, pos, context));
        }
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void endlesssands$useVanillaGeometryStateForCollision(
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState state = (BlockState) (Object) this;
        if (Lavalogging.isLavaLogged(state)) {
            BlockState geometryState = Lavalogging.geometryState(state);
            cir.setReturnValue(state.getBlock().getCollisionShape(geometryState, level, pos, context));
        }
    }

    @Inject(method = "getOcclusionShape", at = @At("HEAD"), cancellable = true)
    private void endlesssands$useVanillaGeometryStateForOcclusion(
            BlockGetter level,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState state = (BlockState) (Object) this;
        if (Lavalogging.isLavaLogged(state)) {
            BlockState geometryState = Lavalogging.geometryState(state);
            cir.setReturnValue(state.getBlock().getOcclusionShape(geometryState, level, pos));
        }
    }

    @Inject(method = "getBlockSupportShape", at = @At("HEAD"), cancellable = true)
    private void endlesssands$useVanillaGeometryStateForBlockSupport(
            BlockGetter level,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState state = (BlockState) (Object) this;
        if (Lavalogging.isLavaLogged(state)) {
            BlockState geometryState = Lavalogging.geometryState(state);
            cir.setReturnValue(state.getBlock().getBlockSupportShape(geometryState, level, pos));
        }
    }

    @Inject(method = "getVisualShape", at = @At("HEAD"), cancellable = true)
    private void endlesssands$useVanillaGeometryStateForVisualShape(
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState state = (BlockState) (Object) this;
        if (Lavalogging.isLavaLogged(state)) {
            BlockState geometryState = Lavalogging.geometryState(state);
            cir.setReturnValue(state.getBlock().getVisualShape(geometryState, level, pos, context));
        }
    }

    @Inject(method = "getInteractionShape", at = @At("HEAD"), cancellable = true)
    private void endlesssands$useVanillaGeometryStateForInteraction(
            BlockGetter level,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState state = (BlockState) (Object) this;
        if (Lavalogging.isLavaLogged(state)) {
            BlockState geometryState = Lavalogging.geometryState(state);
            cir.setReturnValue(state.getBlock().getInteractionShape(geometryState, level, pos));
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
