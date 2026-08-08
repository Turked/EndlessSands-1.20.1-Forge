package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.sound.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, EndlessSands.MOD_ID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.VULTURE_AMBIENT, definition().with(sound(
                ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "entity/vulture_ambient")
        )));
        add(ModSounds.VULTURE_DETECTS, definition().with(sound(
                ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "entity/vulture_detects")
        )));
        add(ModSounds.VULTURE_EAT, definition().with(sound(
                ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "entity/vulture_eat")
        )));
        add(ModSounds.VULTURE_HURT, definition().with(sound(
                ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "entity/vulture_hurt")
        )));
        add(ModSounds.VULTURE_TAKE_OFF, definition().with(sound(
                ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "entity/vulture_take_off")
        )));
    }
}
