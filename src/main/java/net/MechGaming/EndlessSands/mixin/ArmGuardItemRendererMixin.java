package net.MechGaming.EndlessSands.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.MechGaming.EndlessSands.entity.client.VultureRenderer;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.MechGaming.EndlessSands.item.custom.ArmGuardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemRenderer.class)
public abstract class ArmGuardItemRendererMixin {
    @Shadow
    public abstract BakedModel getModel(ItemStack stack, @Nullable Level level,
                                        @Nullable LivingEntity entity, int seed);

    @Inject(
            method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("TAIL")
    )
    private void endlessSands$renderPerchedVulture(@Nullable LivingEntity livingEntity, ItemStack stack,
                                                    ItemDisplayContext displayContext, boolean leftHand,
                                                    PoseStack poseStack, MultiBufferSource buffer,
                                                    @Nullable Level level, int packedLight, int packedOverlay,
                                                    int seed, CallbackInfo callbackInfo) {
        if (!(livingEntity instanceof Player player) || !(stack.getItem() instanceof ArmGuardItem)) {
            return;
        }

        HumanoidArm renderedArm = getRenderedArm(displayContext);
        if (renderedArm == null || renderedArm != player.getMainArm().getOpposite()) {
            return;
        }

        VultureEntity vulture = findArmPerchedVulture(player);
        if (vulture == null) {
            return;
        }

        EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(vulture);
        if (!(entityRenderer instanceof VultureRenderer vultureRenderer)) {
            return;
        }

        poseStack.pushPose();
        BakedModel model = this.getModel(stack, level, livingEntity, seed);
        ForgeHooksClient.handleCameraTransforms(poseStack, model, displayContext, leftHand);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        vultureRenderer.renderArmAttachment(vulture, Minecraft.getInstance().getFrameTime(),
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Nullable
    private static HumanoidArm getRenderedArm(ItemDisplayContext displayContext) {
        return switch (displayContext) {
            case FIRST_PERSON_LEFT_HAND, THIRD_PERSON_LEFT_HAND -> HumanoidArm.LEFT;
            case FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_RIGHT_HAND -> HumanoidArm.RIGHT;
            default -> null;
        };
    }

    @Nullable
    private static VultureEntity findArmPerchedVulture(Player player) {
        UUID playerId = player.getUUID();
        return player.level().getEntitiesOfClass(VultureEntity.class, player.getBoundingBox().inflate(8.0D),
                        vulture -> vulture.isAlive()
                                && vulture.isArmPerched()
                                && playerId.equals(vulture.getArmPerchPlayerUUID()))
                .stream()
                .findFirst()
                .orElse(null);
    }
}
