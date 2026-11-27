package engine;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Keeps the score to beat during a run (Requirement 8 - Gameplay HUD,
 * KAN-24): the best saved high score, and the moment the current run
 * passes it.
 *
 * It holds no drawing code, and reads the high score file only once,
 * when it is created, never from the draw loop.
 */
public class BestScoreTracker {

	/** How long, in ms, the "NEW BEST!" flash lasts. */
	public static final long NEW_BEST_FLASH_MS = 2000;
	/** Blink half-period, in ms, of the "NEW BEST!" flash. */
	public static final long NEW_BEST_BLINK_MS = 150;

	/** Best saved score when the level started. */
	private final int savedBest;
	/** True once the current run has passed the saved best score. */
	private boolean beaten;
	/** Time the saved best score was passed, or -1. */
	private long beatenAt = -1;

	/**
	 * Creates a tracker from a known best score.
	 *
	 * @param savedBest
	 *            Best saved score (0 when there is none).
	 * @param score
	 *            Score of the run when the level starts.
	 */
	public BestScoreTracker(final int savedBest, final int score) {
		this.savedBest = Math.max(0, savedBest);
		// Already beaten in an earlier level of this run: no new flash.
		this.beaten = this.savedBest > 0 && score > this.savedBest;
	}

	/**
	 * Creates a tracker from the saved high score file.
	 *
	 * @param score
	 *            Score of the run when the level starts.
	 * @param logger
	 *            Logger to report a missing or unreadable file.
	 * @return The tracker.
	 */
	public static BestScoreTracker fromHighScores(final int score,
			final Logger logger) {
		int best = 0;
		try {
			// Best score of the mode being played (KAN-59).
			List<Score> highScores = Core.getFileManager()
					.loadHighScores(GameMode.current());
			for (Score highScore : highScores)
				best = Math.max(best, highScore.getScore());
		} catch (IOException | RuntimeException e) {
			logger.warning("Could not load high scores for the HUD: "
					+ e.getMessage());
		}
		return new BestScoreTracker(best, score);
	}

	/**
	 * Checks whether the run just passed the saved best score. Called once
	 * per frame.
	 *
	 * @param now
	 *            Current time in ms.
	 * @param score
	 *            Current score.
	 */
	public void update(final long now, final int score) {
		if (!this.beaten && this.savedBest > 0 && score > this.savedBest) {
			this.beaten = true;
			this.beatenAt = now;
		}
	}

	/**
	 * Forgets a record beaten during this level, used when the level is
	 * restarted and the score goes back down.
	 *
	 * @param score
	 *            Score after the restart.
	 */
	public void reset(final int score) {
		this.beaten = this.savedBest > 0 && score > this.savedBest;
		this.beatenAt = -1;
	}

	/**
	 * @param score
	 *            Current score.
	 * @return Best score to show: the saved one, or the current score once
	 *         it is higher.
	 */
	public int getShownBest(final int score) {
		return Math.max(this.savedBest, score);
	}

	/**
	 * @return True once the current run has passed the saved best score.
	 */
	public boolean isBeaten() {
		return this.beaten;
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True on the "on" frames of the "NEW BEST!" flash.
	 */
	public boolean isNewBestFlashOn(final long now) {
		if (this.beatenAt < 0)
			return false;
		long elapsed = now - this.beatenAt;
		return elapsed < NEW_BEST_FLASH_MS
				&& (elapsed / NEW_BEST_BLINK_MS) % 2 == 0;
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True while the "NEW BEST!" flash is running.
	 */
	public boolean isNewBestShowing(final long now) {
		return this.beatenAt >= 0 && now - this.beatenAt < NEW_BEST_FLASH_MS;
	}
}
