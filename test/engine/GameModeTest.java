package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

/** Stage order and difficulty of the special game modes. */
public class GameModeTest {

	@After
	public void tearDown() {
		GameMode.select(GameMode.CLASSIC);
	}

	@Test
	public void classicAndSpaceOperaPlayTheTenStages() {
		for (GameMode mode : new GameMode[] { GameMode.CLASSIC,
				GameMode.SPACE_OPERA }) {
			assertEquals(1, mode.firstLevel());
			assertTrue(mode.hasLevel(10));
			assertFalse(mode.hasLevel(mode.nextLevel(10)));
			assertTrue(mode.isFullRun());
		}
		assertTrue(GameMode.CLASSIC.showsTutorial());
		assertFalse(GameMode.SPACE_OPERA.showsTutorial());
	}

	@Test
	public void bossRushOnlyPlaysTheBosses() {
		GameMode rush = GameMode.BOSS_RUSH;
		assertEquals(5, rush.firstLevel());
		assertEquals(10, rush.nextLevel(5));
		assertFalse(rush.hasLevel(rush.nextLevel(10)));
		assertFalse(rush.hasLevel(6));
		assertFalse(rush.isFullRun());
	}

	@Test
	public void survivalNeverEnds() {
		GameMode survival = GameMode.SURVIVAL;
		assertTrue(survival.hasLevel(10));
		assertTrue(survival.hasLevel(11));
		assertTrue(survival.hasLevel(250));
		assertFalse(survival.hasLevel(0));
		assertEquals(12, survival.nextLevel(11));
	}

	@Test
	public void stagesPastTenReuseTheNormalOnes() {
		assertEquals(0, GameMode.loopOf(10));
		assertEquals(1, GameMode.loopOf(11));
		assertEquals(5, GameMode.baseLevel(15));
		assertEquals(10, GameMode.baseLevel(20));
		assertTrue(StageHud.isBossLevel(15));
		assertTrue(StageHud.isBossLevel(20));
		assertFalse(StageHud.isBossLevel(11));
	}

	@Test
	public void survivalLoopsGetHarder() {
		List<GameSettings> base = new ArrayList<GameSettings>();
		for (int i = 1; i <= 10; i++)
			base.add(new GameSettings(5, 4, 60, 2000));
		assertSame(base.get(0), GameMode.settingsFor(1, base));
		GameSettings loop1 = GameMode.settingsFor(11, base);
		assertEquals(6, loop1.getFormationWidth());
		assertEquals(55, loop1.getBaseSpeed());
		assertEquals(1600, loop1.getShootingFrecuency());
		GameSettings loop20 = GameMode.settingsFor(201, base);
		assertEquals(7, loop20.getFormationWidth());
		assertEquals(GameMode.MIN_SHOOTING_INTERVAL,
				loop20.getShootingFrecuency());
		assertEquals(1, loop20.getBaseSpeed());
	}

	@Test
	public void selectingNullFallsBackToClassic() {
		GameMode.select(GameMode.SURVIVAL);
		assertSame(GameMode.SURVIVAL, GameMode.current());
		GameMode.select(null);
		assertSame(GameMode.CLASSIC, GameMode.current());
	}
}
