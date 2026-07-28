package net.MechGaming.EndlessSands.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VultureNestBlock extends Block {
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 1, 3);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 6, 14);

    public VultureNestBlock(Properties properties){
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EGGS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
        builder.add(EGGS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPE;
    }
}
