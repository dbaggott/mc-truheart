package io.dnbg.minecraft.actualstats.client;

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
		// Feature wiring goes here in subsequent commits. v0.1.0 intentionally
		// ships a no-op so the project skeleton can be verified end-to-end
		// (build → run → mod loads cleanly) before any feature code lands.
	}
}
