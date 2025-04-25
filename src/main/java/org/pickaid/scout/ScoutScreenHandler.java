package org.pickaid.scout;

import org.pickaid.scout.screen.BagSlot;

import java.util.ArrayList;

public interface ScoutScreenHandler {
	ArrayList<BagSlot> scout$getSatchelSlots();
	ArrayList<BagSlot> scout$getLeftPouchSlots();
	ArrayList<BagSlot> scout$getRightPouchSlots();
}
