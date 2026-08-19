package net.MechGaming.EndlessSands.recipe;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.util.LinedStairData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class LinedStairRecipe extends CustomRecipe {
    public LinedStairRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !findSourceStair(container).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack sourceStack = findSourceStair(container);
        if (sourceStack.isEmpty() || !(sourceStack.getItem() instanceof BlockItem blockItem)) {
            return ItemStack.EMPTY;
        }

        return LinedStairData.createStack(
                ModBlocks.LINED_STAIRS.get(),
                LinedStairData.getSourceId(blockItem.getBlock())
        );
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.LINED_STAIRS.get();
    }

    private static ItemStack findSourceStair(CraftingContainer container) {
        ItemStack sourceStair = ItemStack.EMPTY;
        boolean foundLining = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(ModItems.LIQUID_LINING.get())) {
                if (foundLining) {
                    return ItemStack.EMPTY;
                }
                foundLining = true;
            } else if (LinedStairData.isValidSourceItem(stack) && sourceStair.isEmpty()) {
                sourceStair = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }

        return foundLining ? sourceStair : ItemStack.EMPTY;
    }
}
