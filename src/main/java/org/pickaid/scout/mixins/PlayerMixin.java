package org.pickaid.scout.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.config.ScoutConfig;
import org.pickaid.scout.item.BaseBagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "getProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles()Ljava/util/function/Predicate;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public  void scout$arrowsFromBags(ItemStack pShootable, CallbackInfoReturnable<ItemStack> cir) {
        Predicate predicate = ((ProjectileWeaponItem)pShootable.getItem()).getAllSupportedProjectiles();
        if (ScoutConfig.USE_ARROWS.get()) {
            var self = (Player) (Object) this;
            var leftPouch = ScoutUtil.findBagItem(self, BaseBagItem.BagType.POUCH, false);
            var rightPouch = ScoutUtil.findBagItem(self, BaseBagItem.BagType.POUCH, true);
            var satchel = ScoutUtil.findBagItem(self, BaseBagItem.BagType.SATCHEL, false);

            if (!leftPouch.isEmpty()) {
                BaseBagItem item = (BaseBagItem) leftPouch.getItem();
                var inv = item.getInventory(leftPouch);

                for(int i = 0; i < inv.getContainerSize(); ++i) {
                    ItemStack invStack = inv.getItem(i);
                    if (predicate.test(invStack)) {
                        cir.setReturnValue(invStack);
                    }
                }
            }
            if (!rightPouch.isEmpty()) {
                BaseBagItem item = (BaseBagItem) rightPouch.getItem();
                var inv = item.getInventory(rightPouch);

                for(int i = 0; i < inv.getContainerSize(); ++i) {
                    ItemStack invStack = inv.getItem(i);
                    if (predicate.test(invStack)) {
                        cir.setReturnValue(invStack);
                    }
                }
            }
            if (!satchel.isEmpty()) {
                BaseBagItem item = (BaseBagItem) satchel.getItem();
                var inv = item.getInventory(satchel);

                for(int i = 0; i < inv.getContainerSize(); ++i) {
                    ItemStack invStack = inv.getItem(i);
                    if (predicate.test(invStack)) {
                        cir.setReturnValue(invStack);
                    }
                }
            }
        }
    }
}
