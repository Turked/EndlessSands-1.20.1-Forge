package net.MechGaming.EndlessSands.item;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.BrittlePotBlock;
import net.MechGaming.EndlessSands.item.custom.*;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.rmi.registry.Registry;

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
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL_OF_LORE = ITEMS.register("scroll_of_lore",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL_OF_MEDIOCRITY = ITEMS.register("scroll_of_mediocrity",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL_OF_YEARNING = ITEMS.register("scroll_of_yearning",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VULTURE_EGG = ITEMS.register("vulture_egg",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HANDFUL_OF_SCRAMBLED_EGGS = ITEMS.register("handful_of_scrambled_eggs",
            () -> new Item(new Item.Properties().food(ModFoods.HANDFUL_OF_SCRAMBLED_EGGS)));

    public static final RegistryObject<Item> BOILED_VULTURE_EGG = ITEMS.register("boiled_vulture_egg",
            () -> new Item(new Item.Properties().food(ModFoods.Boiled_VULTURE_EGG)));

    public static final RegistryObject<Item> ROTTED_WOOD = ITEMS.register("rotted_wood",
            () -> new FuelItem(new Item.Properties(), 100));

    public static final RegistryObject<Item> DRAGONITE = ITEMS.register("dragonite",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DRAGONITE_SWORD = ITEMS.register("dragonite_sword",
            () -> new SwordItem(ModToolTiers.DRAGONITE, 3, -2.4f, new Item.Properties()));

    public static final RegistryObject<Item> DRAGONITE_PICKAXE = ITEMS.register("dragonite_pickaxe",
            () -> new PickaxeItem(ModToolTiers.DRAGONITE, 1, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> DRAGONITE_AXE = ITEMS.register("dragonite_axe",
            () -> new AxeItem(ModToolTiers.DRAGONITE, 6.0f, -3.2f, new Item.Properties()));

    public static final RegistryObject<Item> DRAGONITE_SHOVEL = ITEMS.register("dragonite_shovel",
            () -> new ShovelItem(ModToolTiers.DRAGONITE, 1.5f, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> DRAGONITE_HOE = ITEMS.register("dragonite_hoe",
            () -> new HoeItem(ModToolTiers.DRAGONITE, -5, -0.0f, new Item.Properties()));

    public static final RegistryObject<Item> DRAGONITE_HELMET = ITEMS.register("dragonite_helmet",
            () -> new DragoniteArmorItem(ModArmorMaterials.DRAGONITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> DRAGONITE_CHESTPLATE = ITEMS.register("dragonite_chestplate",
            () -> new DragoniteArmorItem(ModArmorMaterials.DRAGONITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> DRAGONITE_LEGGINGS = ITEMS.register("dragonite_leggings",
            () -> new DragoniteArmorItem(ModArmorMaterials.DRAGONITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> DRAGONITE_BOOTS = ITEMS.register("dragonite_boots",
            () -> new DragoniteArmorItem(ModArmorMaterials.DRAGONITE, ArmorItem.Type.BOOTS, new Item.Properties()));




    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
