package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
        simpleItem(ModItems.HANDFUL_OF_SCRAMBLED_EGGS);

        simpleItem(ModItems.VULTURE_EGG);
        simpleItem(ModItems.ITTY_BITTY_BONE);
        simpleItem(ModItems.OLDWORLD_ROSE);
        simpleItem(ModItems.ROTTED_WOOD);
        simpleItem(ModItems.SCROLL_OF_LORE);
        simpleItem(ModItems.SCROLL_OF_MEDIOCRITY);
        simpleItem(ModItems.SCROLL_OF_WISDOM);
        simpleItem(ModItems.SCROLL_OF_YEARNING);

        simpleItem(ModItems.OLDWORLD_SCROLL);
        simpleItem(ModItems.TINY_OLDWORLD_JAR);

        simpleBlockItem(ModBlocks.PALM_DOOR);
        trapdoorItem(ModBlocks.PALM_TRAPDOOR);

        blockItem(ModBlocks.PALM_STAIRS);
        blockItem(ModBlocks.PALM_SLAB);
        blockItem(ModBlocks.PALM_PRESSURE_PLATE);
        blockItem(ModBlocks.PALM_FENCE_GATE);

        fenceItem(ModBlocks.PALM_FENCE, ModBlocks.PALM_PLANKS);
        buttonItem(ModBlocks.PALM_BUTTON, ModBlocks.PALM_PLANKS);
        wallItem(ModBlocks.PALM_WALL, ModBlocks.PALM_PLANKS);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + item.getId().getPath()));
    }

    public void trapdoorItem(RegistryObject<Block> block) {
        this.withExistingParent(block.getId().getPath(),
                modLoc("block/" + block.getId().getPath() + "_bottom"));
    }

    public void blockItem(RegistryObject<Block> block) {
        this.withExistingParent(block.getId().getPath(),
                modLoc("block/" + block.getId().getPath()));
    }

    public void fenceItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture", modLoc("block/" + baseBlock.getId().getPath()));
    }

    public void buttonItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/button_inventory"))
                .texture("texture", modLoc("block/" + baseBlock.getId().getPath()));
    }

    public void wallItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/wall_inventory"))
                .texture("wall", modLoc("block/" + baseBlock.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> block) {
        return withExistingParent(block.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + block.getId().getPath()));
    }

}
