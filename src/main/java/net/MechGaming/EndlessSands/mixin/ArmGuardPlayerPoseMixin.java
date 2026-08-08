package net.MechGaming.EndlessSands.mixin;

import net.MechGaming.EndlessSands.item.custom.ArmGuardItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class ArmGuardPlayerPoseMixin {
    @Shadow
    public ModelPart leftArm;

    @Shadow
    public ModelPart rightArm;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void endlessSands$holdArmGuardStraight(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                                    float ageInTicks, float netHeadYaw, float headPitch,
                                                    CallbackInfo callbackInfo) {
        if (!(entity.getOffhandItem().getItem() instanceof ArmGuardItem)) {
            return;
        }

        ModelPart offhandArm = entity.getMainArm() == HumanoidArm.RIGHT ? this.leftArm : this.rightArm;
        offhandArm.xRot = -(float) Math.PI / 2.0F
                + Mth.clamp(headPitch, -90.0F, 90.0F) * Mth.DEG_TO_RAD;
        offhandArm.yRot = 0.0F;
        offhandArm.zRot = 0.0F;
    }
}
