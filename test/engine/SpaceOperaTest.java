package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

/** Space Opera theme: sectors, bosses, hyperspace timing (KAN-55). */
public class SpaceOperaTest {

	@After
	public void tearDown() {
		GameMode.select(GameMode.CLASSIC);
	}

	@Test
	public void onlyActiveInItsMode() {
		assertFalse(SpaceOpera.isActive());
		GameMode.select(GameMode.SPACE_OPERA);
		assertTrue(SpaceOpera.isActive());
	}

	@Test
	public void everyStageHasASector() {
		for (int level = 1; level <= 10; level++)
			assertTrue(SpaceOpera.sectorTitle(level)
					.startsWith("SECTOR " + level + " - "));
		assertEquals("SECTOR 10 - THE DREADNOUGHT",
				SpaceOpera.sectorTitle(10));
	}

	@Test
	public void bossesHaveNames() {
		assertEquals("WARDEN FRIGATE", SpaceOpera.bossName(5));
		assertEquals("DREADNOUGHT", SpaceOpera.bossName(10));
		assertNull(SpaceOpera.bossName(3));
	}

	@Test
	public void hyperspaceJumpGrowsThenEnds() {
		assertEquals(0f, SpaceOpera.jumpProgress(0), 0f);
		assertEquals(0.5f, SpaceOpera.jumpProgress(SpaceOpera.JUMP_MS / 2),
				0.001f);
		assertEquals(-1f, SpaceOpera.jumpProgress(SpaceOpera.JUMP_MS), 0f);
		assertEquals(1f, SpaceOpera.streakLength(0.5f), 0.001f);
		assertEquals(0f, SpaceOpera.streakLength(-1f), 0f);
	}
}
