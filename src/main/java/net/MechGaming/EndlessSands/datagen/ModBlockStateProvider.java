package net.MechGaming.EndlessSands.datagen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, EndlessSands.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.CURSED_SAND);
        blockWithItem(ModBlocks.CURSED_SANDSTONE);
        blockWithItem(ModBlocks.SUSPICIOUS_CURSED_SAND);
        blockWithItem(ModBlocks.VILLAGE_POT);
        blockWithItem(ModBlocks.PALM_PLANKS);

        blockWithTopBottomAndSides(ModBlocks.FERTILE_SOIL, "fertile_soil_side", "fertile_soil_top");
        blockWithTopBottomAndSides(ModBlocks.PALM_LOG, "palm_log_side", "palm_log_top");
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockWithTopBottomAndSides(RegistryObject<Block> blockRegistryObject, String sideTexture, String topBottomTexture) {
        simpleBlockWithItem(blockRegistryObject.get(),
                models().cubeBottomTop(
                        blockRegistryObject.getId().getPath(),
                        modLoc("block/" + sideTexture),
                        modLoc("block/" + topBottomTexture),
                        modLoc("block/" + topBottomTexture)
                ));
    }
}