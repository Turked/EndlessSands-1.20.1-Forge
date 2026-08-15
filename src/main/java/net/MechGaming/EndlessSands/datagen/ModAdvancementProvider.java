package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.effect.ModEffects;
import net.MechGaming.EndlessSands.entity.ModEntities;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.MechGaming.EndlessSands.worldgen.structure.ModStructures;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
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
        private static final TextColor ORANGE = TextColor.fromRgb(0xFFA500);
        private static final TextColor RED = TextColor.fromRgb(0xFF5555);
        private static final TextColor COOL_BLUE = TextColor.fromRgb(0x55D6FF);

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

            heatstrokeAdvancement(
                    root,
                    "cant_beat_the_heat",
                    ModItems.CANT_BEAT_THE_HEAT_ADVANCEMENT_ICON,
                    0,
                    saver,
                    existingFileHelper
            );
            heatstrokeAdvancement(
                    root,
                    "too_hot_to_handle",
                    ModItems.TOO_HOT_TO_HANDLE_ADVANCEMENT_ICON,
                    1,
                    saver,
                    existingFileHelper
            );
            heatstrokeAdvancement(
                    root,
                    "the_sun_is_a_deadly_laser",
                    ModItems.THE_SUN_IS_A_DEADLY_LASER_ADVANCEMENT_ICON,
                    2,
                    saver,
                    existingFileHelper
            );
            Advancement scorchedEarth = heatstrokeAdvancement(
                    root,
                    "scorched_earth",
                    ModItems.SCORCHED_EARTH_ADVANCEMENT_ICON,
                    3,
                    saver,
                    existingFileHelper
            );

            Advancement vulture = simpleAdvancement(
                    root,
                    "find_a_vulture",
                    ModItems.VULTURE_ADVANCEMENT_ICON.get(),
                    Component.translatable("advancements.endlesssands.find_a_vulture.title"),
                    Component.literal("Chickens... or maybe parrots... evolved into these scavengers of the desert\n")
                            .append(colored("Locate a vulture...", ORANGE))
                            .append(colored(" maybe consider ", COOL_BLUE))
                            .append(coloredItalic("following it", COOL_BLUE)),
                    "look_at_vulture",
                    new ImpossibleTrigger.TriggerInstance(),
                    saver,
                    existingFileHelper
            );

            Advancement crudTree = simpleAdvancement(
                    vulture,
                    "what_a_cruddy_tree",
                    ModBlocks.CRUD_LOG.get(),
                    Component.translatable("advancements.endlesssands.what_a_cruddy_tree.title"),
                    Component.literal("The sun's heat bearing down has turned this tree into practically charcoal\n")
                            .append(colored("Locate a crud tree\n", ORANGE))
                            .append(colored(
                                    "I wonder how the vultures would react to their nest being destroyed/stolen?",
                                    COOL_BLUE
                            )),
                    "located_crud_tree",
                    PlayerTrigger.TriggerInstance.located(
                            LocationPredicate.Builder.location()
                                    .setStructure(ModStructures.CRUD_TREE)
                                    .build()
                    ),
                    saver,
                    existingFileHelper
            );

            simpleAdvancement(
                    crudTree,
                    "what_goes_up_mustnt_come_down",
                    ModItems.FLOATING_ISLAND_ADVANCEMENT_ICON.get(),
                    Component.translatable("advancements.endlesssands.what_goes_up_mustnt_come_down.title"),
                    Component.literal("Seems some of the land managed to escape the ")
                            .append(Component.literal("Pharaoh's grasp")
                                    .withStyle(style -> style.withItalic(true)))
                            .append(Component.literal("\n"))
                            .append(colored("Locate a floating island\n", ORANGE))
                            .append(colored(
                                    "These twigs seem mighty useful. Alas, only half as useful as the stuff we could find ",
                                    COOL_BLUE
                            ))
                            .append(coloredItalic("up there", COOL_BLUE)),
                    "located_floating_island",
                    PlayerTrigger.TriggerInstance.located(
                            LocationPredicate.Builder.location()
                                    .setStructure(ModStructures.FLOATING_ISLAND)
                                    .build()
                    ),
                    saver,
                    existingFileHelper
            );

            Advancement.Builder.advancement()
                    .parent(scorchedEarth)
                    .display(
                            ModItems.TWIG_HAT.get(),
                            Component.translatable("advancements.endlesssands.craft_yourself_some_twig_headgear.title"),
                            Component.literal("Craft a twig hat or twig visor"),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion(
                            "crafted_twig_hat",
                            RecipeCraftedTrigger.TriggerInstance.craftedItem(modLoc("twig_hat"))
                    )
                    .addCriterion(
                            "crafted_twig_visor",
                            RecipeCraftedTrigger.TriggerInstance.craftedItem(modLoc("twig_visor"))
                    )
                    .requirements(RequirementsStrategy.OR)
                    .save(saver, modLoc("craft_yourself_some_twig_headgear"), existingFileHelper);

            Advancement guardUp = simpleAdvancement(
                    vulture,
                    "guard_up",
                    ModItems.ARM_GUARD.get(),
                    Component.translatable("advancements.endlesssands.guard_up.title"),
                    Component.literal("Vulture leather and twigs should be enough to get the job done"),
                    "crafted_arm_guard",
                    RecipeCraftedTrigger.TriggerInstance.craftedItem(modLoc("arm_guard")),
                    saver,
                    existingFileHelper
            );

            Advancement newBestFriend = Advancement.Builder.advancement()
                    .parent(guardUp)
                    .display(
                            ModItems.ARM_GUARD.get(),
                            Component.translatable("advancements.endlesssands.new_best_friend.title"),
                            Component.literal("Tame yourself a loyal avian companion... just make sure you've got the proper ")
                                    .append(Component.literal("gear").withStyle(style -> style.withItalic(true)))
                                    .append(Component.literal(" to do so first")),
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion(
                            "tamed_vulture",
                            TameAnimalTrigger.TriggerInstance.tamedAnimal(
                                    EntityPredicate.Builder.entity().of(ModEntities.VULTURE.get()).build()
                            )
                    )
                    .save(saver, modLoc("new_best_friend"), existingFileHelper);

            simpleAdvancement(
                    newBestFriend,
                    "the_ruins_of_the_oldworld",
                    ModItems.OLDWORLD_SCROLL.get(),
                    Component.translatable("advancements.endlesssands.the_ruins_of_the_oldworld.title"),
                    Component.literal("Your loyal avian friend can lead you there... just don't get your hopes up on what you will find."),
                    "located_remains_of_a_village",
                    PlayerTrigger.TriggerInstance.located(
                            LocationPredicate.Builder.location()
                                    .setStructure(ModStructures.REMAINS_OF_A_VILLAGE)
                                    .build()
                    ),
                    saver,
                    existingFileHelper
            );
        }

        private static Advancement simpleAdvancement(
                Advancement parent,
                String id,
                ItemLike icon,
                Component title,
                Component description,
                String criterionName,
                CriterionTriggerInstance criterion,
                Consumer<Advancement> saver,
                ExistingFileHelper existingFileHelper
        ) {
            return Advancement.Builder.advancement()
                    .parent(parent)
                    .display(
                            icon,
                            title,
                            description,
                            null,
                            FrameType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion(criterionName, criterion)
                    .save(saver, modLoc(id), existingFileHelper);
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
                            heatstrokeTitle(id),
                            heatstrokeDescription(id),
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

        private static Component heatstrokeTitle(String id) {
            MutableComponent title = Component.translatable("advancements.endlesssands." + id + ".title");

            return switch (id) {
                case "cant_beat_the_heat" -> padTitle(title, 6, 2);
                case "too_hot_to_handle" -> padTitle(title, 9, 1);
                case "scorched_earth" -> padTitle(title, 10, 2);
                default -> title;
            };
        }

        private static Component padTitle(MutableComponent title, int normalSpaces, int boldSpaces) {
            // Default-font spaces are 4 px wide, or 5 px when bold.
            return title
                    .append(Component.literal(" ".repeat(normalSpaces)))
                    .append(bold(" ".repeat(boldSpaces)));
        }

        private static Component heatstrokeDescription(String id) {
            return switch (id) {
                case "cant_beat_the_heat" -> Component.literal("Heatstroke, if not treated ")
                        .append(bold("will"))
                        .append(Component.literal(" lead to death:\n"))
                        .append(coloredLine(
                                "Cause",
                                colored("Being out in the sun for over a minute without a full set of sun gear\n", ORANGE),
                                ORANGE
                        ))
                        .append(coloredLine("Effect", colored("Mining Fatigue I\n", RED), RED))
                        .append(coloredLine(
                                "Cure",
                                colored("Get yourself a full set of sun gear... the ", COOL_BLUE)
                                        .append(coloredItalic(
                                                "not implemented yet don't seem to have too much trouble",
                                                COOL_BLUE
                                        )),
                                COOL_BLUE
                        ));
                case "too_hot_to_handle" -> Component.literal("The longer you're in the sun, the ")
                        .append(bold("worse"))
                        .append(Component.literal(" the effect becomes\n"))
                        .append(coloredLine(
                                "Cause",
                                colored(
                                        "Being out in the sun for over 3 minutes without at least 3 pieces of sun gear on\n",
                                        ORANGE
                                ),
                                ORANGE
                        ))
                        .append(coloredLine("Effect", colored("Mining Fatigue I & Slowness I\n", RED), RED))
                        .append(coloredLine(
                                "Cure",
                                colored("Get yourself at least 3 pieces of sun gear... try out some ", COOL_BLUE)
                                        .append(coloredItalic("not implemented yet", COOL_BLUE)),
                                COOL_BLUE
                        ));
                case "the_sun_is_a_deadly_laser" -> Component.literal("They say heatstroke is one of the ")
                        .append(bold("worst"))
                        .append(Component.literal(" ways to go out\n"))
                        .append(coloredLine(
                                "Cause",
                                colored("Being out in the sun for over 5 minutes without at least 2 pieces of sun gear on\n", ORANGE),
                                ORANGE
                        ))
                        .append(coloredLine("Effect", colored("Mining Fatigue I, Slowness I, & Hunger I\n", RED), RED))
                        .append(coloredLine(
                                "Cure",
                                colored("Get yourself just 2 pieces of sun gear... not implemented yet", COOL_BLUE),
                                COOL_BLUE
                        ));
                case "scorched_earth" -> Component.literal("Guess this is the fate that awaits all ")
                        .append(bold("Oldworld"))
                        .append(Component.literal(" creatures\n"))
                        .append(coloredLine(
                                "Cause",
                                colored("Being out in the sun for over 10 minutes without any shade\n", ORANGE),
                                ORANGE
                        ))
                        .append(coloredLine(
                                "Effect",
                                colored("Mining Fatigue I, Slowness I, Hunger I, & a slow, painful death\n", RED),
                                RED
                        ))
                        .append(coloredLine(
                                "Cure",
                                colored("Get some shade, SPF, or equip yourself a ", COOL_BLUE)
                                        .append(coloredItalic("twig hat", COOL_BLUE)),
                                COOL_BLUE
                        ));
                default -> Component.translatable("advancements.endlesssands." + id + ".description");
            };
        }

        private static MutableComponent bold(String text) {
            return Component.literal(text)
                    .withStyle(style -> style.withBold(true));
        }

        private static MutableComponent coloredLine(String label, Component text, TextColor color) {
            return Component.literal("")
                    .append(Component.literal(label)
                            .withStyle(style -> style.withColor(color).withBold(true)))
                    .append(Component.literal(": ")
                            .withStyle(style -> style.withColor(color)))
                    .append(text);
        }

        private static MutableComponent colored(String text, TextColor color) {
            return Component.literal(text)
                    .withStyle(style -> style.withColor(color));
        }

        private static MutableComponent coloredItalic(String text, TextColor color) {
            return Component.literal(text)
                    .withStyle(style -> style.withColor(color).withItalic(true));
        }

        private static ResourceLocation modLoc(String path) {
            return ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, path);
        }
    }
}
