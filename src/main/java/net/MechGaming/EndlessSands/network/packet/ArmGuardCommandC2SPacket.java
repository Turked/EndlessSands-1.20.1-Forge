package net.MechGaming.EndlessSands.network.packet;

import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.inventory.ArmGuardSearchMenu;
import net.MechGaming.EndlessSands.item.custom.ArmGuardItem;
import net.MechGaming.EndlessSands.network.ArmGuardAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ArmGuardCommandC2SPacket(ArmGuardAction action, int vultureId, String argument) {
    private static final int MAX_ARGUMENT_LENGTH = 48;

    public static void encode(ArmGuardCommandC2SPacket message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action);
        buffer.writeVarInt(message.vultureId + 1);
        buffer.writeUtf(message.argument, MAX_ARGUMENT_LENGTH);
    }

    public static ArmGuardCommandC2SPacket decode(FriendlyByteBuf buffer) {
        return new ArmGuardCommandC2SPacket(
                buffer.readEnum(ArmGuardAction.class),
                buffer.readVarInt() - 1,
                buffer.readUtf(MAX_ARGUMENT_LENGTH)
        );
    }

    public static void handle(ArmGuardCommandC2SPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> execute(message, context.getSender()));
        context.setPacketHandled(true);
    }

    private static void execute(ArmGuardCommandC2SPacket message, ServerPlayer player) {
        if (player == null) {
            return;
        }

        ItemStack guard = player.getOffhandItem();
        if (!(guard.getItem() instanceof ArmGuardItem)) {
            return;
        }

        if (message.action == ArmGuardAction.RENAME_GUARD) {
            ArmGuardItem.setBindingName(guard, message.argument);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            return;
        }

        Entity entity = player.serverLevel().getEntity(message.vultureId);
        if (!(entity instanceof VultureEntity vulture) || !vulture.canReceiveGuardCommand(player)) {
            return;
        }

        if (message.action == ArmGuardAction.SEARCH) {
            ArmGuardSearchMenu.open(player, vulture);
            return;
        }

        boolean succeeded = switch (message.action) {
            case RENAME_VULTURE -> {
                String name = ArmGuardItem.sanitizeName(message.argument, "");
                vulture.setCustomName(name.isEmpty() ? null : Component.literal(name));
                yield true;
            }
            case EAT -> vulture.commandEatFrom(player);
            case FOLLOW -> vulture.commandFollow(player);
            case RETURN -> vulture.commandReturnToGuard(player);
            case SEARCH -> false;
            case DIE -> {
                vulture.hurt(vulture.damageSources().playerAttack(player), Float.MAX_VALUE);
                yield true;
            }
            case FLY_HOME -> vulture.commandFlyHome(player);
            case RENAME_GUARD -> true;
        };

        if (!succeeded) {
            player.displayClientMessage(Component.translatable(
                    "message.endlesssands.arm_guard.command_failed"), true);
        }
    }
}
