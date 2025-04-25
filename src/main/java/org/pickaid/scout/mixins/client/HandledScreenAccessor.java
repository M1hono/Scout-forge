package org.pickaid.scout.mixins.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor<T extends AbstractContainerMenu> {
	@Accessor("inventoryLabelX")
	int getX();
	@Accessor("inventoryLabelY")
	int getY();
	@Accessor("imageWidth")
	int getBackgroundWidth();
	@Accessor("imageHeight")
	int getBackgroundHeight();
	@Accessor("menu")
	T getHandler();
}
