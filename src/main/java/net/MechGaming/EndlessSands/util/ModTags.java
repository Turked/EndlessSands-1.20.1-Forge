package net.MechGaming.EndlessSands.util;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        //All blocks containing some amount of blood, like cursed sand, will be tagged as bloodied
        public static final TagKey<Block> BLOODIED = tag("bloodied");

        //All blocks containing no remnants of the curse, like palm trees, will be tagged as spared
        public static final TagKey<Block> SPARED = tag("spared");

        //Blocks that can be infested with termites and rotted.
        // XYLOPHAGE Xylophagy is a term used in ecology to
        // describe the habits of a herbivorous animal whose
        // diet consists primarily (often solely) of wood
        public static final TagKey<Block> XYLOPHAGE = tag("xylophage");

        //Blocks that are at a higher hardness than netherite
        public static final TagKey<Block> NEEDS_DRAGONITE_TOOL = tag("needs_dragonite_tool");

        //Blocks that sand could flow through
        public static final TagKey<Block> GRANULAR = tag("granular");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, name));
        }
    }

        public static class Biomes{
            public static final TagKey<Biome> IS_CURSED_DESERT = tag("is_cursed_desert");

            private static TagKey<Biome> tag(String name){
                return TagKey.create(Registries.BIOME,
                        ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID,name));
            }
        }

    public static class Items {
        // Desert faring wear
        public static final TagKey<Item> IS_SUN_GEAR = tag("is_sun_gear");

        // Helmets that give shade like a hat
        public static final TagKey<Item> DOES_GIVE_SHADE = tag("does_give_shade");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(
                    ResourceLocation.fromNamespaceAndPath(
                            EndlessSands.MOD_ID,
                            name
                    )
            );
        }
    }
}
