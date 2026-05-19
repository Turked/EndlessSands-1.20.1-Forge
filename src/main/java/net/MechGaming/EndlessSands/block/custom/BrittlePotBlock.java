package net.MechGaming.EndlessSands.block.custom;

import net.MechGaming.EndlessSands.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

//Cntrl H and cick on the "Block" to see vanilla
public class BrittlePotBlock extends Block {
    public BrittlePotBlock(Properties pProperties) {
        super(pProperties);
    }

    // On Right click destroy the pot
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        pLevel.destroyBlock(pPos, false);
        pLevel.playSound(pPlayer, pPos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 1f, 1f);

        return InteractionResult.SUCCESS;
    }

    //If Stepped on also destroy the pot
    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (!pEntity.isSteppingCarefully()) {
            pLevel.destroyBlock(pPos, false);
            pLevel.playSound((Player)null, pPos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + pLevel.random.nextFloat() * 0.2F);
        }
    }

    // loot list
    private static final List<Supplier<? extends Item>> LOOT_ITEMS = List.of(
            () -> null, //Chance of getting nothing
            ModItems.OLDWORLD_SCROLL
    );

    // When Destroyed drop a scroll of distant past
    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {

        if (!pLevel.isClientSide && !pState.is(pNewState.getBlock())) {
            Item chosenItem = LOOT_ITEMS.get(pLevel.random.nextInt(LOOT_ITEMS.size())).get();
            if (chosenItem != null) {
                ItemStack rewardStack = new ItemStack(chosenItem);
                popResource(pLevel, pPos, rewardStack);
            }
        }
    }
}
