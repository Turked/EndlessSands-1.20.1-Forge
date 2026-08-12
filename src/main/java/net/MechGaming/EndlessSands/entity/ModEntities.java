package net.MechGaming.EndlessSands.entity;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.custom.PocketSandProjectileEntity;
import net.MechGaming.EndlessSands.entity.custom.RhinoEntity;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.entity.custom.VultureEggProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EndlessSands.MOD_ID);

    public static final RegistryObject<EntityType<RhinoEntity>> RHINO =
            ENTITY_TYPES.register("rhino", () -> EntityType.Builder.of(RhinoEntity::new, MobCategory.CREATURE)
                    .sized(2.5f, 2.5f).build("rhino"));

    public static final RegistryObject<EntityType<VultureEntity>> VULTURE =
            ENTITY_TYPES.register("vulture", () -> EntityType.Builder.of(VultureEntity::new, MobCategory.CREATURE)
                    .sized(0.35F, 0.50F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("vulture"));

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }

    public static final RegistryObject<EntityType<PocketSandProjectileEntity>> POCKET_SAND_PROJECTILE =
            ENTITY_TYPES.register("pocket_sand_projectile", () ->
                    EntityType.Builder.<PocketSandProjectileEntity>of(PocketSandProjectileEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("pocket_sand_projectile"));

    public static final RegistryObject<EntityType<VultureEggProjectileEntity>> VULTURE_EGG_PROJECTILE =
            ENTITY_TYPES.register("vulture_egg_projectile", () ->
                    EntityType.Builder.<VultureEggProjectileEntity>of(VultureEggProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("vulture_egg_projectile"));
}
