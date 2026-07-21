package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.block.custom.CursedSandLayerBlock;
import net.MechGaming.EndlessSands.effect.BuriedInSandState;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class EscapeBuriedInSandC2SPacket {
    public EscapeBuriedInSandC2SPacket() {}

    public static void encode(
            EscapeBuriedInSandC2SPacket message,
            FriendlyByteBuf buffer
    ) {}

    public static EscapeBuriedInSandC2SPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new EscapeBuriedInSandC2SPacket();
    }

    public static void handle(
            EscapeBuriedInSandC2SPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null
                    || !BuriedInSandState.isActive(player)
                    || !player.level().dimension().equals(
                    ModDimensions.ENDLESS_SANDS_LEVEL
            )) {
                return;
            }

            ServerLevel level = player.serverLevel();
            BlockPos buriedPos = BlockPos.containing(
                    BuriedInSandState.getX(player),
                    BuriedInSandState.getY(player),
                    BuriedInSandState.getZ(player)
            );

            Direction facing = player.getDirection();
            BlockPos secondSandPos =
                    buriedPos.relative(facing.getOpposite());
            BlockPos standingPos =
                    buriedPos.relative(facing);

            BuriedInSandState.deactivate(player);

            // Move the player out of the positions that will contain sand.
            player.connection.teleport(
                    standingPos.getX() + 0.5D,
                    standingPos.getY(),
                    standingPos.getZ() + 0.5D,
                    player.getYRot(),
                    0.0F
            );

            placeOneLayer(level, buriedPos);
            placeOneLayer(level, secondSandPos);

            ModMessages.sendToPlayer(
                    new EndBuriedInSandS2CPacket(
                            player.getId()
                    ),
                    player
            );
        });

        context.setPacketHandled(true);
    }

    private static void placeOneLayer(
            ServerLevel level,
            BlockPos requestedPos
    ) {
        BlockPos placePos = requestedPos;

        if (!level.getBlockState(placePos).canBeReplaced()) {
            placePos = placePos.above();
        }

        if (!level.getBlockState(placePos).canBeReplaced()) {
            return;
        }

        BlockState oneLayer =
                ModBlocks.CURSED_SAND_LAYER.get()
                        .defaultBlockState()
                        .setValue(
                                CursedSandLayerBlock.LAYERS,
                                1
                        );

        level.setBlock(
                placePos,
                oneLayer,
                Block.UPDATE_ALL
        );
    }
}