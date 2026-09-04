package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.fluid.FluidConversion;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class FluidConversionItemMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void endlessSands$convertFluidBeforeBurning(CallbackInfo callback) {
        ItemEntity item = (ItemEntity) (Object) this;
        FluidConversion.tryConvert(item);
        if (item.isRemoved()) callback.cancel();
    }
}
