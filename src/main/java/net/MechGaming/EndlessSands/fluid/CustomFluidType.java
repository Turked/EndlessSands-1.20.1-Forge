package net.MechGaming.EndlessSands.fluid;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class CustomFluidType extends FluidType {
    private final boolean lava;
    private final ResourceLocation still;
    private final ResourceLocation flowing;

    public CustomFluidType(Properties properties, String name, boolean lava) {
        super(properties);
        this.lava = lava;
        this.still = ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "block/" + name + "_still");
        this.flowing = ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "block/" + name + "_flow");
    }

    @Override
    public double motionScale(Entity entity) {
        return lava ? ForgeMod.LAVA_TYPE.get().motionScale(entity) : super.motionScale(entity);
    }

    @Override
    public void setItemMovement(ItemEntity entity) {
        (lava ? ForgeMod.LAVA_TYPE.get() : ForgeMod.WATER_TYPE.get()).setItemMovement(entity);
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return !lava && level.dimensionType().ultraWarm();
    }

    @Override
    public @Nullable BlockPathTypes getBlockPathType(FluidState state, BlockGetter level,
                                                    BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
        return !lava && !canFluidLog ? null
                : super.getBlockPathType(state, level, pos, mob, canFluidLog);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        // Access fields lazily: FluidType invokes this before our constructor finishes.
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override public ResourceLocation getStillTexture() { return still; }
            @Override public ResourceLocation getFlowingTexture() { return flowing; }
            // The supplied palette is already colored; applying biome tint would change it.
            @Override public int getTintColor() { return 0xFFFFFFFF; }
        });
    }
}
