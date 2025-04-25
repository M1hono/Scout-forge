package org.pickaid.scout.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.pickaid.scout.ScoutScreenHandler;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.config.ScoutConfig;
import org.pickaid.scout.item.BaseBagItem;
import org.pickaid.scout.screen.BagSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerMixin {
    // 弓箭检索功能
    @Inject(method = "getProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles()Ljava/util/function/Predicate;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void scout$arrowsFromBags(ItemStack pShootable, CallbackInfoReturnable<ItemStack> cir) {
        Predicate predicate = ((ProjectileWeaponItem)pShootable.getItem()).getAllSupportedProjectiles();
        if (ScoutConfig.USE_ARROWS.get()) {
            var self = (Player) (Object) this;
            var leftPouch = ScoutUtil.findBagItem(self, BaseBagItem.BagType.POUCH, false);
            var rightPouch = ScoutUtil.findBagItem(self, BaseBagItem.BagType.POUCH, true);
            var satchel = ScoutUtil.findBagItem(self, BaseBagItem.BagType.SATCHEL, false);

            if (!leftPouch.isEmpty() && self.inventoryMenu instanceof ScoutScreenHandler) {
                ScoutScreenHandler screenHandler = (ScoutScreenHandler) self.inventoryMenu;
                for (BagSlot slot : screenHandler.scout$getLeftPouchSlots()) {
                    if (!slot.isActive()) continue;
                    ItemStack slotStack = slot.getItem();
                    if (!slotStack.isEmpty() && predicate.test(slotStack)) {
                        cir.setReturnValue(slotStack);
                        return;
                    }
                }
            }

            if (!rightPouch.isEmpty() && self.inventoryMenu instanceof ScoutScreenHandler) {
                ScoutScreenHandler screenHandler = (ScoutScreenHandler) self.inventoryMenu;
                for (BagSlot slot : screenHandler.scout$getRightPouchSlots()) {
                    if (!slot.isActive()) continue;
                    ItemStack slotStack = slot.getItem();
                    if (!slotStack.isEmpty() && predicate.test(slotStack)) {
                        cir.setReturnValue(slotStack);
                        return;
                    }
                }
            }

            if (!satchel.isEmpty() && self.inventoryMenu instanceof ScoutScreenHandler) {
                ScoutScreenHandler screenHandler = (ScoutScreenHandler) self.inventoryMenu;
                for (BagSlot slot : screenHandler.scout$getSatchelSlots()) {
                    if (!slot.isActive()) continue;
                    ItemStack slotStack = slot.getItem();
                    if (!slotStack.isEmpty() && predicate.test(slotStack)) {
                        cir.setReturnValue(slotStack);
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void scout$loadBagData(CompoundTag compound, CallbackInfo ci) {
        Player self = (Player)(Object)this;
        ScoutUtil.LOGGER.info("从NBT加载背包数据，玩家：{}", self.getName().getString());

        if (compound.contains("ScoutBags")) {
            CompoundTag scoutData = compound.getCompound("ScoutBags");
            ScoutUtil.LOGGER.info("找到ScoutBags数据");

            if (scoutData.contains("Satchel")) {
                loadSatchelData(self, scoutData.getCompound("Satchel"));
            }

            if (scoutData.contains("LeftPouch")) {
                loadPouchData(self, scoutData.getCompound("LeftPouch"), false);
            }

            if (scoutData.contains("RightPouch")) {
                loadPouchData(self, scoutData.getCompound("RightPouch"), true);
            }
        } else {
            ScoutUtil.LOGGER.info("未找到ScoutBags数据");
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void scout$saveBagData(CompoundTag compound, CallbackInfo ci) {
        Player self = (Player)(Object)this;
        ScoutUtil.LOGGER.info("保存背包数据，玩家：{}", self.getName().getString());

        CompoundTag scoutData = new CompoundTag();
        boolean hasSavedData = false;

        // 保存背包数据
        ItemStack satchelStack = ScoutUtil.findBagItem(self, BaseBagItem.BagType.SATCHEL, false);
        if (!satchelStack.isEmpty()) {
            CompoundTag satchelData = saveSatchelData(self);
            if (!satchelData.isEmpty()) {
                scoutData.put("Satchel", satchelData);
                hasSavedData = true;
                ScoutUtil.LOGGER.info("已保存背包数据");
            }
        }

        // 保存左口袋数据
        ItemStack leftPouchStack = ScoutUtil.findBagItem(self, BaseBagItem.BagType.POUCH, false);
        if (!leftPouchStack.isEmpty()) {
            CompoundTag pouchData = savePouchData(self, false);
            if (!pouchData.isEmpty()) {
                scoutData.put("LeftPouch", pouchData);
                hasSavedData = true;
                ScoutUtil.LOGGER.info("已保存左侧口袋数据");
            }
        }

        // 保存右口袋数据
        ItemStack rightPouchStack = ScoutUtil.findBagItem(self, BaseBagItem.BagType.POUCH, true);
        if (!rightPouchStack.isEmpty()) {
            CompoundTag pouchData = savePouchData(self, true);
            if (!pouchData.isEmpty()) {
                scoutData.put("RightPouch", pouchData);
                hasSavedData = true;
                ScoutUtil.LOGGER.info("已保存右侧口袋数据");
            }
        }

        // 如果有数据，添加到玩家NBT
        if (hasSavedData) {
            compound.put("ScoutBags", scoutData);
            ScoutUtil.LOGGER.info("已将所有背包数据保存到玩家NBT");
        } else {
            ScoutUtil.LOGGER.info("没有找到需要保存的数据");
        }
    }

    // 加载背包数据
    private void loadSatchelData(Player player, CompoundTag data) {
        if (data.contains("Items") && player.inventoryMenu instanceof ScoutScreenHandler) {
            ScoutScreenHandler screenHandler = (ScoutScreenHandler) player.inventoryMenu;
            ListTag itemsList = data.getList("Items", 10); // 10 是 CompoundTag 的 ID
            ScoutUtil.LOGGER.info("加载 {} 个背包物品", itemsList.size());

            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag itemTag = itemsList.getCompound(i);
                int slot = itemTag.getInt("Slot");

                if (slot >= 0 && slot < screenHandler.scout$getSatchelSlots().size()) {
                    if (itemTag.contains("Stack")) {
                        ItemStack stack = ItemStack.of(itemTag.getCompound("Stack"));
                        if (!stack.isEmpty()) {
                            BagSlot bagSlot = screenHandler.scout$getSatchelSlots().get(slot);
                            // 确保槽位已启用
                            bagSlot.setEnabled(true);
                            bagSlot.set(stack);
                            ScoutUtil.LOGGER.debug("加载物品到背包槽位 {}: {}", slot, stack.getDisplayName().getString());
                        }
                    }
                }
            }
        }
    }

    // 加载口袋数据
    private void loadPouchData(Player player, CompoundTag data, boolean isRight) {
        if (data.contains("Items") && player.inventoryMenu instanceof ScoutScreenHandler) {
            ScoutScreenHandler screenHandler = (ScoutScreenHandler) player.inventoryMenu;
            ListTag itemsList = data.getList("Items", 10);
            ScoutUtil.LOGGER.info("加载 {} 个{}侧口袋物品", itemsList.size(), isRight ? "右" : "左");

            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag itemTag = itemsList.getCompound(i);
                int slot = itemTag.getInt("Slot");

                if (slot >= 0) {
                    java.util.List<BagSlot> pouchSlots = isRight ?
                            screenHandler.scout$getRightPouchSlots() :
                            screenHandler.scout$getLeftPouchSlots();

                    if (slot < pouchSlots.size()) {
                        if (itemTag.contains("Stack")) {
                            ItemStack stack = ItemStack.of(itemTag.getCompound("Stack"));
                            if (!stack.isEmpty()) {
                                BagSlot bagSlot = pouchSlots.get(slot);
                                // 确保槽位已启用
                                bagSlot.setEnabled(true);
                                bagSlot.set(stack);
                                ScoutUtil.LOGGER.debug("加载物品到{}侧口袋槽位 {}: {}",
                                        isRight ? "右" : "左", slot, stack.getDisplayName().getString());
                            }
                        }
                    }
                }
            }
        }
    }

    // 保存背包数据
    // 保存背包数据
    private CompoundTag saveSatchelData(Player player) {
        if (!(player.inventoryMenu instanceof ScoutScreenHandler)) {
            ScoutUtil.LOGGER.warn("无法保存背包数据：物品栏不是ScoutScreenHandler");
            return new CompoundTag();
        }

        ScoutScreenHandler screenHandler = (ScoutScreenHandler) player.inventoryMenu;
        CompoundTag data = new CompoundTag();
        ListTag itemsList = new ListTag();
        int savedCount = 0;

        for (int i = 0; i < screenHandler.scout$getSatchelSlots().size(); i++) {
            BagSlot slot = screenHandler.scout$getSatchelSlots().get(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.put("Stack", stack.save(new CompoundTag()));
                itemsList.add(itemTag);
                savedCount++;
                ScoutUtil.LOGGER.debug("已保存背包物品到槽位 {}: {}", i, stack.getDisplayName().getString());
            }
        }

        if (savedCount > 0) {
            data.put("Items", itemsList);
            ScoutUtil.LOGGER.info("已保存 {} 个背包物品", savedCount);
        }

        return data;
    }

    // 保存口袋数据
    private CompoundTag savePouchData(Player player, boolean isRight) {
        if (!(player.inventoryMenu instanceof ScoutScreenHandler)) {
            ScoutUtil.LOGGER.warn("无法保存口袋数据：物品栏不是ScoutScreenHandler");
            return new CompoundTag();
        }

        ScoutScreenHandler screenHandler = (ScoutScreenHandler) player.inventoryMenu;
        CompoundTag data = new CompoundTag();
        ListTag itemsList = new ListTag();
        int savedCount = 0;

        java.util.List<BagSlot> pouchSlots = isRight ?
                screenHandler.scout$getRightPouchSlots() :
                screenHandler.scout$getLeftPouchSlots();

        for (int i = 0; i < pouchSlots.size(); i++) {
            BagSlot slot = pouchSlots.get(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.put("Stack", stack.save(new CompoundTag()));
                itemsList.add(itemTag);
                savedCount++;
                ScoutUtil.LOGGER.debug("已保存{}侧口袋物品到槽位 {}: {}", isRight ? "右" : "左", i, stack.getDisplayName().getString());
            }
        }

        if (savedCount > 0) {
            data.put("Items", itemsList);
            ScoutUtil.LOGGER.info("已保存 {} 个{}侧口袋物品", savedCount, isRight ? "右" : "左");
        }

        return data;
    }
}