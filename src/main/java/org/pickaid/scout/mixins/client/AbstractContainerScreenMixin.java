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

    protected AbstractContainerScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void scout$drawSatchelRow(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        if (this.minecraft != null && this.minecraft.player != null && !ScoutUtilClient.isScreenBlacklisted(this)) {
            var playerInventory = this.minecraft.player.getInventory();

            ItemStack backStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.SATCHEL, false);
            if (!backStack.isEmpty()) {
                BaseBagItem bagItem = (BaseBagItem) backStack.getItem();
                int slots = bagItem.getSlotCount();

                var _hotbarSlot1 = menu.slots.stream().filter(slot->slot.container.equals(playerInventory) && slot.getContainerSlot() == 0).findFirst();
                Slot hotbarSlot1 = _hotbarSlot1.isPresent() ? _hotbarSlot1.get() : null;
                if (hotbarSlot1 != null) {
                    int x = this.inventoryLabelX + hotbarSlot1.x - 8;
                    int y = this.inventoryLabelY + hotbarSlot1.y + 22;

                    pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 32, 176, 4);
                    y += 4;

                    int u = 0;
                    int v = 36;

                    for (int slot = 0; slot < slots; slot++) {
                        if (slot % 9 == 0) {
                            x = this.inventoryLabelX + hotbarSlot1.x - 8;
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

                    x = this.inventoryLabelX + hotbarSlot1.x - 8;
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 54, 176, 7);

                    pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V", remap = false))
    private void scout$drawPouchSlots(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        if (this.minecraft != null && this.minecraft.player != null && !ScoutUtilClient.isScreenBlacklisted(this)) {
            var playerInventory = this.minecraft.player.getInventory();

            ItemStack leftPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, false);
            if (!leftPouchStack.isEmpty()) {
                BaseBagItem bagItem = (BaseBagItem) leftPouchStack.getItem();
                int slots = bagItem.getSlotCount();
                int columns = (int) Math.ceil(slots / 3.0);

                var _topLeftSlot = menu.slots.stream().filter(slot->slot.container.equals(playerInventory) && slot.getContainerSlot() == 9).findFirst();
                Slot topLeftSlot = _topLeftSlot.isPresent() ? _topLeftSlot.get() : null;
                if (topLeftSlot != null) {
                    int x = this.inventoryLabelX + topLeftSlot.x - 8;
                    int y = this.inventoryLabelY + topLeftSlot.y + 53;

                    pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 18, 25, 7, 7);
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
                    x -= 7;
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 25, 7, 7);

                    x = this.inventoryLabelX + topLeftSlot.x - 1;
                    y -= 54;
                    for (int slot = 0; slot < slots; slot++) {
                        if (slot % 3 == 0) {
                            x -= 18;
                            y += 54;
                        }
                        y -= 18;
                        pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 7, 18, 18);
                    }

                    x -= 7;
                    y += 54;
                    for (int i = 0; i < 3; i++) {
                        y -= 18;
                        pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 7, 7, 18);
                    }

                    x = this.inventoryLabelX + topLeftSlot.x - 8;
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

                    pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }

            ItemStack rightPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, true);
            if (!rightPouchStack.isEmpty()) {
                BaseBagItem bagItem = (BaseBagItem) rightPouchStack.getItem();
                int slots = bagItem.getSlotCount();
                int columns = (int) Math.ceil(slots / 3.0);

                var _topRightSlot = menu.slots.stream().filter(slot->slot.container.equals(playerInventory) && slot.getContainerSlot() == 17).findFirst();
                Slot topRightSlot = _topRightSlot.isPresent() ? _topRightSlot.get() : null;
                if (topRightSlot != null) {
                    int x = this.inventoryLabelX + topRightSlot.x + 17;
                    int y = this.inventoryLabelY + topRightSlot.y + 53;

                    pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 25, 25, 7, 7);
                    x += 7;
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
                    pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 32, 25, 7, 7);

                    x = this.inventoryLabelX + topRightSlot.x - 1;
                    y -= 54;
                    for (int slot = 0; slot < slots; slot++) {
                        if (slot % 3 == 0) {
                            x += 18;
                            y += 54;
                        }
                        y -= 18;
                        pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 7, 18, 18);
                    }

                    x += 18;
                    y += 54;
                    for (int i = 0; i < 3; i++) {
                        y -= 18;
                        pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 32, 7, 7, 18);
                    }

                    x = this.inventoryLabelX + topRightSlot.x + 17;
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

                    pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        }
    }

    @Inject(method = "hasClickedOutside", at = @At("TAIL"), cancellable = true)
    private void scout$adjustOutsideBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (this.minecraft != null && this.minecraft.player != null && !ScoutUtilClient.isScreenBlacklisted(this)) {
            ItemStack backStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.SATCHEL, false);
            if (!backStack.isEmpty()) {
                BaseBagItem bagItem = (BaseBagItem) backStack.getItem();
                int slots = bagItem.getSlotCount();
                int rows = (int) Math.ceil(slots / 9.0);

                if (mouseY < (top + this.imageHeight) + 8 + (18 * rows) && mouseY >= (top + this.imageHeight) && mouseX >= left && mouseX < (left + this.imageWidth)) {
                    callbackInfo.setReturnValue(false);
                }
            }

            ItemStack leftPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, false);
            if (!leftPouchStack.isEmpty()) {
                BaseBagItem bagItem = (BaseBagItem) leftPouchStack.getItem();
                int slots = bagItem.getSlotCount();
                int columns = (int) Math.ceil(slots / 3.0);

                if (mouseX >= left - (columns * 18) && mouseX < left && mouseY >= (top + this.imageHeight) - 90 && mouseY < (top + this.imageHeight) - 22) {
                    callbackInfo.setReturnValue(false);
                }
            }

            ItemStack rightPouchStack = ScoutUtil.findBagItem(this.minecraft.player, BaseBagItem.BagType.POUCH, true);
            if (!rightPouchStack.isEmpty()) {
                BaseBagItem bagItem = (BaseBagItem) rightPouchStack.getItem();
                int slots = bagItem.getSlotCount();
                int columns = (int) Math.ceil(slots / 3.0);

                if (mouseX >= (left + this.imageWidth) && mouseX < (left + this.imageWidth) + (columns * 18) && mouseY >= (top + this.imageHeight) - 90 && mouseY < (top + this.imageHeight) - 22) {
                    callbackInfo.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    public void scout$drawOurSlots(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float pPartialTick, CallbackInfo ci) {
        if (this.minecraft != null && this.minecraft.player != null && !ScoutUtilClient.isScreenBlacklisted(this)) {
            for (int i = ScoutUtil.SATCHEL_SLOT_START; i > ScoutUtil.BAG_SLOTS_END; i--) {
                BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(i, this.minecraft.player.inventoryMenu);
                if (slot != null && slot.isActive()) {
                    this.drawSlot(pGuiGraphics, slot);
                }

                if (slot != null && slot.isActive() && this.isPointOverSlot(slot, mouseX, mouseY)) {
                    this.hoveredSlot = slot;
                    int slotX = slot.getX();
                    int slotY = slot.getY();
                    drawSlotHighlight(pGuiGraphics, slotX, slotY, 0);
                }
            }
        }
    }

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    public void scout$fixSlotPos(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if (slot instanceof BagSlot bagSlot) {
            cir.setReturnValue(this.isPointWithinBounds(bagSlot.getX(), bagSlot.getY(), 16, 16, pointX, pointY));
        }
    }

    @Inject(method = "findSlot", at = @At("RETURN"), cancellable = true)
    public void scout$addSlots(double x, double y, CallbackInfoReturnable<Slot> cir) {
        if (this.minecraft != null && this.minecraft.player != null && !ScoutUtilClient.isScreenBlacklisted(this)) {
            for (int i = ScoutUtil.SATCHEL_SLOT_START; i > ScoutUtil.BAG_SLOTS_END; i--) {
                BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(i, this.minecraft.player.inventoryMenu);
                if (slot != null && slot.isActive() && this.isPointOverSlot(slot, x, y)) {
                    cir.setReturnValue(slot);
                }
            }
        }
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void scout$renderBagSlot(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
        if (pSlot instanceof BagSlot bagSlot) {
            int x = bagSlot.getX();
            int y = bagSlot.getY();

            ItemStack itemstack = bagSlot.getItem();
            boolean hasItem = !itemstack.isEmpty();
            boolean isActive = bagSlot.isActive();

            if (isActive) {
                pGuiGraphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 7, 16, 16);

                if (hasItem) {
                    AbstractContainerScreen<?> self = (AbstractContainerScreen<?>)(Object)this;
                    pGuiGraphics.renderItem(itemstack, x, y);
                }
            }

            ci.cancel();
        }
    }

    private void drawSlot(GuiGraphics graphics, Slot slot) {
        if (slot == null) return;

        int x = slot.x;
        int y = slot.y;

        if (slot instanceof BagSlot bagSlot) {
            x = bagSlot.getX();
            y = bagSlot.getY();
        }

        ItemStack itemStack = slot.getItem();
        boolean hasItem = !itemStack.isEmpty();
        boolean isActive = slot.isActive();

        if (isActive) {
            if (slot instanceof BagSlot) {
                graphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 7, 7, 16, 16);
            }

            if (hasItem) {
                AbstractContainerScreen<?> self = (AbstractContainerScreen<?>)(Object)this;
                graphics.renderItem(itemStack, x, y);
            }
        }
    }

    private static void drawSlotHighlight(GuiGraphics graphics, int x, int y, int z) {
        graphics.fillGradient(x, y, x + 16, y + 16, 0x80FFFFFF, 0x80FFFFFF, z);
    }

    private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
        if (slot == null) return false;

        int x = slot.x;
        int y = slot.y;

        if (slot instanceof BagSlot bagSlot) {
            x = bagSlot.getX();
            y = bagSlot.getY();
        }

        return this.isPointWithinBounds(x, y, 16, 16, pointX, pointY);
    }

    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        int i = this.leftPos;
        int j = this.topPos;
        return pointX >= (double)(i + x - 1) && pointX < (double)(i + x + width + 1) && pointY >= (double)(j + y - 1) && pointY < (double)(j + y + height + 1);
    }

    @Shadow
    public abstract int getXSize();

    @Override
    public T getMenu() {
        return menu;
    }
}