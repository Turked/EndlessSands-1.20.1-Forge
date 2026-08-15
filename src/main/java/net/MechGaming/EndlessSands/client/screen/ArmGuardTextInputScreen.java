package net.MechGaming.EndlessSands.client.screen;

import net.MechGaming.EndlessSands.network.ArmGuardAction;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.ArmGuardCommandC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ArmGuardTextInputScreen extends Screen {
    private final Screen parent;
    private final ArmGuardAction action;
    private final int vultureId;
    private final Component hint;
    private final String initialValue;
    private EditBox input;

    public ArmGuardTextInputScreen(Screen parent, ArmGuardAction action, int vultureId,
                                   Component title, Component hint, String initialValue) {
        super(title);
        this.parent = parent;
        this.action = action;
        this.vultureId = vultureId;
        this.hint = hint;
        this.initialValue = initialValue;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        this.input = new EditBox(this.font, centerX - 100, centerY - 10,
                200, 20, this.title);
        this.input.setMaxLength(32);
        this.input.setValue(this.initialValue);
        this.input.setHint(this.hint);
        this.addRenderableWidget(this.input);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> submit())
                .bounds(centerX - 100, centerY + 20, 96, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(centerX + 4, centerY + 20, 96, 20)
                .build());
        this.setInitialFocus(this.input);
        this.input.setFocused(true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2,
                this.height / 2 - 34, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void submit() {
        String value = this.input.getValue();
        ModMessages.sendToServer(new ArmGuardCommandC2SPacket(
                this.action, this.vultureId, value));
        this.minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
