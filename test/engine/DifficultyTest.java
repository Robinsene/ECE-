package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/** Difficulty setting (KAN-68). */
public class DifficultyTest {

	private final GameSettings stage = new GameSettings(5, 4, 40, 2000, true,
			2);

	@Before
	public void noSaveFile() {
		Difficulty.useMemoryOnly();
	}

	@Test
	public void normalKeepsTheStageTable() {
		assertSame(stage, Difficulty.NORMAL.apply(stage));
		assertEquals(3, Difficulty.NORMAL.getLives());
	}

	@Test
	public void easySlowsTheEnemiesDown() {
		GameSettings easy = Difficulty.EASY.apply(stage);
		assertEquals(56, easy.getBaseSpeed());
		assertEquals(3000, easy.getShootingFrecuency());
		assertEquals(3, Difficulty.EASY.getLives());
	}

	@Test
	public void hardSpeedsUpAndRemovesALife() {
		GameSettings hard = Difficulty.HARD.apply(stage);
		assertEquals(28, hard.getBaseSpeed());
		assertEquals(1300, hard.getShootingFrecuency());
		assertEquals(2, Difficulty.HARD.getLives());
	}

	@Test
	public void formationIsUnchanged() {
		GameSettings hard = Difficulty.HARD.apply(stage);
		assertEquals(5, hard.getFormationWidth());
		assertEquals(4, hard.getFormationHeight());
		assertTrue(hard.isDiagonalMovement());
		assertEquals(2, hard.getTwoHitEnemyCount());
	}

	@Test
	public void limitsAreKept() {
		GameSettings fast = new GameSettings(5, 4, 1, 250);
		GameSettings hard = Difficulty.HARD.apply(fast);
		assertEquals(1, hard.getBaseSpeed());
		assertEquals(Difficulty.MIN_SHOOTING_INTERVAL,
				hard.getShootingFrecuency());
	}

	@Test
	public void stepDoesNotWrap() {
		assertSame(Difficulty.EASY, Difficulty.EASY.step(-1));
		assertSame(Difficulty.NORMAL, Difficulty.EASY.step(1));
		assertSame(Difficulty.HARD, Difficulty.HARD.step(1));
	}

	@Test
	public void selectChangesTheCurrentDifficulty() {
		assertSame(Difficulty.NORMAL, Difficulty.current());
		Difficulty.select(Difficulty.HARD);
		assertSame(Difficulty.HARD, Difficulty.current());
		Difficulty.select(null);
		assertSame(Difficulty.NORMAL, Difficulty.current());
	}

	@Test
	public void scoresKeepTheDifficulty() {
		Score hard = new Score("ZED", 5400, 14, Difficulty.HARD);
		assertEquals("5400 14 H", hard.toSaveLine());
		Score loaded = Score.fromSaveLines("ZED", hard.toSaveLine());
		assertSame(Difficulty.HARD, loaded.getDifficulty());
		assertEquals(14, loaded.getStage());

		Score easyClassic = Score.fromSaveLines("ABC", "1200 E");
		assertSame(Difficulty.EASY, easyClassic.getDifficulty());
		assertEquals(0, easyClassic.getStage());
		assertEquals("1200 E", easyClassic.toSaveLine());

		Score normal = Score.fromSaveLines("ABC", "1200 3");
		assertSame(Difficulty.NORMAL, normal.getDifficulty());
		assertEquals("1200 3", normal.toSaveLine());
	}

	@Test
	public void summaryShowsTheDifficultyWhenNotNormal() {
		RunSummary summary = new RunSummary(GameMode.CLASSIC, 3, 60000, 10,
				2, 0, 5);
		assertTrue(summary.firstLine().startsWith("MODE CLASSIC   "));
		summary.withDifficulty(Difficulty.HARD);
		assertTrue(summary.firstLine().startsWith("MODE CLASSIC HARD   "));
	}
}
