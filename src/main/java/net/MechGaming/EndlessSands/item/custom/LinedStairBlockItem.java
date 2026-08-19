package net.MechGaming.EndlessSands.item.custom;

import net.MechGaming.EndlessSands.util.LinedStairData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LinedStairBlockItem extends BlockItem {
    public LinedStairBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (!LinedStairData.hasSourceId(stack)) {
            return Component.translatable("block.endlesssands.lined_stairs");
        }

        Block source = LinedStairData.getSourceBlock(LinedStairData.getSourceId(stack));
        return Component.translatable("item.endlesssands.lined_stairs.named", source.asItem().getDescription());
    }
}
