package org.pickaid.scout.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class SatchelModel<T extends LivingEntity> extends HierarchicalModel<T> {
	private final ModelPart root;
	private final ModelPart satchel;
	private final ModelPart strap;

	public SatchelModel(ModelPart root) {
		super();
		this.root = root;
		this.satchel = root.getChild("satchel");
		this.strap = this.satchel.getChild("strap");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition satchel = partdefinition.addOrReplaceChild("satchel",
				CubeListBuilder.create()
						.texOffs(10, 0)
						.addBox(-6.0F, -12.0F, -2.5F, 2.0F, 3.0F, 5.0F,
								new CubeDeformation(0.275F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		satchel.addOrReplaceChild("strap",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-4.0F, -12.0F, -2.0F, 8.0F, 1.0F, 4.0F,
								new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		satchel.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Animation code would go here if needed
	}
}