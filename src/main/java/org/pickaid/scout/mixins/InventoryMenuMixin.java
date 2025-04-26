package org.pickaid.scout.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.pickaid.scout.ScoutScreenHandler;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.item.BaseBagItem;
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
    @Unique
    public final ArrayList<BagSlot> scout$satchelSlots = new ArrayList<BagSlot>(ScoutUtil.MAX_SATCHEL_SLOTS);
    @Unique
    public final ArrayList<BagSlot> scout$leftPouchSlots = new ArrayList<BagSlot>(ScoutUtil.MAX_POUCH_SLOTS);
    @Unique
    public final ArrayList<BagSlot> scout$rightPouchSlots = new ArrayList<BagSlot>(ScoutUtil.MAX_POUCH_SLOTS);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void scout$addSlots(Inventory pPlayerInventory, boolean pActive, Player pOwner, CallbackInfo ci) {
        InventoryMenu self = (InventoryMenu)(Object)this;
        ScoutUtil.LOGGER.info("正在初始化物品栏额外槽位，玩家：{}", pOwner.getName().getString());

        // 使用从ScoutUtil获取的持久化容器
        SimpleContainer satchelContainer = ScoutUtil.getSatchelContainer(pOwner);
        SimpleContainer leftPouchContainer = ScoutUtil.getLeftPouchContainer(pOwner);
        SimpleContainer rightPouchContainer = ScoutUtil.getRightPouchContainer(pOwner);

        // 清空现有槽位列表，避免重复添加
        scout$satchelSlots.clear();
        scout$leftPouchSlots.clear();
        scout$rightPouchSlots.clear();

        // 创建背包额外行槽位
        Slot hotbarSlot = self.slots.get(0);
        int baseX = hotbarSlot.x;
        int baseY = hotbarSlot.y + 58;

        ScoutUtil.LOGGER.debug("创建背包槽位，基准位置：({}, {})", baseX, baseY);
        for (int i = 0; i < ScoutUtil.MAX_SATCHEL_SLOTS; i++) {
            int row = i / 9;
            int col = i % 9;
            int x = baseX + col * 18;
            int y = baseY + row * 18;

            BagSlot slot = new BagSlot(i, x, y);
            slot.setInventory(satchelContainer);
            slot.slot = ScoutUtil.SATCHEL_SLOT_START - i;
            slot.setEnabled(true);
            scout$satchelSlots.add(slot);
            ScoutUtil.LOGGER.debug("创建背包槽位 {}：位置=({}, {})", i, x, y);
        }

        // 创建左侧口袋槽位
        Slot invSlot = self.slots.get(9);
        baseX = invSlot.x - 27;
        baseY = invSlot.y;

        ScoutUtil.LOGGER.debug("创建左侧口袋槽位，基准位置：({}, {})", baseX, baseY);
        for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
            int col = i / 3;
            int row = i % 3;
            int x = baseX - col * 18;
            int y = baseY + row * 18;

            BagSlot slot = new BagSlot(i, x, y);
            slot.setInventory(leftPouchContainer);
            slot.slot = ScoutUtil.LEFT_POUCH_SLOT_START - i;
            slot.setEnabled(true);
            scout$leftPouchSlots.add(slot);
            ScoutUtil.LOGGER.debug("创建左侧口袋槽位 {}：位置=({}, {})", i, x, y);
        }

        // 创建右侧口袋槽位
        Slot invRightSlot = self.slots.get(17);
        baseX = invRightSlot.x + 27;
        baseY = invRightSlot.y;

        ScoutUtil.LOGGER.debug("创建右侧口袋槽位，基准位置：({}, {})", baseX, baseY);
        for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS; i++) {
            int col = i / 3;
            int row = i % 3;
            int x = baseX + col * 18;
            int y = baseY + row * 18;

            BagSlot slot = new BagSlot(i, x, y);
            slot.setInventory(rightPouchContainer);
            slot.slot = ScoutUtil.RIGHT_POUCH_SLOT_START - i;
            slot.setEnabled(true);
            scout$rightPouchSlots.add(slot);
            ScoutUtil.LOGGER.debug("创建右侧口袋槽位 {}：位置=({}, {})", i, x, y);
        }

        // 根据玩家装备情况更新槽位状态
        updateBagSlots(pOwner);

        // 生成诊断信息
        ScoutUtil.LOGGER.info("物品栏初始化完成：背包槽位={}, 左侧口袋槽位={}, 右侧口袋槽位={}",
                scout$satchelSlots.size(), scout$leftPouchSlots.size(), scout$rightPouchSlots.size());
    }

    @Unique
    private void updateBagSlots(Player player) {
        ScoutUtil.LOGGER.debug("更新槽位状态，根据玩家装备");

        // 检查背包装备
        ItemStack satchelStack = ScoutUtil.findBagItem(player, BaseBagItem.BagType.SATCHEL, false);
        boolean hasSatchel = !satchelStack.isEmpty();

        // 检查左侧口袋装备
        ItemStack leftPouchStack = ScoutUtil.findBagItem(player, BaseBagItem.BagType.POUCH, false);
        boolean hasLeftPouch = !leftPouchStack.isEmpty();

        // 检查右侧口袋装备
        ItemStack rightPouchStack = ScoutUtil.findBagItem(player, BaseBagItem.BagType.POUCH, true);
        boolean hasRightPouch = !rightPouchStack.isEmpty();

        ScoutUtil.LOGGER.debug("装备检查结果：背包={}, 左侧口袋={}, 右侧口袋={}",
                hasSatchel, hasLeftPouch, hasRightPouch);

        // 更新背包槽位启用状态
        for (BagSlot slot : scout$satchelSlots) {
            slot.setEnabled(hasSatchel);
        }

        // 更新左侧口袋槽位启用状态
        for (BagSlot slot : scout$leftPouchSlots) {
            slot.setEnabled(hasLeftPouch);
        }

        // 更新右侧口袋槽位启用状态
        for (BagSlot slot : scout$rightPouchSlots) {
            slot.setEnabled(hasRightPouch);
        }
    }

    @Inject(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onQuickMove(Player pPlayer, int pIndex, CallbackInfoReturnable<ItemStack> cir, ItemStack itemstack) {
        InventoryMenu self = (InventoryMenu)(Object)this;
        ScoutUtil.LOGGER.debug("处理快速移动，槽位索引：{}", pIndex);

        // 更新槽位状态
        updateBagSlots(pPlayer);

        // 从背包槽位移动到物品栏
        if (ScoutUtil.isBagSlot(pIndex)) {
            Slot bagSlot = ScoutUtil.getBagSlot(pIndex, self);
            if (bagSlot != null && bagSlot.hasItem()) {
                ItemStack originalStack = bagSlot.getItem().copy();
                ScoutUtil.LOGGER.debug("从背包槽位移动物品：{}", originalStack.getDisplayName().getString());

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

        // 从物品栏移动到背包槽位
        if (pIndex >= 0 && pIndex < 36) {
            ItemStack stackInSlot = self.getSlot(pIndex).getItem();
            if (!stackInSlot.isEmpty()) {
                ItemStack originalStack = stackInSlot.copy();
                ScoutUtil.LOGGER.debug("从物品栏移动物品：{}", originalStack.getDisplayName().getString());

                boolean handled = false;

                // 尝试所有激活的背包槽位
                for (BagSlot slot : getAllBagSlots(self)) {
                    if (!slot.isActive()) continue;

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
            ScoutUtil.LOGGER.debug("物品 {} 无法放入槽位", stack.getDisplayName().getString());
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
                    ScoutUtil.LOGGER.debug("合并了 {} 个物品到槽位中的 {}", transfer, slotStack.getDisplayName().getString());
                    return stack;
                }
            }
            return stack;
        } else {
            int maxTransfer = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
            if (stack.getCount() <= maxTransfer) {
                slot.set(stack);
                ScoutUtil.LOGGER.debug("整组放入槽位：{}", stack.getDisplayName().getString());
                return ItemStack.EMPTY;
            } else {
                ItemStack toInsert = stack.split(maxTransfer);
                slot.set(toInsert);
                ScoutUtil.LOGGER.debug("部分放入槽位 {} 个：{}", maxTransfer, toInsert.getDisplayName().getString());
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