package org.pickaid.scout.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.pickaid.scout.ScoutUtil;
import org.pickaid.scout.item.BaseBagItem;

import static org.pickaid.scout.Scout.REGISTRATE;

public class ScoutItems {
	public static final ItemEntry<Item> TANNED_LEATHER;
	public static final ItemEntry<Item> SATCHEL_STRAP;
	public static final ItemEntry<BaseBagItem> SATCHEL;
	public static final ItemEntry<BaseBagItem> UPGRADED_SATCHEL;
	public static final ItemEntry<BaseBagItem> POUCH;
	public static final ItemEntry<BaseBagItem> UPGRADED_POUCH;

	static {
		TANNED_LEATHER  = REGISTRATE.item("tanned_leather", Item::new)
				.tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register();
		SATCHEL_STRAP  = REGISTRATE.item("satchel_strap", Item::new)
				.tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register();
		SATCHEL  = REGISTRATE.item("satchel", properties ->  new BaseBagItem(ScoutUtil.MAX_SATCHEL_SLOTS / 2, BaseBagItem.BagType.SATCHEL))
				.properties(properties ->
						properties.stacksTo(1))
				.tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register();
		UPGRADED_SATCHEL  = REGISTRATE.item("upgraded_satchel", properties ->  new BaseBagItem(ScoutUtil.MAX_SATCHEL_SLOTS, BaseBagItem.BagType.SATCHEL))
				.properties(properties ->
						properties.stacksTo(1))
				.tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register();
		POUCH  = REGISTRATE.item("pouch", properties ->  new BaseBagItem(ScoutUtil.MAX_POUCH_SLOTS / 2, BaseBagItem.BagType.POUCH))
				.properties(properties ->
						properties.stacksTo(1))
				.tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register();
		UPGRADED_POUCH  = REGISTRATE.item("upgraded_pouch", properties ->  new BaseBagItem(ScoutUtil.MAX_POUCH_SLOTS, BaseBagItem.BagType.POUCH))
				.properties(properties ->
						properties.stacksTo(1))
				.tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register();
	}

	public static void init() {}
}
