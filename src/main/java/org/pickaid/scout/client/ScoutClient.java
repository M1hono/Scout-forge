package org.pickaid.scout.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.scout.Scout;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.client.render.PouchFeatureRenderer;
import org.pickaid.scout.client.render.SatchelFeatureRenderer;
import org.pickaid.scout.mixins.client.HandledScreenAccessor;
import org.pickaid.scout.screen.BagSlot;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Scout.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScoutClient {
    @SubscribeEvent
    public static void onAddLayer(EntityRenderersEvent.AddLayers event) {
        LivingEntityRenderer renderer = event.getRenderer(EntityType.PLAYER);
        renderer.addLayer(new PouchFeatureRenderer<>(renderer, event.getContext().getItemInHandRenderer()));
        renderer.addLayer(new SatchelFeatureRenderer<>(renderer, event.getEntityModels()));
    }

    @SubscribeEvent
    public static void afterInitScreen(ScreenEvent.Init.Post event) {
        var screen = event.getScreen();
        var client = Minecraft.getInstance();
        if (screen instanceof AbstractContainerScreen<?> handledScreen && client.player != null) {
            if (ScoutUtilClient.isScreenBlacklisted(screen)) {
                for (Slot slot : ScoutUtil.getAllBagSlots(client.player.inventoryMenu)) {
                    BagSlot bagSlot = (BagSlot) slot;
                    bagSlot.setX(Integer.MAX_VALUE);
                    bagSlot.setY(Integer.MAX_VALUE);
                }
                return;
            }

            var handledScreenAccessor = (HandledScreenAccessor<?>) handledScreen;
            InventoryMenu handler = (InventoryMenu) handledScreenAccessor.getHandler();

            var playerInventory = client.player.getInventory();

            int x = 0;
            int y = 0;

            var _hotbarSlot1 = handler.slots.stream().filter(slot->slot.container.equals(playerInventory) && slot.getContainerSlot() == 0).findFirst();
            Slot hotbarSlot1 = _hotbarSlot1.isPresent() ? _hotbarSlot1.get() : null;
            if (hotbarSlot1 != null) {
                if (!hotbarSlot1.isActive()) {
                    for (int i = 0; i < ScoutUtil.MAX_SATCHEL_SLOTS; i++) {
                        BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.SATCHEL_SLOT_START - i, client.player.inventoryMenu);
                        if (slot != null) {
                            slot.setX(Integer.MAX_VALUE);
                            slot.setY(Integer.MAX_VALUE);
                        }
                    }
                } else {
                    x = hotbarSlot1.x;
                    y = hotbarSlot1.y + 27;

                    for (int i = 0; i < ScoutUtil.MAX_SATCHEL_SLOTS; i++) {
                        if (i % 9 == 0) {
                            x = hotbarSlot1.x;
                        }

                        BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.SATCHEL_SLOT_START - i, client.player.inventoryMenu);
                        if (slot != null) {
                            slot.setX(x);
                            slot.setY(y);
                        }

                        x += 18;

                        if ((i + 1) % 9 == 0) {
                            y += 18;
                        }
                    }
                }
            }

            var _topLeftSlot = handler.slots.stream().filter(slot->slot.container.equals(playerInventory) && slot.getContainerSlot() == 9).findFirst();
            Slot topLeftSlot = _topLeftSlot.isPresent() ? _topLeftSlot.get() : null;
            if (topLeftSlot != null) {
                if (!topLeftSlot.isActive()) {
                    for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
                        BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.LEFT_POUCH_SLOT_START - i, client.player.inventoryMenu);
                        if (slot != null) {
                            slot.setX(Integer.MAX_VALUE);
                            slot.setY(Integer.MAX_VALUE);
                        }
                    }
                } else {
                    x = topLeftSlot.x;
                    y = topLeftSlot.y - 18;

                    for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
                        if (i % 3 == 0) {
                            x -= 18;
                            y += 54;
                        }

                        BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.LEFT_POUCH_SLOT_START - i, client.player.inventoryMenu);
                        if (slot != null) {
                            slot.setX(x);
                            slot.setY(y);
                        }

                        y -= 18;
                    }
                }
            }

            var _topRightSlot = handler.slots.stream().filter(slot->slot.container.equals(playerInventory) && slot.getContainerSlot() == 17).findFirst();
            Slot topRightSlot = _topRightSlot.isPresent() ? _topRightSlot.get() : null;
            if (topRightSlot != null) {
                if (!topLeftSlot.isActive()) {
                    for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
                        BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.RIGHT_POUCH_SLOT_START - i, client.player.inventoryMenu);
                        if (slot != null) {
                            slot.setX(Integer.MAX_VALUE);
                            slot.setY(Integer.MAX_VALUE);
                        }
                    }
                } else {
                    x = topRightSlot.x;
                    y = topRightSlot.y - 18;

                    for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
                        if (i % 3 == 0) {
                            x += 18;
                            y += 54;
                        }

                        BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.RIGHT_POUCH_SLOT_START - i, client.player.inventoryMenu);
                        if (slot != null) {
                            slot.setX(x);
                            slot.setY(y);
                        }

                        y -= 18;
                    }
                }
            }
        }
    }
}
