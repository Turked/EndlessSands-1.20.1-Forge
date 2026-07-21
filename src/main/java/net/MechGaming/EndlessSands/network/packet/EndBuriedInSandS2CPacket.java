package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.client.BuriedInSandClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EndBuriedInSandS2CPacket(int entityId) {
    public static void encode(
            EndBuriedInSandS2CPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(message.entityId);
    }

    public static EndBuriedInSandS2CPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new EndBuriedInSandS2CPacket(
                buffer.readVarInt()
        );
    }

    public static void handle(
            EndBuriedInSandS2CPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () ->
                                BuriedInSandClientState.end(
                                        message.entityId
                                )
                )
        );

        context.setPacketHandled(true);
    }
}