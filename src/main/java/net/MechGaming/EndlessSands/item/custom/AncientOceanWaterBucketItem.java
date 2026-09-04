package net.MechGaming.EndlessSands.item.custom;

import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;

public final class AncientOceanWaterBucketItem extends BucketItem {
    public AncientOceanWaterBucketItem(Properties properties) {
        super(ModFluids.ANCIENT_OCEAN_WATER, properties);
    }

    private static boolean acceptsVanillaWater(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof SimpleWaterloggedBlock waterlogged
                && waterlogged.canPlaceLiquid(level, pos, state, Fluids.WATER);
    }

    @Override
    protected boolean canBlockContainFluid(Level level, BlockPos pos, BlockState state) {
        return acceptsVanillaWater(level, pos, state) || super.canBlockContainFluid(level, pos, state);
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos,
                                 @Nullable BlockHitResult hit, @Nullable ItemStack stack) {
        if (!level.dimensionType().ultraWarm() && acceptsVanillaWater(level, pos, level.getBlockState(pos))) {
            // Vanilla WATERLOGGED is a boolean and always holds ordinary water.
            return ((BucketItem) Items.WATER_BUCKET).emptyContents(player, level, pos, hit,
                    new ItemStack(Items.WATER_BUCKET));
        }
        return super.emptyContents(player, level, pos, hit, stack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag tag) {
        return new FluidBucketWrapper(stack);
    }
}
