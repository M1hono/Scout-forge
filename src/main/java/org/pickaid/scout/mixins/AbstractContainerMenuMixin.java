package org.pickaid.scout.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.screen.BagSlot;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract ItemStack getCarried();

    @Shadow public abstract void setCarried(ItemStack stack);

    @Shadow public abstract Slot getSlot(int pSlot);

    @Shadow protected abstract boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_);

    @Shadow public abstract void broadcastChanges();

    // 修复双击快速移动
    @Inject(method = "doClick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;getCarried()Lnet/minecraft/world/item/ItemStack;", ordinal = 11), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void scout$fixDoubleClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci, Inventory inventory, Slot slot3) {
        var cursorStack = this.getCarried();
        if (!cursorStack.isEmpty() && (!slot3.hasItem() || !slot3.mayPickup(player))) {
            var bagSlots = ScoutUtil.getAllBagSlots(player.inventoryMenu);
            if (bagSlots.isEmpty()) return;

            var k = button == 0 ? 0 : bagSlots.size() - 1;
            var o = button == 0 ? 1 : -1;

            for (int n = 0; n < 2; ++n) {
                for (int p = k; p >= 0 && p < bagSlots.size() && cursorStack.getCount() < cursorStack.getMaxStackSize(); p += o) {
                    Slot slot4 = bagSlots.get(p);
                    if (slot4 != null && slot4.hasItem() && AbstractContainerMenu.canItemQuickReplace(slot4, cursorStack, true)
                            && slot4.mayPickup(player) && scout_forge$canTakeItemForPickAll(cursorStack, slot4)) {
                        ItemStack itemStack6 = slot4.getItem();
                        if (n != 0 || itemStack6.getCount() != itemStack6.getMaxStackSize()) {
                            int count = Math.min(itemStack6.getCount(), cursorStack.getMaxStackSize() - cursorStack.getCount());
                            ItemStack itemStack7 = slot4.remove(count);
                            cursorStack.grow(itemStack7.getCount());
                            if (itemStack7.getCount() > 0) {
                                slot4.setChanged();
                            }
                        }
                    }
                }
            }

            // 更新持有物品
            setCarried(cursorStack);
        }
    }

    // 完全重写负数槽位点击处理
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    public void scout$fixNegativeSlotClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (slotId < 0 && ScoutUtil.isBagSlot(slotId)) {
            // 获取背包槽位
            BagSlot bagSlot = (BagSlot) ScoutUtil.getBagSlot(slotId, player.inventoryMenu);
            if (bagSlot == null) return;

            // 确保槽位激活
            if (!bagSlot.isActive()) return;

            AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
            ItemStack cursorStack = self.getCarried();

            switch (clickType) {
                case PICKUP:
                    if (button == 0) { // 左键点击
                        if (cursorStack.isEmpty()) {
                            // 光标为空，拿取物品
                            if (bagSlot.hasItem()) {
                                ItemStack bagItem = bagSlot.getItem();
                                self.setCarried(bagItem);
                                bagSlot.set(ItemStack.EMPTY);
                                bagSlot.setChanged();
                            }
                        } else {
                            // 光标有物品，尝试放置
                            if (!bagSlot.hasItem()) {
                                // 槽位为空，直接放置
                                if (bagSlot.mayPlace(cursorStack)) {
                                    bagSlot.set(cursorStack);
                                    self.setCarried(ItemStack.EMPTY);
                                    bagSlot.setChanged();
                                }
                            } else if (bagSlot.mayPlace(cursorStack)) {
                                // 槽位有物品，尝试合并
                                ItemStack bagItem = bagSlot.getItem();
                                if (ItemStack.isSameItemSameTags(cursorStack, bagItem)) {
                                    int maxStackSize = Math.min(bagSlot.getMaxStackSize(), bagItem.getMaxStackSize());
                                    int spaceLeft = maxStackSize - bagItem.getCount();

                                    if (spaceLeft > 0) {
                                        int transferAmount = Math.min(spaceLeft, cursorStack.getCount());
                                        bagItem.grow(transferAmount);
                                        cursorStack.shrink(transferAmount);

                                        if (cursorStack.isEmpty()) {
                                            self.setCarried(ItemStack.EMPTY);
                                        }

                                        bagSlot.setChanged();
                                    }
                                }
                            }
                        }
                    } else if (button == 1) { // 右键点击
                        if (cursorStack.isEmpty()) {
                            // 光标为空，拿取一半物品
                            if (bagSlot.hasItem()) {
                                ItemStack bagItem = bagSlot.getItem();
                                int amount = (bagItem.getCount() + 1) / 2;
                                ItemStack takenStack = bagSlot.remove(amount);
                                self.setCarried(takenStack);
                                bagSlot.setChanged();
                            }
                        } else {
                            // 光标有物品，尝试放置一个
                            if (bagSlot.mayPlace(cursorStack)) {
                                if (!bagSlot.hasItem()) {
                                    // 槽位为空，放置一个
                                    ItemStack oneItem = cursorStack.copy();
                                    oneItem.setCount(1);
                                    bagSlot.set(oneItem);
                                    cursorStack.shrink(1);

                                    if (cursorStack.isEmpty()) {
                                        self.setCarried(ItemStack.EMPTY);
                                    }

                                    bagSlot.setChanged();
                                } else {
                                    // 槽位有物品，尝试合并一个
                                    ItemStack bagItem = bagSlot.getItem();
                                    if (ItemStack.isSameItemSameTags(cursorStack, bagItem)) {
                                        int maxStackSize = Math.min(bagSlot.getMaxStackSize(), bagItem.getMaxStackSize());

                                        if (bagItem.getCount() < maxStackSize) {
                                            bagItem.grow(1);
                                            cursorStack.shrink(1);

                                            if (cursorStack.isEmpty()) {
                                                self.setCarried(ItemStack.EMPTY);
                                            }

                                            bagSlot.setChanged();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;

                case QUICK_MOVE:
                    // 处理Shift+点击，将物品移到主物品栏
                    if (bagSlot.hasItem()) {
                        ItemStack bagItem = bagSlot.getItem().copy();
                        ItemStack originalStack = bagItem.copy();

                        // 尝试移动到主物品栏 (9-35) 和热键栏 (0-8)
                        if (moveItemStackTo(bagItem, 0, 36, true)) {
                            // 成功移动，更新背包槽位
                            if (bagItem.isEmpty()) {
                                bagSlot.set(ItemStack.EMPTY);
                            } else {
                                // 只移动了部分物品
                                bagSlot.set(bagItem);
                            }
                            bagSlot.setChanged();
                        }
                    }
                    break;

                case SWAP:
                    // 处理数字键切换，与热键栏交换物品
                    if (button >= 0 && button < 9) {
                        Slot hotbarSlot = this.slots.get(button);
                        ItemStack hotbarStack = hotbarSlot.getItem();
                        ItemStack bagStack = bagSlot.getItem();

                        if (bagSlot.mayPlace(hotbarStack) && hotbarSlot.mayPlace(bagStack)) {
                            bagSlot.set(hotbarStack);
                            hotbarSlot.set(bagStack);
                            bagSlot.setChanged();
                            hotbarSlot.setChanged();
                        }
                    }
                    break;

                default:
                    // 其他点击类型不处理
                    return;
            }

            // 广播更改
            self.broadcastChanges();

            // 取消原始处理
            ci.cancel();
        }
    }

    // 修复槽位索引问题
    @Redirect(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"))
    public Object scout$fixSlotIndexing(NonNullList<Slot> self, int index, int slotId, int button, ClickType clickType, Player player) {
        if (ScoutUtil.isBagSlot(index)) {
            Slot bagSlot = ScoutUtil.getBagSlot(index, player.inventoryMenu);
            if (bagSlot != null) {
                return bagSlot;
            }
        }
        return self.get(index);
    }

    // 修复moveItemStackTo方法，支持背包槽位
    @Inject(method = "moveItemStackTo", at = @At("HEAD"), cancellable = true)
    private void scout$fixMoveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) return;

        AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
        Player player = null;

        // 尝试找出玩家
        for (var i = 0; i < self.slots.size(); i++) {
            Slot slot = self.slots.get(i);
            if (slot.container instanceof Inventory) {
                player = ((Inventory)slot.container).player;
                break;
            }
        }

        if (player == null) return;

        // 如果涉及背包槽位，使用自定义逻辑
        if (ScoutUtil.isBagSlot(startIndex) || ScoutUtil.isBagSlot(endIndex) ||
                (startIndex == 0 && endIndex == 0)) { // 特殊情况处理

            boolean result = moveItemStackToBagSlots(stack, startIndex, endIndex, reverseDirection, player);
            cir.setReturnValue(result);
        }
    }

    // 优化的背包物品移动逻辑
    private boolean moveItemStackToBagSlots(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, Player player) {
        boolean moved = false;
        List<Slot> targetSlots;

        // 确定目标槽位列表
        if (startIndex >= 0 && endIndex >= 0) {
            // 常规物品栏到背包槽位
            targetSlots = ScoutUtil.getAllBagSlots(player.inventoryMenu);
            if (targetSlots.isEmpty()) return false;
        } else if (ScoutUtil.isBagSlot(startIndex)) {
            // 背包槽位到常规物品栏
            AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
            targetSlots = self.slots;
        } else {
            // 常规物品栏到背包槽位
            targetSlots = ScoutUtil.getAllBagSlots(player.inventoryMenu);
            if (targetSlots.isEmpty()) return false;
        }

        // 首先尝试合并到相同类型的物品堆
        for (Slot slot : targetSlots) {
            if (slot == null || !slot.isActive() || !slot.mayPlace(stack)) continue;

            ItemStack slotStack = slot.getItem();
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, stack)) {
                int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                int spaceLeft = maxSize - slotStack.getCount();

                if (spaceLeft > 0) {
                    int toTransfer = Math.min(spaceLeft, stack.getCount());
                    slotStack.grow(toTransfer);
                    stack.shrink(toTransfer);
                    slot.setChanged();
                    moved = true;

                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        // 然后尝试放入空槽位
        for (Slot slot : targetSlots) {
            if (slot == null || !slot.isActive() || !slot.mayPlace(stack)) continue;

            if (slot.getItem().isEmpty()) {
                int maxTransfer = Math.min(slot.getMaxStackSize(), stack.getCount());
                ItemStack toPlace = stack.copy();
                toPlace.setCount(maxTransfer);
                slot.set(toPlace);
                stack.shrink(maxTransfer);
                slot.setChanged();
                moved = true;

                if (stack.isEmpty()) {
                    return true;
                }
            }
        }

        return moved;
    }

    // 判断物品是否可以放入槽位
    private boolean scout_forge$canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.mayPlace(stack) && slot.isActive();
    }
}