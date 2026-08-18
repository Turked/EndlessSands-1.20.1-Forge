package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.Lavalogging;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SimpleWaterloggedBlock.class)
public interface SimpleWaterloggedBlockMixin {
    /**
     * @author MechGaming
     * @reason Extend simple waterlogging to persist either vanilla water or lava.
     */
    @Overwrite
    default boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return (fluid == Fluids.WATER || fluid == Fluids.LAVA) && Lavalogging.isEmpty(state);
    }

    /**
     * @author MechGaming
     * @reason Store and tick lava inside any simple waterloggable block.
     */
    @Overwrite
    default boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        Fluid fluid = fluidState.getType();
        if ((fluid != Fluids.WATER && fluid != Fluids.LAVA) || !Lavalogging.isEmpty(state)) {
            return false;
        }

        if (!level.isClientSide()) {
            level.setBlock(pos, Lavalogging.withFluid(state, fluid), 3);
            level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
        }
        return true;
    }

    /**
     * @author MechGaming
     * @reason Return the bucket matching the fluid stored in the block state.
     */
    @Overwrite
    default ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        ItemStack filledBucket;
        if (Lavalogging.isLavaLogged(state)) {
            filledBucket = new ItemStack(Items.LAVA_BUCKET);
        } else if (state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
            filledBucket = new ItemStack(Items.WATER_BUCKET);
        } else {
            return ItemStack.EMPTY;
        }

        BlockState drainedState = Lavalogging.withFluid(state, Fluids.EMPTY);
        level.setBlock(pos, drainedState, 3);
        if (!drainedState.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
        return filledBucket;
    }
}
