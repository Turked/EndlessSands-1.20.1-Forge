package net.MechGaming.EndlessSands.fluid;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.MechGaming.EndlessSands.item.ModItems;
import net.MechGaming.EndlessSands.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = EndlessSands.MOD_ID)
public final class FluidConversion {
    private static final int BLOCKS_PER_TICK = 256;

    private FluidConversion() { }

    /** Called before an item tick, so a nether star can activate lava before it burns. */
    public static void tryConvert(ItemEntity item) {
        if (!(item.level() instanceof ServerLevel level) || item.isRemoved()) return;
        ItemStack stack = item.getItem();
        boolean lava = stack.is(Items.NETHER_STAR);
        if (!lava && !stack.is(ModTags.Items.ELDER_EYES)) return;
        Block original = lava ? Blocks.LAVA : Blocks.WATER;
        for (BlockPos pos : BlockPos.betweenClosedStream(item.getBoundingBox()).toList()) {
            if (!level.hasChunkAt(pos)) continue;
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            if (!state.is(original) || !fluid.isSource()
                    || item.getBoundingBox().minY >= pos.getY() + fluid.getHeight(level, pos)) continue;
            BlockPos source = pos.immutable();
            if (!replace(level, source, state, lava)) continue;
            stack.shrink(1);
            if (stack.isEmpty()) item.discard();
            else item.setItem(stack);
            ConversionData.get(level).enqueue(new Frontier(source, lava, true));
            return;
        }
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
            advanceConversions(level);
        }
    }

    public static void advanceConversions(ServerLevel level) {
        ConversionData data = ConversionData.get(level);
        ArrayDeque<Frontier> queue = data.pending;
        if (queue.isEmpty()) return;
        // A shared level budget keeps large waterfalls/multiple thrown items from stalling a tick.
        for (int count = 0; count < BLOCKS_PER_TICK && !queue.isEmpty(); count++) {
            Frontier frontier = queue.removeFirst();
            if (!level.hasChunkAt(frontier.pos)) {
                queue.addLast(frontier); // Resume when loaded; never discard the edge of a waterfall.
                continue;
            }
            data.queued.remove(frontier);
            data.setDirty();
            Block replacement = frontier.lava ? ModBlocks.STAR_TOUCHED_LAVA.get()
                    : ModBlocks.ANCIENT_OCEAN_WATER.get();
            if (!frontier.converted) {
                BlockState candidate = level.getBlockState(frontier.pos);
                if (!candidate.is(frontier.lava ? Blocks.LAVA : Blocks.WATER)
                        || candidate.getFluidState().isSource()
                        || !replace(level, frontier.pos, candidate, frontier.lava)) continue;
            }
            if (!level.getBlockState(frontier.pos).is(replacement)) continue;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = frontier.pos.relative(direction);
                if (!level.hasChunkAt(neighbor)) {
                    data.enqueue(new Frontier(neighbor, frontier.lava, false));
                    continue;
                }
                BlockState state = level.getBlockState(neighbor);
                if (state.is(frontier.lava ? Blocks.LAVA : Blocks.WATER)
                        && !state.getFluidState().isSource()
                        && replace(level, neighbor, state, frontier.lava)) {
                    data.enqueue(new Frontier(neighbor, frontier.lava, true));
                }
            }
            level.updateNeighborsAt(frontier.pos, replacement);
        }
    }

    private static boolean replace(ServerLevel level, BlockPos pos, BlockState state, boolean lava) {
        Block replacement = lava ? ModBlocks.STAR_TOUCHED_LAVA.get() : ModBlocks.ANCIENT_OCEAN_WATER.get();
        // Retain source/flow depth/falling state; do not turn a waterfall into source blocks.
        return level.setBlock(pos, replacement.defaultBlockState()
                .setValue(LiquidBlock.LEVEL, state.getValue(LiquidBlock.LEVEL)), Block.UPDATE_CLIENTS);
    }

    @SubscribeEvent
    public static void bucketFuel(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().is(ModItems.STAR_TOUCHED_LAVA_BUCKET.get())) event.setBurnTime(20_000);
    }

    private record Frontier(BlockPos pos, boolean lava, boolean converted) { }

    /** Persist unfinished work so crossing a chunk boundary or reloading cannot truncate a conversion. */
    private static final class ConversionData extends SavedData {
        private final ArrayDeque<Frontier> pending = new ArrayDeque<>();
        private final Set<Frontier> queued = new HashSet<>();

        private static ConversionData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(ConversionData::load,
                    ConversionData::new, "endlesssands_fluid_conversions");
        }

        private void enqueue(Frontier frontier) {
            if (queued.add(frontier)) {
                pending.addLast(frontier);
                setDirty();
            }
        }

        private static ConversionData load(CompoundTag tag) {
            ConversionData data = new ConversionData();
            for (Tag entry : tag.getList("Pending", Tag.TAG_COMPOUND)) {
                CompoundTag node = (CompoundTag) entry;
                data.enqueue(new Frontier(BlockPos.of(node.getLong("Pos")),
                        node.getBoolean("Lava"), node.getBoolean("Converted")));
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag nodes = new ListTag();
            for (Frontier frontier : pending) {
                CompoundTag node = new CompoundTag();
                node.putLong("Pos", frontier.pos.asLong());
                node.putBoolean("Lava", frontier.lava);
                node.putBoolean("Converted", frontier.converted);
                nodes.add(node);
            }
            tag.put("Pending", nodes);
            return tag;
        }
    }
}
