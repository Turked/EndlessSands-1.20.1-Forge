package net.MechGaming.EndlessSands.item.custom;

import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.OpenArmGuardMenuC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ArmGuardItem extends Item {
    public static final String ANY_BINDING = "any";
    private static final String BINDING_TAG = "ArmGuardBinding";
    private static final String CONTROLLED_VULTURE_TAG = "ArmGuardVulture";
    private static final int MAX_NAME_LENGTH = 32;

    private final boolean creative;

    public ArmGuardItem(Properties properties, boolean creative) {
        super(properties);
        this.creative = creative;
    }

    public boolean isCreative() {
        return this.creative;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.creative || super.isFoil(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            ModMessages.sendToServer(new OpenArmGuardMenuC2SPacket());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.endlesssands.arm_guard.bound_to", getBindingName(stack))
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static String getBindingName(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(BINDING_TAG)) {
            return ANY_BINDING;
        }
        return sanitizeName(stack.getTag().getString(BINDING_TAG), ANY_BINDING);
    }

    public static void setBindingName(ItemStack stack, String requestedName) {
        String binding = sanitizeName(requestedName, ANY_BINDING);
        if (ANY_BINDING.equalsIgnoreCase(binding)) {
            if (stack.hasTag()) {
                stack.getTag().remove(BINDING_TAG);
            }
            return;
        }
        stack.getOrCreateTag().putString(BINDING_TAG, binding);
    }

    @Nullable
    public static UUID getControlledVulture(ItemStack stack) {
        return stack.hasTag() && stack.getTag().hasUUID(CONTROLLED_VULTURE_TAG)
                ? stack.getTag().getUUID(CONTROLLED_VULTURE_TAG)
                : null;
    }

    public static void linkVulture(ItemStack stack, VultureEntity vulture) {
        stack.getOrCreateTag().putUUID(CONTROLLED_VULTURE_TAG, vulture.getUUID());
    }

    public static void clearControlledVulture(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(CONTROLLED_VULTURE_TAG);
        }
    }

    public static boolean matchesBinding(ItemStack guard, VultureEntity vulture) {
        String binding = getBindingName(guard);
        if (ANY_BINDING.equalsIgnoreCase(binding)) {
            return true;
        }
        return vulture.hasCustomName()
                && binding.equalsIgnoreCase(vulture.getCustomName().getString().trim());
    }

    public static String sanitizeName(String requestedName, String blankValue) {
        String name = requestedName == null ? "" : requestedName.trim();
        if (name.isEmpty()) {
            return blankValue;
        }
        return name.substring(0, Math.min(name.length(), MAX_NAME_LENGTH));
    }
}
