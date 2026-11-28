package engine;

/**
 * What happened during a run, for the end-of-run stats on the score
 * screen (Team ECE - KAN-58). Built once when the run ends; no drawing
 * code, so it is unit tested.
 */
public final class RunSummary {

	/** Mode played. */
	private final GameMode mode;
	/** Difficulty of the run, shown when it is not Normal (KAN-68). */
	private Difficulty difficulty = Difficulty.NORMAL;
	/** Stages cleared alive. */
	private final int stagesCleared;
	/** Play time of the run, in ms. */
	private final long playMillis;
	/** Enemies destroyed. */
	private final int kills;
	/** Best kill combo of the run. */
	private final int bestCombo;
	/** Bosses destroyed. */
	private final int bosses;
	/** Coins picked up. */
	private final int coins;
	/** True if the best combo beats the player's record. */
	private boolean comboRecord;
	/** True if the run lasted longer than the player's longest run. */
	private boolean timeRecord;

	/**
	 * @param mode
	 *            Mode played.
	 * @param stagesCleared
	 *            Stages cleared alive.
	 * @param playMillis
	 *            Play time, in ms.
	 * @param kills
	 *            Enemies destroyed.
	 * @param bestCombo
	 *            Best kill combo.
	 * @param bosses
	 *            Bosses destroyed.
	 * @param coins
	 *            Coins picked up.
	 */
	public RunSummary(final GameMode mode, final int stagesCleared,
			final long playMillis, final int kills, final int bestCombo,
			final int bosses, final int coins) {
		this.mode = mode == null ? GameMode.CLASSIC : mode;
		this.stagesCleared = Math.max(0, stagesCleared);
		this.playMillis = Math.max(0, playMillis);
		this.kills = Math.max(0, kills);
		this.bestCombo = Math.max(0, bestCombo);
		this.bosses = Math.max(0, bosses);
		this.coins = Math.max(0, coins);
	}

	/**
	 * Compares the run with the player's records and marks what it beat.
	 *
	 * @param recordCombo
	 *            Best combo so far.
	 * @param recordMillis
	 *            Longest run so far, in ms.
	 */
	public void compareWith(final int recordCombo, final long recordMillis) {
		this.comboRecord = this.bestCombo > 0 && this.bestCombo > recordCombo;
		this.timeRecord = this.playMillis > 0
				&& this.playMillis > recordMillis;
	}

	/**
	 * Sets the difficulty shown on the first line (Team ECE - KAN-68).
	 *
	 * @param runDifficulty
	 *            Difficulty of the run.
	 * @return This summary.
	 */
	public RunSummary withDifficulty(final Difficulty runDifficulty) {
		this.difficulty = runDifficulty == null ? Difficulty.NORMAL
				: runDifficulty;
		return this;
	}

	/** @return First line: mode (and difficulty), stages, time. */
	public String firstLine() {
		String level = this.difficulty == Difficulty.NORMAL ? ""
				: " " + this.difficulty.getLabel().toUpperCase();
		return "MODE " + this.mode.getTitle().toUpperCase() + level
				+ "   STAGES "
				+ this.stagesCleared + "   TIME "
				+ RunStats.format(this.playMillis);
	}

	/** @return Second line: kills, combo, bosses, coins. */
	public String secondLine() {
		return "KILLS " + this.kills + "   BEST COMBO x" + this.bestCombo
				+ "   BOSSES " + this.bosses + "   COINS " + this.coins;
	}

	/** @return Best combo of the run. */
	public int getBestCombo() {
		return this.bestCombo;
	}

	/** @return Play time, in ms. */
	public long getPlayMillis() {
		return this.playMillis;
	}

	/** @return True if the best combo is a new record. */
	public boolean isComboRecord() {
		return this.comboRecord;
	}

	/** @return True if this is the player's longest run. */
	public boolean isTimeRecord() {
		return this.timeRecord;
	}
}
