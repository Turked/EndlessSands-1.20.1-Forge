package net.MechGaming.EndlessSands.item.custom;

import net.MechGaming.EndlessSands.block.entity.ZenioniteBatteryBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenioniteChargerBlockEntity;
import net.MechGaming.EndlessSands.block.entity.ZenionitePortalFrameBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AdminZenioniteItem extends Item {
    public AdminZenioniteItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        boolean supported = blockEntity instanceof ZenioniteChargerBlockEntity
                || blockEntity instanceof ZenioniteBatteryBlockEntity
                || blockEntity instanceof ZenionitePortalFrameBlockEntity;
        if (!supported) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide) {
            if (blockEntity instanceof ZenioniteChargerBlockEntity charger) {
                charger.fillEnergyToCapacity();
            } else if (blockEntity instanceof ZenioniteBatteryBlockEntity battery) {
                battery.fillEnergyToCapacity();
            } else if (blockEntity instanceof ZenionitePortalFrameBlockEntity frame) {
                frame.fillEnergyToCapacity();
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
