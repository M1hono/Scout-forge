package org.pickaid.scout.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.pickaid.scout.ScoutUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = AbstractContainerMenu.class, priority = 950)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "doClick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;getCarried()Lnet/minecraft/world/item/ItemStack;", ordinal = 11), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void scout$fixDoubleClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci, Inventory inventory, Slot slot3) {
        var cursorStack = this.getCarried();
        if (!cursorStack.isEmpty() && (!slot3.hasItem() || !slot3.mayPickup(player))) {
            var slots = ScoutUtil.getAllBagSlots(player.inventoryMenu);
            var k = button == 0 ? 0 : ScoutUtil.TOTAL_SLOTS - 1;
            var o = button == 0 ? 1 : -1;

            for (int n = 0; n < 2; ++n) {
                for (int p = k; p >= 0 && p < slots.size() && cursorStack.getCount() < cursorStack.getMaxStackSize(); p += o) {
                    Slot slot4 = slots.get(p);
                    if (slot4.hasItem() && canInsertItemIntoSlot(slot4, cursorStack, true) && slot4.mayPickup(player) && this.canPlaceItemInSlot(slot4, cursorStack, true)) {
                        ItemStack itemStack6 = slot4.getItem();
                        if (n != 0 || itemStack6.getCount() != itemStack6.getMaxStackSize()) {
                            int count = Math.min(itemStack6.getCount(), cursorStack.getMaxStackSize() - cursorStack.getCount());
                            ItemStack itemStack7 = slot4.remove(count);
                            cursorStack.grow(itemStack7.getCount());
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "doClick", at = @At(value = "HEAD"), cancellable = true)
    public void scout$fixNegativeSlotClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (slotId < 0 && ScoutUtil.isBagSlot(slotId)) {
            AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
            Slot bagSlot = ScoutUtil.getBagSlot(slotId, player.inventoryMenu);

            if (bagSlot != null) {
                return;
            }
        }
    }

    @Redirect(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"))
    public Object scout$fixSlotIndexing(NonNullList<Slot> self, int index, int slotId, int button, ClickType clickType, Player player) {
        if (ScoutUtil.isBagSlot(index)) {
            return ScoutUtil.getBagSlot(index, player.inventoryMenu);
        } else {
            return self.get(index);
        }
    }

    @Inject(method = "moveItemStackTo", at = @At("HEAD"), cancellable = true)
    private void scout$fixMoveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, CallbackInfoReturnable<Boolean> cir) {
        if (ScoutUtil.isBagSlot(startIndex) || ScoutUtil.isBagSlot(endIndex)) {
            AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
            Player player = null;

            for (var i = 0; i < self.getItems().size(); i++) {
                Slot slot = self.getSlot(i);
                if (slot.container instanceof Inventory) {
                    player = ((Inventory)slot.container).player;
                    break;
                }
            }

            if (player != null) {
                boolean result = moveItemStackToBagSlots(stack, startIndex, endIndex, reverseDirection, player);
                cir.setReturnValue(result);
            }
        }
    }
    private boolean moveItemStackToBagSlots(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, Player player) {
        boolean flag = false;
        List<Slot> slots;

        if (ScoutUtil.isBagSlot(startIndex) && ScoutUtil.isBagSlot(endIndex)) {
            slots = ScoutUtil.getAllBagSlots(player.inventoryMenu);
        } else if (ScoutUtil.isBagSlot(startIndex)) {
            AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
            slots = self.slots;
        } else {
            slots = ScoutUtil.getAllBagSlots(player.inventoryMenu);
        }

        int i = reverseDirection ? endIndex - 1 : startIndex;
        int increment = reverseDirection ? -1 : 1;

        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (reverseDirection ? i < startIndex : i >= endIndex) {
                    break;
                }

                Slot slot;
                if (ScoutUtil.isBagSlot(i)) {
                    slot = ScoutUtil.getBagSlot(i, player.inventoryMenu);
                } else {
                    AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
                    slot = self.slots.get(i);
                }

                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                i += increment;
            }
        }

        if (!stack.isEmpty()) {
            i = reverseDirection ? endIndex - 1 : startIndex;

            while (true) {
                if (reverseDirection ? i < startIndex : i >= endIndex) {
                    break;
                }

                Slot slot;
                if (ScoutUtil.isBagSlot(i)) {
                    slot = ScoutUtil.getBagSlot(i, player.inventoryMenu);
                } else {
                    AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
                    slot = self.slots.get(i);
                }

                ItemStack itemstack = slot.getItem();
                if (itemstack.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.set(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.set(stack.split(stack.getCount()));
                    }

                    slot.setChanged();
                    flag = true;
                    break;
                }

                i += increment;
            }
        }

        return flag;
    }

    private static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        return false;
    }

    public boolean canPlaceItemInSlot(Slot slot, ItemStack stack, boolean allowOverflow) {
        return true;
    }

    @Shadow
    public abstract ItemStack getCarried();

    @Shadow
    public abstract Slot getSlot(int pSlot);
}