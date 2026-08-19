package net.MechGaming.EndlessSands.block.entity;

import net.MechGaming.EndlessSands.util.LinedStairData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

public class LinedStairBlockEntity extends BlockEntity {
    public static final ModelProperty<ResourceLocation> ORIGINAL_STAIR_MODEL = new ModelProperty<>();

    private ResourceLocation originalStairId = LinedStairData.FALLBACK_STAIR_ID;

    public LinedStairBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LINED_STAIRS.get(), pos, state);
    }

    public ResourceLocation getOriginalStairId() {
        return LinedStairData.validateId(originalStairId);
    }

    public void setOriginalStairId(ResourceLocation requestedId) {
        ResourceLocation validId = LinedStairData.validateId(requestedId);
        if (validId.equals(originalStairId)) {
            return;
        }

        originalStairId = validId;
        setChanged();
        requestModelDataUpdate();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        originalStairId = LinedStairData.validateId(
                ResourceLocation.tryParse(tag.getString(LinedStairData.ORIGINAL_STAIR_TAG))
        );
        requestModelDataUpdate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(LinedStairData.ORIGINAL_STAIR_TAG, getOriginalStairId().toString());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(ORIGINAL_STAIR_MODEL, getOriginalStairId())
                .build();
    }
}
