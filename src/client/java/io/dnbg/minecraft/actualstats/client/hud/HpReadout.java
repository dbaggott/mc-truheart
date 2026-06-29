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
 * Renders the player's real (un-rounded) HP just above the heart bar,
 * left-aligned to the hotbar's left edge so it visually parents to the
 * hearts rather than floating in the corner.
 *
 * <p>Attached <em>after</em> the vanilla {@code HEALTH_BAR} element so
 * our text composites on top of anything the heart bar renders in the
 * same area (e.g. absorption hearts that stack upward).
 */
public final class HpReadout {
	/** Vanilla hotbar is 182 px wide, centered horizontally. */
	private static final int HOTBAR_HALF_WIDTH = 91;
	/**
	 * Top of the vanilla heart bar relative to screen bottom. Hearts are
	 * 9 px tall and sit in this row.
	 */
	private static final int HEART_BAR_BOTTOM_OFFSET = 39;
	/**
	 * Base gap between the heart bar's top and our text — the "no armor,
	 * no absorption" case. Additional rows above the hearts shift our text
	 * up by {@link #ROW_HEIGHT} each.
	 */
	private static final int Y_GAP_ABOVE_BAR = 10;
	/**
	 * Height of one HUD icon row (hearts / armor / absorption all use this
	 * pitch). Used to stack our text above any row vanilla is currently
	 * drawing in the area immediately above the hearts.
	 */
	private static final int ROW_HEIGHT = 10;
	/** Bright red that matches the vanilla heart-sprite hue more closely. */
	private static final int COLOR_HP = 0xFFFF2A2A;
	/**
	 * Soft gold for the absorption portion of the line — same brightness
	 * envelope as {@link #COLOR_HP} so the two segments feel like a pair
	 * rather than one bright and one washed out.
	 */
	private static final int COLOR_ABSORPTION = 0xFFFFCC55;

	/**
	 * Unicode BLACK HEART SUIT (U+2665), part of the vanilla Unifont
	 * fallback. Rendered as a regular glyph in the text — sized at text
	 * height (~8 px) instead of the 9-px sprite hearts in the player's
	 * heart row, so it reads as a label rather than another data heart.
	 * Picks up the text's color (red) and drop shadow automatically.
	 */
	private static final String HP_LABEL = "♥";

	private HpReadout() {
	}

	public static void register() {
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.HEALTH_BAR,
			ActualStats.id("hp_readout"),
			HpReadout::extract
		);
	}

	private static void extract(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}

		Font font = mc.font;
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		// Stack our text above any rows the vanilla HUD is currently drawing
		// in the band immediately above the hearts (armor icons; absorption
		// hearts). Each present row takes one ROW_HEIGHT slice, so we offset
		// up by the sum to land in clean airspace above all of them.
		int yGap = Y_GAP_ABOVE_BAR;
		float absorption = player.getAbsorptionAmount();
		if (absorption > 0) {
			yGap += ROW_HEIGHT;
		}
		if (player.getArmorValue() > 0) {
			yGap += ROW_HEIGHT;
		}

		int x = screenWidth / 2 - HOTBAR_HALF_WIDTH;
		int y = screenHeight - HEART_BAR_BOTTOM_OFFSET - yGap;

		// Label: the heart glyph inlined as part of the text, so it shares
		// the text's color, shadow, and baseline. Sized at font height,
		// it's visually obviously a label rather than another data heart.
		String hpText = String.format("%s %.2f / %.0f", HP_LABEL, player.getHealth(), player.getMaxHealth());
		extractor.text(font, hpText, x, y, COLOR_HP, true);

		// When absorption is active, append a gold segment after the HP
		// text. The leading space in " + %.2f" provides symmetric padding
		// on both sides of the plus sign so it reads like the " / " in the
		// HP text.
		if (absorption > 0) {
			int absTextX = x + font.width(hpText);
			String absText = String.format(" + %.2f", absorption);
			extractor.text(font, absText, absTextX, y, COLOR_ABSORPTION, true);
		}
	}
}
