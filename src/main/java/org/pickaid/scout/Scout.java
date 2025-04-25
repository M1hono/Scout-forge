package org.pickaid.scout;

import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.pickaid.scout.config.ScoutConfig;
import org.pickaid.scout.network.ScoutNetworkHandler;
import org.pickaid.scout.registry.ScoutItems;

import java.util.logging.Logger;

@Mod(Scout.MOD_ID)
public class Scout {
	public static final String MOD_ID = "scout";
	public static final Logger LOGGER = Logger.getLogger(MOD_ID);
	public static ResourceLocation source(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
	public static Registrate REGISTRATE = Registrate.create(MOD_ID);

	public Scout() {
		ModLoadingContext cxt = ModLoadingContext.get();
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		registerConfig(cxt);
		modEventBus.addListener(this::commonSetup);

		ScoutItems.init();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(ScoutNetworkHandler::register);
	}

	private void registerConfig(ModLoadingContext cxt) {
		cxt.registerConfig(ModConfig.Type.SERVER, ScoutConfig.SPEC);
	}
}
