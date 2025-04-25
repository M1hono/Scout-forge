package org.pickaid.scout.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.client.ICuriosScreen;

public class ScoutUtilClient {
    public static @Nullable InventoryMenu getPlayerScreenHandler() {
        var client = Minecraft.getInstance();
        if (client != null && client.player != null) {
            return client.player.inventoryMenu;
        }

        return null;
    }

    // FIXME: registry system for mods to register their own blacklisted screens
    public static boolean isScreenBlacklisted(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen || screen instanceof ICuriosScreen;
    }
}
