package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.*;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.generators.BlockModelBuilder;


public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, EndlessSands.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithRandomYRotation(ModBlocks.CURSED_SAND);
        blockWithRandomYRotation(ModBlocks.CURSED_SAPROLITE);
        blockWithRandomYRotation(ModBlocks.CORE_ROCK);
        blockWithRandomYRotation(ModBlocks.DEEP_CRYSTAL_ROCK);
        blockWithRandomYRotation(ModBlocks.LOWER_CRUST_ROCK);
        blockWithRandomYRotation(ModBlocks.CRYSTAL_ROCK);
        blockWithRandomYRotation(ModBlocks.CURSED_BEDROCK);
        cursedSandLayer();

        blockWithItem(ModBlocks.SUSPICIOUS_CURSED_SAND);
        blockWithItem(ModBlocks.VILLAGE_POT);
        blockWithItem(ModBlocks.PALM_PLANKS);

        blockWithTopBottomAndSidesRandomYRotation(ModBlocks.FERTILE_SOIL, "fertile_soil_side", "fertile_soil_top");

        blockWithTopBottomAndSides(ModBlocks.PALM_LOG, "palm_log_side", "palm_log_top");

        stairsBlock(((StairBlock) ModBlocks.PALM_STAIRS.get()), blockTexture(ModBlocks.PALM_PLANKS.get()));
        slabBlock(((SlabBlock) ModBlocks.PALM_SLAB.get()), blockTexture(ModBlocks.PALM_PLANKS.get()), blockTexture(ModBlocks.PALM_PLANKS.get()));

        buttonBlock(((ButtonBlock) ModBlocks.PALM_BUTTON.get()), blockTexture(ModBlocks.PALM_PLANKS.get()));
        pressurePlateBlock(((PressurePlateBlock) ModBlocks.PALM_PRESSURE_PLATE.get()), blockTexture(ModBlocks.PALM_PLANKS.get()));

        fenceBlock(((FenceBlock) ModBlocks.PALM_FENCE.get()), blockTexture(ModBlocks.PALM_PLANKS.get()));
        fenceGateBlock(((FenceGateBlock) ModBlocks.PALM_FENCE_GATE.get()), blockTexture(ModBlocks.PALM_PLANKS.get()));
        wallBlock(((WallBlock) ModBlocks.PALM_WALL.get()), blockTexture(ModBlocks.PALM_PLANKS.get()));

        doorBlockWithRenderType(((DoorBlock) ModBlocks.PALM_DOOR.get()), modLoc("block/palm_door_bottom"), modLoc("block/palm_door_top"), "cutout");
        trapdoorBlockWithRenderType(((TrapDoorBlock) ModBlocks.PALM_TRAPDOOR.get()), modLoc("block/palm_trapdoor"), true, "cutout");

    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockWithTopBottomAndSides(RegistryObject<Block> blockRegistryObject, String sideTexture, String topBottomTexture) {
        simpleBlockWithItem(blockRegistryObject.get(),
                models().cubeBottomTop(
                        blockRegistryObject.getId().getPath(),
                        modLoc("block/" + sideTexture),
                        modLoc("block/" + topBottomTexture),
                        modLoc("block/" + topBottomTexture)
                ));
    }

    private void blockWithRandomYRotation(RegistryObject<Block> blockRegistryObject) {
        Block block = blockRegistryObject.get();
        ModelFile model = cubeAll(block);

        getVariantBuilder(block)
                .partialState()
                .addModels(
                        new ConfiguredModel(model, 0, 0, false),
                        new ConfiguredModel(model, 0, 90, false),
                        new ConfiguredModel(model, 0, 180, false),
                        new ConfiguredModel(model, 0, 270, false)
                );

        simpleBlockItem(block, model);
    }

    private void blockWithTopBottomAndSidesRandomYRotation(RegistryObject<Block> blockRegistryObject, String sideTexture, String topBottomTexture) {
        Block block = blockRegistryObject.get();

        ModelFile model = models().cubeBottomTop(
                blockRegistryObject.getId().getPath(),
                modLoc("block/" + sideTexture),
                modLoc("block/" + topBottomTexture),
                modLoc("block/" + topBottomTexture)
        );

        getVariantBuilder(block)
                .partialState()
                .addModels(
                        new ConfiguredModel(model, 0, 0, false),
                        new ConfiguredModel(model, 0, 90, false),
                        new ConfiguredModel(model, 0, 180, false),
                        new ConfiguredModel(model, 0, 270, false)
                );

        simpleBlockItem(block, model);
    }

    private void cursedSandLayer() {
        Block block = ModBlocks.CURSED_SAND_LAYER.get();

        getVariantBuilder(block).forAllStates(state -> {
            ModelFile model = cursedSandLayerModel(state.getValue(CursedSandLayerBlock.LAYERS));
            return ConfiguredModel.allYRotations(model, 0, false);
        });
    }

    private ModelFile cursedSandLayerModel(int layers) {
        int height = layers * 4;
        String name = "cursed_sand_layer_" + layers;

        BlockModelBuilder model = models().getBuilder(name)
                .texture("particle", modLoc("block/cursed_sand"))
                .texture("texture", modLoc("block/cursed_sand"));

        model.element()
                .from(0, 0, 0)
                .to(16, height, 16)
                .allFaces((direction, face) -> face.texture("#texture").cullface(direction));

        return model;
    }
}
