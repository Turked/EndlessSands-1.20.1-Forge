package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.item.custom.ArmGuardItem;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class OpenArmGuardMenuC2SPacket {
    public static void encode(OpenArmGuardMenuC2SPacket message, FriendlyByteBuf buffer) {
    }

    public static OpenArmGuardMenuC2SPacket decode(FriendlyByteBuf buffer) {
        return new OpenArmGuardMenuC2SPacket();
    }

    public static void handle(OpenArmGuardMenuC2SPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.getOffhandItem().getItem() instanceof ArmGuardItem)) {
                return;
            }

            VultureEntity perched = VultureEntity.findArmPerchedVulture(player, false);
            if (player.isShiftKeyDown()) {
                if (perched != null) {
                    perched.commandDismountFromGuard(player);
                }
                return;
            }

            VultureEntity tamePerched = VultureEntity.findControlledVulture(player);
            ModMessages.sendToPlayer(new OpenArmGuardMenuS2CPacket(
                    tamePerched == null ? -1 : tamePerched.getId(),
                    tamePerched != null && tamePerched.hasKnownHome()), player);
        });
        context.setPacketHandled(true);
    }
}
