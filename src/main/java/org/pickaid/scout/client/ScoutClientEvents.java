package org.pickaid.scout.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.scout.Scout;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.item.BaseBagItem;
import org.pickaid.scout.mixins.client.HandledScreenAccessor;
import org.pickaid.scout.screen.BagSlot;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Scout.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScoutClientEvents {
    @SubscribeEvent
    public static void afterInitScreen(ScreenEvent.Init.Post event) {
        var screen = event.getScreen();
        var client = Minecraft.getInstance();
        if (screen instanceof AbstractContainerScreen<?> handledScreen && client.player != null) {
            ScoutUtil.LOGGER.debug("屏幕初始化：{}", screen.getClass().getName());

            // 对于黑名单屏幕，隐藏所有自定义槽位
            if (ScoutUtilClient.isScreenBlacklisted(screen)) {
                ScoutUtil.LOGGER.debug("屏幕在黑名单中，隐藏所有自定义槽位");
                for (Slot slot : ScoutUtil.getAllBagSlots(client.player.inventoryMenu)) {
                    if (slot instanceof BagSlot bagSlot) {
                        // 将槽位移到不可见位置
                        bagSlot.setX(Integer.MAX_VALUE);
                        bagSlot.setY(Integer.MAX_VALUE);
                        ScoutUtil.LOGGER.debug("隐藏槽位：{}", bagSlot.slot);
                    }
                }
                return;
            }

            var handledScreenAccessor = (HandledScreenAccessor<?>) handledScreen;
            InventoryMenu handler = (InventoryMenu) handledScreenAccessor.getHandler();
            int left = handledScreen.getGuiLeft();
            int top = handledScreen.getGuiTop();

            // 记录屏幕位置信息
            ScoutUtil.LOGGER.debug("屏幕位置：left={}, top={}, width={}, height={}",
                    left, top, handledScreen.width, handledScreen.height);

            var playerInventory = client.player.getInventory();

            // 调整背包槽位位置以适应当前屏幕
            adjustSatchelSlots(client, handler, playerInventory, handledScreen);

            // 调整左侧口袋槽位位置
            adjustLeftPouchSlots(client, handler, playerInventory, handledScreen);

            // 调整右侧口袋槽位位置
            adjustRightPouchSlots(client, handler, playerInventory, handledScreen);

            // 输出诊断信息
            for (Slot slot : ScoutUtil.getAllBagSlots(client.player.inventoryMenu)) {
                if (slot instanceof BagSlot bagSlot) {
                    ScoutUtil.LOGGER.debug("槽位 {}: 位置=({},{}), 激活={}, 容器={}, 物品={}",
                            bagSlot.slot,
                            bagSlot.getX(), bagSlot.getY(),
                            bagSlot.isActive(),
                            bagSlot.inventory != null ? "有效" : "无效",
                            !bagSlot.getItem().isEmpty() ? bagSlot.getItem().getDisplayName().getString() : "空");
                }
            }
        }
    }

    /**
     * 调整背包槽位位置
     */
    private static void adjustSatchelSlots(Minecraft client, InventoryMenu handler,
                                           net.minecraft.world.entity.player.Inventory playerInventory,
                                           AbstractContainerScreen<?> handledScreen) {
        // 检查背包是否装备
        ItemStack backStack = ScoutUtil.findBagItem(client.player, BaseBagItem.BagType.SATCHEL, false);
        if (backStack.isEmpty()) {
            ScoutUtil.LOGGER.debug("未装备背包，跳过背包槽位调整");
            return;
        }

        // 获取热键栏第一个槽位作为参考点
        var _hotbarSlot1 = handler.slots.stream()
                .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 0)
                .findFirst();
        Slot hotbarSlot1 = _hotbarSlot1.orElse(null);

        if (hotbarSlot1 == null) {
            ScoutUtil.LOGGER.debug("未找到热键栏参考点，跳过背包槽位调整");
            return;
        }

        BaseBagItem bagItem = (BaseBagItem) backStack.getItem();
        int slots = bagItem.getSlotCount();

        // 计算基准位置
        int baseX = hotbarSlot1.x;
        int baseY = hotbarSlot1.y + 27;
        ScoutUtil.LOGGER.debug("背包槽位基准位置：({}, {})", baseX, baseY);

        // 调整所有背包槽位位置
        for (int i = 0; i < ScoutUtil.MAX_SATCHEL_SLOTS && i < slots; i++) {
            int row = i / 9;
            int col = i % 9;
            int x = baseX + col * 18;
            int y = baseY + row * 18;

            BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.SATCHEL_SLOT_START - i, client.player.inventoryMenu);
            if (slot != null) {
                slot.setX(x);
                slot.setY(y);
                ScoutUtil.LOGGER.debug("调整背包槽位 {}: 位置=({}, {})", i, x, y);
            }
        }
    }

    /**
     * 调整左侧口袋槽位位置
     */
    private static void adjustLeftPouchSlots(Minecraft client, InventoryMenu handler,
                                             net.minecraft.world.entity.player.Inventory playerInventory,
                                             AbstractContainerScreen<?> handledScreen) {
        // 检查左侧口袋是否装备
        ItemStack leftPouchStack = ScoutUtil.findBagItem(client.player, BaseBagItem.BagType.POUCH, false);
        if (leftPouchStack.isEmpty()) {
            ScoutUtil.LOGGER.debug("未装备左侧口袋，跳过左侧口袋槽位调整");
            return;
        }

        // 获取物品栏左上角槽位作为参考点
        var _topLeftSlot = handler.slots.stream()
                .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 9)
                .findFirst();
        Slot topLeftSlot = _topLeftSlot.orElse(null);

        if (topLeftSlot == null) {
            ScoutUtil.LOGGER.debug("未找到物品栏参考点，跳过左侧口袋槽位调整");
            return;
        }

        BaseBagItem bagItem = (BaseBagItem) leftPouchStack.getItem();
        int slots = bagItem.getSlotCount();

        // 计算基准位置
        int baseX = topLeftSlot.x - 18;
        int baseY = topLeftSlot.y;
        ScoutUtil.LOGGER.debug("左侧口袋槽位基准位置：({}, {})", baseX, baseY);

        // 调整所有左侧口袋槽位位置
        for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS && i < slots; i++) {
            int col = i / 3;
            int row = i % 3;
            int x = baseX - col * 18;
            int y = baseY + row * 18;

            BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.LEFT_POUCH_SLOT_START - i, client.player.inventoryMenu);
            if (slot != null) {
                slot.setX(x);
                slot.setY(y);
                ScoutUtil.LOGGER.debug("调整左侧口袋槽位 {}: 位置=({}, {})", i, x, y);
            }
        }
    }

    /**
     * 调整右侧口袋槽位位置
     */
    private static void adjustRightPouchSlots(Minecraft client, InventoryMenu handler,
                                              net.minecraft.world.entity.player.Inventory playerInventory,
                                              AbstractContainerScreen<?> handledScreen) {
        // 检查右侧口袋是否装备
        ItemStack rightPouchStack = ScoutUtil.findBagItem(client.player, BaseBagItem.BagType.POUCH, true);
        if (rightPouchStack.isEmpty()) {
            ScoutUtil.LOGGER.debug("未装备右侧口袋，跳过右侧口袋槽位调整");
            return;
        }

        // 获取物品栏右上角槽位作为参考点
        var _topRightSlot = handler.slots.stream()
                .filter(slot -> slot.container.equals(playerInventory) && slot.getContainerSlot() == 17)
                .findFirst();
        Slot topRightSlot = _topRightSlot.orElse(null);

        if (topRightSlot == null) {
            ScoutUtil.LOGGER.debug("未找到物品栏参考点，跳过右侧口袋槽位调整");
            return;
        }

        BaseBagItem bagItem = (BaseBagItem) rightPouchStack.getItem();
        int slots = bagItem.getSlotCount();

        // 计算基准位置
        int baseX = topRightSlot.x + 18;
        int baseY = topRightSlot.y;
        ScoutUtil.LOGGER.debug("右侧口袋槽位基准位置：({}, {})", baseX, baseY);

        // 调整所有右侧口袋槽位位置
        for (int i = 0; i < ScoutUtil.MAX_POUCH_SLOTS && i < slots; i++) {
            int col = i / 3;
            int row = i % 3;
            int x = baseX + col * 18;
            int y = baseY + row * 18;

            BagSlot slot = (BagSlot) ScoutUtil.getBagSlot(ScoutUtil.RIGHT_POUCH_SLOT_START - i, client.player.inventoryMenu);
            if (slot != null) {
                slot.setX(x);
                slot.setY(y);
                ScoutUtil.LOGGER.debug("调整右侧口袋槽位 {}: 位置=({}, {})", i, x, y);
            }
        }
    }
}