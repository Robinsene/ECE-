package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Kill combo, multiplier tiers, timeout and pause (KAN-23, KAN-35). */
public class ComboTrackerTest {

	@Test
	public void multiplierTiers() {
		assertEquals(1, ComboTracker.multiplierFor(4));
		assertEquals(2, ComboTracker.multiplierFor(5));
		assertEquals(3, ComboTracker.multiplierFor(10));
		assertEquals(4, ComboTracker.multiplierFor(20));
	}

	@Test
	public void comboEndsAfterTheWindow() {
		ComboTracker combo = new ComboTracker();
		combo.registerKill(0);
		combo.registerKill(500);
		assertEquals(2, combo.getCombo());
		combo.update(500 + ComboTracker.COMBO_WINDOW_MS + 1);
		assertEquals(0, combo.getCombo());
		assertEquals(2, combo.getBestCombo());
	}

	@Test
	public void endedComboFadesOut() {
		ComboTracker combo = new ComboTracker();
		combo.registerKill(0);
		combo.registerKill(10);
		combo.breakCombo(20);
		assertEquals(2, combo.getShownCombo(20));
		assertTrue(combo.getAlpha(20 + ComboTracker.FADE_MS / 2) < 1f);
		assertEquals(0, combo.getShownCombo(20 + ComboTracker.FADE_MS));
	}

	@Test
	public void pauseDoesNotEatTheComboWindow() {
		ComboTracker combo = new ComboTracker();
		combo.registerKill(0);
		for (long t = 100; t <= 10000; t += 100)
			combo.holdDuringPause(t);
		combo.update(10050);
		assertEquals(1, combo.getCombo());
	}
}
