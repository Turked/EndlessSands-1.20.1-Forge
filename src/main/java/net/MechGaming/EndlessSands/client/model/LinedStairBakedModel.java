package net.MechGaming.EndlessSands.client.model;

import net.MechGaming.EndlessSands.block.entity.LinedStairBlockEntity;
import net.MechGaming.EndlessSands.util.LinedStairData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LinedStairBakedModel implements BakedModel {
    private static final Map<BlockState, BakedModel> SOURCE_MODELS = new ConcurrentHashMap<>();

    private final BakedModel fallback;
    private final ItemOverrides itemOverrides;

    public LinedStairBakedModel(BakedModel fallback) {
        this.fallback = fallback;
        this.itemOverrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(
                    BakedModel originalModel,
                    ItemStack stack,
                    @Nullable ClientLevel level,
                    @Nullable LivingEntity entity,
                    int seed
            ) {
                if (!LinedStairData.hasSourceId(stack)) {
                    return fallback.getOverrides().resolve(originalModel, stack, level, entity, seed);
                }

                ItemStack sourceStack = new ItemStack(
                        LinedStairData.getSourceBlock(LinedStairData.getSourceId(stack))
                );
                return Minecraft.getInstance().getItemRenderer().getModel(sourceStack, level, entity, seed);
            }
        };
    }

    public static void clearCache() {
        SOURCE_MODELS.clear();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @NotNull RandomSource random,
            @NotNull ModelData data,
            @Nullable RenderType renderType
    ) {
        ResolvedModel resolved = resolve(state, data);
        return resolved.model().getQuads(resolved.state(), side, random, ModelData.EMPTY, renderType);
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random
    ) {
        return fallback.getQuads(state, side, random);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(
            @NotNull BlockState state,
            @NotNull RandomSource random,
            @NotNull ModelData data
    ) {
        ResolvedModel resolved = resolve(state, data);
        return resolved.model().getRenderTypes(resolved.state(), random, ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return resolve(null, data).model().getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return fallback.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return fallback.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return fallback.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return fallback.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return fallback.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return itemOverrides;
    }

    private ResolvedModel resolve(@Nullable BlockState linedState, ModelData data) {
        ResourceLocation sourceId = data.get(LinedStairBlockEntity.ORIGINAL_STAIR_MODEL);
        if (sourceId == null) {
            return new ResolvedModel(linedState, fallback);
        }

        BlockState sourceState = linedState == null
                ? LinedStairData.getSourceBlock(sourceId).defaultBlockState()
                : LinedStairData.toSourceState(linedState, sourceId);
        BakedModel sourceModel = SOURCE_MODELS.computeIfAbsent(
                sourceState,
                state -> Minecraft.getInstance().getBlockRenderer().getBlockModel(state)
        );
        return new ResolvedModel(sourceState, sourceModel);
    }

    private record ResolvedModel(@Nullable BlockState state, BakedModel model) {
    }
}
