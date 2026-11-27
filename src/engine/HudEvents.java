package engine;

/**
 * Connects the gameplay HUD to the {@link GameEvents} bus (Requirement 8 -
 * Gameplay HUD, KAN-38), so the HUD reacts to what happens in the game
 * instead of GameScreen calling each HUD piece by hand.
 *
 * Subscribe with {@link #attach} when a stage starts and unsubscribe with
 * {@link #detach} when its screen closes, so no listener leaks from one
 * stage or run into the next.
 */
public final class HudEvents implements GameEvents.Listener {

	/** How long, in ms, the kill counter flashes after a kill. */
	public static final long KILL_FLASH_MS = 150;

	/** Combo to break when the player is hit. */
	private final ComboTracker combo;
	/** Time of the last kill, or -1. */
	private long lastKillAt = -1;
	/** True while subscribed. */
	private boolean attached;

	/**
	 * @param combo
	 *            Combo of the stage.
	 */
	public HudEvents(final ComboTracker combo) {
		this.combo = combo;
	}

	/** Subscribes to the events the HUD reacts to. */
	public void attach() {
		if (this.attached)
			return;
		GameEvents.subscribe(GameEvents.Type.PLAYER_HIT, this);
		GameEvents.subscribe(GameEvents.Type.ENEMY_KILLED, this);
		GameEvents.subscribe(GameEvents.Type.LEVEL_START, this);
		this.attached = true;
	}

	/** Unsubscribes from every event. Safe to call twice. */
	public void detach() {
		if (!this.attached)
			return;
		GameEvents.unsubscribe(GameEvents.Type.PLAYER_HIT, this);
		GameEvents.unsubscribe(GameEvents.Type.ENEMY_KILLED, this);
		GameEvents.unsubscribe(GameEvents.Type.LEVEL_START, this);
		this.attached = false;
	}

	@Override
	public void onEvent(final GameEvents.Type type, final int value) {
		long now = System.currentTimeMillis();
		switch (type) {
		case PLAYER_HIT:
			this.combo.breakCombo(now);
			break;
		case ENEMY_KILLED:
			this.lastKillAt = now;
			break;
		case LEVEL_START:
			// The first stage of the mode starts a new run (KAN-37); Boss
			// Rush starts on stage 5 (KAN-58).
			if (value == GameMode.current().firstLevel())
				RunStats.startRun();
			break;
		default:
			break;
		}
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True right after a kill, while the kill counter flashes.
	 */
	public boolean isKillFlashOn(final long now) {
		return this.lastKillAt >= 0 && now - this.lastKillAt < KILL_FLASH_MS;
	}

	/** @return True while subscribed. */
	public boolean isAttached() {
		return this.attached;
	}
}
