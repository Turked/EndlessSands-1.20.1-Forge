package net.MechGaming.EndlessSands.client.screen;

import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.ConfirmDragonEggSacrificeC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public class DragonEggSacrificeScreen extends Screen {
    private final BlockPos holderPos;
    private final InteractionHand hand;

    private DragonEggSacrificeScreen(BlockPos holderPos, InteractionHand hand) {
        super(Component.translatable("screen.endlesssands.sacrifice.title"));
        this.holderPos = holderPos;
        this.hand = hand;
    }

    public static void open(BlockPos holderPos, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new DragonEggSacrificeScreen(holderPos, hand));
    }

    @Override
    protected void init() {
        int y = this.height / 2 + 20;
        addRenderableWidget(Button.builder(
                Component.translatable("screen.endlesssands.sacrifice.place")
                        .withStyle(ChatFormatting.ITALIC), button -> {
                    ModMessages.sendToServer(new ConfirmDragonEggSacrificeC2SPacket(holderPos, hand));
                    onClose();
                }).bounds(this.width / 2 - 155, y, 150, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("screen.endlesssands.sacrifice.walk_away")
                        .withStyle(ChatFormatting.ITALIC), button -> onClose())
                .bounds(this.width / 2 + 5, y, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2,
                this.height / 2 - 35, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
