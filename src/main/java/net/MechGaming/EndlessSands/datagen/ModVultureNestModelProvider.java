package net.MechGaming.EndlessSands.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.MechGaming.EndlessSands.EndlessSands;
import net.minecraft.core.Direction;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Builds ordinary finished models from the authored empty nest and three reusable egg cuboids. */
public final class ModVultureNestModelProvider implements DataProvider {
    private static final Map<String, int[]> COMBINATIONS = Map.of(
            "vulture_nest_full", new int[]{0, 1, 2},
            "vulture_nest_1_egg_variant_1", new int[]{0},
            "vulture_nest_1_egg_variant_2", new int[]{1},
            "vulture_nest_1_egg_variant_3", new int[]{2},
            "vulture_nest_2_eggs_variant_1", new int[]{0, 1},
            "vulture_nest_2_eggs_variant_2", new int[]{0, 2},
            "vulture_nest_2_eggs_variant_3", new int[]{1, 2});
    private static final Direction[] EGG_FACES = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

    private final PackOutput.PathProvider paths;
    private final ExistingFileHelper existingFiles;

    public ModVultureNestModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        this.paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.existingFiles = existingFiles;
        // Declare outputs before other providers validate their blockstate/item-model references.
        COMBINATIONS.keySet().forEach(name -> existingFiles.trackGenerated(
                location("block/" + name), PackType.CLIENT_RESOURCES, ".json", "models"));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JsonObject emptyNest;
        try (Reader reader = existingFiles.getResource(location("block/vulture_nest_empty"),
                PackType.CLIENT_RESOURCES, ".json", "models").openAsReader()) {
            emptyNest = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(
                    new IOException("Unable to read the authored empty vulture nest model", exception));
        }
        if (!emptyNest.has("elements") || !emptyNest.get("elements").isJsonArray()) {
            throw new IllegalStateException("The authored empty vulture nest must contain an elements array");
        }

        JsonArray eggs = eggGeometry();
        var writes = new ArrayList<CompletableFuture<?>>();
        COMBINATIONS.forEach((name, eggIndices) -> {
            JsonObject model = emptyNest.deepCopy();
            JsonArray elements = model.getAsJsonArray("elements");
            JsonArray eggChildren = new JsonArray();
            for (int eggIndex : eggIndices) {
                eggChildren.add(elements.size());
                elements.add(eggs.get(eggIndex).deepCopy());
            }
            // Preserve the authored display settings, UVs, textures and Blockbench group structure.
            if (model.has("groups")) {
                for (JsonElement groupElement : model.getAsJsonArray("groups")) {
                    JsonObject group = groupElement.getAsJsonObject();
                    if (group.has("name") && "Eggs".equals(group.get("name").getAsString())) {
                        group.add("children", eggChildren);
                    }
                }
            }
            writes.add(DataProvider.saveStable(output, model, paths.json(location(name))));
        });
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private JsonArray eggGeometry() {
        // These are the original three 2x2x2 Blockbench-unit eggs, including their exact UV mappings.
        BlockModelBuilder model = new BlockModelBuilder(location("block/vulture_nest_full"), existingFiles);
        egg(model, 3.5F, 1.25F, 8, Direction.Axis.Z, 22.5F, new float[][]{
                {0.5F, 9.5F, 1.5F, 10.5F}, {1.5F, 9.5F, 2.5F, 10.5F},
                {2.5F, 9.5F, 3.5F, 10.5F}, {3.5F, 9.5F, 4.5F, 10.5F},
                {5.5F, 10.5F, 4.5F, 9.5F}, {6.5F, 9.5F, 5.5F, 10.5F}});
        egg(model, 7, 2, 5, Direction.Axis.Y, 0, new float[][]{
                {6.5F, 9.5F, 7.5F, 10.5F}, {9.5F, 9, 10.5F, 10},
                {10, 7, 11, 8}, {9.5F, 10, 10.5F, 11},
                {1.5F, 11.5F, 0.5F, 10.5F}, {2.5F, 10.5F, 1.5F, 11.5F}});
        egg(model, 10, 2, 9, Direction.Axis.X, 22.5F, new float[][]{
                {2.5F, 10.5F, 3.5F, 11.5F}, {3.5F, 10.5F, 4.5F, 11.5F},
                {4.5F, 10.5F, 5.5F, 11.5F}, {5.5F, 10.5F, 6.5F, 11.5F},
                {7.5F, 11.5F, 6.5F, 10.5F}, {11.5F, 9, 10.5F, 10}});
        JsonArray eggs = model.toJson().getAsJsonArray("elements");
        for (int i = 0; i < eggs.size(); i++) {
            eggs.get(i).getAsJsonObject().addProperty("name", "Egg" + (i + 1));
        }
        return eggs;
    }

    private static void egg(BlockModelBuilder model, float x, float y, float z,
                            Direction.Axis axis, float angle, float[][] uvs) {
        var egg = model.element().from(x, y, z).to(x + 2, y + 2, z + 2);
        egg.rotation().origin(x, y, z).axis(axis).angle(angle).end();
        for (int i = 0; i < EGG_FACES.length; i++) {
            float[] uv = uvs[i];
            egg.face(EGG_FACES[i]).uvs(uv[0], uv[1], uv[2], uv[3]).texture("#0");
        }
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, path);
    }

    @Override
    public String getName() {
        return "Vulture nest model combinations: " + EndlessSands.MOD_ID;
    }
}
