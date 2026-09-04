package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.MechGaming.EndlessSands.fluid.StarTouchedLavaFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class StarTouchedLavaEntityMixin {
    @Shadow(remap = false) private FluidType forgeFluidTypeOnEyes;

    /** Preserve normal lava movement and burning. Variable modifiers allow both fluid mixins to coexist. */
    @ModifyVariable(method = "updateFluidHeightAndDoFluidPushing(Ljava/util/function/Predicate;)V",
            at = @At("STORE"), ordinal = 0, remap = false, require = 1)
    private FluidType endlessSands$starTouchedLavaPhysics(FluidType type) {
        return type == ModFluids.STAR_TOUCHED_LAVA_TYPE.get() ? ForgeMod.LAVA_TYPE.get() : type;
    }

    @Inject(method = "updateFluidOnEyes", at = @At("RETURN"), require = 1)
    private void endlessSands$starTouchedLavaImmersion(CallbackInfo ci) {
        if (forgeFluidTypeOnEyes == ModFluids.STAR_TOUCHED_LAVA_TYPE.get()) {
            forgeFluidTypeOnEyes = ForgeMod.LAVA_TYPE.get();
        }
    }

    @ModifyArg(method = "lavaHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 1, require = 1)
    private float endlessSands$starTouchedLavaDamage(float damage) {
        Entity entity = (Entity) (Object) this;
        AABB bounds = entity.getBoundingBox().deflate(0.001D);
        // Check the entire hitbox, not just the feet; shallow flow must actually touch the entity.
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                new BlockPos(Mth.ceil(bounds.maxX) - 1, Mth.ceil(bounds.maxY) - 1, Mth.ceil(bounds.maxZ) - 1))) {
            FluidState state = entity.level().getFluidState(pos);
            if (state.getType() instanceof StarTouchedLavaFluid
                    && pos.getY() + state.getHeight(entity.level(), pos) >= bounds.minY) {
                // Only the existing lava hit changes: timing, armor and fire resistance stay vanilla.
                return damage * 2.0F;
            }
        }
        return damage;
    }
}
