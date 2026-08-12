package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SuspiciousCursedSandBlock extends BrushableBlock {
    public static final ResourceLocation ARCHAEOLOGY_LOOT =
            new ResourceLocation(EndlessSands.MOD_ID, "archaeology/suspicious_cursed_sand");

    public SuspiciousCursedSandBlock(Block turnsInto, Properties properties) {
        super(turnsInto, properties, net.minecraft.sounds.SoundEvents.BRUSH_SAND,
                net.minecraft.sounds.SoundEvents.BRUSH_SAND_COMPLETED);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        ensureLootTable(level, pos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ensureLootTable(level, pos);
        super.tick(state, level, pos, random);
    }

    private static void ensureLootTable(Level level, BlockPos pos) {
        if (level.isClientSide
                || !(level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable)
                || !brushable.getItem().isEmpty()) {
            return;
        }

        CompoundTag saved = brushable.saveWithFullMetadata();
        if (!saved.contains("LootTable")) {
            brushable.setLootTable(ARCHAEOLOGY_LOOT, level.getRandom().nextLong());
            brushable.setChanged();
        }
    }
}
