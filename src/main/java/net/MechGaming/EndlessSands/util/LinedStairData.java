package net.MechGaming.EndlessSands.util;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.LinedStairBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class LinedStairData {
    public static final String ORIGINAL_STAIR_TAG = "OriginalStair";
    public static final ResourceLocation FALLBACK_STAIR_ID = new ResourceLocation("minecraft", "sandstone_stairs");
    private static final ResourceLocation LINED_STAIR_ID =
            new ResourceLocation(EndlessSands.MOD_ID, "lined_stairs");

    private LinedStairData() {
    }

    public static boolean isValidSourceItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && isValidSourceBlock(blockItem.getBlock());
    }

    public static boolean isValidSourceBlock(@Nullable Block block) {
        if (!(block instanceof StairBlock) || block instanceof LinedStairBlock) {
            return false;
        }

        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id != null && !LINED_STAIR_ID.equals(id) && block.asItem() instanceof BlockItem;
    }

    public static ResourceLocation validateId(@Nullable ResourceLocation requestedId) {
        if (requestedId != null && ForgeRegistries.BLOCKS.containsKey(requestedId)) {
            Block requested = ForgeRegistries.BLOCKS.getValue(requestedId);
            if (isValidSourceBlock(requested)) {
                return requestedId;
            }
        }
        return FALLBACK_STAIR_ID;
    }

    public static Block getSourceBlock(@Nullable ResourceLocation requestedId) {
        ResourceLocation validId = validateId(requestedId);
        Block block = ForgeRegistries.BLOCKS.getValue(validId);
        return isValidSourceBlock(block) ? block : Blocks.SANDSTONE_STAIRS;
    }

    public static ResourceLocation getSourceId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        ResourceLocation requested = tag == null
                ? null
                : ResourceLocation.tryParse(tag.getString(ORIGINAL_STAIR_TAG));
        return validateId(requested);
    }

    public static boolean hasSourceId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(ORIGINAL_STAIR_TAG, Tag.TAG_STRING);
    }

    public static ResourceLocation getSourceId(Block block) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return validateId(id);
    }

    public static void setSourceId(ItemStack stack, @Nullable ResourceLocation requestedId) {
        stack.getOrCreateTag().putString(ORIGINAL_STAIR_TAG, validateId(requestedId).toString());
    }

    public static ItemStack createStack(Block linedStairBlock, @Nullable ResourceLocation sourceId) {
        ItemStack result = new ItemStack(linedStairBlock);
        setSourceId(result, sourceId);
        return result;
    }

    public static BlockState toSourceState(BlockState linedState, @Nullable ResourceLocation sourceId) {
        BlockState source = getSourceBlock(sourceId).defaultBlockState();
        source = copyIfPresent(linedState, source, StairBlock.FACING);
        source = copyIfPresent(linedState, source, StairBlock.HALF);
        source = copyIfPresent(linedState, source, StairBlock.SHAPE);
        if (source.hasProperty(StairBlock.WATERLOGGED)) {
            source = source.setValue(StairBlock.WATERLOGGED, false);
        }
        return source;
    }

    private static <T extends Comparable<T>> BlockState copyIfPresent(
            BlockState from,
            BlockState to,
            net.minecraft.world.level.block.state.properties.Property<T> property
    ) {
        return from.hasProperty(property) && to.hasProperty(property)
                ? to.setValue(property, from.getValue(property))
                : to;
    }
}
