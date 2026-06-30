package net.MechGaming.EndlessSands.item;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EndlessSands.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ENDLESS_SANDS_TAB = CREATIVE_MODE_TABS.register("endless_sands_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.CURSED_POCKET_SAND.get()))
                    .title(Component.translatable("creativetab.endless_sands_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        // Item Items
                        pOutput.accept(ModItems.CURSED_POCKET_SAND.get());
                        pOutput.accept(ModItems.BITTY_BONE.get());
                        pOutput.accept(ModItems.ITTY_BITTY_BONE.get());
                        pOutput.accept(ModItems.OLDWORLD_ROSE.get());
                        pOutput.accept(ModItems.SCROLL_OF_LORE.get());
                        pOutput.accept(ModItems.SCROLL_OF_WISDOM.get());
                        pOutput.accept(ModItems.SCROLL_OF_MEDIOCRITY.get());
                        pOutput.accept(ModItems.SCROLL_OF_YEARNING.get());

                        // Advanced Items
                        pOutput.accept(ModItems.TINY_OLDWORLD_JAR.get());
                        pOutput.accept(ModItems.BRITTLE_BONE_MEAL.get());
                        pOutput.accept(ModItems.OLDWORLD_SCROLL.get());
                        pOutput.accept(ModItems.VULTURE_EGG.get());

                        //Food Items
                        pOutput.accept(ModItems.HANDFUL_OF_SCRAMBLED_EGGS.get());
                        pOutput.accept(ModItems.BOILED_VULTURE_EGG.get());

                        //Fuel Items
                        pOutput.accept(ModItems.ROTTED_WOOD.get());

                        //Tool Items
                        pOutput.accept(ModItems.DRAGONITE_SWORD.get());
                        pOutput.accept(ModItems.DRAGONITE_PICKAXE.get());
                        pOutput.accept(ModItems.DRAGONITE_AXE.get());
                        pOutput.accept(ModItems.DRAGONITE_SHOVEL.get());
                        pOutput.accept(ModItems.DRAGONITE_HOE.get());

                        // Block Items
                        pOutput.accept(ModBlocks.CURSED_SAND.get());
                        pOutput.accept(ModBlocks.CURSED_SANDSTONE.get());
                        pOutput.accept(ModBlocks.SUSPICIOUS_CURSED_SAND.get());
                        pOutput.accept(ModBlocks.FERTILE_SOIL.get());
                        pOutput.accept(ModBlocks.PALM_LOG.get());
                        pOutput.accept(ModBlocks.PALM_PLANKS.get());

                        // Advanced Blocks
                        pOutput.accept(ModBlocks.VILLAGE_POT.get());

                        //Building Blocks
                        pOutput.accept(ModBlocks.PALM_DOOR.get());
                        pOutput.accept(ModBlocks.PALM_SLAB.get());
                        pOutput.accept(ModBlocks.PALM_TRAPDOOR.get());
                        pOutput.accept(ModBlocks.PALM_WALL.get());
                        pOutput.accept(ModBlocks.PALM_FENCE.get());
                        pOutput.accept(ModBlocks.PALM_FENCE_GATE.get());
                        pOutput.accept(ModBlocks.PALM_BUTTON.get());
                        pOutput.accept(ModBlocks.PALM_PRESSURE_PLATE.get());
                        pOutput.accept(ModBlocks.PALM_STAIRS.get());
                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
