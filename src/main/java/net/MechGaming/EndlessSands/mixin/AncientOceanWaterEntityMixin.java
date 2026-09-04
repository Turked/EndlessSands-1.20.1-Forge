package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class AncientOceanWaterEntityMixin {
    @Shadow(remap = false) private FluidType forgeFluidTypeOnEyes;
    @Unique private boolean endlessSands$eyesInAncientOceanWater;

    /** Keep ordinary swimming, currents and extinguishing without changing the rendered fluid. */
    @ModifyVariable(method = "updateFluidHeightAndDoFluidPushing(Ljava/util/function/Predicate;)V",
            at = @At("STORE"), ordinal = 0, remap = false, require = 1)
    private FluidType endlessSands$ancientOceanWaterPhysics(FluidType type) {
        return type == ModFluids.ANCIENT_OCEAN_WATER_TYPE.get() ? ForgeMod.WATER_TYPE.get() : type;
    }

    @Inject(method = "updateFluidOnEyes", at = @At("RETURN"), require = 1)
    private void endlessSands$ancientOceanWaterImmersion(CallbackInfo ci) {
        // Use vanilla's exact eye-height/boat checks, and reset the flag when leaving this fluid.
        endlessSands$eyesInAncientOceanWater = forgeFluidTypeOnEyes == ModFluids.ANCIENT_OCEAN_WATER_TYPE.get();
        if (endlessSands$eyesInAncientOceanWater) {
            // The standard HUD still needs WATER_TYPE to display a full row of oxygen bubbles.
            forgeFluidTypeOnEyes = ForgeMod.WATER_TYPE.get();
            Entity entity = (Entity) (Object) this;
            if (entity instanceof Player && entity.getAirSupply() != entity.getMaxAirSupply()) {
                // Refill on entry even if a Water Breathing effect skips the normal air update.
                entity.setAirSupply(entity.getMaxAirSupply());
            }
        }
    }

    @ModifyVariable(method = "setAirSupply", at = @At("HEAD"), argsOnly = true, require = 1)
    private int endlessSands$oxygenRichWater(int air) {
        Entity entity = (Entity) (Object) this;
        return endlessSands$eyesInAncientOceanWater && entity instanceof Player
                ? entity.getMaxAirSupply() : air;
    }
}
