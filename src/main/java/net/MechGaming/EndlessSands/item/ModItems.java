package net.MechGaming.EndlessSands.item;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EndlessSands.MOD_ID);

    public static final RegistryObject<Item> CURSED_POCKET_SAND = ITEMS.register("cursed_pocket_sand",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BITTY_BONE = ITEMS.register("bitty_bone",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ITTY_BITTY_BONE = ITEMS.register("itty_bitty_bone",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
