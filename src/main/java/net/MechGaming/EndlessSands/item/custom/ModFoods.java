package net.MechGaming.EndlessSands.item.custom;

import net.minecraft.world.food.FoodProperties;

//Holds all food properties
public class ModFoods {
    public static final float VULTURE_JERKY_SATURATION = 0.6F;
    public static final FoodProperties HANDFUL_OF_SCRAMBLED_EGGS = new FoodProperties.Builder().nutrition(2).meat().build();
    public static final FoodProperties Boiled_VULTURE_EGG = new FoodProperties.Builder().nutrition(2).build();
    public static final FoodProperties VULTURE_JERKY = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(VULTURE_JERKY_SATURATION / 4.0F)
            .meat()
            .build();
}
