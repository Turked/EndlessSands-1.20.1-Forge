package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.recipe.ModRecipeSerializers;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CURSED_SAND.get())
                .pattern("SS")
                .pattern("SS")
                .define('S', ModItems.CURSED_POCKET_SAND.get())
                .unlockedBy(getHasName(ModItems.CURSED_POCKET_SAND.get()), has(ModItems.CURSED_POCKET_SAND.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ITTY_BITTY_BONE.get(), 2)
                .requires(ModItems.BITTY_BONE.get())
                .unlockedBy(getHasName(ModItems.BITTY_BONE.get()), has(ModItems.BITTY_BONE.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.ROTTED_PLANKS.get(), 2)
                .requires(ModBlocks.ROTTED_LOG.get())
                .unlockedBy(getHasName(ModBlocks.ROTTED_LOG.get()), has(ModBlocks.ROTTED_LOG.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.TWIG.get(), 2)
                .requires(Items.STICK)
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.TWIG_HAT.get())
                .pattern("TTT")
                .pattern("T T")
                .define('T', ModBlocks.TWIG.get())
                .unlockedBy(getHasName(ModBlocks.TWIG.get()), has(ModBlocks.TWIG.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TWIG_VISOR.get())
                .pattern("TTT")
                .define('T', ModBlocks.TWIG.get())
                .unlockedBy(getHasName(ModBlocks.TWIG.get()), has(ModBlocks.TWIG.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ARM_GUARD.get())
                .pattern("TL")
                .pattern("LT")
                .define('T', ModBlocks.TWIG.get())
                .define('L', Items.LEATHER)
                .unlockedBy(getHasName(ModItems.LEATHER_SCRAP.get()), has(ModItems.LEATHER_SCRAP.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.LEATHER)
                .pattern("SS")
                .pattern("SS")
                .define('S', ModItems.LEATHER_SCRAP.get())
                .unlockedBy(getHasName(ModItems.LEATHER_SCRAP.get()), has(ModItems.LEATHER_SCRAP.get()))
                .save(pWriter, EndlessSands.MOD_ID + ":leather_from_scraps");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.TWIG.get(), 3)
                .requires(ModItems.TWIG_VISOR.get())
                .unlockedBy(getHasName(ModItems.TWIG_VISOR.get()), has(ModItems.TWIG_VISOR.get()))
                .save(pWriter, EndlessSands.MOD_ID + ":twig_visor_to_twigs");

        oreSmelting(pWriter, List.of(ModBlocks.CURSED_COBBLED_SAPROLITE.get()),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.CURSED_SAPROLITE.get(),
                0.1F, 200, "cursed_saprolite");

        SpecialRecipeBuilder.special(ModRecipeSerializers.TWIG_VISOR_ATTACHMENT.get())
                .save(pWriter, EndlessSands.MOD_ID + ":twig_visor_attachment");

        SpecialRecipeBuilder.special(ModRecipeSerializers.TWIG_HAT_SALVAGE.get())
                .save(pWriter, EndlessSands.MOD_ID + ":twig_hat_to_twigs");
    }


    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                    pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer, EndlessSands.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }

    }


}
