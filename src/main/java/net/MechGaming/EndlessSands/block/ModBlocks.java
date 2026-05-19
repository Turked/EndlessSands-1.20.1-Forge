package net.MechGaming.EndlessSands.block;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.BrittlePotBlock;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EndlessSands.MOD_ID);

    public static final RegistryObject<Block> CURSED_SAND = registerBlock("cursed_sand",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND)));
    // You can override default block behaviors with . like ...copy(Blocks.SAND).sound(SoundType.SOUL_SAND)

    public static final RegistryObject<Block> CURSED_SANDSTONE = registerBlock("cursed_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SANDSTONE)));

    public static final RegistryObject<Block> SUSPICIOUS_CURSED_SAND = registerBlock("suspicious_cursed_sand",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SUSPICIOUS_SAND)));

    public static final RegistryObject<Block> VILLAGE_POT = registerBlock("village_pot",
            () -> new BrittlePotBlock(BlockBehaviour.Properties.copy(Blocks.DECORATED_POT)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
