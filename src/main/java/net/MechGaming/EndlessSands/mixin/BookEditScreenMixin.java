package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.util.ExpandedInventoryHelper;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin {
    @ModifyConstant(method = "saveChanges", constant = @Constant(intValue = 40))
    private int endlessSands$moveOffhandBookSlot(int original) {
        return ExpandedInventoryHelper.EXPANDED_OFFHAND_SLOT;
    }
}
