package engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Extra life every 1000 points, capped by the max lives (KAN-25). */
public class ExtraLifeTrackerTest {

	@Test
	public void oneLifePerThreshold() {
		ExtraLifeTracker tracker = new ExtraLifeTracker(0);
		assertEquals(0, tracker.update(0, 999, 3, 5));
		assertEquals(1, tracker.update(0, 1000, 3, 5));
		assertEquals(2000, tracker.getNextLifeAt());
	}

	@Test
	public void bigJumpGivesSeveralLivesUpToTheCap() {
		ExtraLifeTracker tracker = new ExtraLifeTracker(0);
		assertEquals(1, tracker.update(0, 3500, 4, 5));
		assertEquals(4000, tracker.getNextLifeAt());
	}

	@Test
	public void progressTowardTheNextLife() {
		ExtraLifeTracker tracker = new ExtraLifeTracker(1200);
		assertEquals(0.3f, tracker.getProgress(1300), 0.001f);
	}
}
