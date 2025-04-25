package org.pickaid.scout.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.client.model.SatchelModel;
import org.pickaid.scout.item.BaseBagItem;

public class SatchelFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public static final ModelLayerLocation SATCHEL_LAYER = new ModelLayerLocation(new ResourceLocation("scout:satchel"), "layer0");
	private static final ResourceLocation SATCHEL_TEXTURE = new ResourceLocation(ScoutUtil.MOD_ID, "textures/entity/satchel.png");
	private static final ResourceLocation UPGRADED_SATCHEL_TEXTURE = new ResourceLocation(ScoutUtil.MOD_ID, "textures/entity/upgraded_satchel.png");

	private final SatchelModel<T> satchel;

	public SatchelFeatureRenderer(RenderLayerParent<T, M> render, EntityModelSet set) {
		super(render);
		this.satchel = new SatchelModel<>(set.bakeLayer(SATCHEL_LAYER));
	}

	/**
	 * Returns the location of an entity's texture.
	 *
	 * @param pEntity
	 */
	@Override
	public ResourceLocation getTextureLocation(T pEntity) {
		return SATCHEL_TEXTURE;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		var satchel = ScoutUtil.findBagItem((Player) entity, BaseBagItem.BagType.SATCHEL, false);

		if (!satchel.isEmpty()) {
			BaseBagItem satchelItem = (BaseBagItem) satchel.getItem();
			var texture = SATCHEL_TEXTURE;
			if (satchelItem.getSlotCount() == ScoutUtil.MAX_SATCHEL_SLOTS)
				texture = UPGRADED_SATCHEL_TEXTURE;

			poseStack.pushPose();
			((PlayerModel<?>) this.getParentModel()).body.translateAndRotate(poseStack);
			this.getParentModel().copyPropertiesTo(this.getParentModel());
			VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
					buffer, RenderType.armorCutoutNoCull(texture), false, satchel.hasFoil()
					);
			this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
		}
	}
}
