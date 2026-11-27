package engine;

/**
 * Game modes picked from the "Modes" menu (Team ECE - Special Game Modes,
 * KAN-52 / KAN-56).
 *
 * A mode decides which stages a run goes through and how hard they are;
 * the stages themselves are the normal {@link screen.GameScreen}. The
 * rules here have no side effect, so they are unit tested.
 */
public enum GameMode {

	/** The normal run: stages 1 to 10, bosses on 5 and 10. */
	CLASSIC("Classic", "10 stages, 2 bosses. The original run."),
	/** The normal run with the space opera theme (KAN-55). */
	SPACE_OPERA("Space Opera",
			"Hyperspace jumps, laser duels and a dreadnought."),
	/** Endless stages that keep getting harder (KAN-53). */
	SURVIVAL("Survival", "Endless stages. Every 10 stages they get faster."),
	/** Only the boss stages, back to back (KAN-54). */
	BOSS_RUSH("Boss Rush", "Only the bosses, back to back. No breathers.");

	/** Number of stages of a normal run. */
	public static final int RUN_LENGTH = 10;
	/** Shooting interval kept by Survival, however hard it gets, in ms. */
	public static final int MIN_SHOOTING_INTERVAL = 150;
	/** Each Survival loop shoots this much more often. */
	public static final double LOOP_SHOOTING_FACTOR = 0.8;

	/** Mode of the current or next run. */
	private static GameMode current = CLASSIC;

	/** Name shown in the menu. */
	private final String title;
	/** One-line description shown in the menu. */
	private final String description;

	GameMode(final String title, final String description) {
		this.title = title;
		this.description = description;
	}

	/** @return Name shown in the menu. */
	public String getTitle() {
		return this.title;
	}

	/** @return One-line description shown in the menu. */
	public String getDescription() {
		return this.description;
	}

	/** @return Mode of the current or next run. */
	public static GameMode current() {
		return current;
	}

	/**
	 * Sets the mode of the next run.
	 *
	 * @param mode
	 *            Mode to play; null means Classic.
	 */
	public static void select(final GameMode mode) {
		current = mode == null ? CLASSIC : mode;
	}

	/** @return First stage of a run in this mode. */
	public int firstLevel() {
		return this == BOSS_RUSH ? 5 : 1;
	}

	/**
	 * @param level
	 *            Stage just played.
	 * @return Next stage. Past the last stage of the mode, a number for
	 *         which {@link #hasLevel} is false.
	 */
	public int nextLevel(final int level) {
		if (this == BOSS_RUSH)
			return level < 5 ? 5 : level < RUN_LENGTH ? RUN_LENGTH
					: RUN_LENGTH + 1;
		return level + 1;
	}

	/**
	 * @param level
	 *            A stage number.
	 * @return True if a run in this mode plays that stage.
	 */
	public boolean hasLevel(final int level) {
		if (level < 1)
			return false;
		switch (this) {
		case SURVIVAL:
			return true;
		case BOSS_RUSH:
			return level == 5 || level == RUN_LENGTH;
		default:
			return level <= RUN_LENGTH;
		}
	}

	/**
	 * @return True for the modes that play the whole 10-stage run, the
	 *         only ones that can unlock "game won" achievements.
	 */
	public boolean isFullRun() {
		return this == CLASSIC || this == SPACE_OPERA;
	}

	/** @return True if the run should start with the tutorial. */
	public boolean showsTutorial() {
		return this == CLASSIC;
	}

	/**
	 * @param level
	 *            A stage number.
	 * @return Survival loop of that stage: 0 for stages 1 to 10, 1 for 11
	 *         to 20, and so on.
	 */
	public static int loopOf(final int level) {
		return Math.max(0, level - 1) / RUN_LENGTH;
	}

	/**
	 * @param level
	 *            A stage number, any size.
	 * @return The matching stage of a normal run, 1 to 10, so stage 15 is
	 *         played like stage 5 (a mid boss).
	 */
	public static int baseLevel(final int level) {
		return Math.max(0, level - 1) % RUN_LENGTH + 1;
	}

	/**
	 * Difficulty of a stage. Stages 1 to 10 use the normal settings; past
	 * stage 10 (Survival), the stage of the same rank is reused with
	 * enemies that shoot more often and move faster, and one more column
	 * per loop (up to two).
	 *
	 * @param level
	 *            Stage number.
	 * @param base
	 *            Settings of stages 1 to 10.
	 * @return Settings to play that stage with.
	 */
	public static GameSettings settingsFor(final int level,
			final java.util.List<GameSettings> base) {
		GameSettings normal = base.get(baseLevel(level) - 1);
		int loop = loopOf(level);
		if (loop == 0)
			return normal;
		int width = normal.getFormationWidth() + Math.min(loop, 2);
		int speed = Math.max(1, normal.getBaseSpeed() - 5 * loop);
		int shooting = Math.max(MIN_SHOOTING_INTERVAL,
				(int) Math.round(normal.getShootingFrecuency()
						* Math.pow(LOOP_SHOOTING_FACTOR, loop)));
		return new GameSettings(width, normal.getFormationHeight(), speed,
				shooting, normal.isDiagonalMovement(),
				normal.getTwoHitEnemyCount());
	}
}
