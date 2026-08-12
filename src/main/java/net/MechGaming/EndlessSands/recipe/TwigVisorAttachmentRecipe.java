package net.MechGaming.EndlessSands.recipe;

import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.util.SunGearHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TwigVisorAttachmentRecipe extends CustomRecipe {
    public TwigVisorAttachmentRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !findValidHelmet(container).isEmpty() && countTwigVisors(container) == 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack helmet = findValidHelmet(container);
        if (helmet.isEmpty() || countTwigVisors(container) != 1) {
            return ItemStack.EMPTY;
        }

        ItemStack result = helmet.copyWithCount(1);
        SunGearHelper.attachTwigVisor(result);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TWIG_VISOR_ATTACHMENT.get();
    }

    private static ItemStack findValidHelmet(CraftingContainer container) {
        ItemStack validHelmet = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || stack.is(ModItems.TWIG_VISOR.get())) {
                continue;
            }

            if (!SunGearHelper.canAttachTwigVisor(stack) || !validHelmet.isEmpty()) {
                return ItemStack.EMPTY;
            }

            validHelmet = stack;
        }

        return validHelmet;
    }

    private static int countTwigVisors(CraftingContainer container) {
        int visors = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).is(ModItems.TWIG_VISOR.get())) {
                visors++;
            }
        }

        return visors;
    }
}
