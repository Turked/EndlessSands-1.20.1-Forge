package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;

public class ModItemModelProvider extends ItemModelProvider {
    private static LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();
    static {
        trimMaterials.put(TrimMaterials.QUARTZ, 0.1F);
        trimMaterials.put(TrimMaterials.IRON, 0.2F);
        trimMaterials.put(TrimMaterials.NETHERITE, 0.3F);
        trimMaterials.put(TrimMaterials.REDSTONE, 0.4F);
        trimMaterials.put(TrimMaterials.COPPER, 0.5F);
        trimMaterials.put(TrimMaterials.GOLD, 0.6F);
        trimMaterials.put(TrimMaterials.EMERALD, 0.7F);
        trimMaterials.put(TrimMaterials.DIAMOND, 0.8F);
        trimMaterials.put(TrimMaterials.LAPIS, 0.9F);
        trimMaterials.put(TrimMaterials.AMETHYST, 1.0F);
    }

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, EndlessSands.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.BITTY_BONE);
        simpleItem(ModItems.CURSED_POCKET_SAND);
        simpleItem(ModItems.BRITTLE_BONE_MEAL);
        simpleItem(ModItems.DRAGONITE);

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

        handheldItem(ModItems.DRAGONITE_PICKAXE);
        handheldItem(ModItems.DRAGONITE_AXE);
        handheldItem(ModItems.DRAGONITE_SHOVEL);
        handheldItem(ModItems.DRAGONITE_HOE);
        handheldItem(ModItems.DRAGONITE_SWORD);

        trimmedArmorItem(ModItems.DRAGONITE_HELMET);
        trimmedArmorItem(ModItems.DRAGONITE_CHESTPLATE);
        trimmedArmorItem(ModItems.DRAGONITE_LEGGINGS);
        trimmedArmorItem(ModItems.DRAGONITE_BOOTS);

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

    // Shoutout to El_Redstoniano for making this
    private void trimmedArmorItem(RegistryObject<Item> itemRegistryObject) {
        final String MOD_ID = EndlessSands.MOD_ID; // Change this to your mod id

        if(itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.entrySet().forEach(entry -> {

                ResourceKey<TrimMaterial> trimMaterial = entry.getKey();
                float trimValue = entry.getValue();

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = "item/" + itemRegistryObject.getId().getPath();
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
                ResourceLocation armorItemResLoc = modLoc(armorItemPath);
                ResourceLocation trimResLoc = mcLoc(trimPath); // minecraft namespace
                ResourceLocation trimNameResLoc = modLoc(currentTrimName);

                // This is used for making the ExistingFileHelper acknowledge that this texture exist, so this will
                // avoid an IllegalArgumentException
                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // Trimmed armorItem files
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc)
                        .texture("layer1", trimResLoc);

                // Non-trimmed armorItem file (normal variant)
                this.withExistingParent(itemRegistryObject.getId().getPath(),
                                mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc))
                        .predicate(mcLoc("trim_type"), trimValue).end()
                        .texture("layer0",
                                modLoc("item/" + itemRegistryObject.getId().getPath()));
            });
        }
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

    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(), mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + item.getId().getPath()));
    }

}
