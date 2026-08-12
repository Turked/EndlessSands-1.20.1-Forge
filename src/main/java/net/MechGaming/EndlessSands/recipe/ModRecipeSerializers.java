package net.MechGaming.EndlessSands.recipe;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, EndlessSands.MOD_ID);

    public static final RegistryObject<RecipeSerializer<TwigVisorAttachmentRecipe>> TWIG_VISOR_ATTACHMENT =
            RECIPE_SERIALIZERS.register("crafting_special_twig_visor_attachment",
                    () -> new SimpleCraftingRecipeSerializer<>(TwigVisorAttachmentRecipe::new));

    public static final RegistryObject<RecipeSerializer<TwigHatSalvageRecipe>> TWIG_HAT_SALVAGE =
            RECIPE_SERIALIZERS.register("crafting_special_twig_hat_salvage",
                    () -> new SimpleCraftingRecipeSerializer<>(TwigHatSalvageRecipe::new));

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
