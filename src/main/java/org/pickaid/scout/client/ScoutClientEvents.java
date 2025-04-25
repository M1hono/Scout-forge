package org.pickaid.scout.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.scout.Scout;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.mixins.client.HandledScreenAccessor;
import org.pickaid.scout.screen.BagSlot;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Scout.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScoutClientEvents {
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

            // 调整背包槽位
            var hotbarSlot1 = handler.slots.stream()
                    .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 0)
                    .findFirst()
                    .orElse(null);

            if (hotbarSlot1 != null) {
                int x = hotbarSlot1.x;
                int y = hotbarSlot1.y + 27;

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

            // 调整左侧口袋槽位
            var topLeftSlot = handler.slots.stream()
                    .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 9)
                    .findFirst()
                    .orElse(null);

            if (topLeftSlot != null) {
                int x = topLeftSlot.x - 18;
                int y = topLeftSlot.y + 36;

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

            // 调整右侧口袋槽位
            var topRightSlot = handler.slots.stream()
                    .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 17)
                    .findFirst()
                    .orElse(null);

            if (topRightSlot != null) {
                int x = topRightSlot.x + 18;
                int y = topRightSlot.y + 36;

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
