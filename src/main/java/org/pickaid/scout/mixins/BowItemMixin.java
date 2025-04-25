package org.pickaid.scout.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.config.ScoutConfig;
import org.pickaid.scout.item.BaseBagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V") , locals = LocalCapture.CAPTURE_FAILHARD)
    public void scout$arrowsFromBags(ItemStack itemStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft, CallbackInfo ci, Player playerEntity, boolean bl, ItemStack itemstack, int s, float f, boolean flag1) {
        if (ScoutConfig.USE_ARROWS.get()) {
            boolean infinity = bl && itemStack.is(Items.ARROW);
            boolean hasRan = false;

            if (!infinity && !playerEntity.getAbilities().invulnerable) {
                var leftPouch = ScoutUtil.findBagItem(playerEntity, BaseBagItem.BagType.POUCH, false);
                var rightPouch = ScoutUtil.findBagItem(playerEntity, BaseBagItem.BagType.POUCH, true);
                var satchel = ScoutUtil.findBagItem(playerEntity, BaseBagItem.BagType.SATCHEL, false);

                if (!leftPouch.isEmpty()) {
                    BaseBagItem item = (BaseBagItem) leftPouch.getItem();
                    var inv = item.getInventory(leftPouch);

                    for(int i = 0; i < inv.getContainerSize(); ++i) {
                        ItemStack invStack = inv.getItem(i);
                        if (invStack.equals(itemStack)) {
                            invStack.shrink(1);
                            if (invStack.isEmpty()) {
                                inv.setItem(i, ItemStack.EMPTY);
                            }
                            inv.setChanged();
                            hasRan = true;
                            break;
                        }
                    }
                }
                if (!rightPouch.isEmpty() && !hasRan) {
                    BaseBagItem item = (BaseBagItem) rightPouch.getItem();
                    var inv = item.getInventory(rightPouch);

                    for(int i = 0; i < inv.getContainerSize(); ++i) {
                        ItemStack invStack = inv.getItem(i);
                        if (invStack.equals(itemStack)) {
                            invStack.shrink(1);
                            if (invStack.isEmpty()) {
                                inv.setItem(i, ItemStack.EMPTY);
                            }
                            inv.setChanged();
                            hasRan = true;
                            break;
                        }
                    }
                }
                if (!satchel.isEmpty() && !hasRan) {
                    BaseBagItem item = (BaseBagItem) satchel.getItem();
                    var inv = item.getInventory(satchel);

                    for(int i = 0; i < inv.getContainerSize(); ++i) {
                        ItemStack invStack = inv.getItem(i);
                        if (invStack.equals(itemStack)) {
                            invStack.shrink(1);
                            if (invStack.isEmpty()) {
                                inv.setItem(i, ItemStack.EMPTY);
                            }
                            inv.setChanged();
                            hasRan = true;
                            break;
                        }
                    }
                }
            }
        }
    }
}
