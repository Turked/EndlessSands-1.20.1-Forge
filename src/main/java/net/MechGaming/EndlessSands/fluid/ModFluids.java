package net.MechGaming.EndlessSands.fluid;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, EndlessSands.MOD_ID);
    public static final DeferredRegister<FluidType> TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, EndlessSands.MOD_ID);

    public static final RegistryObject<FluidType> ANCIENT_OCEAN_WATER_TYPE = TYPES.register(
            "ancient_ocean_water", () -> new CustomFluidType(FluidType.Properties.create()
                    .descriptionId("block.endlesssands.ancient_ocean_water")
                    .fallDistanceModifier(0F).canExtinguish(true).canConvertToSource(true)
                    .supportsBoating(true).canHydrate(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH),
                    "ancient_ocean_water", false));
    public static final RegistryObject<FluidType> STAR_TOUCHED_LAVA_TYPE = TYPES.register(
            "star_touched_lava", () -> new CustomFluidType(FluidType.Properties.create()
                    .descriptionId("block.endlesssands.star_touched_lava")
                    .canSwim(false).canDrown(false).pathType(BlockPathTypes.LAVA).adjacentPathType(null)
                    .lightLevel(15).density(3000).viscosity(6000).temperature(2600)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA),
                    "star_touched_lava", true));

    public static final RegistryObject<AncientOceanWaterFluid.Source> ANCIENT_OCEAN_WATER =
            FLUIDS.register("ancient_ocean_water", AncientOceanWaterFluid.Source::new);
    public static final RegistryObject<AncientOceanWaterFluid.Flowing> FLOWING_ANCIENT_OCEAN_WATER =
            FLUIDS.register("flowing_ancient_ocean_water", AncientOceanWaterFluid.Flowing::new);
    public static final RegistryObject<StarTouchedLavaFluid.Source> STAR_TOUCHED_LAVA =
            FLUIDS.register("star_touched_lava", StarTouchedLavaFluid.Source::new);
    public static final RegistryObject<StarTouchedLavaFluid.Flowing> FLOWING_STAR_TOUCHED_LAVA =
            FLUIDS.register("flowing_star_touched_lava", StarTouchedLavaFluid.Flowing::new);

    private ModFluids() { }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
    }

    public static void commonSetup() {
        FluidType lava = STAR_TOUCHED_LAVA_TYPE.get();
        FluidInteractionRegistry.addInteraction(lava, new InteractionInformation(
                (level, pos, neighbor, state) -> level.getFluidState(neighbor).is(FluidTags.WATER),
                state -> state.isSource() ? Blocks.OBSIDIAN.defaultBlockState()
                        : Blocks.COBBLESTONE.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(lava, new InteractionInformation(
                (level, pos, neighbor, state) -> level.getBlockState(pos.below()).is(Blocks.SOUL_SOIL)
                        && level.getBlockState(neighbor).is(Blocks.BLUE_ICE),
                Blocks.BASALT.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new InteractionInformation(
                ANCIENT_OCEAN_WATER_TYPE.get(),
                state -> state.isSource() ? Blocks.OBSIDIAN.defaultBlockState()
                        : Blocks.COBBLESTONE.defaultBlockState()));

        DefaultDispenseItemBehavior buckets = new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource source, ItemStack stack) {
                DispensibleContainerItem bucket = (DispensibleContainerItem) stack.getItem();
                var pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                if (bucket.emptyContents(null, source.getLevel(), pos, null, stack)) {
                    return new ItemStack(Items.BUCKET);
                }
                return super.execute(source, stack);
            }
        };
        DispenserBlock.registerBehavior(ModItems.ANCIENT_OCEAN_WATER_BUCKET.get(), buckets);
        DispenserBlock.registerBehavior(ModItems.STAR_TOUCHED_LAVA_BUCKET.get(), buckets);
    }
}
