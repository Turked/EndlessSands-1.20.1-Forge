package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.entity.ZenionitePortalFrameBlockEntity;
import net.MechGaming.EndlessSands.event.ModEvents;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ZenionitePortalBlock extends Block {
    public static final BooleanProperty TRANSFER_ACTIVE = BooleanProperty.create("transfer_active");
    public static final BooleanProperty PHARAOH_IMPRISONED =
            BooleanProperty.create("pharaoh_imprisoned");
    private static final VoxelShape SHAPE = Block.box(
            0.0D, 11.75D, 0.0D,
            16.0D, 12.25D, 16.0D
    );
    private static final int VALIDATION_INTERVAL_TICKS = 10;

    public ZenionitePortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(TRANSFER_ACTIVE, false)
                .setValue(PHARAOH_IMPRISONED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TRANSFER_ACTIVE, PHARAOH_IMPRISONED);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(PHARAOH_IMPRISONED) ? Shapes.block() : SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(PHARAOH_IMPRISONED) ? Shapes.block() : Shapes.empty();
    }

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston
    ) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        if (!ZenionitePortalFrameBlockEntity.isPortalInteriorActive(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        level.scheduleTick(pos, this, VALIDATION_INTERVAL_TICKS);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(PHARAOH_IMPRISONED)
                || state.getValue(TRANSFER_ACTIVE)
                || !(entity instanceof ServerPlayer player)
                || player.level().dimension().equals(ModDimensions.PRISON_REALM_LEVEL)
                || player.isOnPortalCooldown()
                || !player.canChangeDimensions()) {
            return;
        }

        player.setPortalCooldown();
        ModEvents.enterPrisonRealm(player, pos);
    }
}
