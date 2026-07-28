package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CrudLogBlock;
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
import net.minecraftforge.client.model.generators.ModelBuilder.FaceRotation;


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
        crudLog();

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

    private void crudLog() {
        Block block = ModBlocks.CRUD_LOG.get();

        ModelFile vertical = models().cubeBottomTop(
                "crud_log",
                modLoc("block/crud_log_side"),
                modLoc("block/crud_log_top"),
                modLoc("block/crud_log_top")
        );

        ModelFile xNormal = crudLogHorizontalModel("crud_log_x", Direction.Axis.X, "crud_log_side");
        ModelFile xDroppings = crudLogHorizontalModel("crud_log_x_bird_droppings", Direction.Axis.X, "crud_log_side_with_bird_droppings");
        ModelFile zNormal = crudLogHorizontalModel("crud_log_z", Direction.Axis.Z, "crud_log_side");
        ModelFile zDroppings = crudLogHorizontalModel("crud_log_z_bird_droppings", Direction.Axis.Z, "crud_log_side_with_bird_droppings");

        getVariantBuilder(block).forAllStates(state -> {
            Direction.Axis axis = state.getValue(CrudLogBlock.AXIS);
            boolean birdDroppings = state.getValue(CrudLogBlock.BIRD_DROPPINGS);

            ModelFile model = vertical;
            if (axis == Direction.Axis.X) {
                model = birdDroppings ? xDroppings : xNormal;
            } else if (axis == Direction.Axis.Z) {
                model = birdDroppings ? zDroppings : zNormal;
            }

            return ConfiguredModel.builder().modelFile(model).build();
        });

        simpleBlockItem(block, vertical);
    }

    private ModelFile crudLogHorizontalModel(String name, Direction.Axis axis, String skyTexture) {
        BlockModelBuilder model = models().getBuilder(name)
                .texture("particle", modLoc("block/crud_log_side"))
                .texture("side", modLoc("block/crud_log_side"))
                .texture("end", modLoc("block/crud_log_top"))
                .texture("sky", modLoc("block/" + skyTexture));

        var element = model.element().from(0, 0, 0).to(16, 16, 16);

        if (axis == Direction.Axis.X) {
            element.face(Direction.UP).texture("#sky").rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.UP);
            element.face(Direction.DOWN).texture("#side").rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.DOWN);
            element.face(Direction.EAST).texture("#end").cullface(Direction.EAST);
            element.face(Direction.WEST).texture("#end").cullface(Direction.WEST);
            element.face(Direction.NORTH).texture("#side").rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.NORTH);
            element.face(Direction.SOUTH).texture("#side").rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.SOUTH);
        } else {
            element.face(Direction.UP).texture("#sky").cullface(Direction.UP);
            element.face(Direction.DOWN).texture("#side").cullface(Direction.DOWN);
            element.face(Direction.NORTH).texture("#end").cullface(Direction.NORTH);
            element.face(Direction.SOUTH).texture("#end").cullface(Direction.SOUTH);
            element.face(Direction.EAST).texture("#side").rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.EAST);
            element.face(Direction.WEST).texture("#side").rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.WEST);
        }

        return model;
    }
}
