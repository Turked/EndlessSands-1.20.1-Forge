package net.MechGaming.EndlessSands.worldgen.structure;

import com.mojang.serialization.Codec;
import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, EndlessSands.MOD_ID);

    public static final RegistryObject<StructureType<CrudTreeStructure>> CRUD_TREE_TYPE =
            STRUCTURE_TYPES.register("crud_tree", () -> new StructureType<CrudTreeStructure>() {
                @Override
                public Codec<CrudTreeStructure> codec() {
                    return CrudTreeStructure.CODEC;
                }
            });

    public static final RegistryObject<StructureType<FloatingIslandStructure>> FLOATING_ISLAND_TYPE =
            STRUCTURE_TYPES.register("floating_island", () -> new StructureType<FloatingIslandStructure>() {
                @Override
                public Codec<FloatingIslandStructure> codec() {
                    return FloatingIslandStructure.CODEC;
                }
            });

    public static final ResourceKey<Structure> CRUD_TREE = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "crud_tree")
    );

    public static final ResourceKey<StructureSet> CRUD_TREE_SET = ResourceKey.create(
            Registries.STRUCTURE_SET,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "crud_tree")
    );

    public static final ResourceKey<Structure> FLOATING_ISLAND = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "floating_island")
    );

    public static final ResourceKey<StructureSet> FLOATING_ISLAND_SET = ResourceKey.create(
            Registries.STRUCTURE_SET,
            ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, "floating_island")
    );

    public static void register(IEventBus eventBus){
        STRUCTURE_TYPES.register(eventBus);
    }
}
