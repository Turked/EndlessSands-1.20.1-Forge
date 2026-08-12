package net.MechGaming.EndlessSands.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public final class SunGearHelper {
    public static final String TWIG_VISOR_TAG = "EndlessSandsTwigVisor";

    private SunGearHelper() {
    }

    public static boolean isSunGear(ItemStack stack) {
        return stack.is(ModTags.Items.IS_SUN_GEAR) || hasTwigVisor(stack);
    }

    public static boolean givesShade(ItemStack stack) {
        return stack.is(ModTags.Items.DOES_GIVE_SHADE)
                || stack.is(ModTags.Items.IS_SHADE)
                || hasTwigVisor(stack);
    }

    public static boolean canAttachTwigVisor(ItemStack stack) {
        if (stack.isEmpty() || hasTwigVisor(stack)) {
            return false;
        }

        return stack.getItem() instanceof ArmorItem armorItem
                && armorItem.getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    public static boolean hasTwigVisor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TWIG_VISOR_TAG);
    }

    public static void attachTwigVisor(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(TWIG_VISOR_TAG, true);
    }
}
