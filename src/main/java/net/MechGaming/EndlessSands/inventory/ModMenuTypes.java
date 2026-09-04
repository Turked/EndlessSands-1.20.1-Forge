package net.MechGaming.EndlessSands.inventory;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EndlessSands.MOD_ID);

    public static final RegistryObject<MenuType<ArmGuardSearchMenu>> ARM_GUARD_SEARCH =
            MENU_TYPES.register("arm_guard_search", () -> IForgeMenuType.create(ArmGuardSearchMenu::new));

    public static final RegistryObject<MenuType<ZenioniteChargerMenu>> ZENIONITE_CHARGER =
            MENU_TYPES.register("zenionite_charger", () -> IForgeMenuType.create(ZenioniteChargerMenu::new));

    public static final RegistryObject<MenuType<ZenioniteBatteryMenu>> ZENIONITE_BATTERY =
            MENU_TYPES.register("zenionite_battery", () -> IForgeMenuType.create(ZenioniteBatteryMenu::new));

    public static final RegistryObject<MenuType<ZenioniteBeaconMenu>> ZENIONITE_BEACON =
            MENU_TYPES.register("zenionite_beacon", () -> IForgeMenuType.create(ZenioniteBeaconMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
