package org.pickaid.scout.mixins;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.pickaid.scout.ScoutScreenHandler;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.screen.BagSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin implements ScoutScreenHandler {
    protected InventoryMenuMixin() {
        super();
    }

    @Unique
    public final ArrayList<BagSlot> scout$satchelSlots = new ArrayList<BagSlot>(ScoutUtil.MAX_SATCHEL_SLOTS);
    @Unique
    public final ArrayList<BagSlot> scout$leftPouchSlots = new ArrayList<BagSlot>(ScoutUtil.MAX_POUCH_SLOTS);
    @Unique
    public final ArrayList<BagSlot> scout$rightPouchSlots = new ArrayList<BagSlot>(ScoutUtil.MAX_POUCH_SLOTS);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void scout$addSlots(Inventory pPlayerInventory, boolean pActive, Player pOwner, CallbackInfo ci) {
        // satchel
        int x = 8;
        int y = 168;

        for (int i = 0; i < ScoutUtil.MAX_SATCHEL_SLOTS; i++) {
            if (i % 9 == 0) {
                x = 8;
            }

            BagSlot slot = new BagSlot(i, x, y);
            slot.slot = ScoutUtil.SATCHEL_SLOT_START - i;
            scout$satchelSlots.add(slot);

            x += 18;

            if ((i + 1) % 9 == 0) {
                y += 18;
            }
        }

        // left pouch
        x = 8;
        y = 66;

        for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
            if (i % 3 == 0) {
                x -= 18;
                y += 54;
            }

            BagSlot slot = new BagSlot(i, x, y);
            slot.slot = ScoutUtil.LEFT_POUCH_SLOT_START - i;
            scout$leftPouchSlots.add(slot);

            y -= 18;
        }

        // right pouch
        x = 152;
        y = 66;

        for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
            if (i % 3 == 0) {
                x += 18;
                y += 54;
            }

            BagSlot slot = new BagSlot(i, x, y);
            slot.slot = ScoutUtil.RIGHT_POUCH_SLOT_START - i;
            scout$rightPouchSlots.add(slot);

            y -= 18;
        }
    }
    @Inject(
            method = "quickMoveStack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void onQuickMove(Player pPlayer, int pIndex, CallbackInfoReturnable<ItemStack> cir, ItemStack itemstack) {
        InventoryMenu self = (InventoryMenu)(Object)this;
        if (ScoutUtil.isBagSlot(pIndex)) {
            Slot bagSlot = ScoutUtil.getBagSlot(pIndex, self);
            if (bagSlot != null && bagSlot.hasItem()) {
                ItemStack originalStack = bagSlot.getItem().copy();
                ItemStack resultStack = ItemStack.EMPTY;
                if (bagSlot.mayPickup(pPlayer)) {
                    int startSlot = 9;
                    int endSlot = 36;
                    resultStack = moveItemStackTo(originalStack.copy(), startSlot, endSlot, false);
                    if (!resultStack.isEmpty()) {
                        resultStack = moveItemStackTo(resultStack, 0, 9, false);
                    }
                    if (!resultStack.isEmpty()) {
                        bagSlot.set(resultStack);
                    } else {
                        bagSlot.set(ItemStack.EMPTY);
                    }
                    bagSlot.setChanged();
                    cir.setReturnValue(originalStack.isEmpty() ? ItemStack.EMPTY : originalStack);
                    return;
                }
            }
        }

        if (pIndex >= 0 && pIndex < 36) {
            ItemStack stackInSlot = self.getSlot(pIndex).getItem();
            if (!stackInSlot.isEmpty()) {
                ItemStack originalStack = stackInSlot.copy();
                boolean handled = false;
                for (BagSlot slot : getAllBagSlots(self)) {
                    if (!slot.hasItem() || canItemStacksStack(slot.getItem(), stackInSlot)) {
                        ItemStack result = tryMoveToSlot(stackInSlot.copy(), slot, pPlayer);
                        if (result.isEmpty()) {
                            self.getSlot(pIndex).set(ItemStack.EMPTY);
                            handled = true;
                            break;
                        } else if (result.getCount() < stackInSlot.getCount()) {
                            self.getSlot(pIndex).set(result);
                            handled = true;
                            break;
                        }
                    }
                }

                if (handled) {
                    self.getSlot(pIndex).setChanged();
                    cir.setReturnValue(originalStack);
                    return;
                }
            }
        }
    }

    @Unique
    private ArrayList<BagSlot> getAllBagSlots(InventoryMenu handler) {
        ArrayList<BagSlot> allSlots = new ArrayList<>(ScoutUtil.TOTAL_SLOTS);
        allSlots.addAll(scout$getSatchelSlots());
        allSlots.addAll(scout$getLeftPouchSlots());
        allSlots.addAll(scout$getRightPouchSlots());
        return allSlots;
    }

    @Unique
    private ItemStack tryMoveToSlot(ItemStack stack, BagSlot slot, Player player) {
        if (!slot.mayPlace(stack)) {
            return stack;
        }

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            if (canItemStacksStack(stack, slotStack)) {
                int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                int transfer = Math.min(stack.getCount(), maxSize - slotStack.getCount());

                if (transfer > 0) {
                    slotStack.grow(transfer);
                    stack.shrink(transfer);
                    slot.setChanged();
                    return stack;
                }
            }
            return stack;
        } else {
            int maxTransfer = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
            if (stack.getCount() <= maxTransfer) {
                slot.set(stack);
                return ItemStack.EMPTY;
            } else {
                ItemStack toInsert = stack.split(maxTransfer);
                slot.set(toInsert);
                return stack;
            }
        }
    }

    @Unique
    private boolean canItemStacksStack(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty() || !ItemStack.isSameItem(stack1, stack2)) {
            return false;
        }

        CompoundTag tag1 = stack1.getTag();
        CompoundTag tag2 = stack2.getTag();

        if (tag1 == null && tag2 != null || tag1 != null && tag2 == null) {
            return false;
        }

        return (tag1 == null || tag1.equals(tag2)) && stack1.areCapsCompatible(stack2);
    }

    @Unique
    private ItemStack moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        InventoryMenu self = (InventoryMenu)(Object)this;
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = self.slots.get(i);
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

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot1 = self.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(stack.split(stack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return stack;
    }

    @Override
    @Unique
    public final ArrayList<BagSlot> scout$getSatchelSlots() {
        return scout$satchelSlots;
    }
    @Override
    @Unique
    public final ArrayList<BagSlot> scout$getLeftPouchSlots() {
        return scout$leftPouchSlots;
    }
    @Override
    @Unique
    public final ArrayList<BagSlot> scout$getRightPouchSlots() {
        return scout$rightPouchSlots;
    }

}
