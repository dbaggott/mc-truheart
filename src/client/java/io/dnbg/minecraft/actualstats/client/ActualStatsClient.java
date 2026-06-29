package io.dnbg.minecraft.actualstats.client;

import io.dnbg.minecraft.actualstats.client.hud.StatsHudOverlay;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client entry point. Register HUD callbacks, attack callbacks, and any
 * other client-only wiring here.
 *
 * <p>Feature classes (HUD overlay, damage tracker, …) live as siblings in
 * this package — this file stays as a small wiring hub.
 */
public class ActualStatsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		StatsHudOverlay.register();
	}
}
