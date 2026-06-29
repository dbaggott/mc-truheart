package io.dnbg.minecraft.actualstats.client.hud;

import io.dnbg.minecraft.actualstats.ActualStats;
import io.dnbg.minecraft.actualstats.client.mixin.FoodDataAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;

/**
 * Renders the food-side hidden stats above the vanilla food bar:
 * <ul>
 *   <li><strong>Saturation</strong> — float, depletes silently before food
 *       level drops; gates HP regen and sprint stamina.</li>
 *   <li><strong>Exhaustion</strong> — float 0.0–4.0 that accumulates from
 *       movement/damage and resets when it hits 4.0, at which point it
 *       consumes 1 saturation (or 1 food level when saturation is 0). The
 *       bar visualizes how close the next saturation tick is.</li>
 * </ul>
 *
 * <p>Food level itself (the integer 0–20) is omitted from text because the
 * vanilla half-drumstick row already shows it precisely; duplicating it
 * adds noise without adding info.
 *
 * <p>Exhaustion is private on {@link FoodData} and read via the
 * {@link FoodDataAccessor} mixin.
 */
public final class FoodReadout {
	private static final int HOTBAR_HALF_WIDTH = 91;
	private static final int FOOD_BAR_BOTTOM_OFFSET = 39;
	private static final int Y_GAP_ABOVE_BAR = 10;
	/**
	 * Height of one HUD icon row (food / oxygen / mount-health bars all use
	 * this pitch). We stack our overlay above any row vanilla is currently
	 * drawing above the food bar.
	 */
	private static final int ROW_HEIGHT = 10;

	private static final int EXHAUSTION_BAR_WIDTH = 80;
	private static final int EXHAUSTION_BAR_HEIGHT = 3;
	/** Gap between the exhaustion bar's bottom and the saturation text's top. */
	private static final int BAR_TEXT_GAP = 2;
	private static final float EXHAUSTION_MAX = 4.0f;

	/** Soft amber — easier on the eyes than the saturated drumstick orange. */
	private static final int COLOR_SATURATION = 0xFFCC9966;
	/** Semi-transparent black behind the exhaustion bar fill. */
	private static final int COLOR_BAR_BG = 0x80000000;
	/** Brighter amber for the fill, distinguishable from the saturation text. */
	private static final int COLOR_EXHAUSTION_FILL = 0xFFFFAA55;

	private FoodReadout() {
	}

	public static void register() {
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.FOOD_BAR,
			ActualStats.id("food_readout"),
			FoodReadout::extract
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

		FoodData food = player.getFoodData();

		// Stack our overlay above any row vanilla is currently drawing above
		// the food bar (oxygen bubbles when underwater / regenerating air;
		// mount-health bar when riding a LivingEntity).
		int yGap = Y_GAP_ABOVE_BAR;
		if (player.getAirSupply() < player.getMaxAirSupply()) {
			yGap += ROW_HEIGHT;
		}
		if (player.isPassenger() && player.getVehicle() instanceof LivingEntity) {
			yGap += ROW_HEIGHT;
		}

		// Saturation text — right-aligned to the food bar's right edge.
		String text = String.format("%.2f", food.getSaturationLevel());
		int rightX = screenWidth / 2 + HOTBAR_HALF_WIDTH;
		int textX = rightX - font.width(text);
		int textY = screenHeight - FOOD_BAR_BOTTOM_OFFSET - yGap;
		extractor.text(font, text, textX, textY, COLOR_SATURATION, true);

		// Exhaustion bar — directly above the saturation text, same right edge.
		float exhaustion = ((FoodDataAccessor) (Object) food).getExhaustionLevel();
		float ratio = Math.min(exhaustion / EXHAUSTION_MAX, 1.0f);
		int barRightX = rightX;
		int barLeftX = barRightX - EXHAUSTION_BAR_WIDTH;
		int barTopY = textY - BAR_TEXT_GAP - EXHAUSTION_BAR_HEIGHT;
		int barBottomY = barTopY + EXHAUSTION_BAR_HEIGHT;

		extractor.fill(barLeftX, barTopY, barRightX, barBottomY, COLOR_BAR_BG);
		int fillWidth = (int) (EXHAUSTION_BAR_WIDTH * ratio);
		if (fillWidth > 0) {
			extractor.fill(barLeftX, barTopY, barLeftX + fillWidth, barBottomY, COLOR_EXHAUSTION_FILL);
		}
	}
}
