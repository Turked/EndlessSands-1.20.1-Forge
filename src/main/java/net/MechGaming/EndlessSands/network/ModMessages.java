package net.MechGaming.EndlessSands.network;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.network.packet.BeginBuriedInSandS2CPacket;
import net.MechGaming.EndlessSands.network.packet.EndBuriedInSandS2CPacket;
import net.MechGaming.EndlessSands.network.packet.EscapeBuriedInSandC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(
                            EndlessSands.MOD_ID,
                            "messages"
                    ),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    private ModMessages() {}

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                BeginBuriedInSandS2CPacket.class,
                BeginBuriedInSandS2CPacket::encode,
                BeginBuriedInSandS2CPacket::decode,
                BeginBuriedInSandS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                EscapeBuriedInSandC2SPacket.class,
                EscapeBuriedInSandC2SPacket::encode,
                EscapeBuriedInSandC2SPacket::decode,
                EscapeBuriedInSandC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                EndBuriedInSandS2CPacket.class,
                EndBuriedInSandS2CPacket::encode,
                EndBuriedInSandS2CPacket::decode,
                EndBuriedInSandS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendToPlayer(
            Object message,
            ServerPlayer player
    ) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                message
        );
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}