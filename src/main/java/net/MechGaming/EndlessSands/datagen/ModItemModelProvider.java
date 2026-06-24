package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, EndlessSands.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.BITTY_BONE);
        simpleItem(ModItems.CURSED_POCKET_SAND);
        simpleItem(ModItems.BRITTLE_BONE_MEAL);
        simpleItem(ModItems.BOILED_VULTURE_EGG);
        simpleItem(ModItems.VULTURE_EGG);
        simpleItem(ModItems.ITTY_BITTY_BONE);
        simpleItem(ModItems.HANDFUL_OF_SCRAMBLED_EGGS);
        simpleItem(ModItems.OLDWORLD_ROSE);
        simpleItem(ModItems.OLDWORLD_SCROLL);
        simpleItem(ModItems.ROTTED_WOOD);
        simpleItem(ModItems.SCROLL_OF_LORE);
        simpleItem(ModItems.SCROLL_OF_MEDIOCRITY);
        simpleItem(ModItems.SCROLL_OF_WISDOM);
        simpleItem(ModItems.SCROLL_OF_YEARNING);
        simpleItem(ModItems.TINY_OLDWORLD_JAR);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/generated"))
                .texture("layer0",
                        ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "item/" + item.getId().getPath()));
    }
}