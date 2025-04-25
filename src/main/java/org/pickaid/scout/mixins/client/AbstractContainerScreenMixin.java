package org.pickaid.scout.mixins.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.client.ScoutUtilClient;
import org.pickaid.scout.item.BaseBagItem;
import org.pickaid.scout.screen.BagSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    protected int inventoryLabelX;
    @Shadow
    protected int inventoryLabelY;
    @Shadow
    protected int imageWidth;
    @Shadow
    protected int imageHeight;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Final
    @Shadow
    protected T menu;

    @Shadow protected abstract boolean isHovering(Slot pSlot, double pMouseX, double pMouseY);

    protected AbstractContainerScreenMixin(Component pTitle) {
        super(pTitle);
    }

    // 绘制背包额外行
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V", shift = At.Shift.AFTER))
    private void scout$drawSatchelRow(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null || ScoutUtilClient.isScreenBlacklisted(this)) {
            return;
        }

        var playerInventory = this.minecraft.player.getInventory();

        // 查找背包物品
        ItemStack backStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.SATCHEL, false);
        if (!backStack.isEmpty()) {
            BaseBagItem bagItem = (BaseBagItem) backStack.getItem();
            int slots = bagItem.getSlotCount();

            // 找到热键栏第一个槽位作为参考点
            var _hotbarSlot1 = menu.slots.stream()
                    .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 0)
                    .findFirst();

            Slot hotbarSlot1 = _hotbarSlot1.orElse(null);
            if (hotbarSlot1 == null) return;

            // 计算相对于容器的位置
            int x = leftPos + hotbarSlot1.x - 8;
            int y = topPos + hotbarSlot1.y + 22;

            pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            // 绘制顶部
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 32, 176, 4);
            y += 4;

            int u = 0;
            int v = 36;

            // 按行绘制槽位
            for (int slot = 0; slot < slots; slot++) {
                if (slot % 9 == 0) {
                    x = leftPos + hotbarSlot1.x - 8;
                    u = 0;
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, u, v, 7, 18);
                    x += 7;
                    u += 7;
                }

                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, u, v, 18, 18);

                x += 18;
                u += 18;

                if ((slot + 1) % 9 == 0) {
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, u, v, 7, 18);
                    y += 18;
                }
            }

            // 绘制底部
            x = leftPos + hotbarSlot1.x - 8;
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 54, 176, 7);
        }
    }

    // 绘制旁边袋的槽位
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V", remap = false))
    private void scout$drawPouchSlots(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null || ScoutUtilClient.isScreenBlacklisted(this)) {
            return;
        }

        var playerInventory = this.minecraft.player.getInventory();

        // 绘制左侧袋
        ItemStack leftPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, false);
        if (!leftPouchStack.isEmpty()) {
            BaseBagItem bagItem = (BaseBagItem) leftPouchStack.getItem();
            int slots = bagItem.getSlotCount();
            int columns = (int) Math.ceil(slots / 3.0);

            // 找到物品栏左上角槽位作为参考点
            var _topLeftSlot = menu.slots.stream()
                    .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 9)
                    .findFirst();

            Slot topLeftSlot = _topLeftSlot.orElse(null);
            if (topLeftSlot == null) return;

            // 计算相对于容器的位置
            int x = leftPos + topLeftSlot.x - 8;
            int y = topPos + topLeftSlot.y + 53;

            pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            // 绘制右下角
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 18, 25, 7, 7);

            // 绘制底边
            for (int i = 0; i < columns; i++) {
                x -= 11;
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 25, 11, 7);
            }
            if (columns > 1) {
                for (int i = 0; i < columns - 1; i++) {
                    x -= 7;
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 25, 7, 7);
                }
            }

            // 绘制左下角
            x -= 7;
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 25, 7, 7);

            // 绘制槽位背景
            x = leftPos + topLeftSlot.x - 1;
            y -= 54;
            for (int slot = 0; slot < slots; slot++) {
                if (slot % 3 == 0) {
                    x -= 18;
                    y += 54;
                }
                y -= 18;
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 7, 18, 18);
            }

            // 绘制左边缘
            x -= 7;
            y += 54;
            for (int i = 0; i < 3; i++) {
                y -= 18;
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 7, 7, 18);
            }

            // 绘制顶边
            x = leftPos + topLeftSlot.x - 8;
            y -= 7;
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 18, 0, 7, 7);
            for (int i = 0; i < columns; i++) {
                x -= 11;
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 0, 11, 7);
            }
            if (columns > 1) {
                for (int i = 0; i < columns - 1; i++) {
                    x -= 7;
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 0, 7, 7);
                }
            }
            x -= 7;
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 0, 7, 7);
        }

        // 绘制右侧袋
        ItemStack rightPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, true);
        if (!rightPouchStack.isEmpty()) {
            BaseBagItem bagItem = (BaseBagItem) rightPouchStack.getItem();
            int slots = bagItem.getSlotCount();
            int columns = (int) Math.ceil(slots / 3.0);

            // 找到物品栏右上角槽位作为参考点
            var _topRightSlot = menu.slots.stream()
                    .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 17)
                    .findFirst();

            Slot topRightSlot = _topRightSlot.orElse(null);
            if (topRightSlot == null) return;

            // 计算相对于容器的位置
            int x = leftPos + topRightSlot.x + 17;
            int y = topPos + topRightSlot.y + 53;

            pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            // 绘制左下角
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 25, 25, 7, 7);
            x += 7;

            // 绘制底边
            for (int i = 0; i < columns; i++) {
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 25, 11, 7);
                x += 11;
            }
            if (columns > 1) {
                for (int i = 0; i < columns - 1; i++) {
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 25, 7, 7);
                    x += 7;
                }
            }

            // 绘制右下角
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 32, 25, 7, 7);

            // 绘制槽位背景
            x = leftPos + topRightSlot.x + 1;
            y -= 54;
            for (int slot = 0; slot < slots; slot++) {
                if (slot % 3 == 0) {
                    x += 18;
                    y += 54;
                }
                y -= 18;
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 7, 18, 18);
            }

            // 绘制右边缘
            x += 18;
            y += 54;
            for (int i = 0; i < 3; i++) {
                y -= 18;
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 32, 7, 7, 18);
            }

            // 绘制顶边
            x = leftPos + topRightSlot.x + 17;
            y -= 7;
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 25, 0, 7, 7);
            x += 7;
            for (int i = 0; i < columns; i++) {
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 0, 11, 7);
                x += 11;
            }
            if (columns > 1) {
                for (int i = 0; i < columns - 1; i++) {
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 0, 7, 7);
                    x += 7;
                }
            }
            pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 32, 0, 7, 7);
        }
    }

    // 调整点击区域检测，确保额外槽位也能被点击
    @Inject(method = "hasClickedOutside", at = @At("TAIL"), cancellable = true)
    private void scout$adjustOutsideBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (this.minecraft == null || this.minecraft.player == null || ScoutUtilClient.isScreenBlacklisted(this)) {
            return;
        }

        // 检查背包额外行区域
        ItemStack backStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.SATCHEL, false);
        if (!backStack.isEmpty()) {
            BaseBagItem bagItem = (BaseBagItem) backStack.getItem();
            int slots = bagItem.getSlotCount();
            int rows = (int) Math.ceil(slots / 9.0);

            // 如果点击在背包额外行区域内，则不是"外部"点击
            if (mouseY >= top + this.imageHeight &&
                    mouseY < top + this.imageHeight + 8 + (18 * rows) &&
                    mouseX >= left &&
                    mouseX < left + this.imageWidth) {
                callbackInfo.setReturnValue(false);
            }
        }

        // 检查左侧袋区域
        ItemStack leftPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, false);
        if (!leftPouchStack.isEmpty()) {
            BaseBagItem bagItem = (BaseBagItem) leftPouchStack.getItem();
            int slots = bagItem.getSlotCount();
            int columns = (int) Math.ceil(slots / 3.0);

            // 如果点击在左侧袋区域内，则不是"外部"点击
            if (mouseX >= left - (columns * 18) - 7 &&
                    mouseX < left &&
                    mouseY >= top + this.imageHeight - 90 &&
                    mouseY < top + this.imageHeight - 22) {
                callbackInfo.setReturnValue(false);
            }
        }

        // 检查右侧袋区域
        ItemStack rightPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, true);
        if (!rightPouchStack.isEmpty()) {
            BaseBagItem bagItem = (BaseBagItem) rightPouchStack.getItem();
            int slots = bagItem.getSlotCount();
            int columns = (int) Math.ceil(slots / 3.0);

            // 如果点击在右侧袋区域内，则不是"外部"点击
            if (mouseX >= left + this.imageWidth &&
                    mouseX < left + this.imageWidth + (columns * 18) + 7 &&
                    mouseY >= top + this.imageHeight - 90 &&
                    mouseY < top + this.imageHeight - 22) {
                callbackInfo.setReturnValue(false);
            }
        }
    }

    // 绘制自定义槽位内的物品
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    public void scout$drawBagSlots(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float pPartialTick, CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null || ScoutUtilClient.isScreenBlacklisted(this)) {
            return;
        }

        // 从高到低索引渲染背包槽位，确保正确的重叠顺序
        for (int i = ScoutUtil.SATCHEL_SLOT_START; i > ScoutUtil.BAG_SLOTS_END; i--) {
            BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(i, this.minecraft.player.inventoryMenu);
            if (slot != null && slot.isActive()) {
                // 绘制槽位和物品
                renderBagSlot(pGuiGraphics, slot);

                // 检查鼠标是否悬停在该槽位上
                if (isPointOverSlot(slot, mouseX, mouseY)) {
                    this.hoveredSlot = slot;
                    drawSlotHighlight(pGuiGraphics, slot.getX() + leftPos, slot.getY() + topPos, 0);
                }
            }
        }
    }

    // 修复槽位悬停检测
    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    public void scout$fixSlotPos(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if (slot instanceof BagSlot bagSlot) {
            // 对于BagSlot使用特定位置进行悬停检测
            cir.setReturnValue(isPointWithinBounds(bagSlot.getX() + leftPos, bagSlot.getY() + topPos, 16, 16, pointX, pointY));
        }
    }

    // 修复槽位查找
    @Inject(method = "findSlot", at = @At("RETURN"), cancellable = true)
    public void scout$addSlots(double x, double y, CallbackInfoReturnable<Slot> cir) {
        if (this.minecraft == null || this.minecraft.player == null || ScoutUtilClient.isScreenBlacklisted(this)) {
            return;
        }

        // 尝试在点击位置找到背包槽位
        for (int i = ScoutUtil.SATCHEL_SLOT_START; i > ScoutUtil.BAG_SLOTS_END; i--) {
            BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(i, this.minecraft.player.inventoryMenu);
            if (slot != null && slot.isActive() && isPointOverSlot(slot, x, y)) {
                cir.setReturnValue(slot);
                return;
            }
        }
    }

    // 修复槽位渲染
    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void scout$renderBagSlot(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
        if (pSlot instanceof BagSlot bagSlot) {
            // 特殊渲染背包槽位
            renderBagSlot(pGuiGraphics, bagSlot);
            ci.cancel();
        }
    }

    // 渲染背包槽位及其中的物品
    private void renderBagSlot(GuiGraphics graphics, BagSlot bagSlot) {
        if (bagSlot == null || !bagSlot.isActive()) return;

        int x = bagSlot.getX() + leftPos;
        int y = bagSlot.getY() + topPos;

        // 绘制槽位背景
        graphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 7, 16, 16);

        // 如果槽位有物品，则绘制物品
        ItemStack itemstack = bagSlot.getItem();
        if (!itemstack.isEmpty()) {
            graphics.renderItem(itemstack, x, y);
            graphics.renderItemDecorations(this.font, itemstack, x, y, null);
        }
    }

    // 绘制槽位高亮
    private static void drawSlotHighlight(GuiGraphics graphics, int x, int y, int z) {
        graphics.fillGradient(x, y, x + 16, y + 16, 0x80FFFFFF, 0x80FFFFFF, z);
    }

    // 检查点是否在槽位上
    private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
        if (slot == null) return false;

        if (slot instanceof BagSlot bagSlot) {
            return isPointWithinBounds(bagSlot.getX() + leftPos, bagSlot.getY() + topPos, 16, 16, pointX, pointY);
        } else {
            return isHovering(slot, pointX, pointY);
        }
    }

    // 检查点是否在指定区域内
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return pointX >= (double)(x - 1) &&
                pointX < (double)(x + width + 1) &&
                pointY >= (double)(y - 1) &&
                pointY < (double)(y + height + 1);
    }

    @Override
    public T getMenu() {
        return menu;
    }
}