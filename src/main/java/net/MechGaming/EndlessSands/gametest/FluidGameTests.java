package net.MechGaming.EndlessSands.gametest;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.fluid.FluidConversion;
import net.MechGaming.EndlessSands.fluid.ModFluids;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(EndlessSands.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FluidGameTests {
    private FluidGameTests() { }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void registrationAndVanillaProperties(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WaterFluid water = ModFluids.ANCIENT_OCEAN_WATER.get();
        LavaFluid lava = ModFluids.STAR_TOUCHED_LAVA.get();
        helper.assertTrue(water.is(FluidTags.WATER) && lava.is(FluidTags.LAVA), "Missing vanilla fluid tags");
        helper.assertTrue(water.getTickDelay(level) == Fluids.WATER.getTickDelay(level)
                && water.getDropOff(level) == ((WaterFluid) Fluids.WATER).getDropOff(level)
                && lava.getTickDelay(level) == Fluids.LAVA.getTickDelay(level)
                && lava.getDropOff(level) == ((LavaFluid) Fluids.LAVA).getDropOff(level), "Overworld flow differs from vanilla");
        ServerLevel nether = level.getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null && lava.getTickDelay(nether) == Fluids.LAVA.getTickDelay(nether)
                && lava.getDropOff(nether) == ((LavaFluid) Fluids.LAVA).getDropOff(nether), "Nether lava flow differs from vanilla");
        helper.assertTrue(lava.getFluidType().getLightLevel() == 15, "Lava must emit full light");
        helper.assertTrue(lava.getFluidType().getTemperature() == 2600, "Star Touched Lava must be twice as hot");
        helper.assertTrue(ForgeHooks.getBurnTime(new ItemStack(ModItems.STAR_TOUCHED_LAVA_BUCKET.get()),
                RecipeType.SMELTING) == 20_000, "Lava bucket fuel time changed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void netherStarConvertsOnlyItsSourceAndConnectedFlow(GameTestHelper helper) {
        conversion(helper, true);
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void elderEyeConvertsOnlyItsSourceAndConnectedFlow(GameTestHelper helper) {
        conversion(helper, false);
    }

    private static void conversion(GameTestHelper helper, boolean lava) {
        Block vanilla = lava ? Blocks.LAVA : Blocks.WATER;
        Block converted = lava ? ModBlocks.STAR_TOUCHED_LAVA.get() : ModBlocks.ANCIENT_OCEAN_WATER.get();
        Item trigger = lava ? Items.NETHER_STAR : ModItems.ELDER_EYE.get();
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos flowing = source.east();
        BlockPos falling = flowing.below();
        BlockPos otherSource = source.south();
        BlockPos disconnected = source.north().west();
        place(helper, source, vanilla.defaultBlockState());
        place(helper, flowing, vanilla.defaultBlockState().setValue(LiquidBlock.LEVEL, 4));
        place(helper, falling, vanilla.defaultBlockState().setValue(LiquidBlock.LEVEL, 8));
        place(helper, otherSource, vanilla.defaultBlockState());
        place(helper, disconnected, vanilla.defaultBlockState().setValue(LiquidBlock.LEVEL, 3));
        ItemEntity drop = drop(helper, source, new ItemStack(trigger, 2));
        drop.tick(); // Exercise the actual item-tick hook, not just the conversion helper.
        FluidConversion.advanceConversions(helper.getLevel());
        helper.assertTrue(helper.getBlockState(source).is(converted), "Source did not convert");
        helper.assertTrue(helper.getBlockState(flowing).is(converted)
                && helper.getBlockState(flowing).getValue(LiquidBlock.LEVEL) == 4, "Flow depth was not preserved");
        helper.assertTrue(helper.getBlockState(falling).is(converted)
                && helper.getBlockState(falling).getValue(LiquidBlock.LEVEL) == 8, "Falling flow did not convert");
        helper.assertTrue(helper.getBlockState(otherSource).is(vanilla), "Another source was incorrectly consumed");
        helper.assertTrue(helper.getBlockState(disconnected).is(vanilla), "Disconnected flow was converted");
        helper.assertTrue(drop.getItem().getCount() == 1, "Conversion must consume exactly one item");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void conversionRequiresCorrectItemAndSource(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        place(helper, pos, Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 4));
        ItemEntity star = drop(helper, pos, new ItemStack(Items.NETHER_STAR));
        FluidConversion.tryConvert(star);
        helper.assertTrue(!star.isRemoved() && helper.getBlockState(pos).is(Blocks.LAVA), "Flowing lava accepted a star");
        place(helper, pos, Blocks.WATER.defaultBlockState());
        FluidConversion.tryConvert(star);
        helper.assertTrue(!star.isRemoved() && helper.getBlockState(pos).is(Blocks.WATER), "Star converted water");
        ItemEntity eye = drop(helper, pos, new ItemStack(ModItems.ELDER_EYE.get()));
        place(helper, pos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
        FluidConversion.tryConvert(eye);
        helper.assertTrue(!eye.isRemoved(), "Flowing water consumed an elder eye");
        place(helper, pos, Blocks.LAVA.defaultBlockState());
        FluidConversion.tryConvert(eye);
        helper.assertTrue(!eye.isRemoved() && helper.getBlockState(pos).is(Blocks.LAVA), "Eye converted lava");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void bucketsRoundTripAndWaterlog(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        for (Item bucket : new Item[]{ModItems.ANCIENT_OCEAN_WATER_BUCKET.get(), ModItems.STAR_TOUCHED_LAVA_BUCKET.get()}) {
            place(helper, pos, Blocks.AIR.defaultBlockState());
            ItemStack stack = new ItemStack(bucket);
            helper.assertTrue(FluidUtil.getFluidContained(stack).isPresent(), "Bucket lacks fluid capability");
            helper.assertTrue(((BucketItem) bucket).emptyContents(null, helper.getLevel(), helper.absolutePos(pos), null, stack),
                    "Bucket failed to place its fluid");
            BlockState state = helper.getBlockState(pos);
            ItemStack result = ((BucketPickup) state.getBlock()).pickupBlock(helper.getLevel(), helper.absolutePos(pos), state);
            helper.assertTrue(result.is(bucket), "Picking up the source returned the wrong bucket");
            helper.assertTrue(stack.getCraftingRemainingItem().is(Items.BUCKET), "Empty bucket remainder missing");
        }
        BlockState slab = Blocks.OAK_SLAB.defaultBlockState();
        place(helper, pos, slab);
        helper.assertTrue(((BucketItem) ModItems.ANCIENT_OCEAN_WATER_BUCKET.get()).emptyContents(null,
                helper.getLevel(), helper.absolutePos(pos), null,
                new ItemStack(ModItems.ANCIENT_OCEAN_WATER_BUCKET.get())), "Slab waterlogging failed");
        helper.assertTrue(helper.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED), "Slab remained dry");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void mobWaterImmersionAndLavaBurningRetainVanillaBehavior(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        place(helper, pos, ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState());
        place(helper, pos.above(), ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState());
        Cow cow = helper.spawn(EntityType.COW, pos);
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.setSecondsOnFire(10);
        cow.setAirSupply(20);
        for (int i = 0; i < 4; i++) cow.tick();
        helper.assertTrue(cow.isInWater() && cow.isEyeInFluid(FluidTags.WATER), "Ancient water did not use vanilla immersion");
        helper.assertTrue(!cow.isOnFire() && cow.getAirSupply() < 20, "Ancient water must extinguish and permit drowning");
        // Remove water first, otherwise ordinary lava/water cooling should (correctly) occur.
        place(helper, pos.above(), Blocks.AIR.defaultBlockState());
        place(helper, pos, Blocks.AIR.defaultBlockState());
        place(helper, pos, ModBlocks.STAR_TOUCHED_LAVA.get().defaultBlockState());
        float health = cow.getHealth();
        for (int i = 0; i < 4; i++) {
            cow.setDeltaMovement(Vec3.ZERO);
            cow.tick();
        }
        helper.assertTrue(cow.isInLava() && cow.isOnFire() && cow.getHealth() < health,
                "Star Touched Lava did not retain vanilla burning/damage");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void ancientWaterLetsPlayersBreatheWithFullOxygenBubbles(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        Player player = helper.makeMockSurvivalPlayer();
        BlockPos absolute = helper.absolutePos(pos);
        player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        player.setNoGravity(true);
        player.setAirSupply(-19); // Enter with almost no air; breathing must prevent even the first drowning hit.
        float health = player.getHealth();
        place(helper, pos, ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState());
        place(helper, pos.above(), ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState());
        for (int i = 0; i < 400; i++) {
            player.baseTick();
            helper.assertTrue(player.getAirSupply() == player.getMaxAirSupply(), "Oxygen decreased in Ancient Ocean Water");
            // This is the same condition Forge's air HUD uses, even when air is completely full.
            helper.assertTrue(player.isEyeInFluidType(ForgeMod.WATER_TYPE.get()), "Full oxygen bubbles would be hidden");
        }
        helper.assertTrue(player.isInWater() && player.getHealth() == health, "Ancient water lost swimming or caused drowning");
        Player waterBreathingPlayer = helper.makeMockSurvivalPlayer();
        waterBreathingPlayer.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        waterBreathingPlayer.setAirSupply(-19);
        waterBreathingPlayer.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200));
        waterBreathingPlayer.baseTick();
        helper.assertTrue(waterBreathingPlayer.getAirSupply() == waterBreathingPlayer.getMaxAirSupply(),
                "Water Breathing prevented ancient water from refilling oxygen on entry");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void ancientWaterBreathingFollowsTheFluidAtThePlayersEyes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        Player player = helper.makeMockSurvivalPlayer();
        BlockPos absolute = helper.absolutePos(pos);
        player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        player.setNoGravity(true);
        place(helper, pos, ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState());
        for (int depth : new int[]{0, 1, 8}) {
            place(helper, pos.above(), ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState().setValue(LiquidBlock.LEVEL, depth));
            player.baseTick();
            helper.assertTrue(player.getAirSupply() == player.getMaxAirSupply()
                    && player.isEyeInFluidType(ForgeMod.WATER_TYPE.get()), "Source/flowing/falling ancient water did not allow breathing");
            // Feet remain in Ancient Ocean Water, but ordinary water covers the eyes.
            place(helper, pos.above(), Blocks.WATER.defaultBlockState());
            player.baseTick();
            helper.assertTrue(player.getAirSupply() == player.getMaxAirSupply() - 1, "Breathing leaked into ordinary water");
        }
        player.setAirSupply(-19);
        float health = player.getHealth();
        player.invulnerableTime = 0;
        player.baseTick();
        helper.assertTrue(player.getHealth() < health, "Ordinary water no longer causes drowning");
        // A thin layer below eye height must not activate immersion/the oxygen HUD.
        place(helper, pos.above(), ModBlocks.ANCIENT_OCEAN_WATER.get().defaultBlockState().setValue(LiquidBlock.LEVEL, 7));
        player.baseTick();
        helper.assertTrue(player.getEyeInFluidType().isAir(), "Shallow water below the eyes counted as submerged");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void starLavaDoublesDamageButKeepsItsTimingAndFireProtection(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        float[] vanillaDamage = new float[24];
        for (boolean starTouched : new boolean[]{false, true}) {
            place(helper, pos, Blocks.AIR.defaultBlockState());
            place(helper, pos, (starTouched ? ModBlocks.STAR_TOUCHED_LAVA.get() : Blocks.LAVA).defaultBlockState());
            Cow cow = helper.spawn(EntityType.COW, pos);
            cow.setNoAi(true);
            cow.setNoGravity(true);
            cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0D);
            cow.setHealth(200.0F);
            for (int tick = 0; tick < vanillaDamage.length; tick++) {
                float health = cow.getHealth();
                cow.baseTick();
                float damage = health - cow.getHealth();
                if (starTouched) {
                    helper.assertTrue(damage == vanillaDamage[tick] * 2.0F, "Lava damage amount/timing differs at tick " + tick);
                } else {
                    vanillaDamage[tick] = damage;
                }
            }
            helper.assertTrue(cow.isInLava() && cow.isOnFire() && vanillaDamage[0] == 0.0F
                    && vanillaDamage[1] == 4.0F, "Vanilla lava contact/first-tick grace was lost");
            cow.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200));
            cow.invulnerableTime = 0;
            float protectedHealth = cow.getHealth();
            cow.lavaHurt();
            helper.assertTrue(cow.getHealth() == protectedHealth, "Lava bypassed Fire Resistance");
            cow.discard();
        }
        var blaze = helper.spawn(EntityType.BLAZE, pos);
        float health = blaze.getHealth();
        blaze.lavaHurt();
        helper.assertTrue(blaze.getHealth() == health, "Star lava damaged a fire-immune mob");
        Player player = helper.makeMockSurvivalPlayer();
        BlockPos absolute = helper.absolutePos(pos);
        player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        health = player.getHealth();
        player.lavaHurt();
        helper.assertTrue(player.getHealth() == health - 8.0F, "Star lava did not deal 8 damage to a survival player");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void starLavaDamageUsesActualContactIncludingFlowAndHitboxEdges(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        Cow cow = helper.spawn(EntityType.COW, pos);
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
        BlockPos absolute = helper.absolutePos(pos);
        for (int depth : new int[]{0, 1, 7, 8}) {
            place(helper, pos, ModBlocks.STAR_TOUCHED_LAVA.get().defaultBlockState().setValue(LiquidBlock.LEVEL, depth));
            cow.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
            cow.setHealth(20.0F);
            cow.invulnerableTime = 0;
            cow.lavaHurt();
            helper.assertTrue(cow.getHealth() == 12.0F,
                    "Star lava depth " + depth + " did not deal 8 damage; remaining health: " + cow.getHealth());
        }
        // Same block coordinates, but hovering above the shallow fluid's surface is not contact.
        place(helper, pos, ModBlocks.STAR_TOUCHED_LAVA.get().defaultBlockState().setValue(LiquidBlock.LEVEL, 7));
        cow.setPos(absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5);
        cow.setHealth(20.0F);
        cow.invulnerableTime = 0;
        cow.clearFire();
        cow.baseTick();
        helper.assertTrue(!cow.isInLava() && cow.getHealth() == 20.0F, "Shallow lava hurt an entity above its surface");
        // The entity's center is outside the fluid block, but its hitbox still touches the side.
        place(helper, pos, ModBlocks.STAR_TOUCHED_LAVA.get().defaultBlockState());
        cow.setPos(absolute.getX() + 1.1, absolute.getY(), absolute.getZ() + 0.5);
        cow.setHealth(20.0F);
        cow.invulnerableTime = 0;
        cow.baseTick();
        helper.assertTrue(cow.isInLava() && cow.getHealth() == 12.0F, "Star lava touching the hitbox edge did not deal double damage");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void lavaCoolingWorksWithBothWaterTypes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        for (Block lava : new Block[]{Blocks.LAVA, ModBlocks.STAR_TOUCHED_LAVA.get()}) {
            for (Block water : new Block[]{Blocks.WATER, ModBlocks.ANCIENT_OCEAN_WATER.get()}) {
                for (int depth : new int[]{0, 4}) {
                    place(helper, pos, Blocks.AIR.defaultBlockState());
                    place(helper, pos.east(), Blocks.AIR.defaultBlockState());
                    place(helper, pos.east(), water.defaultBlockState());
                    place(helper, pos, lava.defaultBlockState().setValue(LiquidBlock.LEVEL, depth));
                    helper.assertTrue(helper.getBlockState(pos).is(depth == 0 ? Blocks.OBSIDIAN : Blocks.COBBLESTONE),
                            "Lava/water cooling product differs from vanilla");
                }
            }
        }
        place(helper, pos.east(), Blocks.AIR.defaultBlockState());
        place(helper, pos.below(), Blocks.SOUL_SOIL.defaultBlockState());
        place(helper, pos.east(), Blocks.BLUE_ICE.defaultBlockState());
        place(helper, pos, ModBlocks.STAR_TOUCHED_LAVA.get().defaultBlockState());
        helper.assertTrue(helper.getBlockState(pos).is(Blocks.BASALT), "Star lava did not form basalt");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reskinned_fluids")
    public static void bothReskinsActuallyFlow(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        for (FlowingFluid fluid : new FlowingFluid[]{ModFluids.ANCIENT_OCEAN_WATER.get(), ModFluids.STAR_TOUCHED_LAVA.get()}) {
            place(helper, source, Blocks.AIR.defaultBlockState());
            place(helper, source.below(), Blocks.AIR.defaultBlockState());
            place(helper, source, fluid.defaultFluidState().createLegacyBlock());
            fluid.tick(helper.getLevel(), helper.absolutePos(source), fluid.defaultFluidState());
            helper.assertTrue(helper.getLevel().getFluidState(helper.absolutePos(source.below())).getType()
                    == fluid.getFlowing(), "Fluid did not flow downward with its custom flowing type");
        }
        helper.succeed();
    }

    private static void place(GameTestHelper helper, BlockPos pos, BlockState state) {
        helper.getLevel().setBlock(helper.absolutePos(pos), state, Block.UPDATE_CLIENTS);
    }

    private static ItemEntity drop(GameTestHelper helper, BlockPos pos, ItemStack stack) {
        BlockPos absolute = helper.absolutePos(pos);
        ItemEntity item = new ItemEntity(helper.getLevel(), absolute.getX() + 0.5,
                absolute.getY() + 0.2, absolute.getZ() + 0.5, stack);
        item.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(item);
        return item;
    }
}
