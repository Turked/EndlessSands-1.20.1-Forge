package net.MechGaming.EndlessSands.block.custom;

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
}