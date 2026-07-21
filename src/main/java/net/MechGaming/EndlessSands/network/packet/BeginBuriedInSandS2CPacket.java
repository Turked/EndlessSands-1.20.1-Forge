package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.client.BuriedInSandClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record BeginBuriedInSandS2CPacket(int entityId) {
    public static void encode(
            BeginBuriedInSandS2CPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(message.entityId);
    }

    public static BeginBuriedInSandS2CPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new BeginBuriedInSandS2CPacket(
                buffer.readVarInt()
        );
    }

    public static void handle(
            BeginBuriedInSandS2CPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () ->
                                BuriedInSandClientState.begin(
                                        message.entityId
                                )
                )
        );

        context.setPacketHandled(true);
    }
}