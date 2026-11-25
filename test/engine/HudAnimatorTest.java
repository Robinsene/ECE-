package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/** Score count-up, life blink, low-life pulse and level intro (KAN-35). */
public class HudAnimatorTest {

	private HudAnimator hud;

	@Before
	public void setUp() {
		hud = new HudAnimator();
		hud.update(0, 100, 3, 0, 0); // first frame: shown as is
	}

	@Test
	public void firstFrameShowsScoreWithoutAnimation() {
		assertEquals(100, hud.getDisplayedScore());
		assertFalse(hud.isScoreFlashing(0));
	}

	@Test
	public void scoreCountsUpThenReachesTarget() {
		hud.update(16, 1100, 3, 0, 0);
		int midway = hud.getDisplayedScore();
		assertTrue("score should roll, not jump", midway > 100 && midway < 1100);
		assertTrue(hud.isScoreFlashing(16));
		for (long t = 32; t <= 1000; t += 16)
			hud.update(t, 1100, 3, 0, 0);
		assertEquals(1100, hud.getDisplayedScore());
		assertFalse(hud.isScoreFlashing(1000));
	}

	@Test
	public void scoreGoingDownIsShownAtOnce() {
		hud.update(16, 40, 3, 0, 0);
		assertEquals(40, hud.getDisplayedScore());
	}

	@Test
	public void lostLifeBlinksThenStops() {
		hud.update(1000, 100, 2, 0, 0);
		assertEquals(2, hud.getLostSlotToDraw(1000));
		assertEquals(-1, hud.getLostSlotToDraw(1000 + HudAnimator.LIFE_BLINK_PERIOD_MS));
		assertEquals(-1, hud.getLostSlotToDraw(1000 + HudAnimator.LIFE_BLINK_MS));
	}

	@Test
	public void gainedLifeBlinks() {
		hud.update(1000, 100, 4, 0, 0);
		assertEquals(-1, hud.getGainedSlotToHide(1000));
		assertEquals(3, hud.getGainedSlotToHide(1000 + HudAnimator.LIFE_BLINK_PERIOD_MS));
		assertEquals(-1, hud.getGainedSlotToHide(1000 + HudAnimator.LIFE_BLINK_MS));
	}

	@Test
	public void lowLifePulseOnlyOnLastLife() {
		assertFalse(hud.isLowLifePulseOn(0));
		hud.update(16, 100, 1, 0, 0);
		assertTrue(hud.isLowLifePulseOn(0));
		assertFalse(hud.isLowLifePulseOn(HudAnimator.LOW_LIFE_PULSE_MS));
		hud.update(32, 100, 0, 0, 0);
		assertFalse("no pulse once the run is over", hud.isLowLifePulseOn(0));
	}

	@Test
	public void levelIntroPulsesForAWhile() {
		assertFalse(hud.isLevelPulseOn(0));
		hud.startLevel(5000);
		hud.startLevel(5200); // only the first call counts
		assertTrue(hud.isLevelPulseOn(5000));
		assertFalse(hud.isLevelPulseOn(5000 + HudAnimator.LEVEL_PULSE_MS));
		assertFalse(hud.isLevelPulseOn(5000 + HudAnimator.LEVEL_INTRO_MS));
	}

	@Test
	public void currencyGainFlashes() {
		hud.update(100, 100, 3, 5, 0);
		assertTrue(hud.isCoinFlashing(100));
		assertFalse(hud.isDiamondFlashing(100));
		assertFalse(hud.isCoinFlashing(100 + HudAnimator.CURRENCY_FLASH_MS));
		hud.update(200, 100, 3, 2, 1);
		assertTrue(hud.isDiamondFlashing(200));
	}
}
