package net.MechGaming.EndlessSands.fluid;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraftforge.fluids.FluidType;

/** Inherit lava's Nether flow rate, fire, random ticks and downward cooling. */
public abstract class StarTouchedLavaFluid extends LavaFluid {
    @Override public Fluid getSource() { return ModFluids.STAR_TOUCHED_LAVA.get(); }
    @Override public Fluid getFlowing() { return ModFluids.FLOWING_STAR_TOUCHED_LAVA.get(); }
    @Override public Item getBucket() { return ModItems.STAR_TOUCHED_LAVA_BUCKET.get(); }
    @Override public FluidType getFluidType() { return ModFluids.STAR_TOUCHED_LAVA_TYPE.get(); }
    @Override public boolean isSame(Fluid other) { return other instanceof StarTouchedLavaFluid; }

    @Override
    public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        super.animateTick(level, pos, state, random);
        if (random.nextInt(100) != 0 || !level.getBlockState(pos.above()).isAir()) {
            return;
        }

        // Sparse starlight rises from the actual surface, including shallow flowing lava.
        level.addParticle(ParticleTypes.END_ROD,
                pos.getX() + random.nextDouble(), pos.getY() + state.getHeight(level, pos) + 0.02D,
                pos.getZ() + random.nextDouble(),
                (random.nextDouble() - 0.5D) * 0.01D, 0.025D, (random.nextDouble() - 0.5D) * 0.01D);
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.STAR_TOUCHED_LAVA.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    public static final class Source extends StarTouchedLavaFluid {
        @Override public int getAmount(FluidState state) { return 8; }
        @Override public boolean isSource(FluidState state) { return true; }
    }

    public static final class Flowing extends StarTouchedLavaFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override public boolean isSource(FluidState state) { return false; }
    }
}
