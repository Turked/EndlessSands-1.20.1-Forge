package net.MechGaming.EndlessSands.recipe;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TwigHatSalvageRecipe extends CustomRecipe {
    private static final int MAX_TWIGS = 5;

    public TwigHatSalvageRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !findOnlyTwigHat(container).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack hat = findOnlyTwigHat(container);
        if (hat.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(ModBlocks.TWIG.get(), getTwigReturnCount(hat));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TWIG_HAT_SALVAGE.get();
    }

    private static ItemStack findOnlyTwigHat(CraftingContainer container) {
        ItemStack hat = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.is(ModItems.TWIG_HAT.get()) || !hat.isEmpty()) {
                return ItemStack.EMPTY;
            }

            hat = stack;
        }

        return hat;
    }

    private static int getTwigReturnCount(ItemStack hat) {
        if (!hat.isDamageableItem()) {
            return MAX_TWIGS;
        }

        int maxDamage = hat.getMaxDamage();
        int durabilityRemaining = maxDamage - hat.getDamageValue();
        return Mth.clamp(Mth.floor((float) MAX_TWIGS * durabilityRemaining / maxDamage), 1, MAX_TWIGS);
    }
}
