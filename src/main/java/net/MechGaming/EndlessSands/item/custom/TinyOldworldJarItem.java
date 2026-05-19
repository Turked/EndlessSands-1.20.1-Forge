package net.MechGaming.EndlessSands.item.custom;

import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;


import java.util.function.Supplier;
import java.util.List;

public class TinyOldworldJarItem extends Item {
    public TinyOldworldJarItem(Properties pProperties) {
        super(pProperties);
    }

    // loot list
    private static final List<Supplier<? extends Item>> LOOT_ITEMS = List.of(
            () -> Items.DIRT,
            ModItems.OLDWORLD_ROSE,
            () -> Items.EMERALD,
            () -> Items.CLAY_BALL
    );

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        if(!pLevel.isClientSide){
            // pick a random item from loot list
            Item chosenItem = LOOT_ITEMS.get(pLevel.random.nextInt(LOOT_ITEMS.size())).get();

            // play break sound
            pLevel.playSound(
                    null,
                    pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                    SoundEvents.DECORATED_POT_BREAK,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            // show particles
            ((ServerLevel) pLevel).sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, itemstack),
                    pPlayer.getX() + 0,
                    pPlayer.getY() + 1.0,
                    pPlayer.getZ() + 0,
                    20,
                    0.3,
                    0.4,
                    0.3,
                    0.1
            );

            // remove one jar
            itemstack.shrink(1);

            // create itemstack reward
            ItemStack rewardStack = new ItemStack(chosenItem);

            // try to add it to inventory
            boolean itemAdded = pPlayer.addItem(rewardStack);
            // if it fails, drop it near the player
            if (itemAdded){
                pPlayer.drop(rewardStack, false);
            }
        }


        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }
}
