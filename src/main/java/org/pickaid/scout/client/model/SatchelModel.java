package org.pickaid.scout.client.model;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
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

	@Override
	public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		satchel.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

	}
}
