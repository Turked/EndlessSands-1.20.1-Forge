package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.client.screen.DragonEggSacrificeScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenDragonEggSacrificeS2CPacket(BlockPos holderPos, InteractionHand hand) {
    public static void encode(OpenDragonEggSacrificeS2CPacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.holderPos);
        buffer.writeEnum(message.hand);
    }

    public static OpenDragonEggSacrificeS2CPacket decode(FriendlyByteBuf buffer) {
        return new OpenDragonEggSacrificeS2CPacket(
                buffer.readBlockPos(), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(OpenDragonEggSacrificeS2CPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DragonEggSacrificeScreen.open(message.holderPos, message.hand)));
        context.setPacketHandled(true);
    }
}
