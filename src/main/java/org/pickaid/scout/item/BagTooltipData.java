package org.pickaid.scout.item;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class BagTooltipData implements TooltipComponent {
	private final ArrayList<ItemStack> inventory;
	private final int slotCount;

	public BagTooltipData(ArrayList<ItemStack> inventory, int slots) {
		this.inventory = inventory;
		this.slotCount = slots;
	}

	public ArrayList<ItemStack> getInventory() {
		return this.inventory;
	}

	public int getSlotCount() {
		return this.slotCount;
	}
}
