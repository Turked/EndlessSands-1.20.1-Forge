package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.effect.ModEffects;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends ForgeAdvancementProvider {
    public ModAdvancementProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, registries, existingFileHelper, List.of(new EndlessSandsAdvancements()));
    }

    private static final class EndlessSandsAdvancements implements AdvancementGenerator {
        private static final ResourceLocation ROOT_ID = modLoc("what_have_i_done");
        private static final ResourceLocation BACKGROUND = modLoc("textures/block/cursed_sand.png");

        @Override
        public void generate(
                HolderLookup.Provider registries,
                Consumer<Advancement> saver,
                ExistingFileHelper existingFileHelper
        ) {
            Advancement root = Advancement.Builder.advancement()
                    .display(
                            ModBlocks.CURSED_SAND.get(),
                            Component.translatable("advancements.endlesssands.what_have_i_done.title"),
                            Component.translatable("advancements.endlesssands.what_have_i_done.description"),
                            BACKGROUND,
                            FrameType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion(
                            "entered_endless_sands",
                            PlayerTrigger.TriggerInstance.located(
                                    LocationPredicate.inDimension(ModDimensions.ENDLESS_SANDS_LEVEL)
                            )
                    )
                    .save(saver, ROOT_ID, existingFileHelper);

            Advancement cantBeatTheHeat = heatstrokeAdvancement(
                    root,
                    "cant_beat_the_heat",
                    ModItems.CANT_BEAT_THE_HEAT_ADVANCEMENT_ICON,
                    0,
                    saver,
                    existingFileHelper
            );
            Advancement scorchedEarth = heatstrokeAdvancement(
                    cantBeatTheHeat,
                    "scorched_earth",
                    ModItems.SCORCHED_EARTH_ADVANCEMENT_ICON,
                    1,
                    saver,
                    existingFileHelper
            );
            Advancement deadlyLaser = heatstrokeAdvancement(
                    scorchedEarth,
                    "the_sun_is_a_deadly_laser",
                    ModItems.THE_SUN_IS_A_DEADLY_LASER_ADVANCEMENT_ICON,
                    2,
                    saver,
                    existingFileHelper
            );
            heatstrokeAdvancement(
                    deadlyLaser,
                    "too_hot_to_handle",
                    ModItems.TOO_HOT_TO_HANDLE_ADVANCEMENT_ICON,
                    3,
                    saver,
                    existingFileHelper
            );
        }

        private static Advancement heatstrokeAdvancement(
                Advancement parent,
                String id,
                RegistryObject<Item> icon,
                int tier,
                Consumer<Advancement> saver,
                ExistingFileHelper existingFileHelper
        ) {
            MobEffectsPredicate.MobEffectInstancePredicate tierPredicate =
                    new MobEffectsPredicate.MobEffectInstancePredicate(
                            MinMaxBounds.Ints.exactly(tier),
                            MinMaxBounds.Ints.ANY,
                            null,
                            null
                    );

            return Advancement.Builder.advancement()
                    .parent(parent)
                    .display(
                            icon.get(),
                            Component.translatable("advancements.endlesssands." + id + ".title"),
                            Component.translatable("advancements.endlesssands." + id + ".description"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion(
                            "reach_heatstroke_tier_" + (tier + 1),
                            EffectsChangedTrigger.TriggerInstance.hasEffects(
                                    MobEffectsPredicate.effects().and(ModEffects.HEATSTROKE.get(), tierPredicate)
                            )
                    )
                    .save(saver, modLoc(id), existingFileHelper);
        }

        private static ResourceLocation modLoc(String path) {
            return ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, path);
        }
    }
}
