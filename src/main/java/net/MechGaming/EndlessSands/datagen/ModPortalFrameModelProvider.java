package net.MechGaming.EndlessSands.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.block.custom.ZenionitePortalFrameBlock.Port;
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
import java.util.concurrent.CompletableFuture;

/** Reuses the authored atlas directly; no generated PNGs or runtime texture editing. */
public final class ModPortalFrameModelProvider implements DataProvider {
    private final PackOutput.PathProvider paths;
    private final ExistingFileHelper existingFiles;

    public ModPortalFrameModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        this.paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.existingFiles = existingFiles;
        for (boolean powered : new boolean[]{false, true}) {
            declare(bodyName(powered));
            for (Port port : Port.values()) declare(portName(port, powered));
        }
    }

    public static String bodyName(boolean powered) {
        return "zenionite_portal_frame_connected" + (powered ? "_powered" : "");
    }

    public static String portName(Port port, boolean powered) {
        return "zenionite_portal_frame_port_" + port.getSerializedName() + (powered ? "_powered" : "");
    }

    private void declare(String name) {
        existingFiles.trackGenerated(location("block/" + name), PackType.CLIENT_RESOURCES, ".json", "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JsonObject authored;
        try (Reader reader = existingFiles.getResource(location("block/zenionite_portal_frame"),
                PackType.CLIENT_RESOURCES, ".json", "models").openAsReader()) {
            authored = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
        var writes = new ArrayList<CompletableFuture<?>>();
        for (boolean powered : new boolean[]{false, true}) {
            String texture = "endlesssands:block/zenionite_portal_frame" + (powered ? "_powered" : "");
            JsonObject body = authored.deepCopy();
            body.getAsJsonObject("textures").addProperty("3", texture);
            body.getAsJsonObject("textures").addProperty("particle", texture);
            JsonArray elements = body.getAsJsonArray("elements");
            JsonObject base = elements.get(0).getAsJsonObject();
            float height = base.getAsJsonArray("to").get(1).getAsFloat();
            JsonObject up = base.getAsJsonObject("faces").remove("up").getAsJsonObject();
            JsonArray uv = up.getAsJsonArray("uv");
            // Retain the center and four corners of the original top without overlapping faces.
            int[] edges = {0, 4, 12, 16};
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if ((x == 1) != (z == 1)) continue;
                    JsonObject tile = topTile(edges[x], edges[z], edges[x + 1], edges[z + 1], height);
                    JsonObject face = up.deepCopy();
                    face.add("uv", numbers(
                            interpolate(uv, 0, edges[x]), interpolate(uv, 1, edges[z]),
                            interpolate(uv, 0, edges[x + 1]), interpolate(uv, 1, edges[z + 1])));
                    JsonObject faces = new JsonObject();
                    faces.add("up", face);
                    tile.add("faces", faces);
                    elements.add(tile);
                }
            }
            writes.add(DataProvider.saveStable(output, body, paths.json(location(bodyName(powered)))));
            for (Port port : Port.values()) {
                BlockModelBuilder patch = new BlockModelBuilder(location("block/" + portName(port, powered)),
                        existingFiles).texture("3", texture);
                float[] patchUv = switch (port) {
                    case INPUT -> new float[]{2, 0, 6, 2};   // Atlas pixels x4..11, y0..3.
                    case OUTPUT -> new float[]{2, 6, 6, 8}; // Atlas pixels x4..11, y12..15.
                    case NONE -> new float[]{6, 2, 8, 6};   // Plain east strip, without a C.
                };
                patch.element().from(4, height, 0).to(12, height, 4)
                        .face(Direction.UP).uvs(patchUv[0], patchUv[1], patchUv[2], patchUv[3]).texture("#3");
                JsonObject model = patch.toJson();
                model.getAsJsonArray("elements").get(0).getAsJsonObject()
                        .getAsJsonObject("faces").getAsJsonObject("up")
                        .addProperty("rotation", port == Port.INPUT ? 0 : port == Port.OUTPUT ? 180 : 270);
                writes.add(DataProvider.saveStable(output, model, paths.json(location(portName(port, powered)))));
            }
        }
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private static float interpolate(JsonArray uv, int axis, int coordinate) {
        float start = uv.get(axis).getAsFloat();
        return start + (uv.get(axis + 2).getAsFloat() - start) * coordinate / 16.0F;
    }

    private static JsonObject topTile(int x1, int z1, int x2, int z2, float height) {
        JsonObject tile = new JsonObject();
        tile.add("from", numbers(x1, height, z1));
        tile.add("to", numbers(x2, height, z2));
        return tile;
    }

    private static JsonArray numbers(float... values) {
        JsonArray array = new JsonArray();
        for (float value : values) array.add(value);
        return array;
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(EndlessSands.MOD_ID, path);
    }

    @Override
    public String getName() { return "Connected portal frame models: " + EndlessSands.MOD_ID; }
}
