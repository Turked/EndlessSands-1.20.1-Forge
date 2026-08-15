package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CrudLogBlock;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.MechGaming.EndlessSands.block.custom.TwigBlock;
import net.MechGaming.EndlessSands.block.custom.VultureNestBlock;
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
        blockWithRandomYRotation(ModBlocks.CURSED_COBBLED_SAPROLITE);
        blockWithRandomYRotation(ModBlocks.CORE_ROCK);
        blockWithRandomYRotation(ModBlocks.DEEP_CRYSTAL_ROCK);
        blockWithRandomYRotation(ModBlocks.LOWER_CRUST_ROCK);
        blockWithRandomYRotation(ModBlocks.CRYSTAL_ROCK);
        blockWithRandomYRotation(ModBlocks.CURSED_BEDROCK);
        cursedSandLayer();

        suspiciousCursedSand();
        blockWithItem(ModBlocks.VILLAGE_POT);
        blockWithItem(ModBlocks.ROTTED_PLANKS);
        vultureNest();
        twig();
        oldworldSapling();

        blockWithTopBottomAndSidesRandomYRotation(ModBlocks.FERTILE_SOIL, "fertile_soil_side", "fertile_soil_top");

        blockWithTopBottomAndSides(ModBlocks.ROTTED_LOG, "rotted_log_side", "rotted_log_top");
        crudLog();

        stairsBlock(((StairBlock) ModBlocks.ROTTED_STAIRS.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()));
        slabBlock(((SlabBlock) ModBlocks.ROTTED_SLAB.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()));

        buttonBlock(((ButtonBlock) ModBlocks.ROTTED_BUTTON.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()));
        pressurePlateBlock(((PressurePlateBlock) ModBlocks.ROTTED_PRESSURE_PLATE.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()));

        fenceBlock(((FenceBlock) ModBlocks.ROTTED_FENCE.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()));
        fenceGateBlock(((FenceGateBlock) ModBlocks.ROTTED_FENCE_GATE.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()));
        wallBlock(((WallBlock) ModBlocks.ROTTED_WALL.get()), blockTexture(ModBlocks.ROTTED_PLANKS.get()));

        doorBlockWithRenderType(((DoorBlock) ModBlocks.ROTTED_DOOR.get()), modLoc("block/rotted_door_bottom"), modLoc("block/rotted_door_top"), "cutout");
        trapdoorBlockWithRenderType(((TrapDoorBlock) ModBlocks.ROTTED_TRAPDOOR.get()), modLoc("block/rotted_trapdoor"), true, "cutout");

    }

    private void oldworldSapling() {
        Block block = ModBlocks.OLDWORLD_SAPLING.get();
        ModelFile model = models().getExistingFile(modLoc("block/oldworld_sapling"));
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void suspiciousCursedSand() {
        Block block = ModBlocks.SUSPICIOUS_CURSED_SAND.get();
        ModelFile model = cubeAll(block);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .build());
        simpleBlockItem(block, model);
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

    private void vultureNest(){
        Block block = ModBlocks.VULTURE_NEST.get();

        ModelFile empty = models().getExistingFile(modLoc("block/vulture_nest_empty"));
        ModelFile full = models().getExistingFile(modLoc("block/vulture_nest_full"));

        ModelFile egg1v1 = models().getExistingFile(modLoc("block/vulture_nest_1_egg_variant_1"));
        ModelFile egg1v2 = models().getExistingFile(modLoc("block/vulture_nest_1_egg_variant_2"));
        ModelFile egg1v3 = models().getExistingFile(modLoc("block/vulture_nest_1_egg_variant_3"));

        ModelFile egg2v1 = models().getExistingFile(modLoc("block/vulture_nest_2_eggs_variant_1"));
        ModelFile egg2v2 = models().getExistingFile(modLoc("block/vulture_nest_2_eggs_variant_2"));
        ModelFile egg2v3 = models().getExistingFile(modLoc("block/vulture_nest_2_eggs_variant_3"));

        getVariantBuilder(block).forAllStates(state -> {
            int eggs = state.getValue(VultureNestBlock.EGGS);
            int variant = state.getValue(VultureNestBlock.VARIANT);

            ModelFile model = switch (eggs) {
                case 0 -> empty;
                case 1 -> switch (variant) {
                    case 2 -> egg1v2;
                    case 3 -> egg1v3;
                    default -> egg1v1;
                };
                case 2 -> switch (variant) {
                    case 2 -> egg2v2;
                    case 3 -> egg2v3;
                    default -> egg2v1;
                };
                case 3 -> full;
                default -> empty;
            };

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(vultureNestYRot(state.getValue(VultureNestBlock.FACING)))
                    .build();
        });
    }

    private int vultureNestYRot(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90;
            case WEST -> 180;
            case NORTH -> 270;
            default -> 0;
        };
    }

    private void twig() {
        Block block = ModBlocks.TWIG.get();
        ModelFile[][][] variantModels = new ModelFile[4][4][16];

        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                for (int rotation = 0; rotation < 16; rotation++) {
                    variantModels[x][z][rotation] = twigVariantModel(
                            "twig_offset_" + x + "_" + z + "_rotation_" + rotation,
                            TwigBlock.offsetFor(x),
                            TwigBlock.offsetFor(z),
                            rotation
                    );
                }
            }
        }

        getVariantBuilder(block).forAllStates(state -> {
            return ConfiguredModel.builder()
                    .modelFile(variantModels
                            [state.getValue(TwigBlock.OFFSET_X)]
                            [state.getValue(TwigBlock.OFFSET_Z)]
                            [state.getValue(TwigBlock.ROTATION)])
                    .build();
        });

        simpleBlockItem(block, models().getExistingFile(modLoc("block/twig")));
    }

    private ModelFile twigVariantModel(String name, int xOffset, int zOffset, int rotation) {
        BlockModelBuilder model = models().getBuilder(name)
                .texture("0", modLoc("block/twig"))
                .texture("particle", modLoc("block/twig"));
        float globalAngle = rotation * 22.5F;

        RotatedTwigElement rodGeometry = rotatedTwigElement(
                6.998F + xOffset, 4.998F + zOffset,
                8.002F + xOffset, 9.002F + zOffset,
                7.0F + xOffset, 8.0F + zOffset,
                0.0F,
                8.0F + xOffset, 8.0F + zOffset,
                globalAngle
        );

        var rod = model.element()
                .from(rodGeometry.fromX(), -0.002F, rodGeometry.fromZ())
                .to(rodGeometry.toX(), 1.002F, rodGeometry.toZ());
        if (rodGeometry.angle() != 0.0F) {
            rod.rotation()
                    .origin(rodGeometry.originX(), 0.0F, rodGeometry.originZ())
                    .axis(Direction.Axis.Y)
                    .angle(rodGeometry.angle())
                    .end();
        }
        int rodQuarterTurns = rodGeometry.quarterTurns();
        rod.face(rotatedHorizontalFace(Direction.NORTH, rodQuarterTurns)).uvs(4, 2, 5, 3).texture("#0");
        rod.face(rotatedHorizontalFace(Direction.EAST, rodQuarterTurns)).uvs(0, 0, 4, 1).texture("#0");
        rod.face(rotatedHorizontalFace(Direction.SOUTH, rodQuarterTurns)).uvs(3, 4, 4, 5).texture("#0");
        rod.face(rotatedHorizontalFace(Direction.WEST, rodQuarterTurns)).uvs(0, 1, 4, 2).texture("#0");
        rod.face(Direction.UP).uvs(1, 6, 0, 2).rotation(faceRotationFor(rodQuarterTurns)).texture("#0");
        rod.face(Direction.DOWN).uvs(2, 2, 1, 6).rotation(faceRotationFor(rodQuarterTurns)).texture("#0");

        RotatedTwigElement rightBranchGeometry = rotatedTwigElement(
                7.0F + xOffset, 8.25F + zOffset,
                8.0F + xOffset, 10.25F + zOffset,
                7.0F + xOffset, 8.25F + zOffset,
                -45.0F,
                8.0F + xOffset, 8.0F + zOffset,
                globalAngle
        );

        var rightBranch = model.element()
                .from(rightBranchGeometry.fromX(), 0.0F, rightBranchGeometry.fromZ())
                .to(rightBranchGeometry.toX(), 1.0F, rightBranchGeometry.toZ());
        if (rightBranchGeometry.angle() != 0.0F) {
            rightBranch.rotation()
                    .origin(rightBranchGeometry.originX(), 0.0F, rightBranchGeometry.originZ())
                    .axis(Direction.Axis.Y)
                    .angle(rightBranchGeometry.angle())
                    .end();
        }
        int rightBranchQuarterTurns = rightBranchGeometry.quarterTurns();
        rightBranch.face(rotatedHorizontalFace(Direction.NORTH, rightBranchQuarterTurns)).uvs(4, 3, 5, 4).texture("#0");
        rightBranch.face(rotatedHorizontalFace(Direction.EAST, rightBranchQuarterTurns)).uvs(2, 2, 4, 3).texture("#0");
        rightBranch.face(rotatedHorizontalFace(Direction.SOUTH, rightBranchQuarterTurns)).uvs(4, 4, 5, 5).texture("#0");
        rightBranch.face(rotatedHorizontalFace(Direction.WEST, rightBranchQuarterTurns)).uvs(2, 3, 4, 4).texture("#0");
        rightBranch.face(Direction.UP).uvs(5, 2, 4, 0).rotation(faceRotationFor(rightBranchQuarterTurns)).texture("#0");
        rightBranch.face(Direction.DOWN).uvs(3, 4, 2, 6).rotation(faceRotationFor(rightBranchQuarterTurns)).texture("#0");

        RotatedTwigElement leftBranchGeometry = rotatedTwigElement(
                6.999F + xOffset, 8.999F + zOffset,
                8.001F + xOffset, 10.001F + zOffset,
                7.0F + xOffset, 8.75F + zOffset,
                22.5F,
                8.0F + xOffset, 8.0F + zOffset,
                globalAngle
        );

        var leftBranch = model.element()
                .from(leftBranchGeometry.fromX(), -0.001F, leftBranchGeometry.fromZ())
                .to(leftBranchGeometry.toX(), 1.001F, leftBranchGeometry.toZ());
        if (leftBranchGeometry.angle() != 0.0F) {
            leftBranch.rotation()
                    .origin(leftBranchGeometry.originX(), 0.0F, leftBranchGeometry.originZ())
                    .axis(Direction.Axis.Y)
                    .angle(leftBranchGeometry.angle())
                    .end();
        }
        int leftBranchQuarterTurns = leftBranchGeometry.quarterTurns();
        leftBranch.face(rotatedHorizontalFace(Direction.NORTH, leftBranchQuarterTurns)).uvs(5, 0, 6, 1).texture("#0");
        leftBranch.face(rotatedHorizontalFace(Direction.EAST, leftBranchQuarterTurns)).uvs(5, 1, 6, 2).texture("#0");
        leftBranch.face(rotatedHorizontalFace(Direction.SOUTH, leftBranchQuarterTurns)).uvs(5, 2, 6, 3).texture("#0");
        leftBranch.face(rotatedHorizontalFace(Direction.WEST, leftBranchQuarterTurns)).uvs(3, 5, 4, 6).texture("#0");
        leftBranch.face(Direction.UP).uvs(6, 4, 5, 3).rotation(faceRotationFor(leftBranchQuarterTurns)).texture("#0");
        leftBranch.face(Direction.DOWN).uvs(5, 5, 4, 6).rotation(faceRotationFor(leftBranchQuarterTurns)).texture("#0");

        return model;
    }

    private RotatedTwigElement rotatedTwigElement(
            float fromX,
            float fromZ,
            float toX,
            float toZ,
            float localOriginX,
            float localOriginZ,
            float localAngle,
            float globalOriginX,
            float globalOriginZ,
            float globalAngle
    ) {
        float centerX = (fromX + toX) / 2.0F;
        float centerZ = (fromZ + toZ) / 2.0F;
        float[] locallyRotated = rotatePoint(
                centerX,
                centerZ,
                localOriginX,
                localOriginZ,
                localAngle
        );
        float[] rotatedCenter = rotatePoint(
                locallyRotated[0],
                locallyRotated[1],
                globalOriginX,
                globalOriginZ,
                globalAngle
        );

        float totalAngle = localAngle + globalAngle;
        int quarterTurns = (int) Math.floor((totalAngle + 45.0F) / 90.0F);
        float modelAngle = totalAngle - quarterTurns * 90.0F;
        float width = toX - fromX;
        float depth = toZ - fromZ;

        if (Math.abs(quarterTurns) % 2 == 1) {
            float swap = width;
            width = depth;
            depth = swap;
        }

        return new RotatedTwigElement(
                rotatedCenter[0] - width / 2.0F,
                rotatedCenter[1] - depth / 2.0F,
                rotatedCenter[0] + width / 2.0F,
                rotatedCenter[1] + depth / 2.0F,
                rotatedCenter[0],
                rotatedCenter[1],
                modelAngle,
                quarterTurns
        );
    }

    private Direction rotatedHorizontalFace(Direction face, int quarterTurns) {
        Direction result = face;

        for (int i = 0; i < Math.floorMod(quarterTurns, 4); i++) {
            result = switch (result) {
                case NORTH -> Direction.WEST;
                case WEST -> Direction.SOUTH;
                case SOUTH -> Direction.EAST;
                case EAST -> Direction.NORTH;
                default -> result;
            };
        }

        return result;
    }

    private FaceRotation faceRotationFor(int quarterTurns) {
        return switch (Math.floorMod(-quarterTurns, 4)) {
            case 1 -> FaceRotation.CLOCKWISE_90;
            case 2 -> FaceRotation.UPSIDE_DOWN;
            case 3 -> FaceRotation.COUNTERCLOCKWISE_90;
            default -> FaceRotation.ZERO;
        };
    }

    private float[] rotatePoint(
            float x,
            float z,
            float originX,
            float originZ,
            float angleDegrees
    ) {
        double angle = Math.toRadians(angleDegrees);
        double relativeX = x - originX;
        double relativeZ = z - originZ;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new float[]{
                (float) (originX + relativeX * cos + relativeZ * sin),
                (float) (originZ - relativeX * sin + relativeZ * cos)
        };
    }

    private record RotatedTwigElement(
            float fromX,
            float fromZ,
            float toX,
            float toZ,
            float originX,
            float originZ,
            float angle,
            int quarterTurns
    ) {}

}
