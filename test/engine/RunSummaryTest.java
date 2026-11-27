package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

/** End-of-run stats (KAN-58). */
public class RunSummaryTest {

	@After
	public void tearDown() {
		GameMode.select(GameMode.CLASSIC);
		RunStats.startRun();
	}

	@Test
	public void linesShowTheRun() {
		RunSummary summary = new RunSummary(GameMode.SURVIVAL, 12, 402000,
				87, 14, 2, 340);
		assertEquals("MODE SURVIVAL   STAGES 12   TIME 6:42",
				summary.firstLine());
		assertEquals("KILLS 87   BEST COMBO x14   BOSSES 2   COINS 340",
				summary.secondLine());
	}

	@Test
	public void recordsAreOnlyBeatenByMore() {
		RunSummary summary = new RunSummary(GameMode.CLASSIC, 3, 60000, 20,
				6, 0, 10);
		summary.compareWith(6, 59000);
		assertFalse(summary.isComboRecord());
		assertTrue(summary.isTimeRecord());
		summary.compareWith(0, 0);
		assertTrue(summary.isComboRecord());
	}

	@Test
	public void emptyRunBeatsNothing() {
		RunSummary summary = new RunSummary(null, -1, 0, 0, 0, 0, 0);
		summary.compareWith(0, 0);
		assertFalse(summary.isComboRecord());
		assertFalse(summary.isTimeRecord());
		assertTrue(summary.firstLine().startsWith("MODE CLASSIC"));
	}

	@Test
	public void runStatsKeepTheBestComboAndBosses() {
		RunStats.startRun();
		RunStats.recordCombo(5);
		RunStats.recordCombo(3);
		RunStats.recordBossBeaten();
		assertEquals(5, RunStats.getBestCombo());
		assertEquals(1, RunStats.getBossesBeaten());
		RunStats.startRun();
		assertEquals(0, RunStats.getBestCombo());
	}

	@Test
	public void bossRushStartsItsRunOnStageFive() {
		ComboTracker combo = new ComboTracker();
		HudEvents events = new HudEvents(combo);
		events.attach();
		try {
			GameMode.select(GameMode.BOSS_RUSH);
			RunStats.recordBossBeaten();
			GameEvents.emit(GameEvents.Type.LEVEL_START, 5);
			assertEquals(0, RunStats.getBossesBeaten());
		} finally {
			events.detach();
		}
	}
}
