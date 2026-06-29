package io.dnbg.minecraft.actualstats.client.hud;

import io.dnbg.minecraft.actualstats.ActualStats;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

/**
 * Renders the player's real (un-rounded) HP at the top-left of the HUD.
 *
 * <p>The vanilla heart bar shows HP as half-heart pips, which lossily rounds
 * the underlying float (regen, magic damage, and absorption all leave you
 * sitting on values the pip rendering can't show precisely). This element
 * draws the raw {@code player.getHealth() / player.getMaxHealth()} value.
 *
 * <p>26.2 changed the HUD pipeline to a two-phase extract/render model. We
 * implement the extract phase — feeding text into a {@link GuiGraphicsExtractor}
 * that the renderer drains later in the frame. Attaching <em>before</em>
 * {@code VanillaHudElements.CHAT} keeps the overlay above the world but
 * under chat output, matching where F3 debug info sits.
 */
public final class StatsHudOverlay {
	private static final int PADDING_X = 4;
	private static final int PADDING_Y = 4;

	// ARGB color. The alpha byte must be set (full-opaque = 0xFF……) or
	// the text renderer treats the color as transparent and draws nothing.
	private static final int COLOR_OWN_HP = 0xFFFF5555;

	private StatsHudOverlay() {
	}

	public static void register() {
		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CHAT,
			ActualStats.id("own_hp"),
			StatsHudOverlay::extractRenderState
		);
	}

	private static void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}

		Font font = mc.font;
		String line = formatHp(player.getHealth(), player.getMaxHealth());
		extractor.text(font, line, PADDING_X, PADDING_Y, COLOR_OWN_HP, true);
	}

	private static String formatHp(float current, float max) {
		// One decimal for current (matches the granularity the game actually
		// computes — finer is float noise) and zero for max (max-HP is
		// integer-valued in practice).
		return String.format("HP %.1f / %.0f", current, max);
	}
}
