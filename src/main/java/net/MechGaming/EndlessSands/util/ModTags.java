package net.MechGaming.EndlessSands.util;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks{
        //All blocks containing some amount of blood, like cursed sand, will be tagged as bloodied
        public static final TagKey<Block> BLOODIED = tag("bloodied");
        //All blocks containing no remnants of the curse, like palm trees, will be tagged as spared
        public static final TagKey<Block> SPARED = tag("spared");

        private static TagKey<Block> tag(String name){
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, name));
        }

    }



    public static class Items{

    }
}
