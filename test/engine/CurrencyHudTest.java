package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Rolling balance, +N/-N popup merging and number format (KAN-35). */
public class CurrencyHudTest {

	@Test
	public void firstValueIsShownWithoutPopup() {
		CurrencyHud.Track track = new CurrencyHud.Track();
		track.update(250, 0);
		assertEquals(250, track.shownValue());
		assertEquals(1.0, track.popup(0), 0.0);
	}

	@Test
	public void balanceRollsToTheNewValue() {
		CurrencyHud.Track track = new CurrencyHud.Track();
		track.update(0, 0);
		track.update(100, 0);
		track.update(100, 50);
		int midway = track.shownValue();
		assertTrue(midway > 0 && midway < 100);
		track.update(100, 1000);
		assertEquals(100, track.shownValue());
	}

	@Test
	public void quickGainsMergeIntoOnePopup() {
		CurrencyHud.Track track = new CurrencyHud.Track();
		track.update(0, 0);
		track.update(5, 100);
		track.update(8, 300);
		assertEquals(8, track.popupAmount);
		assertFalse(track.spending);
		track.update(18, 2000); // too late to merge
		assertEquals(10, track.popupAmount);
	}

	@Test
	public void spendingStartsANewRedPopup() {
		CurrencyHud.Track track = new CurrencyHud.Track();
		track.update(50, 0);
		track.update(60, 100);
		track.update(30, 200);
		assertTrue(track.spending);
		assertEquals(30, track.popupAmount);
	}

	@Test
	public void snapSkipsTheAnimation() {
		CurrencyHud.Track track = new CurrencyHud.Track();
		track.update(0, 0);
		track.update(500, 10);
		track.snap();
		assertEquals(500, track.shownValue());
		assertEquals(1.0, track.pulse(10), 0.0);
	}

	@Test
	public void largeBalancesAreCompact() {
		assertEquals("9999", CurrencyHud.format(9999));
		assertEquals("12.3K", CurrencyHud.format(12345));
		assertEquals("1.5M", CurrencyHud.format(1500000));
		assertEquals("-12.0K", CurrencyHud.format(-12000));
	}
}
