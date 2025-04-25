package org.pickaid.scout.client.gui;

import com.google.common.math.IntMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.item.BagTooltipData;

import java.math.RoundingMode;
import java.util.ArrayList;

public class BagTooltipComponent implements ClientTooltipComponent {
	private final ArrayList<ItemStack> inventory;
	private final int slotCount;

	public BagTooltipComponent(BagTooltipData data) {
		this.inventory = data.getInventory();
		this.slotCount = data.getSlotCount();
	}

	@Override
	public int getHeight() {
		return (18 * IntMath.divide(slotCount, 6, RoundingMode.UP)) + 2;
	}

	@Override
	public int getWidth(Font font) {
		return 18 * (Math.min(slotCount, 6));
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		int originalX = x;

		for (int i = 0; i < slotCount; i++) {
			this.drawSlot(x, y, i, graphics, font);

			x += 18;
			if ((i + 1) % 6 == 0) {
				y += 18;
				x = originalX;
			}
		}
	}

	private void drawSlot(int x, int y, int index, GuiGraphics graphics, Font textRenderer) {
		ItemStack itemStack = this.inventory.get(index);
		graphics.blit(ScoutUtil.SLOT_TEXTURE, x, y, 0, 46, 7, 18, 18, 256, 256);
		graphics.renderItem(itemStack, x + 1, y + 1, index);
		graphics.renderItemDecorations(textRenderer, itemStack, x + 1, y + 1);
	}
}
