package net.MechGaming.EndlessSands.sound;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EndlessSands.MOD_ID);

    public static final RegistryObject<SoundEvent> VULTURE_AMBIENT = registerSoundEvent("vulture_ambient");
    public static final RegistryObject<SoundEvent> VULTURE_DETECTS = registerSoundEvent("vulture_detects");
    public static final RegistryObject<SoundEvent> VULTURE_EAT = registerSoundEvent("vulture_eat");
    public static final RegistryObject<SoundEvent> VULTURE_HURT = registerSoundEvent("vulture_hurt");
    public static final RegistryObject<SoundEvent> VULTURE_TAKE_OFF = registerSoundEvent("vulture_take_off");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, name)
        ));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
