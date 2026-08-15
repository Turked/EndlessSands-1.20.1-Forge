package net.MechGaming.EndlessSands.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.MechGaming.EndlessSands.entity.custom.OldworldGolemAnimation;
import net.MechGaming.EndlessSands.entity.custom.OldworldGolemEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class OldworldGolemModel<T extends OldworldGolemEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart sapling;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart rightHandItem;
    private final ModelPart topHalf;
    private final ModelPart bottomHalf;
    private final ModelPart bar1;
    private final ModelPart bar3;

    public OldworldGolemModel(ModelPart root) {
        this.root = root;
        ModelPart body = root.getChild("body");
        ModelPart upperBody = body.getChild("upper_body");
        ModelPart bar2 = upperBody.getChild("bar2");
        this.sapling = body.getChild("Sapling");
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.rightHandItem = this.rightArm.getChild("right_hand_item");
        this.topHalf = bar2.getChild("top_half");
        this.bottomHalf = bar2.getChild("bottom_half");
        this.bar1 = upperBody.getChild("bar1");
        this.bar3 = upperBody.getChild("bar3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));

        PartDefinition upperBody = body.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -23.0F, -6.0F, 14.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-7.0F, -33.0F, -6.0F, 14.0F, 2.0F, 11.0F, new CubeDeformation(0.002F))
                .texOffs(50, 0).addBox(-9.0F, -33.0F, -6.0F, 2.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(72, 38).addBox(-7.0F, -31.0F, 3.0F, 14.0F, 8.0F, 2.0F, new CubeDeformation(0.002F))
                .texOffs(40, 56).addBox(7.0F, -33.0F, -6.0F, 2.0F, 12.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 31.0F, 0.0F));

        upperBody.addOrReplaceChild("bar1", CubeListBuilder.create().texOffs(22, 74).addBox(-9.0F, -31.0F, -6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.002F)), PartPose.offset(4.0F, 0.0F, 0.0F));

        PartDefinition bar2 = upperBody.addOrReplaceChild("bar2", CubeListBuilder.create(), PartPose.offset(8.0F, 0.0F, 0.0F));
        bar2.addOrReplaceChild("top_half", CubeListBuilder.create().texOffs(74, 77).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.002F)), PartPose.offset(-8.0F, -29.0F, 0.0F));
        bar2.addOrReplaceChild("bottom_half", CubeListBuilder.create().texOffs(38, 79).addBox(-2.0F, -4.0F, 0.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.002F)), PartPose.offset(-7.0F, -23.0F, -6.0F));

        upperBody.addOrReplaceChild("bar3", CubeListBuilder.create().texOffs(30, 74).addBox(2.0F, -31.0F, -6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.002F)), PartPose.offset(1.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("lower_body", CubeListBuilder.create().texOffs(76, 0).addBox(-4.5F, -21.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 31.0F, 0.0F));

        PartDefinition sapling = body.addOrReplaceChild("Sapling", CubeListBuilder.create(), PartPose.offset(14.0F, 31.0F, 9.0F));

        PartDefinition branches = sapling.addOrReplaceChild("branches", CubeListBuilder.create().texOffs(76, 17).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-14.0F, 0.0F, -9.0F));
        branches.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 83).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 83).addBox(-1.0F, -2.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -26.0F, 2.0F, 0.0F, 0.0F, -1.5708F));
        branches.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(82, 81).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -26.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
        branches.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(80, 31).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -29.0F, 4.0F, 1.5708F, 0.0F, -1.5708F));
        branches.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(80, 26).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -29.0F, -1.0F, 1.5708F, 0.0F, -1.5708F));
        branches.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(54, 79).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -29.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
        branches.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(46, 79).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -29.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
        branches.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(82, 77).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -26.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition leaves = sapling.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(-14.0F, 0.0F, -9.0F));
        PartDefinition lowerLeaves = leaves.addOrReplaceChild("lower_leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        lowerLeaves.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(76, 11).addBox(1.25F, -1.75F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -26.0F, 5.0F, -1.5708F, 1.4399F, -1.5708F));
        lowerLeaves.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(72, 48).addBox(0.5F, -2.25F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -26.0F, -1.0F, 1.5708F, 1.4399F, 1.5708F));
        lowerLeaves.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(22, 68).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(1.0F, -26.0F, 0.0F, 0.0F, 0.0F, 0.1309F));
        lowerLeaves.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(22, 62).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.0F, -26.0F, 0.0F, 0.0F, 0.0F, -0.1309F));
        leaves.addOrReplaceChild("upper_leaves", CubeListBuilder.create().texOffs(40, 26).addBox(-5.0F, -32.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 38).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(66, 77).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, -2.0F));

        PartDefinition rightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 26).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(50, 23).addBox(-11.5F, 25.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));
        rightArm.addOrReplaceChild("right_hand_item", CubeListBuilder.create(), PartPose.offset(-11.0F, 25.5F, -3.5F));

        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 26).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 62).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 11.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(66, 56).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 11.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        OldworldGolemAnimation animation = entity.getGolemAnimation();
        this.sapling.visible = !entity.isSaplingRemoved();

        switch (animation) {
            case WALK -> this.animate(entity.walkAnimationState, OldworldGolemAnimations.WALK, ageInTicks);
            case ATTACK -> this.animate(entity.attackAnimationState, OldworldGolemAnimations.ATTACK, ageInTicks);
            case INSPECT_ROSE -> this.animate(entity.inspectRoseAnimationState, OldworldGolemAnimations.INSPECT_ROSE, ageInTicks);
            case REMOVE_SAPLING -> this.animate(entity.removeSaplingAnimationState, OldworldGolemAnimations.REMOVE_SAPLING, ageInTicks);
            case IDLE -> {
            }
        }

        if (entity.isSaplingRemoved() && animation != OldworldGolemAnimation.REMOVE_SAPLING) {
            this.applyOpenChestPose();
        }

        if (animation == OldworldGolemAnimation.IDLE
                || animation == OldworldGolemAnimation.WALK
                || animation == OldworldGolemAnimation.REMOVE_SAPLING) {
            this.head.yRot += Mth.wrapDegrees(netHeadYaw) * Mth.DEG_TO_RAD;
            this.head.xRot += Mth.clamp(headPitch, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
        }
    }

    private void applyOpenChestPose() {
        this.topHalf.xRot += 90.0F * Mth.DEG_TO_RAD;
        this.topHalf.zRot -= 90.0F * Mth.DEG_TO_RAD;
        this.topHalf.x -= 11.0F;
        this.topHalf.y -= 1.0F;
        this.topHalf.z -= 2.0F;

        this.bottomHalf.xRot -= 90.0F * Mth.DEG_TO_RAD;
        this.bottomHalf.zRot -= 90.0F * Mth.DEG_TO_RAD;
        this.bottomHalf.x += 4.25F;
        this.bottomHalf.y -= 2.0F;
        this.bottomHalf.z += 2.0F;

        this.bar1.x -= 2.0F;
        this.bar3.x += 2.0F;
    }

    public void translateToRightHand(PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.rightArm.translateAndRotate(poseStack);
        this.rightHandItem.translateAndRotate(poseStack);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
