package net.MechGaming.EndlessSands.item;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.item.custom.BrittleBoneMealItem;
import net.MechGaming.EndlessSands.item.custom.CursedPocketSandItem;
import net.MechGaming.EndlessSands.item.custom.OldworldScrollItem;
import net.MechGaming.EndlessSands.item.custom.TinyOldworldJarItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EndlessSands.MOD_ID);

    public static final RegistryObject<Item> CURSED_POCKET_SAND = ITEMS.register("cursed_pocket_sand",
            () -> new CursedPocketSandItem(new Item.Properties()));

    public static final RegistryObject<Item> BITTY_BONE = ITEMS.register("bitty_bone",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ITTY_BITTY_BONE = ITEMS.register("itty_bitty_bone",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TINY_OLDWORLD_JAR = ITEMS.register("tiny_oldworld_jar",
            () -> new TinyOldworldJarItem(new Item.Properties()));

    public static final RegistryObject<Item> OLDWORLD_ROSE = ITEMS.register("oldworld_rose",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BRITTLE_BONE_MEAL = ITEMS.register("brittle_bone_meal",
            () -> new BrittleBoneMealItem(new Item.Properties()));

    public static final RegistryObject<Item> OLDWORLD_SCROLL = ITEMS.register("oldworld_scroll",
            () -> new OldworldScrollItem(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL_OF_WISDOM = ITEMS.register("scroll_of_wisdom",
            () -> new OldworldScrollItem(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL_OF_LORE = ITEMS.register("scroll_of_lore",
            () -> new OldworldScrollItem(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL_OF_MEDIOCRITY = ITEMS.register("scroll_of_mediocrity",
            () -> new OldworldScrollItem(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL_OF_YEARNING = ITEMS.register("scroll_of_yearning",
            () -> new OldworldScrollItem(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
