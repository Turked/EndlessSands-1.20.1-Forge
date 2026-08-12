package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.worldgen.VultureHomeSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class CrudLogBlock extends RotatedPillarBlock {
    public static final BooleanProperty BIRD_DROPPINGS = BooleanProperty.create("bird_droppings");

    public CrudLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, net.minecraft.core.Direction.Axis.Y)
                .setValue(BIRD_DROPPINGS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AXIS, BIRD_DROPPINGS);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            VultureHomeSavedData.get(serverLevel).refreshTreeAt(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
