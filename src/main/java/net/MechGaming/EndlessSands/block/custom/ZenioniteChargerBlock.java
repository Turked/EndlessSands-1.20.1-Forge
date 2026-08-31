package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.entity.ModBlockEntities;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ZenioniteChargerBlock extends BaseEntityBlock {
    public static final IntegerProperty WATER = IntegerProperty.create("water", 0, 8);
    public static final IntegerProperty LAVA = IntegerProperty.create("lava", 0, 8);
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 8);
    public static final BooleanProperty POWER_DRAINING = BooleanProperty.create("power_draining");

    public ZenioniteChargerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(WATER, 0)
                .setValue(LAVA, 0)
                .setValue(POWER, 0)
                .setValue(POWER_DRAINING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATER, LAVA, POWER, POWER_DRAINING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZenioniteChargerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type
    ) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                ModBlockEntities.ZENIONITE_CHARGER.get(),
                ZenioniteChargerBlockEntity::serverTick
        );
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
        ItemStack held = player.getItemInHand(hand);
        boolean filledBucket = held.is(Items.WATER_BUCKET) || held.is(Items.LAVA_BUCKET);
        if (filledBucket) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            if (level.getBlockEntity(pos) instanceof ZenioniteChargerBlockEntity charger
                    && charger.tryManualBucket(player, held)) {
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        if (hand == InteractionHand.MAIN_HAND) {
            ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offhand.is(Items.WATER_BUCKET) || offhand.is(Items.LAVA_BUCKET)) {
                return InteractionResult.PASS;
            }
        }

        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ZenioniteChargerBlockEntity charger) {
            boolean expanded = ExpandedInventoryHelper.isUnlocked(player);
            NetworkHooks.openScreen(serverPlayer, charger, data -> {
                data.writeBlockPos(pos);
                data.writeBoolean(expanded);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ZenioniteChargerBlockEntity charger) {
            charger.dropInventory();
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
