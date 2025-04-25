package org.pickaid.scout.mixins.server;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.pickaid.scout.server.ScoutUtilServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @Redirect(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack scout$fixQuickMove(AbstractContainerMenu instance, Player player, int i) {
        ScoutUtilServer.setCurrentPlayer(player);
        ItemStack ret = instance.quickMoveStack(player, i);
        ScoutUtilServer.clearCurrentPlayer();
        return ret;
    }
}
