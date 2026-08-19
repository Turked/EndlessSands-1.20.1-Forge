package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.LinedStairBlock;
import net.MechGaming.EndlessSands.block.entity.LinedStairBlockEntity;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.util.LinedStairData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LinedStairGameTests {
    private LinedStairGameTests() {
    }

    @GameTest(template = "empty")
    public static void craftingPreservesSourceStair(GameTestHelper helper) {
        TransientCraftingContainer grid = craftingGrid();
        CraftingRecipe liningRecipe = recipe(helper, "liquid_lining");
        grid.setItem(0, new ItemStack(Items.HONEYCOMB));
        grid.setItem(1, new ItemStack(Items.SLIME_BALL));
        helper.assertTrue(liningRecipe.matches(grid, helper.getLevel()), "Liquid Lining recipe did not match");
        ItemStack lining = liningRecipe.assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(lining.is(ModItems.LIQUID_LINING.get()), "Liquid Lining recipe returned the wrong item");
        helper.assertTrue(lining.getCount() == 16, "Liquid Lining recipe did not return 16 items");

        grid = craftingGrid();
        CraftingRecipe stairRecipe = recipe(helper, "lined_stairs");
        grid.setItem(0, new ItemStack(Blocks.SANDSTONE_STAIRS));
        grid.setItem(1, new ItemStack(ModItems.LIQUID_LINING.get()));
        helper.assertTrue(stairRecipe.matches(grid, helper.getLevel()), "Lined stair recipe did not match sandstone stairs");
        ItemStack result = stairRecipe.assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(ModBlocks.LINED_STAIRS_ITEM.get()), "Lined stair recipe returned the wrong item");
        helper.assertTrue(
                LinedStairData.getSourceId(result).equals(new ResourceLocation("minecraft", "sandstone_stairs")),
                "Lined stair recipe did not save the sandstone stair ID"
        );

        grid = craftingGrid();
        grid.setItem(0, new ItemStack(Blocks.QUARTZ_STAIRS));
        grid.setItem(1, new ItemStack(ModItems.LIQUID_LINING.get()));
        ItemStack quartzResult = stairRecipe.assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(
                LinedStairData.getSourceId(quartzResult).equals(new ResourceLocation("minecraft", "quartz_stairs")),
                "A second vanilla stair material did not preserve its identity"
        );
        helper.assertTrue(
                quartzResult.getHoverName().getString().equals("Lined Quartz Stairs"),
                "The lined stair did not receive its source stair's dynamic name"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void exactWaterStatesAndBuckets(GameTestHelper helper) {
        LinedStairBlock block = ModBlocks.LINED_STAIRS.get();
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.setBlock(relativePos, block.defaultBlockState());

        BlockState flowEight = LinedStairBlock.withFluidState(
                block.defaultBlockState(),
                Fluids.FLOWING_WATER.getFlowing(8, false)
        );
        helper.assertTrue(!flowEight.getFluidState().isSource(), "Flow level 8 collapsed into a source");
        helper.assertTrue(flowEight.getFluidState().getAmount() == 8, "Flow level 8 lost its amount");

        BlockState falling = LinedStairBlock.withFluidState(
                block.defaultBlockState(),
                Fluids.FLOWING_WATER.getFlowing(8, true)
        );
        helper.assertTrue(
                falling.getFluidState().getValue(net.minecraft.world.level.material.FlowingFluid.FALLING),
                "Falling water lost its falling state"
        );

        helper.setBlock(relativePos, flowEight);
        ItemStack flowingPickup = block.pickupBlock(helper.getLevel(), absolutePos, flowEight);
        helper.assertTrue(flowingPickup.isEmpty(), "A full-height non-source flow was bucketed");
        helper.assertTrue(
                helper.getLevel().getFluidState(absolutePos).getAmount() == 8
                        && !helper.getLevel().getFluidState(absolutePos).isSource(),
                "A refused bucket pickup changed full-height flowing water"
        );
        helper.setBlock(relativePos, block.defaultBlockState());

        helper.assertTrue(
                block.placeLiquid(helper.getLevel(), absolutePos, helper.getLevel().getBlockState(absolutePos),
                        Fluids.WATER.getSource(false)),
                "A water bucket source could not enter an empty lined stair"
        );
        BlockState sourceFilled = helper.getLevel().getBlockState(absolutePos);
        helper.assertTrue(sourceFilled.getFluidState().isSource(), "Source water was not retained");
        ItemStack pickup = block.pickupBlock(helper.getLevel(), absolutePos, sourceFilled);
        helper.assertTrue(pickup.is(Items.WATER_BUCKET), "Source pickup did not return a water bucket");
        helper.assertTrue(helper.getLevel().getBlockState(absolutePos).is(block), "Bucket pickup removed the lined stair");
        helper.assertTrue(helper.getLevel().getFluidState(absolutePos).isEmpty(), "Bucket pickup left water behind");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void vanillaDispenserBucketsRoundTrip(GameTestHelper helper) {
        BlockPos dispenserPos = new BlockPos(1, 1, 1);
        BlockPos linedPos = new BlockPos(2, 1, 1);
        BlockPos powerPos = new BlockPos(1, 2, 1);
        helper.setBlock(
                dispenserPos,
                Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST)
        );
        helper.setBlock(linedPos, ModBlocks.LINED_STAIRS.get().defaultBlockState());
        DispenserBlockEntity dispenser = (DispenserBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(dispenserPos));
        dispenser.setItem(0, new ItemStack(Items.WATER_BUCKET));
        helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK);

        helper.runAtTickTime(8, () -> {
            helper.assertTrue(
                    helper.getBlockState(linedPos).getFluidState().isSource(),
                    "A dispenser water bucket did not fill the lined stair"
            );
            helper.assertTrue(dispenser.getItem(0).is(Items.BUCKET), "Dispenser insertion did not leave an empty bucket");
            helper.setBlock(powerPos, Blocks.AIR);
        });
        helper.runAtTickTime(12, () -> helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK));
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(
                    helper.getBlockState(linedPos).getFluidState().isEmpty(),
                    "A dispenser empty bucket did not extract a lined-stair source"
            );
            helper.assertTrue(
                    dispenser.getItem(0).is(Items.WATER_BUCKET),
                    "Dispenser extraction did not return a water bucket"
            );
            helper.assertTrue(
                    helper.getBlockState(linedPos).is(ModBlocks.LINED_STAIRS.get()),
                    "Dispenser extraction removed the lined stair"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 180)
    public static void waterFlowsThroughAndRecedesWithoutReplacingStairs(GameTestHelper helper) {
        LinedStairBlock linedStair = ModBlocks.LINED_STAIRS.get();
        BlockState alignedStair = linedStair.defaultBlockState()
                .setValue(LinedStairBlock.FACING, Direction.NORTH)
                .setValue(LinedStairBlock.HALF, Half.BOTTOM)
                .setValue(LinedStairBlock.SHAPE, StairsShape.STRAIGHT);
        BlockPos sourcePos = new BlockPos(1, 1, 1);
        BlockPos firstStairPos = new BlockPos(2, 1, 1);
        BlockPos secondStairPos = new BlockPos(3, 1, 1);
        BlockPos exitPos = new BlockPos(4, 1, 1);

        for (int x = 0; x <= 5; x++) {
            for (int z = 0; z <= 2; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
            helper.setBlock(new BlockPos(x, 1, 0), Blocks.STONE);
            helper.setBlock(new BlockPos(x, 1, 2), Blocks.STONE);
        }
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.STONE);
        helper.setBlock(firstStairPos, alignedStair);
        helper.setBlock(secondStairPos, alignedStair);
        helper.setBlock(sourcePos, Blocks.WATER);

        helper.runAtTickTime(35, () -> {
            helper.assertTrue(
                    !helper.getBlockState(firstStairPos).getFluidState().isEmpty(),
                    "Water did not enter the first lined stair"
            );
            helper.assertTrue(
                    !helper.getBlockState(secondStairPos).getFluidState().isEmpty(),
                    "Water did not continue through aligned stair openings"
            );
            helper.assertTrue(
                    !helper.getBlockState(exitPos).getFluidState().isEmpty(),
                    "Water did not flow back out of the lined stairs"
            );
            helper.setBlock(sourcePos, Blocks.AIR);
        });
        helper.runAtTickTime(140, () -> {
            helper.assertTrue(
                    helper.getBlockState(firstStairPos).is(linedStair)
                            && helper.getBlockState(secondStairPos).is(linedStair),
                    "Receding water replaced a lined stair"
            );
            helper.assertTrue(
                    helper.getBlockEntity(firstStairPos) instanceof LinedStairBlockEntity
                            && helper.getBlockEntity(secondStairPos) instanceof LinedStairBlockEntity,
                    "Receding water deleted a lined stair block entity"
            );
            helper.assertTrue(
                    helper.getBlockState(firstStairPos).getFluidState().isEmpty()
                            && helper.getBlockState(secondStairPos).getFluidState().isEmpty(),
                    "Stored flowing water did not weaken and disappear after its source was removed"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void fallingWaterEntersFromAbove(GameTestHelper helper) {
        BlockPos linedPos = new BlockPos(1, 1, 1);
        BlockPos sourcePos = new BlockPos(1, 3, 1);
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        helper.setBlock(
                linedPos,
                ModBlocks.LINED_STAIRS.get().defaultBlockState()
                        .setValue(LinedStairBlock.FACING, Direction.NORTH)
                        .setValue(LinedStairBlock.HALF, Half.BOTTOM)
                        .setValue(LinedStairBlock.SHAPE, StairsShape.STRAIGHT)
        );
        helper.setBlock(sourcePos, Blocks.WATER);

        helper.runAtTickTime(35, () -> {
            BlockState state = helper.getBlockState(linedPos);
            helper.assertTrue(state.is(ModBlocks.LINED_STAIRS.get()), "Falling water replaced the lined stair");
            helper.assertTrue(!state.getFluidState().isEmpty(), "Falling water did not enter the lined stair from above");
            helper.assertTrue(
                    !state.getFluidState().isSource()
                            && state.getFluidState().getValue(net.minecraft.world.level.material.FlowingFluid.FALLING),
                    "Vertical flow did not preserve its falling-water state"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void identityAndRegistrationStayLocal(GameTestHelper helper) {
        helper.assertTrue(
                ModBlocks.LINED_STAIRS.get().getStateDefinition().getPossibleStates().size() == 800,
                "Universal lined stair state count changed unexpectedly"
        );
        helper.assertTrue(
                !Blocks.OAK_STAIRS.defaultBlockState().hasProperty(LinedStairBlock.EXACT_WATER_LEVEL),
                "Exact water level leaked onto an ordinary stair"
        );
        long registeredLinedStairs = ForgeRegistries.BLOCKS.getKeys().stream()
                .filter(id -> id.getNamespace().equals(EndlessSands.MOD_ID))
                .filter(id -> id.getPath().equals("lined_stairs"))
                .count();
        helper.assertTrue(registeredLinedStairs == 1, "More than one universal lined-stair block was registered");

        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.setBlock(relativePos, ModBlocks.LINED_STAIRS.get().defaultBlockState());
        helper.assertTrue(
                helper.getLevel().getBlockEntity(absolutePos) instanceof LinedStairBlockEntity,
                "Placed lined stair did not create its non-ticking block entity"
        );
        LinedStairBlockEntity blockEntity = (LinedStairBlockEntity) helper.getLevel().getBlockEntity(absolutePos);
        blockEntity.setOriginalStairId(new ResourceLocation("minecraft", "quartz_stairs"));
        ItemStack picked = ModBlocks.LINED_STAIRS.get().getCloneItemStack(
                helper.getLevel(), absolutePos, helper.getLevel().getBlockState(absolutePos)
        );
        helper.assertTrue(
                LinedStairData.getSourceId(picked).equals(new ResourceLocation("minecraft", "quartz_stairs")),
                "Pick-block did not preserve the source stair ID"
        );
        helper.succeed();
    }

    private static CraftingRecipe recipe(GameTestHelper helper, String path) {
        return (CraftingRecipe) helper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation(EndlessSands.MOD_ID, path))
                .orElseThrow(() -> new IllegalStateException("Missing recipe endlesssands:" + path));
    }

    private static TransientCraftingContainer craftingGrid() {
        AbstractContainerMenu menu = new AbstractContainerMenu(null, -1) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
        return new TransientCraftingContainer(menu, 2, 1);
    }
}
