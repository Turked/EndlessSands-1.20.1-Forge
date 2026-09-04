package net.MechGaming.EndlessSands.fluid;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.fluids.FluidType;

/** Inherit water's flow, source conversion, sounds and interactions unchanged. */
public abstract class AncientOceanWaterFluid extends WaterFluid {
    @Override public Fluid getSource() { return ModFluids.ANCIENT_OCEAN_WATER.get(); }
    @Override public Fluid getFlowing() { return ModFluids.FLOWING_ANCIENT_OCEAN_WATER.get(); }
    @Override public Item getBucket() { return ModItems.ANCIENT_OCEAN_WATER_BUCKET.get(); }
    @Override public FluidType getFluidType() { return ModFluids.ANCIENT_OCEAN_WATER_TYPE.get(); }
    @Override public boolean isSame(Fluid other) { return other instanceof AncientOceanWaterFluid; }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    public static final class Source extends AncientOceanWaterFluid {
        @Override public int getAmount(FluidState state) { return 8; }
        @Override public boolean isSource(FluidState state) { return true; }
    }

    public static final class Flowing extends AncientOceanWaterFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override public boolean isSource(FluidState state) { return false; }
    }
}
