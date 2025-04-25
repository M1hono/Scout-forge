package org.pickaid.scout.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class ScoutConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.BooleanValue ALLOW_SHULKERS;
	public static final ForgeConfigSpec.BooleanValue USE_ARROWS;

	static {
		BUILDER.push("Scout Configuration");

		ALLOW_SHULKERS = BUILDER
				.comment("Allow shulker boxes to be placed in bags. Bags are already blacklisted from shulker boxes with no toggle.")
				.define("allowShulkers", true);

		USE_ARROWS = BUILDER
				.comment("Allow bags to act as a quiver and pull arrows.")
				.define("useArrows", true);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}

	private static File getConfigFile() {
		return new File(FMLPaths.CONFIGDIR.get().toFile(), "scout.toml");
	}
}