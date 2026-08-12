package net.MechGaming.EndlessSands.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.MechGaming.EndlessSands.EndlessSands;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.item.custom.ArmGuardItem;
import net.MechGaming.EndlessSands.network.ArmGuardAction;
import net.MechGaming.EndlessSands.network.ModMessages;
import net.MechGaming.EndlessSands.network.packet.ArmGuardCommandC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class ArmGuardRadialScreen extends Screen {
    private static final int TEXTURE_SIZE = 256;
    private static final int ATLAS_WIDTH = TEXTURE_SIZE * 4;
    private static final int ATLAS_HEIGHT = TEXTURE_SIZE * 2;
    private static final double INNER_RADIUS = 16.0D;
    private static final double OUTER_RADIUS = 128.0D;

    private static final ResourceLocation BACKGROUND = texture("arm_guard_radial_background");
    private static final ResourceLocation HIGHLIGHTS = texture("arm_guard_radial_highlights");
    private static final ResourceLocation TEXT = texture("arm_guard_radial_text");

    private static final ArmGuardAction[] SECTOR_ACTIONS = {
            ArmGuardAction.FOLLOW,
            ArmGuardAction.SEARCH,
            ArmGuardAction.RENAME_GUARD,
            ArmGuardAction.FLY_HOME,
            ArmGuardAction.DIE,
            ArmGuardAction.RENAME_VULTURE,
            ArmGuardAction.RETURN,
            ArmGuardAction.EAT
    };

    private final int vultureId;
    private final boolean hasHome;
    private int openTicks;

    public ArmGuardRadialScreen(int vultureId, boolean hasHome) {
        super(Component.translatable("screen.endlesssands.arm_guard_radial"));
        this.vultureId = vultureId;
        this.hasHome = hasHome;
    }

    @Override
    public void tick() {
        this.openTicks++;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float scale = wheelScale();
        float originX = (this.width - TEXTURE_SIZE * scale) * 0.5F;
        float originY = (this.height - TEXTURE_SIZE * scale) * 0.5F;
        int selection = selectionAt(mouseX, mouseY, originX, originY, scale);

        RenderSystem.enableBlend();
        graphics.pose().pushPose();
        graphics.pose().translate(originX, originY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.24F);
        graphics.blit(BACKGROUND, 0, 0, 0.0F, 0.0F,
                TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);

        if (selection >= 0 && isSectorAvailable(selection)) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int atlasX = selection % 4 * TEXTURE_SIZE;
            int atlasY = selection / 4 * TEXTURE_SIZE;
            graphics.blit(HIGHLIGHTS, 0, 0, atlasX, atlasY,
                    TEXTURE_SIZE, TEXTURE_SIZE, ATLAS_WIDTH, ATLAS_HEIGHT);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXT,
                0, 0, 0.0F, 0.0F,
                TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            choose(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1 && this.openTicks > 1) {
            choose(mouseX, mouseY);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void choose(double mouseX, double mouseY) {
        float scale = wheelScale();
        float originX = (this.width - TEXTURE_SIZE * scale) * 0.5F;
        float originY = (this.height - TEXTURE_SIZE * scale) * 0.5F;
        int selection = selectionAt(mouseX, mouseY, originX, originY, scale);
        if (selection < 0) {
            onClose();
            return;
        }
        if (!isSectorAvailable(selection)) {
            return;
        }

        ArmGuardAction action = SECTOR_ACTIONS[selection];
        if (action == ArmGuardAction.RENAME_GUARD) {
            ItemStack guard = this.minecraft.player == null
                    ? ItemStack.EMPTY
                    : this.minecraft.player.getOffhandItem();
            openInput(action,
                    Component.translatable("screen.endlesssands.rename_guard"),
                    Component.translatable("screen.endlesssands.guard_name_hint"),
                    ArmGuardItem.getBindingName(guard));
            return;
        }
        if (action == ArmGuardAction.RENAME_VULTURE) {
            String currentName = "";
            if (this.minecraft.level != null) {
                Entity entity = this.minecraft.level.getEntity(this.vultureId);
                if (entity instanceof VultureEntity vulture && vulture.hasCustomName()) {
                    currentName = vulture.getCustomName().getString();
                }
            }
            openInput(action,
                    Component.translatable("screen.endlesssands.rename_vulture"),
                    Component.translatable("screen.endlesssands.vulture_name_hint"),
                    currentName);
            return;
        }
        if (action == ArmGuardAction.SEARCH) {
            ModMessages.sendToServer(new ArmGuardCommandC2SPacket(action, this.vultureId, ""));
            onClose();
            return;
        }

        ModMessages.sendToServer(new ArmGuardCommandC2SPacket(action, this.vultureId, ""));
        onClose();
    }

    private void openInput(ArmGuardAction action, Component title, Component hint, String initialValue) {
        this.minecraft.setScreen(new ArmGuardTextInputScreen(
                this, action, this.vultureId, title, hint, initialValue));
    }

    private int selectionAt(double mouseX, double mouseY, float originX, float originY, float scale) {
        double x = (mouseX - originX) / scale - TEXTURE_SIZE * 0.5D;
        double y = (mouseY - originY) / scale - TEXTURE_SIZE * 0.5D;
        double radius = Math.sqrt(x * x + y * y);
        if (radius < INNER_RADIUS || radius > OUTER_RADIUS) {
            return -1;
        }
        double clockwiseFromTop = Math.toDegrees(Math.atan2(y, x)) + 450.0D;
        return Math.floorMod((int) Math.floor(clockwiseFromTop / 45.0D), 8);
    }

    private boolean isSectorAvailable(int sector) {
        ArmGuardAction action = SECTOR_ACTIONS[sector];
        if (action == ArmGuardAction.RENAME_GUARD) {
            return true;
        }
        return hasVulture() && (action != ArmGuardAction.FLY_HOME || this.hasHome);
    }

    private boolean hasVulture() {
        return this.vultureId >= 0;
    }

    private float wheelScale() {
        float fitScale = Math.min(1.0F, Math.min(
                (this.width - 20.0F) / TEXTURE_SIZE,
                (this.height - 20.0F) / TEXTURE_SIZE));
        return fitScale * 0.8F;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(EndlessSands.MOD_ID, "textures/gui/" + name + ".png");
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
