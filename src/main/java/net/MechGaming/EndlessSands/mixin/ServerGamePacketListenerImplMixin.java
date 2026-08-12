package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyConstant(method = "handleEditBook", constant = @Constant(intValue = 40))
    private int endlessSands$moveOffhandBookSlot(int original) {
        return ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT;
    }

    @Inject(method = "handleSetCarriedItem", at = @At("HEAD"), cancellable = true)
    private void endlessSands$blockLockedTenthHotbarSlot(ServerboundSetCarriedItemPacket packet,
                                                         CallbackInfo callbackInfo) {
        if (packet.getSlot() == ExpandedInventoryHelper.VANILLA_HOTBAR_SIZE
                && !ExpandedInventoryHelper.isUnlocked(this.player)) {
            callbackInfo.cancel();
        }
    }
}
