package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.event.DragonEggSacrificeEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ConfirmDragonEggSacrificeC2SPacket(BlockPos holderPos, InteractionHand hand) {
    public static void encode(ConfirmDragonEggSacrificeC2SPacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.holderPos);
        buffer.writeEnum(message.hand);
    }

    public static ConfirmDragonEggSacrificeC2SPacket decode(FriendlyByteBuf buffer) {
        return new ConfirmDragonEggSacrificeC2SPacket(
                buffer.readBlockPos(), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(ConfirmDragonEggSacrificeC2SPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                DragonEggSacrificeEvents.tryBegin(player, message.holderPos, message.hand);
            }
        });
        context.setPacketHandled(true);
    }
}
