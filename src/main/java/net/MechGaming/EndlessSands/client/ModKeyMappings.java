package net.MechGaming.EndlessSands.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ModKeyMappings {
    public static final KeyMapping HOTBAR_SLOT_10 = new KeyMapping(
            "key.endlesssands.hotbar_10",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_0,
            "key.categories.inventory"
    );

    private ModKeyMappings() {
    }
}
