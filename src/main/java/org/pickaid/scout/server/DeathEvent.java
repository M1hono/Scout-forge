package org.pickaid.scout.server;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.scout.Scout;
import org.pickaid.scout.ScoutScreenHandler;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.item.BaseBagItem;
import org.pickaid.scout.network.ScoutNetworkHandler;
import org.pickaid.scout.screen.BagSlot;

import java.util.ArrayList;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = Scout.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DeathEvent {

    @SubscribeEvent
    public static void DeathEvent(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            ScoutScreenHandler handler = (ScoutScreenHandler) serverPlayer.inventoryMenu;

            if (!serverPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                ItemStack backStack = ScoutUtil.findBagItem(serverPlayer, BaseBagItem.BagType.SATCHEL, false);
                if (!backStack.isEmpty()) {
                    BaseBagItem bagItem = (BaseBagItem) backStack.getItem();
                    int slots = bagItem.getSlotCount();

                    ArrayList<BagSlot> bagSlots = handler.scout$getSatchelSlots();

                    for (int i = 0; i < slots; i++) {
                        BagSlot slot = bagSlots.get(i);
                        slot.setInventory(null);
                        slot.setEnabled(false);
                    }
                }

                ItemStack leftPouchStack = ScoutUtil.findBagItem(serverPlayer, BaseBagItem.BagType.POUCH, false);
                if (!leftPouchStack.isEmpty()) {
                    BaseBagItem bagItem = (BaseBagItem) leftPouchStack.getItem();
                    int slots = bagItem.getSlotCount();

                    ArrayList<BagSlot> bagSlots = handler.scout$getLeftPouchSlots();

                    for (int i = 0; i < slots; i++) {
                        BagSlot slot = bagSlots.get(i);
                        slot.setInventory(null);
                        slot.setEnabled(false);
                    }
                }

                ItemStack rightPouchStack = ScoutUtil.findBagItem(serverPlayer, BaseBagItem.BagType.POUCH, true);
                if (!rightPouchStack.isEmpty()) {
                    BaseBagItem bagItem = (BaseBagItem) rightPouchStack.getItem();
                    int slots = bagItem.getSlotCount();

                    ArrayList<BagSlot> bagSlots = handler.scout$getRightPouchSlots();

                    for (int i = 0; i < slots; i++) {
                        BagSlot slot = bagSlots.get(i);
                        slot.setInventory(null);
                        slot.setEnabled(false);
                    }
                }
                ScoutNetworkHandler.sendBagUpdateToClient(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            ScoutScreenHandler handler = (ScoutScreenHandler) serverPlayer.inventoryMenu;

            if (!serverPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                ItemStack backStack = ScoutUtil.findBagItem(serverPlayer, BaseBagItem.BagType.SATCHEL, false);
                if (!backStack.isEmpty()) {
                    BaseBagItem bagItem = (BaseBagItem) backStack.getItem();
                    int slots = bagItem.getSlotCount();

                    ArrayList<BagSlot> bagSlots = handler.scout$getSatchelSlots();

                    for (int i = 0; i < slots; i++) {
                        BagSlot slot = bagSlots.get(i);
                        slot.setInventory(null);
                        slot.setEnabled(false);
                    }
                }

                ItemStack leftPouchStack = ScoutUtil.findBagItem(serverPlayer, BaseBagItem.BagType.POUCH, false);
                if (!leftPouchStack.isEmpty()) {
                    BaseBagItem bagItem = (BaseBagItem) leftPouchStack.getItem();
                    int slots = bagItem.getSlotCount();

                    ArrayList<BagSlot> bagSlots = handler.scout$getLeftPouchSlots();

                    for (int i = 0; i < slots; i++) {
                        BagSlot slot = bagSlots.get(i);
                        slot.setInventory(null);
                        slot.setEnabled(false);
                    }
                }

                ItemStack rightPouchStack = ScoutUtil.findBagItem(serverPlayer, BaseBagItem.BagType.POUCH, true);
                if (!rightPouchStack.isEmpty()) {
                    BaseBagItem bagItem = (BaseBagItem) rightPouchStack.getItem();
                    int slots = bagItem.getSlotCount();

                    ArrayList<BagSlot> bagSlots = handler.scout$getRightPouchSlots();

                    for (int i = 0; i < slots; i++) {
                        BagSlot slot = bagSlots.get(i);
                        slot.setInventory(null);
                        slot.setEnabled(false);
                    }
                }
                ScoutNetworkHandler.sendBagUpdateToClient(serverPlayer);
            }
        }
    }
}
