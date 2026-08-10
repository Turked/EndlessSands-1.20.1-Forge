package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.client.HeatstrokeWarningClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record HeatstrokeWarningS2CPacket(int tier) {
    public static void encode(HeatstrokeWarningS2CPacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.tier);
    }

    public static HeatstrokeWarningS2CPacket decode(FriendlyByteBuf buffer) {
        return new HeatstrokeWarningS2CPacket(buffer.readVarInt());
    }

    public static void handle(
            HeatstrokeWarningS2CPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> HeatstrokeWarningClientState.show(message.tier)
                )
        );
        context.setPacketHandled(true);
    }
}
