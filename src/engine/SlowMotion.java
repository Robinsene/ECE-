package engine;

/**
 * Short slow motion on the final hit of a boss (Team ECE - KAN-66). While
 * it runs, the game skips the gameplay update one frame out of two, so
 * everything on screen moves at half speed. Drawing goes on every frame.
 */
public final class SlowMotion {

	/** How long the slow motion lasts, in ms. */
	public static final long DURATION_MS = 700;

	/** Time the slow motion ends, or -1 when it never ran. */
	private long endsAt = -1;
	/** Whether the previous frame was skipped. */
	private boolean skippedLast;

	/**
	 * Starts the slow motion, or restarts it.
	 *
	 * @param now
	 *            Current time, in ms.
	 */
	public void trigger(final long now) {
		this.endsAt = now + DURATION_MS;
		this.skippedLast = false;
	}

	/**
	 * @param now
	 *            Current time, in ms.
	 * @return True while the slow motion runs.
	 */
	public boolean isActive(final long now) {
		return this.endsAt >= 0 && now < this.endsAt;
	}

	/**
	 * Called once per frame.
	 *
	 * @param now
	 *            Current time, in ms.
	 * @return True when this frame's gameplay update should be skipped.
	 */
	public boolean skipFrame(final long now) {
		if (!isActive(now)) {
			this.skippedLast = false;
			return false;
		}
		this.skippedLast = !this.skippedLast;
		return this.skippedLast;
	}
}
