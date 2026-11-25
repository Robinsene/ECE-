package engine;

/**
 * Play time of the current run, across stages (Requirement 8 - Gameplay
 * HUD, KAN-37). Each stage has its own GameScreen, so the run clock lives
 * here and is reset when a new run starts at stage 1.
 *
 * Only time spent playing counts: the screen calls {@link #tick} once per
 * playing frame, and not while paused or during the countdown.
 */
public final class RunStats {

	/** Longest gap, in ms, between two ticks that still counts as play
	 * (longer gaps are a pause, a loading screen or a debugger stop). */
	private static final long MAX_TICK_MS = 250;

	/** Play time of the run, in ms. */
	private static long playMillis;
	/** Time of the last tick, or -1. */
	private static long lastTick = -1;

	private RunStats() {
	}

	/** Starts a new run: play time back to 0. */
	public static synchronized void startRun() {
		playMillis = 0;
		lastTick = -1;
	}

	/**
	 * Adds the time since the last tick. Called once per playing frame.
	 *
	 * @param now
	 *            Current time in ms.
	 */
	public static synchronized void tick(final long now) {
		if (lastTick >= 0) {
			long delta = now - lastTick;
			if (delta > 0 && delta <= MAX_TICK_MS)
				playMillis += delta;
		}
		lastTick = now;
	}

	/** Stops counting until the next tick, e.g. while paused. */
	public static synchronized void hold() {
		lastTick = -1;
	}

	/**
	 * @return Play time of the run, in ms.
	 */
	public static synchronized long getPlayMillis() {
		return playMillis;
	}

	/**
	 * @param millis
	 *            A duration in ms.
	 * @return That duration as m:ss.
	 */
	public static String format(final long millis) {
		long seconds = Math.max(0, millis) / 1000;
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}
}
