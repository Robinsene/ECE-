package engine;

import java.awt.Color;

/**
 * Positions, sizes and colours of the gameplay HUD (Requirement 8 -
 * Gameplay HUD, KAN-32), in one place.
 *
 * Every HUD element of Team ECE reads its layout from here instead of
 * keeping its own magic numbers, so moving or resizing one element cannot
 * silently overlap another. Values are in screen pixels, for the
 * 448 x 520 game window. The list of every overlay drawn during gameplay,
 * ours and other teams', is in docs/hud-overlays.md (KAN-34).
 */
public final class HudLayout {

	private HudLayout() {
	}

	// --- Top bar (y 0 - 39) ---

	/** Y of the green line under the top bar; the game area starts below. */
	public static final int TOP_BAR_BOTTOM = 39;
	/** Baseline Y of the LIVES label and the score. */
	public static final int TOP_ROW_BASELINE = 25;

	/** Left X of the LIVES label. */
	public static final int LIVES_X = 20;
	/** Horizontal space reserved for the "LIVES" label. 50 rather than 46
	 * so the label no longer touches the first life icon (KAN-34). */
	public static final int LIVES_LABEL_WIDTH = 50;
	/** Horizontal space reserved per life icon. */
	public static final int LIVES_ICON_SPACING = 35;
	/** Top Y of the life icons. */
	public static final int LIVES_ICON_Y = 10;
	/** Left X of the first life icon. */
	public static final int LIVES_ICONS_X = LIVES_X + LIVES_LABEL_WIDTH;

	/** Top Y of the extra-life bar, under the life icons (KAN-25). */
	public static final int EXTRA_LIFE_BAR_Y = 31;
	/** Height of the extra-life bar. */
	public static final int EXTRA_LIFE_BAR_HEIGHT = 3;
	/** Width of the extra-life bar (three life slots). */
	public static final int EXTRA_LIFE_BAR_WIDTH = LIVES_ICON_SPACING * 3 - 9;

	/** Baseline Y of the top-centre level indicator. */
	public static final int LEVEL_INDICATOR_Y = 14;

	/** Margin between the score and the right edge of the screen. */
	public static final int SCORE_ZONE_MARGIN = 8;
	/** Gap between the "SCORE" label and its value. */
	public static final int SCORE_LABEL_GAP = 8;
	/** Baseline Y of the best score, above the score (KAN-24). */
	public static final int BEST_SCORE_Y = 11;

	/** Y offset of the diamond counter below the score line. */
	public static final int DIAMONDS_Y_OFFSET = 11;
	/** Size of the diamond icon drawn next to the count. */
	public static final int DIAMOND_ICON_SIZE = 10;
	/** Gap between the diamond icon and its number. */
	public static final int DIAMOND_ICON_GAP = 4;

	// --- Under the top bar ---

	/** Baseline Y of the wave counter, centred in the item HUD row (KAN-29). */
	public static final int WAVE_TEXT_Y = 52;
	/** Width of the wave progress bar. */
	public static final int WAVE_BAR_WIDTH = 70;
	/** Top Y of the boss health bar, a row below the item slots. */
	public static final int BOSS_BAR_Y = 64;
	/** Baseline Y of the combo text under the item HUD (KAN-23). */
	public static final int COMBO_BASELINE = 72;
	/** Baseline Y of the combo text under the boss health bar. */
	public static final int COMBO_BASELINE_BOSS = 88;
	/** Baseline Y of the combo text while an achievement popup covers the
	 * right side from y 46 to 90 (KAN-34). */
	public static final int COMBO_BASELINE_UNDER_POPUP = 106;
	/** Smallest gap between the item slots and the wave counter. */
	public static final int WAVE_MIN_GAP = 10;

	// --- Colours ---

	/** Gold of a beaten record (KAN-24). */
	public static final Color RECORD_GOLD = new Color(255, 200, 40);
	/** Colours of the combo text for multipliers x1 to x4 (KAN-23). */
	public static final Color[] COMBO_COLORS = { Color.WHITE, Color.GREEN,
			Color.YELLOW, new Color(255, 120, 30) };
	/** Background of the top HUD bar (KAN-26). */
	public static final Color HUD_PANEL_COLOR = new Color(8, 24, 12);
	/** Separators between the top HUD sections (KAN-26). */
	public static final Color HUD_SEPARATOR_COLOR = new Color(30, 90, 40);
}
