package engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** High scores per game mode (KAN-59). */
public class ModeScoresTest {

	@Test
	public void classicKeepsTheOriginalFile() {
		assertEquals("scores", FileManager.scoresFileName(GameMode.CLASSIC));
		assertEquals("scores", FileManager.scoresFileName(null));
		assertEquals("scores-survival",
				FileManager.scoresFileName(GameMode.SURVIVAL));
		assertEquals("scores-boss_rush",
				FileManager.scoresFileName(GameMode.BOSS_RUSH));
	}

	@Test
	public void oldScoreLinesStillLoad() {
		Score old = Score.fromSaveLines("ABC", "1200");
		assertEquals("ABC", old.getName());
		assertEquals(1200, old.getScore());
		assertEquals(0, old.getStage());
		assertEquals("1200", old.toSaveLine());
	}

	@Test
	public void survivalScoresKeepTheStage() {
		Score survival = new Score("ZED", 5400, 14);
		assertEquals("5400 14", survival.toSaveLine());
		Score loaded = Score.fromSaveLines("ZED", survival.toSaveLine());
		assertEquals(5400, loaded.getScore());
		assertEquals(14, loaded.getStage());
	}
}
