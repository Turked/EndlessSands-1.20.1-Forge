package net.MechGaming.EndlessSands.block;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.BrittlePotBlock;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
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
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SUSPICIOUS_SAND).noLootTable()));

    public static final RegistryObject<Block> FERTILE_SOIL = registerBlock("fertile_soil",
            () -> new Block(BlockBehaviour.Properties.copy(ModBlocks.CURSED_SAND.get())));

    public static final RegistryObject<Block> PALM_LOG = registerBlock("palm_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.ACACIA_LOG)));

    public static final RegistryObject<Block> VILLAGE_POT = registerBlock("village_pot",
            () -> new BrittlePotBlock(BlockBehaviour.Properties.copy(Blocks.DECORATED_POT)));

    public static final RegistryObject<Block> PALM_PLANKS = registerBlock("palm_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    public static final RegistryObject<Block> PALM_STAIRS = registerBlock("palm_stairs",
            () -> new StairBlock(() -> ModBlocks.PALM_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    public static final RegistryObject<Block> PALM_SLAB = registerBlock("palm_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    public static final RegistryObject<Block> PALM_BUTTON = registerBlock("palm_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON),
                    BlockSetType.ACACIA, 10, true));

    public static final RegistryObject<Block> PALM_PRESSURE_PLATE = registerBlock("palm_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS),
                    BlockSetType.ACACIA));

    public static final RegistryObject<Block> PALM_FENCE = registerBlock("palm_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    public static final RegistryObject<Block> PALM_FENCE_GATE = registerBlock("palm_fence_gate",
            () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS),
                    SoundEvents.FENCE_GATE_OPEN, SoundEvents.FENCE_GATE_CLOSE));

    public static final RegistryObject<Block> PALM_WALL = registerBlock("palm_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    public static final RegistryObject<Block> PALM_DOOR = registerBlock("palm_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), BlockSetType.ACACIA));

    public static final RegistryObject<Block> PALM_TRAPDOOR = registerBlock("palm_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion(), BlockSetType.ACACIA));




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
