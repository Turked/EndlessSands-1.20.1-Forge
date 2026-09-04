package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.entity.ZenioniteBeaconBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ZenioniteBeaconBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty BEAM_ACTIVE = BooleanProperty.create("beam_active");

    public ZenioniteBeaconBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(BEAM_ACTIVE, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZenioniteBeaconBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                ModBlockEntities.ZENIONITE_BEACON.get(),
                ZenioniteBeaconBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide
                && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof ZenioniteBeaconBlockEntity beacon) {
            beacon.setOwner(player.getUUID());
        }
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
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ZenioniteBeaconBlockEntity beacon = getOrCreateBeaconBlockEntity(level, pos, state);
            beacon.setOwnerIfAbsent(player.getUUID());
            boolean expanded = ExpandedInventoryHelper.isUnlocked(player);
            NetworkHooks.openScreen(serverPlayer, beacon, data -> {
                data.writeBlockPos(pos);
                data.writeBoolean(expanded);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, BEAM_ACTIVE);
    }

    private static ZenioniteBeaconBlockEntity getOrCreateBeaconBlockEntity(
            Level level,
            BlockPos pos,
            BlockState state
    ) {
        if (level.getBlockEntity(pos) instanceof ZenioniteBeaconBlockEntity beacon) {
            return beacon;
        }

        // Replaces Zenionite Beacons saved before they stopped using the vanilla beacon entity.
        level.removeBlockEntity(pos);
        ZenioniteBeaconBlockEntity beacon = new ZenioniteBeaconBlockEntity(pos, state);
        level.setBlockEntity(beacon);
        return beacon;
    }
}
