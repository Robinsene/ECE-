package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Test;

/** Floating "+N" coin popups (KAN-39). */
public class PickupPopupsTest {

	@Test
	public void popupFliesFromPickupToHudAndFades() {
		PickupPopups popups = new PickupPopups();
		popups.add(5, Color.YELLOW, 100, 400, 224, 30, 0);
		PickupPopups.Popup popup = popups.getActive(0).get(0);
		assertEquals("+5", popup.getText());
		assertEquals(100, popup.getX(0));
		assertEquals(400, popup.getY(0));
		assertEquals(224, popup.getX(PickupPopups.LIFE_MS));
		assertEquals(30, popup.getY(PickupPopups.LIFE_MS));
		assertEquals(1f, popup.getAlpha(100), 0f);
		assertTrue(popup.getAlpha(650) < 1f);
		assertTrue(popups.getActive(PickupPopups.LIFE_MS).isEmpty());
	}

	@Test
	public void ignoresZeroAndKeepsAtMostTheCap() {
		PickupPopups popups = new PickupPopups();
		popups.add(0, Color.YELLOW, 0, 0, 0, 0, 0);
		assertTrue(popups.getActive(0).isEmpty());
		for (int i = 0; i < 30; i++)
			popups.add(1, Color.YELLOW, 0, 0, 0, 0, 0);
		assertEquals(PickupPopups.MAX_POPUPS, popups.getActive(0).size());
	}
}
