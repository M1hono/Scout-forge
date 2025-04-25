package org.pickaid.scout.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.pickaid.scout.ScoutScreenHandler;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.item.BaseBagItem;
import org.pickaid.scout.screen.BagSlot;

import java.util.ArrayList;
import java.util.function.Supplier;

public class BagUpdatePacket {

    public BagUpdatePacket() {
    }

    public BagUpdatePacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var client = Minecraft.getInstance();
            assert client.player != null;
            ScoutScreenHandler screenHandler = (ScoutScreenHandler) client.player.inventoryMenu;

            ItemStack satchelStack = ScoutUtil.findBagItem(client.player, BaseBagItem.BagType.SATCHEL, false);
            ArrayList<BagSlot> satchelSlots = screenHandler.scout$getSatchelSlots();

            for (int i = 0; i < ScoutUtil.MAX_SATCHEL_SLOTS; i++) {
                BagSlot slot = satchelSlots.get(i);
                slot.setInventory(null);
                slot.setEnabled(false);
            }
            if (!satchelStack.isEmpty()) {
                BaseBagItem satchelItem = (BaseBagItem) satchelStack.getItem();
                Container satchelInv = satchelItem.getInventory(satchelStack);

                for (int i = 0; i < satchelItem.getSlotCount(); i++) {
                    BagSlot slot = satchelSlots.get(i);
                    slot.setInventory(satchelInv);
                    slot.setEnabled(true);
                }
            }

            ItemStack leftPouchStack = ScoutUtil.findBagItem(client.player, BaseBagItem.BagType.POUCH, false);
            ArrayList<BagSlot> leftPouchSlots = screenHandler.scout$getLeftPouchSlots();

            for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
                BagSlot slot = leftPouchSlots.get(i);
                slot.setInventory(null);
                slot.setEnabled(false);
            }
            if (!leftPouchStack.isEmpty()) {
                BaseBagItem leftPouchItem = (BaseBagItem) leftPouchStack.getItem();
                Container leftPouchInv = leftPouchItem.getInventory(leftPouchStack);

                for (int i = 0; i < leftPouchItem.getSlotCount(); i++) {
                    BagSlot slot = leftPouchSlots.get(i);
                    slot.setInventory(leftPouchInv);
                    slot.setEnabled(true);
                }
            }

            ItemStack rightPouchStack = ScoutUtil.findBagItem(client.player, BaseBagItem.BagType.POUCH, true);
            ArrayList<BagSlot> rightPouchSlots = screenHandler.scout$getRightPouchSlots();

            for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
                BagSlot slot = rightPouchSlots.get(i);
                slot.setInventory(null);
                slot.setEnabled(false);
            }
            if (!rightPouchStack.isEmpty()) {
                BaseBagItem rightPouchItem = (BaseBagItem) rightPouchStack.getItem();
                Container rightPouchInv = rightPouchItem.getInventory(rightPouchStack);

                for (int i = 0; i < rightPouchItem.getSlotCount(); i++) {
                    BagSlot slot = rightPouchSlots.get(i);
                    slot.setInventory(rightPouchInv);
                    slot.setEnabled(true);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}