package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.ZenionitePortalFrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ZenionitePortalFrameBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty BEDROCK = BooleanProperty.create("bedrock");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<Port> NORTH = EnumProperty.create("north", Port.class);
    public static final EnumProperty<Port> EAST = EnumProperty.create("east", Port.class);
    public static final EnumProperty<Port> SOUTH = EnumProperty.create("south", Port.class);
    public static final EnumProperty<Port> WEST = EnumProperty.create("west", Port.class);

    public enum Port implements StringRepresentable {
        NONE("none"), INPUT("input"), OUTPUT("output");

        private final String name;

        Port(String name) { this.name = name; }

        @Override
        public String getSerializedName() { return name; }
    }

    public static EnumProperty<Port> portProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> throw new IllegalArgumentException("Portal ports must be horizontal");
        };
    }
    private static final VoxelShape BASE_SHAPE = Block.box(0.0D, 0.0D, 0.0D,
            16.0D, 13.0D, 16.0D);
    private static final VoxelShape FILLED_SHAPE = Shapes.or(BASE_SHAPE,
            Block.box(5.0D, 13.0D, 5.0D, 11.0D, 16.0D, 11.0D));

    public ZenionitePortalFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(NORTH, Port.NONE).setValue(EAST, Port.NONE)
                .setValue(SOUTH, Port.NONE).setValue(WEST, Port.NONE)
                .setValue(BEDROCK, false)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BEDROCK, POWERED, NORTH, EAST, SOUTH, WEST);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BEDROCK, false)
                .setValue(POWERED, false);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.BEDROCK) || state.getValue(BEDROCK)) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(pos) instanceof ZenionitePortalFrameBlockEntity frame
                && frame.isPharaohGate()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(BEDROCK, true), Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            ZenionitePortalFrameBlockEntity.updatePortalState(level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(BEDROCK) ? FILLED_SHAPE : BASE_SHAPE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(BEDROCK) ? 15 : 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        for (Direction side : Direction.Plane.HORIZONTAL) {
            rotated = rotated.setValue(portProperty(rotation.rotate(side)), state.getValue(portProperty(side)));
        }
        return rotated;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
        for (Direction side : Direction.Plane.HORIZONTAL) {
            mirrored = mirrored.setValue(portProperty(mirror.mirror(side)), state.getValue(portProperty(side)));
        }
        return mirrored;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZenionitePortalFrameBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide || type != ModBlockEntities.ZENIONITE_PORTAL_FRAME.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) ->
                ZenionitePortalFrameBlockEntity.serverTick(
                        tickLevel,
                        pos,
                        tickState,
                        (ZenionitePortalFrameBlockEntity) blockEntity
                );
    }
}
