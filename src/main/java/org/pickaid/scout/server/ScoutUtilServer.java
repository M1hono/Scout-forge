package org.pickaid.scout.server;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.pickaid.scout.ScoutUtil;

public class ScoutUtilServer {
	private static Player currentPlayer = null;

	public static void setCurrentPlayer(Player player) {
		if (currentPlayer != null) {
			ScoutUtil.LOGGER.warn("[Scout] New player set during existing quick move, expect players getting wrong items!");
		}
		currentPlayer = player;
	}

	public static void clearCurrentPlayer() {
		currentPlayer = null;
	}

	public static @Nullable Player getCurrentPlayer() {
		return currentPlayer;
	}
}
