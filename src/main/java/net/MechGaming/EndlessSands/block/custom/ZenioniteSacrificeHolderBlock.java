package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.block.entity.ZenionitePortalFrameBlockEntity;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.OpenDragonEggSacrificeS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ZenioniteSacrificeHolderBlock extends Block {
    public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");
    private static final VoxelShape EMPTY_SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public ZenioniteSacrificeHolderBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OCCUPIED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(Items.DRAGON_EGG)
                || state.getValue(OCCUPIED)
                || !ZenionitePortalFrameBlockEntity.canAcceptDragonEgg(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(new OpenDragonEggSacrificeS2CPacket(pos, hand), serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return state.getValue(OCCUPIED) ? Block.box(0, 0, 0, 16, 16, 16) : EMPTY_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED);
    }
}
