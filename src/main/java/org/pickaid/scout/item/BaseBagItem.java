package org.pickaid.scout.item;

import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.pickaid.scout.ScoutNetworking;
import org.pickaid.scout.ScoutScreenHandler;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.network.ScoutNetworkHandler;
import org.pickaid.scout.screen.BagSlot;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BaseBagItem extends Item implements ICurioItem {
    private static final String ITEMS_KEY = "Items";

    private final int slots;
    private final BagType type;

    public BaseBagItem(int slots, BagType type) {
        super(new Properties());

        if (type == BagType.SATCHEL && slots > ScoutUtil.MAX_SATCHEL_SLOTS) {
            throw new IllegalArgumentException("Satchel has too many slots.");
        }
        if (type == BagType.POUCH && slots > ScoutUtil.MAX_POUCH_SLOTS) {
            throw new IllegalArgumentException("Pouch has too many slots.");
        }

        this.slots = slots;
        this.type = type;
    }

    public int getSlotCount() {
        return this.slots;
    }

    public BagType getType() {
        return this.type;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        super.appendHoverText(stack, level, tooltip, pIsAdvanced);
        tooltip.add(Component.translatable("tooltip.scout.slots", Component.literal(String.valueOf(this.slots)).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
    }

    public Container getInventory(ItemStack stack) {
        SimpleContainer inventory = new SimpleContainer(this.slots) {
//            @Override
//            public void set() {
//                stack.getOrCreateNbt().put(ITEMS_KEY, ScoutUtil.inventoryToTag(this));
//                super.markDirty();
//            }

            @Override
            public void setChanged() {
                stack.getOrCreateTag().put(ITEMS_KEY, ScoutUtil.inventoryToTag(this));
                super.setChanged();
            }
        };

        CompoundTag compound = stack.getOrCreateTag();
        if (!compound.contains(ITEMS_KEY)) {
            compound.put(ITEMS_KEY, new CompoundTag());
        }

        ListTag items = compound.getList(ITEMS_KEY, 10);

        ScoutUtil.inventoryFromTag(items, inventory);

        return inventory;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        Container inventory = getInventory(stack);

        for (int i = 0; i < slots; i++) {
            stacks.add(inventory.getItem(i));
        }

        if (stacks.stream().allMatch(ItemStack::isEmpty)) return Optional.empty();

        return Optional.of(new BagTooltipData(stacks, slots));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player)
            updateSlots(player);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player)
            updateSlots(player);
    }

    private void updateSlots(Player player) {
        ScoutScreenHandler handler = (ScoutScreenHandler) player.inventoryMenu;

        ItemStack satchelStack = ScoutUtil.findBagItem(player, BagType.SATCHEL, false);
        ArrayList<BagSlot> satchelSlots = handler.scout$getSatchelSlots();

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

        ItemStack leftPouchStack = ScoutUtil.findBagItem(player, BagType.POUCH, false);
        ArrayList<BagSlot> leftPouchSlots = handler.scout$getLeftPouchSlots();

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

        ItemStack rightPouchStack = ScoutUtil.findBagItem(player, BagType.POUCH, true);
        ArrayList<BagSlot> rightPouchSlots = handler.scout$getRightPouchSlots();

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
        if (player instanceof ServerPlayer serverPlayer) {
            ScoutNetworkHandler.sendBagUpdateToClient(serverPlayer);
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        Item item = stack.getItem();
        Entity entity = slotContext.entity();

        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(slotContext.entity()).resolve();
        if (curiosHandler.isEmpty()){ return false; }
        ItemStack slotStack = curiosHandler.get().getEquippedCurios().getStackInSlot(slotContext.index());
        Item slotItem = slotStack.getItem();

        if (slotItem instanceof BaseBagItem) {
            if (((BaseBagItem) item).getType() == BagType.SATCHEL) {
                if (((BaseBagItem) slotItem).getType() == BagType.SATCHEL) {
                    return true;
                } else {
                    return ScoutUtil.findBagItem((Player) entity, BagType.SATCHEL, false).isEmpty();
                }
            } else if (((BaseBagItem) item).getType() == BagType.POUCH) {
                if (((BaseBagItem) slotItem).getType() == BagType.POUCH) {
                    return true;
                } else {
                    return ScoutUtil.findBagItem((Player) entity, BagType.POUCH, true).isEmpty();
                }
            }
        } else {
            if (((BaseBagItem) item).getType() == BagType.SATCHEL) {
                return ScoutUtil.findBagItem((Player) entity, BagType.SATCHEL, false).isEmpty();
            } else if (((BaseBagItem) item).getType() == BagType.POUCH) {
                return ScoutUtil.findBagItem((Player) entity, BagType.POUCH, true).isEmpty();
            }
        }

        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        var inv = getInventory(stack);

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var invStack = inv.getItem(i);
            invStack.inventoryTick(world, entity, i, false);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var inv = getInventory(stack);
        var entity = slotContext.entity();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var invStack = inv.getItem(i);
            invStack.inventoryTick(entity.level(), entity, i, false);
        }
    }


    public enum BagType {
        SATCHEL,
        POUCH
    }
}
