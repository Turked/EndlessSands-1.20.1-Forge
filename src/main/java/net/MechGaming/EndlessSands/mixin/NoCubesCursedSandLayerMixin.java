package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "fr.max2.nocubesreloaded.base.mesh.mesher.CuboidMesher", remap = false)
public abstract class NoCubesCursedSandLayerMixin {
    @Redirect(
            method = "getMeshConfiguration",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;m_60734_()Lnet/minecraft/world/level/block/Block;",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private Block endlesssands$treatCursedSandLayerAsSnow(BlockState state) {
        return state.is(ModBlocks.CURSED_SAND_LAYER.get()) ? Blocks.SNOW : state.getBlock();
    }

    @Redirect(
            method = "buildLeyerMesh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;m_60734_()Lnet/minecraft/world/level/block/Block;",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private static Block endlesssands$treatCursedSandNeighborsAsSnow(BlockState state) {
        if (state.is(ModBlocks.CURSED_SAND_LAYER.get()) || state.is(ModBlocks.CURSED_SAND.get())) {
            return Blocks.SNOW;
        }

        return state.getBlock();
    }

    @Redirect(
            method = "buildLeyerMesh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;m_61143_(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private static Comparable<?> endlesssands$translateCursedSandLayersToSnowLayers(BlockState state, Property<?> property) {
        if (property == SnowLayerBlock.LAYERS) {
            if (state.is(ModBlocks.CURSED_SAND_LAYER.get())) {
                return state.getValue(CursedSandLayerBlock.LAYERS) * 2;
            }

            if (state.is(ModBlocks.CURSED_SAND.get())) {
                return 8;
            }
        }

        return endlesssands$getValue(state, property);
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Comparable<?> endlesssands$getValue(BlockState state, Property<?> property) {
        return state.getValue((Property) property);
    }
}