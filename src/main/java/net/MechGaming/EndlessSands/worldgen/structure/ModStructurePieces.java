package net.MechGaming.EndlessSands.worldgen.structure;

import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructurePieces {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, EndlessSands.MOD_ID);

    public static final RegistryObject<StructurePieceType> CRUD_TREE =
            STRUCTURE_PIECES.register("crud_tree",
                    () -> (context, tag) -> new CrudTreeStructure.Piece(tag));

    public static void register(IEventBus eventBus){
        STRUCTURE_PIECES.register(eventBus);
    }
}
