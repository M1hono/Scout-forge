package org.pickaid.scout.client;


import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.scout.Scout;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.client.render.PouchFeatureRenderer;
import org.pickaid.scout.client.render.SatchelFeatureRenderer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Scout.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScoutClient {
    @SubscribeEvent
    public static void onAddLayer(EntityRenderersEvent.AddLayers event) {
        for (String skinName : event.getSkins()) {
            if (event.getSkin(skinName) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new PouchFeatureRenderer<>(renderer, event.getContext().getItemInHandRenderer()));
                renderer.addLayer(new SatchelFeatureRenderer<>(renderer, event.getEntityModels()));
                ScoutUtil.LOGGER.debug("Added Scout layers to player skin: {}", skinName);
            }
        }
    }

}
