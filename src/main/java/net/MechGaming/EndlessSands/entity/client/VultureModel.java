package net.MechGaming.EndlessSands.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.MechGaming.EndlessSands.entity.custom.VultureAnimation;
import net.MechGaming.EndlessSands.entity.custom.VultureEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class VultureModel<T extends VultureEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart all;
    private final ModelPart torsoAndLegs;
    private final ModelPart torso;
    private final ModelPart legs;
    private final ModelPart leftLeg;
    private final ModelPart leftLowerLeg;
    private final ModelPart upperLeftLeg;
    private final ModelPart rightLeg;
    private final ModelPart lowerRightLeg;
    private final ModelPart upperRightLeg;
    private final ModelPart headAndNeck;
    private final ModelPart wings;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart mouthItem;
    private final ModelPart leftTalonItem;
    private final ModelPart rightTalonItem;

    public VultureModel(ModelPart root) {
        this.root = root;
        this.all = root.getChild("All");
        this.torsoAndLegs = this.all.getChild("Torso And Legs");
        this.torso = this.torsoAndLegs.getChild("Torso");
        this.legs = this.torsoAndLegs.getChild("Legs");
        this.leftLeg = this.legs.getChild("Left Leg");
        this.leftLowerLeg = this.leftLeg.getChild("Left Lower Leg");
        this.upperLeftLeg = this.leftLeg.getChild("Upper Left Leg");
        this.rightLeg = this.legs.getChild("Right Leg");
        this.lowerRightLeg = this.rightLeg.getChild("Lower Right Leg");
        this.upperRightLeg = this.rightLeg.getChild("Upper Right Leg");
        this.headAndNeck = this.all.getChild("Head & Neck");
        this.wings = this.all.getChild("Wings");
        this.rightWing = this.wings.getChild("Right Wing");
        this.leftWing = this.wings.getChild("Left Wing");
        this.mouthItem = this.headAndNeck.getChild("mouth_item");
        this.leftTalonItem = this.leftLowerLeg.getChild("left_talon_item");
        this.rightTalonItem = this.lowerRightLeg.getChild("right_talon_item");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition all = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition torsoAndLegs = all.addOrReplaceChild("Torso And Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torso = torsoAndLegs.addOrReplaceChild("Torso", CubeListBuilder.create(), PartPose.offset(0.5F, -11.3331F, 1.757F));
        torso.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -4.0F, 7.0F, 8.0F, 5.0F, new CubeDeformation(0.002F)), PartPose.offsetAndRotation(-0.5F, 3.1215F, 2.9166F, 0.3927F, 0.0F, 0.0F));

        PartDefinition legs = torsoAndLegs.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition leftLeg = legs.addOrReplaceChild("Left Leg", CubeListBuilder.create(), PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition leftLowerLeg = leftLeg.addOrReplaceChild("Left Lower Leg", CubeListBuilder.create().texOffs(40, 6).addBox(4.0F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 8).addBox(4.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 10).addBox(6.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 40).addBox(2.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 40).addBox(3.0F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 24).addBox(5.0F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 26).addBox(4.0F, -3.0F, 2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 28).addBox(4.0F, -4.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 30).addBox(4.0F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        leftLowerLeg.addOrReplaceChild("left_talon_item", CubeListBuilder.create(), PartPose.offset(4.5F, -1.0F, 1.5F));

        leftLeg.addOrReplaceChild("Upper Left Leg", CubeListBuilder.create().texOffs(16, 42).addBox(-0.5F, 0.9F, -0.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(20, 42).addBox(-0.5F, -1.1F, -0.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(32, 42).addBox(-0.5F, -2.1F, -0.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(38, 18).addBox(-1.5F, -0.1F, -0.7F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(36, 42).addBox(-0.5F, -0.1F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(4.5F, -6.9F, 3.45F, 0.2618F, 0.0F, 0.0F));

        PartDefinition rightLeg = legs.addOrReplaceChild("Right Leg", CubeListBuilder.create(), PartPose.offset(-4.75F, -3.45F, 1.725F));
        PartDefinition lowerRightLeg = rightLeg.addOrReplaceChild("Lower Right Leg", CubeListBuilder.create().texOffs(32, 40).addBox(4.0F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(4.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 34).addBox(6.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 40).addBox(2.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 36).addBox(3.0F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 38).addBox(5.0F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 40).addBox(4.0F, -3.0F, 2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 41).addBox(4.0F, -4.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 41).addBox(4.0F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.25F, 3.45F, -1.725F));
        lowerRightLeg.addOrReplaceChild("right_talon_item", CubeListBuilder.create(), PartPose.offset(4.5F, -1.0F, 1.5F));

        rightLeg.addOrReplaceChild("Upper Right Leg", CubeListBuilder.create().texOffs(8, 41).addBox(-0.5F, 0.9F, -0.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(12, 41).addBox(-0.5F, -1.1F, -0.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(24, 41).addBox(-0.5F, -2.1F, -0.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(16, 38).addBox(-1.5F, -0.1F, -0.7F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(28, 41).addBox(-0.5F, -0.1F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(2.25F, -3.45F, 1.725F, 0.2618F, 0.0F, 0.0F));

        PartDefinition headAndNeck = all.addOrReplaceChild("Head & Neck", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -16.0F, -10.0F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(16, 25).addBox(0.0F, -14.0F, -12.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 7).addBox(-2.0F, -15.0F, -14.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-2.0F, -16.0F, -16.0F, 6.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 42).addBox(-1.0F, -15.0F, -17.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
                .texOffs(0, 43).addBox(2.0F, -15.0F, -17.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
                .texOffs(40, 0).addBox(0.0F, -15.0F, -17.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 32).addBox(0.0F, -14.0F, -18.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 4).addBox(0.0F, -13.0F, -19.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 17).addBox(2.0F, -15.0F, -16.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.001F))
                .texOffs(34, 35).addBox(-1.0F, -15.0F, -16.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, -0.25F, 1.25F));
        headAndNeck.addOrReplaceChild("mouth_item", CubeListBuilder.create(), PartPose.offset(1.0F, -12.375F, -19.125F));

        PartDefinition wings = all.addOrReplaceChild("Wings", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition rightWing = wings.addOrReplaceChild("Right Wing", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5F, -6.4167F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(8, 36).addBox(-1.5F, -4.4167F, -3.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 13).addBox(-1.5F, -4.4167F, 2.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5017F, -10.9346F, 1.7828F, 0.3927F, 0.0F, 0.0F));
        rightWing.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(38, 22).addBox(1.0F, 2.0F, -3.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 1.5833F, -2.5F, 1.5708F, 0.0F, 0.0F));
        rightWing.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(30, 30).addBox(1.0F, 1.0F, -4.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -0.4167F, -2.5F, 1.5708F, 0.0F, 0.0F));
        rightWing.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 35).addBox(1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -1.4167F, -2.5F, 1.5708F, 0.0F, 0.0F));

        PartDefinition leftWing = wings.addOrReplaceChild("Left Wing", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5F, -6.4167F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(8, 36).addBox(-1.5F, -4.4167F, -3.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 13).addBox(-1.5F, -4.4167F, 2.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4983F, -10.9346F, 1.7828F, 0.3927F, 0.0F, 0.0F));
        leftWing.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(38, 22).addBox(1.0F, 2.0F, -3.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 1.5833F, -2.5F, 1.5708F, 0.0F, 0.0F));
        leftWing.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(30, 30).addBox(1.0F, 1.0F, -4.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -0.4167F, -2.5F, 1.5708F, 0.0F, 0.0F));
        leftWing.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(24, 35).addBox(1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -1.4167F, -2.5F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        VultureAnimation animation = entity.getVultureAnimation();

        switch (animation) {
            case IDLE_PERCHED -> {
                if (entity.isArmPerched()) {
                    this.animate(entity.idlePerchedAnimationState, VultureAnimations.ARM_PERCHED, ageInTicks);
                } else {
                    this.animate(entity.idlePerchedAnimationState, VultureAnimations.IDLE_PERCHED, ageInTicks, 0.75F);
                }
            }
            case FLAP_WINGS -> this.animate(entity.flapWingsAnimationState, VultureAnimations.FLAP_WINGS, ageInTicks);
            case WALKING -> this.animate(entity.walkingAnimationState, VultureAnimations.WALKING, ageInTicks);
            case EATING_OFF_GROUND -> this.animate(entity.eatingOffGroundAnimationState, VultureAnimations.EATING_OFF_GROUND, ageInTicks);
            case RAISE_LANDING_GEAR -> this.animate(entity.raiseLandingGearAnimationState, VultureAnimations.RAISE_LANDING_GEAR, ageInTicks);
            case SOARING -> this.animate(entity.soaringAnimationState, VultureAnimations.SOARING, ageInTicks);
            case RISING -> this.animate(entity.risingAnimationState, VultureAnimations.RISING, ageInTicks);
            case DEPLOY_LANDING_GEAR -> this.animate(entity.deployLandingGearAnimationState, VultureAnimations.DEPLOY_LANDING_GEAR, ageInTicks);
            case FALLING -> this.animate(entity.fallingAnimationState, VultureAnimations.FALLING, ageInTicks);
            case LANDING -> this.animate(entity.landingAnimationState, VultureAnimations.LANDING, ageInTicks);
            case STILL_PERCHED -> {
            }
        }

        if (!entity.isArmPerched()
                && animation != VultureAnimation.EATING_OFF_GROUND
                && animation != VultureAnimation.RAISE_LANDING_GEAR
                && animation != VultureAnimation.FALLING) {
            applySafeLook(netHeadYaw, headPitch);
        }
    }

    public void translateToMouth(PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.all.translateAndRotate(poseStack);
        this.headAndNeck.translateAndRotate(poseStack);
        this.mouthItem.translateAndRotate(poseStack);
    }

    public void translateToLeftTalon(PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.all.translateAndRotate(poseStack);
        this.torsoAndLegs.translateAndRotate(poseStack);
        this.legs.translateAndRotate(poseStack);
        this.leftLeg.translateAndRotate(poseStack);
        this.leftLowerLeg.translateAndRotate(poseStack);
        this.leftTalonItem.translateAndRotate(poseStack);
    }

    public void translateToRightTalon(PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.all.translateAndRotate(poseStack);
        this.torsoAndLegs.translateAndRotate(poseStack);
        this.legs.translateAndRotate(poseStack);
        this.rightLeg.translateAndRotate(poseStack);
        this.lowerRightLeg.translateAndRotate(poseStack);
        this.rightTalonItem.translateAndRotate(poseStack);
    }

    private void applySafeLook(float netHeadYaw, float headPitch) {
        float desiredYaw = this.headAndNeck.yRot + Mth.wrapDegrees(netHeadYaw) * Mth.DEG_TO_RAD;
        float neckYaw = Mth.clamp(desiredYaw, -30.0F * Mth.DEG_TO_RAD, 30.0F * Mth.DEG_TO_RAD);
        this.all.yRot += desiredYaw - neckYaw;
        this.headAndNeck.yRot = neckYaw;
        this.headAndNeck.xRot += Mth.clamp(headPitch, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.all.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
