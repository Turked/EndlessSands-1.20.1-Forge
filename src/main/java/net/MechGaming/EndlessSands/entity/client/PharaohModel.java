package net.MechGaming.EndlessSands.entity.client;

// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.MechGaming.EndlessSands.entity.custom.PharaohEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PharaohModel<T extends PharaohEntity> extends EntityModel<T> {
	private final ModelPart head;
	private final ModelPart nemes_headdress;
	private final ModelPart shield;
	private final ModelPart spoke1;
	private final ModelPart spoke2;
	private final ModelPart shield2;
	private final ModelPart body;
	private final ModelPart vest;
	private final ModelPart belt;
	private final ModelPart left_leg;
	private final ModelPart thigh_guard;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart thigh_guard2;

	public PharaohModel(ModelPart root) {
		this.head = root.getChild("head");
		this.nemes_headdress = this.head.getChild("nemes_headdress");
		this.shield = this.nemes_headdress.getChild("shield");
		this.spoke1 = this.nemes_headdress.getChild("spoke1");
		this.spoke2 = this.nemes_headdress.getChild("spoke2");
		this.shield2 = this.nemes_headdress.getChild("shield2");
		this.body = root.getChild("body");
		this.vest = this.body.getChild("vest");
		this.belt = this.body.getChild("belt");
		this.left_leg = root.getChild("left_leg");
		this.thigh_guard = this.left_leg.getChild("thigh_guard");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.right_leg = root.getChild("right_leg");
		this.thigh_guard2 = this.right_leg.getChild("thigh_guard2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition nemes_headdress = head.addOrReplaceChild("nemes_headdress", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 0.0F, -3.0F, 12.0F, 5.0F, 10.0F, new CubeDeformation(0.25F))
		.texOffs(44, 0).addBox(-3.5F, -6.5F, 5.0F, 12.0F, 6.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offset(-2.5F, 6.0F, -2.0F));

		PartDefinition shield = nemes_headdress.addOrReplaceChild("shield", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, -3.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 3.5F, 0.0F, 0.0F, 0.3927F));

		PartDefinition spoke1 = nemes_headdress.addOrReplaceChild("spoke1", CubeListBuilder.create().texOffs(60, 59).addBox(-1.25F, -5.5F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.5F, -2.0F, -0.3491F, 0.0305F, 0.0873F));

		PartDefinition spoke2 = nemes_headdress.addOrReplaceChild("spoke2", CubeListBuilder.create().texOffs(0, 62).addBox(-1.25F, -5.25F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -2.5F, -2.0F, -0.3491F, -0.0305F, -0.0873F));

		PartDefinition shield2 = nemes_headdress.addOrReplaceChild("shield2", CubeListBuilder.create().texOffs(30, 31).addBox(-1.5F, -3.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, 0.0F, 3.5F, -3.1416F, 0.0F, 2.7489F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 15).addBox(-4.0F, -5.5F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.5F, 0.0F));

		PartDefinition vest = body.addOrReplaceChild("vest", CubeListBuilder.create().texOffs(60, 41).addBox(-3.0F, 2.0F, 1.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 54).addBox(-4.0F, 4.0F, 1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(44, 13).addBox(-5.0F, 6.0F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 57).addBox(2.0F, 6.0F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 67).addBox(1.0F, 4.0F, 1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 29).addBox(-4.0F, 4.0F, 6.0F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 68).addBox(4.0F, 6.0F, 6.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 68).addBox(-6.0F, 6.0F, 6.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 13).addBox(-5.0F, 5.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 29).addBox(4.0F, 5.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 62).addBox(-3.0F, 2.0F, 6.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 44).addBox(-2.0F, 0.0F, 6.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -4.0F));

		PartDefinition vest_r1 = vest.addOrReplaceChild("vest_r1", CubeListBuilder.create().texOffs(8, 65).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 47).addBox(-11.0F, -3.5F, -0.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, 7.5F, 4.5F, -1.5708F, 0.0F, 0.0F));

		PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(22, 62).addBox(-2.0F, -8.0F, -2.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(68, 8).addBox(-1.0F, -11.5F, -2.75F, 2.0F, 4.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(14, 65).addBox(-1.0F, -11.5F, 1.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(44, 8).addBox(-4.0F, -5.5F, -2.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 46).addBox(-1.9F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition thigh_guard = left_leg.addOrReplaceChild("thigh_guard", CubeListBuilder.create().texOffs(32, 59).addBox(1.0F, 7.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.25F))
		.texOffs(60, 44).addBox(1.0F, 6.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(40, 68).addBox(2.0F, 6.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(44, 68).addBox(3.0F, 6.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(48, 68).addBox(3.5F, 6.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(56, 68).addBox(3.5F, 6.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(68, 59).addBox(3.5F, 6.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(68, 61).addBox(3.0F, 6.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(68, 63).addBox(2.0F, 6.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(68, 65).addBox(1.0F, 6.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(52, 68).addBox(3.5F, 6.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(-1.9F, -12.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 46).addBox(-3.5F, -10.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(56, 13).addBox(-3.5F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.5F))
		.texOffs(68, 67).addBox(-4.0F, -5.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 22.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 46).addBox(-3.5F, -10.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(56, 21).addBox(-3.5F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.5F))
		.texOffs(68, 67).addBox(-4.0F, -5.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 22.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 46).addBox(-1.9F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.1F, 12.0F, 0.0F));

		PartDefinition thigh_guard2 = right_leg.addOrReplaceChild("thigh_guard2", CubeListBuilder.create().texOffs(46, 59).addBox(0.0F, 7.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.25F))
		.texOffs(20, 70).addBox(0.0F, 6.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(24, 70).addBox(1.0F, 6.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(28, 70).addBox(2.0F, 6.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(44, 70).addBox(2.0F, 6.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(70, 47).addBox(1.0F, 6.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(48, 70).addBox(0.0F, 6.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(70, 49).addBox(-0.5F, 6.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(70, 51).addBox(-0.5F, 6.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(52, 70).addBox(-0.5F, 6.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
		.texOffs(56, 70).addBox(-0.5F, 6.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(-1.9F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
