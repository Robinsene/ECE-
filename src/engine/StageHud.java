package engine;

/**
 * State of the stage HUD (Requirement 8 - Gameplay HUD): wave progress
 * (KAN-29), the "STAGE CLEAR" banner (KAN-30) and the boss warning
 * (KAN-31).
 *
 * It holds no drawing code. The game screen feeds it the enemy counts and
 * level events, and the draw manager asks it what to show. Every method
 * takes the current time explicitly, so the class can be driven by a fake
 * clock in tests.
 */
public class StageHud {

	/** Time, in ms, the "STAGE CLEAR" banner takes to slide in. */
	public static final long BANNER_SLIDE_MS = 300;
	/** Delay, in ms, before the "NEXT: STAGE" line appears. */
	public static final long NEXT_LINE_DELAY_MS = 500;
	/** Blink half-period, in ms, of the boss warning. */
	public static final long WARNING_BLINK_MS = 250;
	/** Levels that end with a boss. */
	private static final int[] BOSS_LEVELS = { 5, 10 };

	/** Enemies in the wave when the level started. */
	private int waveTotal;
	/** Enemies still alive. */
	private int waveRemaining;

	/** Time the stage was cleared, or -1. */
	private long clearedAt = -1;
	/** Points earned during the cleared stage. */
	private int stagePoints;

	/**
	 * Records the size of the wave at the start of the level.
	 *
	 * @param total
	 *            Enemies in the formation.
	 */
	public void startWave(final int total) {
		this.waveTotal = Math.max(0, total);
		this.waveRemaining = this.waveTotal;
		this.clearedAt = -1;
	}

	/**
	 * Updates the enemies still alive. Called once per frame.
	 *
	 * @param remaining
	 *            Enemies still alive.
	 */
	public void updateWave(final int remaining) {
		this.waveRemaining = Math.max(0, Math.min(this.waveTotal, remaining));
	}

	/**
	 * @return Enemies in the wave when the level started.
	 */
	public int getWaveTotal() {
		return this.waveTotal;
	}

	/**
	 * @return Enemies still alive.
	 */
	public int getWaveRemaining() {
		return this.waveRemaining;
	}

	/**
	 * @return Share of the wave destroyed, from 0 to 1.
	 */
	public float getWaveProgress() {
		if (this.waveTotal == 0)
			return 0f;
		return 1f - (float) this.waveRemaining / this.waveTotal;
	}

	/**
	 * Starts the "STAGE CLEAR" banner. Only the first call counts.
	 *
	 * @param now
	 *            Current time in ms.
	 * @param points
	 *            Points earned during the stage.
	 */
	public void stageCleared(final long now, final int points) {
		if (this.clearedAt >= 0)
			return;
		this.clearedAt = now;
		this.stagePoints = Math.max(0, points);
	}

	/**
	 * @return True once the stage has been cleared.
	 */
	public boolean isStageCleared() {
		return this.clearedAt >= 0;
	}

	/**
	 * @return Points earned during the cleared stage.
	 */
	public int getStagePoints() {
		return this.stagePoints;
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return How far the banner has slid in, from 0 (off screen) to 1.
	 */
	public float getBannerSlide(final long now) {
		if (this.clearedAt < 0)
			return 0f;
		float t = Math.min(1f, (float) (now - this.clearedAt)
				/ BANNER_SLIDE_MS);
		// Ease out: fast start, soft landing.
		return 1f - (1f - t) * (1f - t);
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True once the "NEXT: STAGE" line should be shown.
	 */
	public boolean isNextLineShown(final long now) {
		return this.clearedAt >= 0
				&& now - this.clearedAt >= NEXT_LINE_DELAY_MS;
	}

	/**
	 * @param level
	 *            A level number.
	 * @return True if that level ends with a boss.
	 */
	public static boolean isBossLevel(final int level) {
		// Survival keeps going past stage 10: stage 15 is played like 5.
		int base = level > 0 ? GameMode.baseLevel(level) : level;
		for (int bossLevel : BOSS_LEVELS)
			if (bossLevel == base)
				return true;
		return false;
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True on the "on" frames of the boss warning blink.
	 */
	public static boolean isWarningBlinkOn(final long now) {
		return (now / WARNING_BLINK_MS) % 2 == 0;
	}
}
