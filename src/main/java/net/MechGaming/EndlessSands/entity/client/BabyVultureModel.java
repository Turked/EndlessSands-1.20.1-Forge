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

public class BabyVultureModel<T extends VultureEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart legs;
    private final ModelPart head;
    private final ModelPart wings;

    public BabyVultureModel(ModelPart root) {
        this.root = root;
        this.torso = root.getChild("Torso");
        this.legs = root.getChild("Legs");
        this.head = root.getChild("Head");
        this.wings = root.getChild("Wings");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild("Legs", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 20).addBox(1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -13.5F, 1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(8, 20).addBox(0.25F, -12.5F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 20).addBox(-1.25F, -12.5F, 5.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
                .texOffs(8, 20).addBox(-1.25F, -12.5F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 20).addBox(0.25F, -12.5F, 5.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, 24.0F, -0.75F));
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 17).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.75F, 2.5F, -0.3927F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(20, 20).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 6.0F, -0.6109F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 20).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 6.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition wings = partdefinition.addOrReplaceChild("Wings", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        wings.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 12).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 12).addBox(-8.0F, -2.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -5.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        if (entity.getVultureAnimation() == VultureAnimation.WALKING) {
            this.animate(entity.walkingAnimationState, BabyVultureAnimations.WALKING, ageInTicks);
        }

        this.root.yRot += Mth.PI + Mth.wrapDegrees(netHeadYaw) * Mth.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
