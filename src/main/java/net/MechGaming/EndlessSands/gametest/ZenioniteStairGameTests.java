package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.ZenioniteStairBlock;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity.FlowContext;
import net.MechGaming.EndlessSands.block.entity.ZenioniteStairBlockEntity.Lane;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.MechGaming.EndlessSands.util.LinedStairData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZenioniteStairGameTests {
    private ZenioniteStairGameTests() {
    }

    @GameTest(template = "empty")
    public static void mysticalFluidsRemainIndependent(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState());
        ZenioniteStairBlockEntity stair = stair(helper, pos);
        stair.setFluid(Lane.LEFT, ModFluids.ANCIENT_OCEAN_WATER.get().getSource(false));
        stair.setFluid(Lane.RIGHT, ModFluids.FLOWING_STAR_TOUCHED_LAVA.get()
                .getFlowing(5, false));
        helper.assertTrue(stair.getFluid(Lane.LEFT).getType()
                        .isSame(ModFluids.ANCIENT_OCEAN_WATER.get())
                        && stair.getFluid(Lane.RIGHT).getType()
                        .isSame(ModFluids.STAR_TOUCHED_LAVA.get())
                        && stair.getFluid(Lane.RIGHT).getAmount() == 5,
                "Zenionite stair lanes lost a mystical fluid's exact identity");
        helper.assertTrue(helper.getBlockState(pos).getValue(ZenioniteStairBlock.HAS_LAVA),
                "Star Touched Lava did not light its Zenionite stair lane");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dualFluidsAreIndependentAndPersist(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH));
        ZenioniteStairBlockEntity stair = stair(helper, pos);

        stair.setFluid(Lane.LEFT, Fluids.WATER.getSource(false));
        stair.setFluid(Lane.RIGHT, Fluids.FLOWING_LAVA.getFlowing(4, false));
        helper.assertTrue(stair.getFluid(Lane.LEFT).is(FluidTags.WATER)
                        && stair.getFluid(Lane.LEFT).isSource(),
                "The left source-water state was not retained");
        helper.assertTrue(stair.getFluid(Lane.RIGHT).is(FluidTags.LAVA)
                        && !stair.getFluid(Lane.RIGHT).isSource()
                        && stair.getFluid(Lane.RIGHT).getAmount() == 4,
                "The right flowing-lava level was not retained");
        helper.assertTrue(stair.countLoggedHalves(FluidTags.WATER) == 1
                        && stair.countLoggedHalves(FluidTags.LAVA) == 1,
                "Mixed halves were not counted independently");

        BlockState state = helper.getBlockState(pos);
        helper.assertTrue(!state.getValue(StairBlock.WATERLOGGED),
                "Zenionite stairs used the vanilla waterlogged property");
        helper.assertTrue(state.getFluidState().isEmpty(),
                "A Zenionite stair exposed one half as a full-block vanilla fluid");
        helper.assertTrue(state.getValue(ZenioniteStairBlock.HAS_LAVA),
                "A lava half did not enable stair light emission");

        CompoundTag saved = stair.saveWithFullMetadata();
        ZenioniteStairBlockEntity restored = new ZenioniteStairBlockEntity(
                helper.absolutePos(pos), state);
        restored.load(saved);
        helper.assertTrue(restored.getFluid(Lane.LEFT).isSource()
                        && restored.getFluid(Lane.LEFT).is(FluidTags.WATER),
                "The left half did not survive an NBT round trip");
        helper.assertTrue(restored.getFluid(Lane.RIGHT).is(FluidTags.LAVA)
                        && restored.getFluid(Lane.RIGHT).getAmount() == 4,
                "The right half did not survive an NBT round trip");
        stair.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        stair.setFluid(Lane.RIGHT, Fluids.EMPTY.defaultFluidState());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sideEntryTargetsOnlyThePhysicalHalf(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        BlockState state = ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH);
        helper.setBlock(relativePos, state);
        ZenioniteStairBlock block = ModBlocks.ZENIONITE_STAIRS.get();
        ZenioniteStairBlockEntity stair = stair(helper, relativePos);

        helper.assertTrue(block.placeLiquidFromFlow(helper.getLevel(), absolutePos, state,
                        Direction.EAST, Fluids.FLOWING_WATER.getFlowing(6, false)),
                "Water entering from the stair's left side was rejected");
        helper.assertTrue(stair.getFluid(Lane.LEFT).is(FluidTags.WATER)
                        && stair.getFluid(Lane.RIGHT).isEmpty(),
                "Left-side flow did not stay in the left half");

        state = helper.getLevel().getBlockState(absolutePos);
        helper.assertTrue(block.placeLiquidFromFlow(helper.getLevel(), absolutePos, state,
                        Direction.WEST, Fluids.FLOWING_LAVA.getFlowing(5, false)),
                "Lava entering from the stair's right side was rejected");
        helper.assertTrue(stair.getFluid(Lane.LEFT).is(FluidTags.WATER)
                        && stair.getFluid(Lane.RIGHT).is(FluidTags.LAVA),
                "Right-side flow overwrote or crossed into the left half");
        stair.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        stair.setFluid(Lane.RIGHT, Fluids.EMPTY.defaultFluidState());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void playerBucketsTargetAndExtractIndependentHalves(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        BlockState state = ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH);
        helper.setBlock(relativePos, state);
        ZenioniteStairBlock block = ModBlocks.ZENIONITE_STAIRS.get();
        ZenioniteStairBlockEntity stair = stair(helper, relativePos);
        Player player = helper.makeMockPlayer();
        Vec3 center = Vec3.atCenterOf(absolutePos);
        BlockHitResult leftHit = new BlockHitResult(
                center.add(-0.25D, 0.0D, 0.0D), Direction.UP, absolutePos, false);
        BlockHitResult rightHit = new BlockHitResult(
                center.add(0.25D, 0.0D, 0.0D), Direction.UP, absolutePos, false);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        block.use(state, helper.getLevel(), absolutePos, player,
                InteractionHand.MAIN_HAND, leftHit);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
        block.use(helper.getBlockState(relativePos), helper.getLevel(), absolutePos, player,
                InteractionHand.MAIN_HAND, rightHit);
        helper.assertTrue(stair.getFluid(Lane.LEFT).is(FluidTags.WATER)
                        && stair.getFluid(Lane.RIGHT).is(FluidTags.LAVA),
                "Player bucket targeting did not preserve mixed left/right halves");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        block.use(helper.getBlockState(relativePos), helper.getLevel(), absolutePos, player,
                InteractionHand.MAIN_HAND, rightHit);
        helper.assertTrue(player.getMainHandItem().is(Items.LAVA_BUCKET)
                        && stair.getFluid(Lane.RIGHT).isEmpty(),
                "An empty bucket did not extract the targeted right lava half");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        block.use(helper.getBlockState(relativePos), helper.getLevel(), absolutePos, player,
                InteractionHand.MAIN_HAND, leftHit);
        helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET)
                        && stair.getFluid(Lane.LEFT).isEmpty(),
                "An empty bucket did not extract the targeted left water half");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oppositeFacingStairsKeepWorldSpaceLanes(GameTestHelper helper) {
        BlockPos sourcePos = new BlockPos(1, 1, 1);
        BlockPos targetPos = sourcePos.south();
        helper.setBlock(sourcePos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH));
        helper.setBlock(targetPos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.SOUTH));
        ZenioniteStairBlockEntity target = stair(helper, targetPos);
        BlockPos absoluteSource = helper.absolutePos(sourcePos);
        BlockPos absoluteTarget = helper.absolutePos(targetPos);
        ZenioniteStairBlock targetBlock = ModBlocks.ZENIONITE_STAIRS.get();
        BlockState targetState = helper.getBlockState(targetPos);
        ZenioniteStairBlockEntity.runWithFlowContext(
                new FlowContext(absoluteSource, Lane.LEFT, Direction.WEST,
                        Fluids.WATER.getSource(false)),
                () -> targetBlock.placeLiquidFromFlow(helper.getLevel(), absoluteTarget,
                        targetState, Direction.SOUTH, Fluids.FLOWING_WATER.getFlowing(8, false)));
        ZenioniteStairBlockEntity.runWithFlowContext(
                new FlowContext(absoluteSource, Lane.RIGHT, Direction.EAST,
                        Fluids.LAVA.getSource(false)),
                () -> targetBlock.placeLiquidFromFlow(helper.getLevel(), absoluteTarget,
                        helper.getLevel().getBlockState(absoluteTarget), Direction.SOUTH,
                        Fluids.FLOWING_LAVA.getFlowing(7, false)));

        helper.assertTrue(target.getFluid(Lane.RIGHT).is(FluidTags.WATER),
                "Opposite-facing stairs changed the world-left water lane");
        helper.assertTrue(target.getFluid(Lane.LEFT).is(FluidTags.LAVA),
                "Opposite-facing stairs changed the world-right lava lane");
        target.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        target.setFluid(Lane.RIGHT, Fluids.EMPTY.defaultFluidState());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void orthogonalConnectionUsesTheTouchingHalf(GameTestHelper helper) {
        BlockPos sourcePos = new BlockPos(1, 1, 2);
        BlockPos targetPos = sourcePos.east();
        BlockState targetState = ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH);
        helper.setBlock(targetPos, targetState);

        ZenioniteStairBlockEntity target = stair(helper, targetPos);
        ZenioniteStairBlock targetBlock = ModBlocks.ZENIONITE_STAIRS.get();
        ZenioniteStairBlockEntity.runWithFlowContext(
                new FlowContext(helper.absolutePos(sourcePos), Lane.LEFT, Direction.NORTH,
                        Fluids.WATER.getSource(false)),
                () -> targetBlock.placeLiquidFromFlow(
                        helper.getLevel(), helper.absolutePos(targetPos), targetState,
                        Direction.EAST, Fluids.FLOWING_WATER.getFlowing(8, false)));

        helper.assertTrue(target.getFluid(Lane.LEFT).is(FluidTags.WATER)
                        && target.getFluid(Lane.RIGHT).isEmpty(),
                "An orthogonal connection filled the far half instead of the touching half");
        target.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedTouchingHalfIsNotAValidFlowRoute(GameTestHelper helper) {
        BlockPos sourcePos = new BlockPos(1, 1, 1);
        BlockPos targetPos = sourcePos.east();
        BlockState targetState = ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH);
        helper.setBlock(targetPos, targetState);
        ZenioniteStairBlockEntity target = stair(helper, targetPos);
        target.setFluid(Lane.LEFT, Fluids.LAVA.getSource(false));

        boolean[] accepted = {true};
        ZenioniteStairBlockEntity.runWithFlowContext(
                new FlowContext(helper.absolutePos(sourcePos), Lane.RIGHT, Direction.EAST,
                        Fluids.WATER.getSource(false)),
                () -> accepted[0] = ModBlocks.ZENIONITE_STAIRS.get().canPlaceLiquid(
                        helper.getLevel(), helper.absolutePos(targetPos), targetState,
                        Fluids.WATER));
        helper.assertTrue(!accepted[0] && target.getFluid(Lane.RIGHT).isEmpty(),
                "Flow treated an occupied touching half as valid because the far half was empty");
        target.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void farSideFluidCannotSustainTheOtherHalf(GameTestHelper helper) {
        BlockPos stairPos = new BlockPos(2, 1, 2);
        helper.setBlock(stairPos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH));
        helper.setBlock(stairPos.east(), Blocks.WATER.defaultBlockState());
        ZenioniteStairBlockEntity stair = stair(helper, stairPos);
        stair.setFluid(Lane.LEFT, Fluids.FLOWING_WATER.getFlowing(5, false));

        tickStair(helper, stairPos, stair, 5);
        helper.assertTrue(stair.getFluid(Lane.LEFT).isEmpty(),
                "Water on the right side sustained the independently logged left half");
        helper.setBlock(stairPos.east(), Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 30)
    public static void scheduledZenioniteFlowUsesTheRuntimeMixin(GameTestHelper helper) {
        BlockPos sourcePos = new BlockPos(1, 1, 2);
        BlockPos targetPos = sourcePos.east();
        BlockState stairState = ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH);
        helper.setBlock(sourcePos, stairState);
        helper.setBlock(targetPos, stairState);
        helper.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(sourcePos.west(), Blocks.STONE.defaultBlockState());
        helper.setBlock(sourcePos.north(), Blocks.STONE.defaultBlockState());
        helper.setBlock(sourcePos.south(), Blocks.STONE.defaultBlockState());
        helper.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(targetPos.east(), Blocks.STONE.defaultBlockState());
        helper.setBlock(targetPos.north(), Blocks.STONE.defaultBlockState());
        helper.setBlock(targetPos.south(), Blocks.STONE.defaultBlockState());
        stair(helper, sourcePos).setFluid(Lane.RIGHT, Fluids.WATER.getSource(false));

        helper.runAtTickTime(8, () -> {
            ZenioniteStairBlockEntity target = stair(helper, targetPos);
            helper.assertTrue(target.getFluid(Lane.LEFT).is(FluidTags.WATER)
                            && target.getFluid(Lane.RIGHT).isEmpty(),
                    "Scheduled stair flow did not enter through the touching half via the mixin");
            stair(helper, sourcePos).setFluid(Lane.RIGHT, Fluids.EMPTY.defaultFluidState());
            target.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void fluidAboveRetainsFallingState(GameTestHelper helper) {
        BlockPos lowerPos = new BlockPos(2, 1, 2);
        BlockPos upperPos = lowerPos.above();
        BlockState lowerState = ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH);
        BlockState upperState = lowerState.setValue(StairBlock.HALF,
                net.minecraft.world.level.block.state.properties.Half.TOP);
        helper.setBlock(lowerPos, lowerState);
        helper.setBlock(upperPos, upperState);
        ZenioniteStairBlockEntity lower = stair(helper, lowerPos);
        ZenioniteStairBlockEntity upper = stair(helper, upperPos);
        upper.setFluid(Lane.LEFT, Fluids.WATER.getSource(false));
        lower.setFluid(Lane.LEFT, Fluids.FLOWING_WATER.getFlowing(8, true));

        tickStair(helper, lowerPos, lower, 5);
        helper.assertTrue(lower.getFluid(Lane.LEFT).is(FluidTags.WATER)
                        && lower.getFluid(Lane.LEFT).getValue(FlowingFluid.FALLING),
                "A falling lane lost its falling state while fed from above");
        upper.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        lower.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twoAllowedWaterSourcesCreateALaneSource(GameTestHelper helper) {
        BlockPos stairPos = new BlockPos(2, 1, 1);
        helper.setBlock(stairPos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH));
        helper.setBlock(stairPos.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(stairPos.west(), Blocks.WATER.defaultBlockState());
        helper.setBlock(stairPos.south(), Blocks.WATER.defaultBlockState());
        helper.setBlock(stairPos.north(), Blocks.STONE.defaultBlockState());
        helper.setBlock(stairPos.east(), Blocks.STONE.defaultBlockState());
        helper.setBlock(stairPos.west().west(), Blocks.STONE.defaultBlockState());
        helper.setBlock(stairPos.west().north(), Blocks.STONE.defaultBlockState());
        helper.setBlock(stairPos.west().south(), Blocks.STONE.defaultBlockState());
        helper.setBlock(stairPos.south().south(), Blocks.STONE.defaultBlockState());
        helper.setBlock(stairPos.south().east(), Blocks.STONE.defaultBlockState());
        ZenioniteStairBlockEntity stair = stair(helper, stairPos);
        stair.setFluid(Lane.LEFT, Fluids.FLOWING_WATER.getFlowing(5, false));

        tickStair(helper, stairPos, stair, 5);
        helper.assertTrue(stair.getFluid(Lane.LEFT).isSource(),
                "Two valid adjacent water sources did not create a source in the lane");
        stair.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        helper.setBlock(stairPos.west(), Blocks.AIR.defaultBlockState());
        helper.setBlock(stairPos.south(), Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void liquidLiningCannotCraftZenioniteStairs(GameTestHelper helper) {
        CraftingRecipe recipe = recipe(helper, "lined_stairs");
        TransientCraftingContainer grid = craftingGrid();
        grid.setItem(0, new ItemStack(ModBlocks.ZENIONITE_STAIRS.get()));
        grid.setItem(1, new ItemStack(ModItems.LIQUID_LINING.get()));

        helper.assertTrue(!LinedStairData.isValidSourceBlock(ModBlocks.ZENIONITE_STAIRS.get()),
                "Zenionite stairs remained valid Liquid Lining sources");
        helper.assertTrue(!recipe.matches(grid, helper.getLevel()),
                "Zenionite stairs still matched the Liquid Lining recipe");
        helper.assertTrue(recipe.assemble(grid, helper.getLevel().registryAccess()).isEmpty(),
                "The Liquid Lining recipe still assembled Zenionite stairs");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void loggedHalvesFillHorizontalChargersEverySecond(GameTestHelper helper) {
        BlockPos mixedChargerPos = new BlockPos(1, 1, 1);
        BlockPos waterChargerPos = new BlockPos(1, 1, 3);
        BlockPos mixedStairPos = mixedChargerPos.east();
        BlockPos waterStairPos = waterChargerPos.east();
        helper.setBlock(mixedChargerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        helper.setBlock(waterChargerPos, ModBlocks.ZENIONITE_CHARGER.get().defaultBlockState());
        helper.setBlock(mixedStairPos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState());
        helper.setBlock(waterStairPos, ModBlocks.ZENIONITE_STAIRS.get().defaultBlockState());

        ZenioniteStairBlockEntity mixedStair = stair(helper, mixedStairPos);
        mixedStair.setFluid(Lane.LEFT, Fluids.WATER.getSource(false));
        mixedStair.setFluid(Lane.RIGHT, Fluids.FLOWING_LAVA.getFlowing(3, false));
        ZenioniteStairBlockEntity waterStair = stair(helper, waterStairPos);
        waterStair.setFluid(Lane.LEFT, Fluids.WATER.getSource(false));
        waterStair.setFluid(Lane.RIGHT, Fluids.FLOWING_WATER.getFlowing(5, false));

        ZenioniteChargerBlockEntity mixedCharger = charger(helper, mixedChargerPos);
        ZenioniteChargerBlockEntity waterCharger = charger(helper, waterChargerPos);
        tickCharger(helper, mixedChargerPos, mixedCharger, 19);
        tickCharger(helper, waterChargerPos, waterCharger, 19);
        helper.assertTrue(mixedCharger.getWaterAmount() == 0 && mixedCharger.getLavaAmount() == 0,
                "A mixed stair paid out before twenty ticks");
        helper.assertTrue(waterCharger.getWaterAmount() == 0,
                "A double-water stair paid out before twenty ticks");

        tickCharger(helper, mixedChargerPos, mixedCharger, 1);
        tickCharger(helper, waterChargerPos, waterCharger, 1);
        helper.assertTrue(mixedCharger.getWaterAmount() == 1_000
                        && mixedCharger.getLavaAmount() == 1_000,
                "A mixed stair did not provide one water and one lava unit per second");
        helper.assertTrue(waterCharger.getWaterAmount() == 2_000,
                "Two water halves did not provide two water units per second");
        helper.assertTrue(mixedCharger.getWaterCollectionProgress() == 0
                        && mixedCharger.getLavaCollectionProgress() == 0
                        && waterCharger.getWaterCollectionProgress() == 0,
                "Zenionite stair intake was also counted by the slow environmental collector");
        mixedStair.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        mixedStair.setFluid(Lane.RIGHT, Fluids.EMPTY.defaultFluidState());
        waterStair.setFluid(Lane.LEFT, Fluids.EMPTY.defaultFluidState());
        waterStair.setFluid(Lane.RIGHT, Fluids.EMPTY.defaultFluidState());
        helper.succeed();
    }

    private static ZenioniteStairBlockEntity stair(GameTestHelper helper, BlockPos pos) {
        if (helper.getBlockEntity(pos) instanceof ZenioniteStairBlockEntity stair) {
            return stair;
        }
        throw new IllegalStateException("Missing Zenionite stair block entity at " + pos);
    }

    private static ZenioniteChargerBlockEntity charger(GameTestHelper helper, BlockPos pos) {
        if (helper.getBlockEntity(pos) instanceof ZenioniteChargerBlockEntity charger) {
            return charger;
        }
        throw new IllegalStateException("Missing Zenionite Charger block entity at " + pos);
    }

    private static void tickCharger(
            GameTestHelper helper,
            BlockPos relativePos,
            ZenioniteChargerBlockEntity charger,
            int count
    ) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenioniteChargerBlockEntity.serverTick(helper.getLevel(), absolutePos,
                    helper.getLevel().getBlockState(absolutePos), charger);
        }
    }

    private static void tickStair(
            GameTestHelper helper,
            BlockPos relativePos,
            ZenioniteStairBlockEntity stair,
            int count
    ) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        for (int tick = 0; tick < count; tick++) {
            ZenioniteStairBlockEntity.serverTick(helper.getLevel(), absolutePos,
                    helper.getLevel().getBlockState(absolutePos), stair);
        }
    }

    private static CraftingRecipe recipe(GameTestHelper helper, String path) {
        return (CraftingRecipe) helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, path))
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
