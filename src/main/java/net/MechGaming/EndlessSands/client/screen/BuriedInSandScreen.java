package net.MechGaming.EndlessSands.client.screen;

import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.client.BuriedInSandClientState;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.EscapeBuriedInSandC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class BuriedInSandScreen extends Screen {
    private static final ResourceLocation CURSED_SAND =
            new ResourceLocation(
                    EndlessSands.MOD_ID,
                    "textures/block/cursed_sand.png"
            );

    private Checkbox dontAskAgain;

    public BuriedInSandScreen() {
        super(Component.translatable(
                "screen.endlesssands.buried.title"
        ));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int buttonY = height / 2 + 25;

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "screen.endlesssands.buried.escape"
                        ),
                        button -> escape()
                ).bounds(centerX - 102, buttonY, 100, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "screen.endlesssands.buried.leave"
                        ),
                        button -> leaveGame()
                ).bounds(centerX + 2, buttonY, 100, 20).build()
        );

        dontAskAgain = addRenderableWidget(
                new Checkbox(
                        centerX - 75,
                        buttonY + 28,
                        150,
                        20,
                        Component.translatable(
                                "screen.endlesssands.buried.dont_ask"
                        ),
                        BuriedInSandClientState.getDontAskAgain()
                )
        );
    }

    private void escape() {
        BuriedInSandClientState.setDontAskAgain(
                dontAskAgain.selected()
        );

        ModMessages.sendToServer(
                new EscapeBuriedInSandC2SPacket()
        );

        // The server's End packet closes the screen.
    }

    private void leaveGame() {
        BuriedInSandClientState.setDontAskAgain(
                dontAskAgain.selected()
        );

        if (minecraft.level != null) {
            minecraft.level.disconnect();
        }

        minecraft.clearLevel(
                new GenericDirtMessageScreen(
                        Component.translatable(
                                "menu.savingLevel"
                        )
                )
        );

        minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        // Tile the existing cursed-sand block texture.
        for (int x = 0; x < width; x += 16) {
            for (int y = 0; y < height; y += 16) {
                graphics.blit(
                        CURSED_SAND,
                        x,
                        y,
                        0,
                        0,
                        16,
                        16,
                        16,
                        16
                );
            }
        }

        // Darken it like the death screen.
        graphics.fillGradient(
                0,
                0,
                width,
                height,
                0x88000000,
                0xAA000000
        );

        int centerX = width / 2;
        int textY = height / 2 - 55;

        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "screen.endlesssands.buried.line1"
                ),
                centerX,
                textY,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "screen.endlesssands.buried.line2"
                ),
                centerX,
                textY + 18,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "screen.endlesssands.buried.line3"
                ),
                centerX,
                textY + 36,
                0xFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}