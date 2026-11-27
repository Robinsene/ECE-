package engine;

import java.awt.Color;

/**
 * Space Opera theme (Team ECE - KAN-55): the normal run dressed as a space
 * battle. The Free Squadron jumps from sector to sector to reach the
 * Obsidian Dominion's dreadnought. Names, ships and music are our own.
 *
 * No drawing code here: names, colours and the timing of the hyperspace
 * jump, so the theme can be unit tested.
 */
public final class SpaceOpera {

	/** How long the hyperspace jump lasts at the start of a stage, in ms. */
	public static final long JUMP_MS = 1600;
	/** Colour of the player's lasers. */
	public static final Color PLAYER_LASER = new Color(90, 255, 130);
	/** Colour of the enemies' lasers. */
	public static final Color ENEMY_LASER = new Color(255, 70, 60);
	/** The player's side. */
	public static final String PLAYER_FACTION = "FREE SQUADRON";
	/** The enemy's side. */
	public static final String ENEMY_FACTION = "OBSIDIAN DOMINION";

	/** Sector of each stage of the run, stage 1 first. */
	private static final String[] SECTORS = { "VARO NEBULA",
			"HELIX GATE", "ASHFALL DRIFT", "CINDER REACH",
			"WARDEN'S WATCH", "GLASS CANYON", "PALE MERIDIAN",
			"THE SHATTERED RING", "IRON VEIL", "THE DREADNOUGHT" };

	private SpaceOpera() {
	}

	/** @return True while a Space Opera run is being played. */
	public static boolean isActive() {
		return GameMode.current() == GameMode.SPACE_OPERA;
	}

	/**
	 * @param level
	 *            Stage number.
	 * @return "SECTOR n - NAME" for that stage.
	 */
	public static String sectorTitle(final int level) {
		int base = GameMode.baseLevel(level);
		return "SECTOR " + level + " - " + SECTORS[base - 1];
	}

	/**
	 * @param level
	 *            Stage number.
	 * @return Name of the boss of that stage, or null if there is none.
	 */
	public static String bossName(final int level) {
		if (!StageHud.isBossLevel(level))
			return null;
		return GameMode.baseLevel(level) == 5 ? "WARDEN FRIGATE"
				: "DREADNOUGHT";
	}

	/**
	 * Progress of the hyperspace jump.
	 *
	 * @param elapsed
	 *            Time since the stage started, in ms.
	 * @return 0 at the start of the jump to 1 at its end, or -1 once the
	 *         ship is out of hyperspace.
	 */
	public static float jumpProgress(final long elapsed) {
		if (elapsed < 0 || elapsed >= JUMP_MS)
			return -1f;
		return (float) elapsed / JUMP_MS;
	}

	/**
	 * Length of the star streaks during the jump: they grow quickly, then
	 * shrink back to dots as the ship drops out of hyperspace.
	 *
	 * @param progress
	 *            Jump progress from {@link #jumpProgress}.
	 * @return Streak length factor, 0 to 1.
	 */
	public static float streakLength(final float progress) {
		if (progress < 0f)
			return 0f;
		return (float) Math.sin(Math.PI * Math.min(1f, progress));
	}
}
