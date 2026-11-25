package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

/** HUD listeners on the GameEvents bus (KAN-38). */
public class HudEventsTest {

	private final ComboTracker combo = new ComboTracker();
	private final HudEvents events = new HudEvents(combo);

	@After
	public void tearDown() {
		events.detach();
	}

	@Test
	public void playerHitBreaksTheCombo() {
		events.attach();
		long now = System.currentTimeMillis();
		combo.registerKill(now);
		combo.registerKill(now);
		GameEvents.emit(GameEvents.Type.PLAYER_HIT, 2);
		assertEquals(0, combo.getCombo());
	}

	@Test
	public void killFlashesTheCounter() {
		events.attach();
		GameEvents.emit(GameEvents.Type.ENEMY_KILLED, 10);
		assertTrue(events.isKillFlashOn(System.currentTimeMillis()));
	}

	@Test
	public void stageOneStartsANewRun() {
		events.attach();
		RunStats.startRun();
		RunStats.tick(0);
		RunStats.tick(100);
		GameEvents.emit(GameEvents.Type.LEVEL_START, 4);
		assertEquals(100, RunStats.getPlayMillis());
		GameEvents.emit(GameEvents.Type.LEVEL_START, 1);
		assertEquals(0, RunStats.getPlayMillis());
	}

	@Test
	public void detachedListenerIgnoresEvents() {
		events.attach();
		events.attach(); // subscribing twice must not double the listener
		events.detach();
		assertFalse(events.isAttached());
		combo.registerKill(System.currentTimeMillis());
		GameEvents.emit(GameEvents.Type.PLAYER_HIT, 1);
		assertEquals(1, combo.getCombo());
	}
}
