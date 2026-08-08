package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EndlessSands.MOD_ID);

    public static final RegistryObject<BlockEntityType<VultureNestBlockEntity>> VULTURE_NEST =
            BLOCK_ENTITIES.register("vulture_nest", () ->
                    BlockEntityType.Builder.of(VultureNestBlockEntity::new, ModBlocks.VULTURE_NEST.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
