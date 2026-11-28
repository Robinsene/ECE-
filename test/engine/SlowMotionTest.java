package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Slow motion on the final hit of a boss (KAN-66). */
public class SlowMotionTest {

	@Test
	public void offUntilTriggered() {
		SlowMotion slow = new SlowMotion();
		assertFalse(slow.isActive(0));
		assertFalse(slow.skipFrame(0));
		assertFalse(slow.skipFrame(16));
	}

	@Test
	public void skipsOneFrameOutOfTwo() {
		SlowMotion slow = new SlowMotion();
		slow.trigger(1000);
		int skipped = 0;
		for (int frame = 0; frame < 20; frame++)
			if (slow.skipFrame(1000 + frame * 16))
				skipped++;
		assertEquals(10, skipped);
	}

	@Test
	public void endsAfterItsDuration() {
		SlowMotion slow = new SlowMotion();
		slow.trigger(1000);
		assertTrue(slow.isActive(1000 + SlowMotion.DURATION_MS - 1));
		assertFalse(slow.isActive(1000 + SlowMotion.DURATION_MS));
		assertFalse(slow.skipFrame(1000 + SlowMotion.DURATION_MS));
	}

	@Test
	public void retriggerRestartsTheTimer() {
		SlowMotion slow = new SlowMotion();
		slow.trigger(1000);
		slow.trigger(1500);
		assertTrue(slow.isActive(1500 + SlowMotion.DURATION_MS - 1));
	}
}
