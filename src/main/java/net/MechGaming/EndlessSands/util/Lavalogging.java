package net.MechGaming.EndlessSands.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class Lavalogging {
    public static final BooleanProperty LAVA_LOGGED = BooleanProperty.create("endlesssands_lava_logged");

    private Lavalogging() {
    }

    public static boolean isSupported(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.hasProperty(LAVA_LOGGED);
    }

    public static boolean isLavaLogged(BlockState state) {
        return isSupported(state) && state.getValue(LAVA_LOGGED);
    }

    public static boolean isEmpty(BlockState state) {
        return isSupported(state)
                && !state.getValue(BlockStateProperties.WATERLOGGED)
                && !state.getValue(LAVA_LOGGED);
    }

    public static BlockState geometryState(BlockState state) {
        return isLavaLogged(state) ? state.setValue(LAVA_LOGGED, false) : state;
    }

    public static BlockState withFluid(BlockState state, Fluid fluid) {
        return state
                .setValue(BlockStateProperties.WATERLOGGED, fluid == Fluids.WATER)
                .setValue(LAVA_LOGGED, fluid == Fluids.LAVA);
    }
}
