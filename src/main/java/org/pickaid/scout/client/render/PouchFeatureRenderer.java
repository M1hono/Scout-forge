package org.pickaid.scout.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.item.BaseBagItem;

public class PouchFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final ItemInHandRenderer heldItemRenderer;

	public PouchFeatureRenderer(RenderLayerParent<T, M> render, ItemInHandRenderer heldItemRenderer) {
		super(render);
		this.heldItemRenderer = heldItemRenderer;
	}
	@Override
	public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		var leftPouch = ScoutUtil.findBagItem((Player) entity, BaseBagItem.BagType.POUCH, false);
		var rightPouch = ScoutUtil.findBagItem((Player) entity, BaseBagItem.BagType.POUCH, true);

		if (!leftPouch.isEmpty()) {
			pPoseStack.pushPose();
			((PlayerModel<?>) this.getParentModel()).leftLeg.translateAndRotate(pPoseStack);
			pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			pPoseStack.scale(0.325F, 0.325F, 0.325F);
			pPoseStack.translate(0F, -0.325F, -0.475F);
			this.heldItemRenderer.renderItem(entity, leftPouch, ItemDisplayContext.FIXED, false, pPoseStack, pBuffer, pPackedLight);
			pPoseStack.popPose();
		}
		if (!rightPouch.isEmpty()) {
			pPoseStack.pushPose();
			((PlayerModel<?>) this.getParentModel()).rightLeg.translateAndRotate(pPoseStack);
			pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			pPoseStack.scale(0.325F, 0.325F, 0.325F);
			pPoseStack.translate(0F, -0.325F, 0.475F);
			this.heldItemRenderer.renderItem(entity, rightPouch, ItemDisplayContext.FIXED, false, pPoseStack, pBuffer, pPackedLight);
			pPoseStack.popPose();
		}

	}
}
