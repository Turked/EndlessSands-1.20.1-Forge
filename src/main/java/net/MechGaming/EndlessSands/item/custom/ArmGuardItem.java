package net.MechGaming.EndlessSands.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArmGuardItem extends Item {
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
        return hand == InteractionHand.OFF_HAND
                ? InteractionResultHolder.pass(stack)
                : InteractionResultHolder.fail(stack);
    }
}
