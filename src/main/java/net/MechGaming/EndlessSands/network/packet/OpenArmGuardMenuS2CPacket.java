package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.client.ArmGuardClientScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenArmGuardMenuS2CPacket(int vultureId, boolean hasHome) {
    public static void encode(OpenArmGuardMenuS2CPacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.vultureId + 1);
        buffer.writeBoolean(message.hasHome);
    }

    public static OpenArmGuardMenuS2CPacket decode(FriendlyByteBuf buffer) {
        return new OpenArmGuardMenuS2CPacket(buffer.readVarInt() - 1, buffer.readBoolean());
    }

    public static void handle(OpenArmGuardMenuS2CPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ArmGuardClientScreens.openRadial(message.vultureId, message.hasHome)
        ));
        context.setPacketHandled(true);
    }
}
