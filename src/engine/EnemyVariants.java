package engine;

/**
 * When the new enemy types appear (Team ECE - KAN-67). Divers leave the
 * formation and dive at the player from stage 3; splitters, which break
 * into two fragments, appear from stage 6. As for the attack patterns,
 * each Survival loop counts as 3 more stages.
 */
public final class EnemyVariants {

	/** First stage with divers. */
	public static final int DIVERS_FROM = 3;
	/** First stage with splitters. */
	public static final int SPLITTERS_FROM = 6;
	/** Time between two divers on the first stage that has them, in ms. */
	static final int FIRST_DIVER_INTERVAL = 7000;
	/** Shortest time between two divers, in ms. */
	static final int MIN_DIVER_INTERVAL = 2500;
	/** Interval gained per stage after the first one with divers, in ms. */
	static final int DIVER_INTERVAL_STEP = 500;
	/** Most splitters in one formation. */
	static final int MAX_SPLITTERS = 3;

	private EnemyVariants() {
	}

	/**
	 * @param level
	 *            Stage number.
	 * @return Stage used to unlock the variants: the stage of the normal
	 *         run, plus 3 per Survival loop.
	 */
	public static int effectiveStage(final int level) {
		int safeLevel = Math.max(1, level);
		return GameMode.baseLevel(safeLevel) + 3 * GameMode.loopOf(safeLevel);
	}

	/**
	 * @param level
	 *            Stage number.
	 * @return Time between two divers in ms, or 0 when the stage has none.
	 */
	public static int diverInterval(final int level) {
		int stage = effectiveStage(level);
		if (stage < DIVERS_FROM)
			return 0;
		return Math.max(MIN_DIVER_INTERVAL, FIRST_DIVER_INTERVAL
				- DIVER_INTERVAL_STEP * (stage - DIVERS_FROM));
	}

	/**
	 * @param level
	 *            Stage number.
	 * @return Number of splitters in the formation of that stage.
	 */
	public static int splitterCount(final int level) {
		int stage = effectiveStage(level);
		if (stage < SPLITTERS_FROM)
			return 0;
		return Math.min(MAX_SPLITTERS, 1 + (stage - SPLITTERS_FROM) / 2);
	}
}
