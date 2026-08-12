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

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
