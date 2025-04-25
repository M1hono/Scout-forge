package org.pickaid.scout;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.pickaid.scout.item.BaseBagItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScoutUtil {
	public static final Logger LOGGER = LoggerFactory.getLogger("Scout");
	public static final String MOD_ID = "scout";
	public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/slots.png");

	public static final TagKey<Item> TAG_ITEM_BLACKLIST = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "blacklist"));

	public static final int MAX_SATCHEL_SLOTS = 18;
	public static final int MAX_POUCH_SLOTS = 6;
	public static final int TOTAL_SLOTS = MAX_SATCHEL_SLOTS + MAX_POUCH_SLOTS + MAX_POUCH_SLOTS;

	public static final int SATCHEL_SLOT_START = -1100;
	public static final int LEFT_POUCH_SLOT_START = SATCHEL_SLOT_START - MAX_SATCHEL_SLOTS;
	public static final int RIGHT_POUCH_SLOT_START = LEFT_POUCH_SLOT_START - MAX_POUCH_SLOTS;
	public static final int BAG_SLOTS_END = RIGHT_POUCH_SLOT_START - MAX_POUCH_SLOTS;

	public static net.minecraft.world.item.ItemStack findBagItem(Player player, BaseBagItem.BagType type, boolean right) {
		ItemStack targetStack = ItemStack.EMPTY;

		boolean hasFirstPouch = false;
        Optional<ICuriosItemHandler> curiosHandler =  CuriosApi.getCuriosInventory(player).resolve();
		if (curiosHandler.isPresent()) {
            List<SlotResult> component = curiosHandler.get().findCurios(itemStack -> itemStack.getItem() instanceof BaseBagItem);
			for (SlotResult result : component) {
				ItemStack slotStack = result.stack();
                BaseBagItem item = (BaseBagItem) slotStack.getItem();
				if (item.getType() == type) {
                    if (type == BaseBagItem.BagType.POUCH) {
                        if (right && !hasFirstPouch) {
                            hasFirstPouch = true;
                        } else {
                            targetStack = slotStack;
                            break;
                        }
                    } else {
                        targetStack = slotStack;
                        break;
                    }
                }
			}
		}

		return targetStack;
	}

	public static CompoundTag inventoryToTag(Container inventory) {
        CompoundTag tag = new CompoundTag();

		for(int i = 0; i < inventory.getContainerSize(); i++) {
            CompoundTag stackTag = new CompoundTag();
			stackTag.putInt("Slot", i);
			stackTag.put("Stack", inventory.getItem(i).save(new CompoundTag()));
			tag = stackTag;
		}

		return tag;
	}

	public static void inventoryFromTag(ListTag tag, SimpleContainer inventory) {
		inventory.clearContent();
		tag.forEach(element -> {
			CompoundTag stackTag = (CompoundTag) element;
			int slot = stackTag.getInt("Slot");
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(stackTag.getString("id")));
            ItemStack stack = null;
            if (item != null) {
                stack = new ItemStack(item);
                stack.setTag(stackTag);
            } else {
                return;
            }
            inventory.setItem(slot, stack);
		});
	}

	public static boolean isBagSlot(int slot) {
		return slot <= SATCHEL_SLOT_START && slot > BAG_SLOTS_END;
	}

	public static @Nullable Slot getBagSlot(int slot, InventoryMenu playerScreenHandler) {
		var scoutScreenHandler = (ScoutScreenHandler) playerScreenHandler;
		if (slot <= SATCHEL_SLOT_START && slot > LEFT_POUCH_SLOT_START) {
			int realSlot = Math.abs(slot - SATCHEL_SLOT_START);
			var slots = scoutScreenHandler.scout$getSatchelSlots();

			return slots.get(realSlot);
		} else if (slot <= LEFT_POUCH_SLOT_START && slot > RIGHT_POUCH_SLOT_START) {
			int realSlot = Math.abs(slot - LEFT_POUCH_SLOT_START);
			var slots = scoutScreenHandler.scout$getLeftPouchSlots();

			return slots.get(realSlot);
		} else if (slot <= RIGHT_POUCH_SLOT_START && slot > BAG_SLOTS_END) {
			int realSlot = Math.abs(slot - RIGHT_POUCH_SLOT_START);
			var slots = scoutScreenHandler.scout$getRightPouchSlots();

			return slots.get(realSlot);
		} else {
			return null;
		}
	}

	public static List<Slot> getAllBagSlots(InventoryMenu playerScreenHandler) {
		var scoutScreenHandler = (ScoutScreenHandler) playerScreenHandler;
		ArrayList<Slot> out = new ArrayList<>(TOTAL_SLOTS);
		out.addAll(scoutScreenHandler.scout$getSatchelSlots());
		out.addAll(scoutScreenHandler.scout$getLeftPouchSlots());
		out.addAll(scoutScreenHandler.scout$getRightPouchSlots());
		return out;
	}
}
