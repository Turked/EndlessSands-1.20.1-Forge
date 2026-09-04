package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.block.entity.ZenioniteBeaconBlockEntity;
import net.MechGaming.EndlessSands.inventory.ZenioniteBeaconMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ZenioniteBeaconControlC2SPacket(BlockPos beaconPos, Action action) {
    public enum Action {
        TOGGLE_ENABLED,
        TOGGLE_ENTOMBED
    }

    public static void encode(
            ZenioniteBeaconControlC2SPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(message.beaconPos);
        buffer.writeEnum(message.action);
    }

    public static ZenioniteBeaconControlC2SPacket decode(FriendlyByteBuf buffer) {
        return new ZenioniteBeaconControlC2SPacket(
                buffer.readBlockPos(),
                buffer.readEnum(Action.class));
    }

    public static void handle(
            ZenioniteBeaconControlC2SPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> execute(message, context.getSender()));
        context.setPacketHandled(true);
    }

    private static void execute(
            ZenioniteBeaconControlC2SPacket message,
            ServerPlayer player
    ) {
        if (player == null
                || !(player.containerMenu instanceof ZenioniteBeaconMenu menu)
                || !menu.getBeaconPos().equals(message.beaconPos)
                || !(player.level().getBlockEntity(message.beaconPos)
                instanceof ZenioniteBeaconBlockEntity beacon)
                || !beacon.stillValid(player)) {
            return;
        }

        switch (message.action) {
            case TOGGLE_ENABLED -> beacon.toggleEnabled();
            case TOGGLE_ENTOMBED -> beacon.toggleEntombed();
        }
        player.containerMenu.broadcastChanges();
    }
}
