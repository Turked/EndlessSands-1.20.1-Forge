package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.Lavalogging;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Function;

@Mixin(StateDefinition.Builder.class)
public abstract class StateDefinitionBuilderMixin<O, S extends StateHolder<O, S>> {
    @Shadow
    @Final
    private Map<String, Property<?>> properties;

    @Inject(method = "create", at = @At("HEAD"))
    private void endlesssands$addLavaLoggedProperty(
            Function<O, S> stateValueFunction,
            StateDefinition.Factory<O, S> stateFunction,
            CallbackInfoReturnable<StateDefinition<O, S>> cir
    ) {
        if (this.properties.containsValue(BlockStateProperties.WATERLOGGED)
                && !this.properties.containsKey(Lavalogging.LAVA_LOGGED.getName())) {
            ((StateDefinition.Builder<O, S>) (Object) this).add(Lavalogging.LAVA_LOGGED);
        }
    }
}
