package net.MechGaming.EndlessSands.event;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public final class ElderEyeEvents {
    private ElderEyeEvents() {
    }

    @SubscribeEvent
    public static void addElderGuardianDrop(LivingDropsEvent event) {
        if (event.getEntity().getType() != EntityType.ELDER_GUARDIAN || !ModItems.ELDER_EYE.isPresent()) {
            return;
        }

        ItemEntity elderEye = new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(ModItems.ELDER_EYE.get())
        );
        elderEye.setDefaultPickUpDelay();
        event.getDrops().add(elderEye);
    }
}
