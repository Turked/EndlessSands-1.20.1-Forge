package net.MechGaming.EndlessSands.item;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.util.ModTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

public class ModToolTiers {
    public static final Tier DRAGONITE = TierSortingRegistry.registerTier(
            new ForgeTier(5, 2500, 10.0f, 5, 20,
                    ModTags.Blocks.NEEDS_DRAGONITE_TOOL, () -> Ingredient.of(ModItems.DRAGONITE.get())),
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "dragonite"),
            //Tier less than this tier
            List.of(Tiers.NETHERITE),
            //Tier greater than this tier
            List.of()
    );
}
