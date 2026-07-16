package net.MechGaming.EndlessSands.worldgen.dimension;

import com.mojang.serialization.Codec;
import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModChunkGenerators {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, EndlessSands.MOD_ID);

    public static final RegistryObject<Codec<? extends ChunkGenerator>> ENDLESS_SANDS =
            CHUNK_GENERATORS.register("endless_sands", () -> EndlessSandsChunkGenerator.CODEC);

    public static void register(IEventBus eventBus){
        CHUNK_GENERATORS.register(eventBus);
    }
}
