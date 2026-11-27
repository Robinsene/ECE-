package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.event.KeyEvent;

import org.junit.Test;

/** Key remapping (KAN-63). Uses an instance that never touches the file. */
public class KeyBindingsTest {

	private final KeyBindings keys = new KeyBindings(false);

	@Test
	public void defaultsAreTheClassicKeys() {
		assertEquals(KeyEvent.VK_LEFT, keys.getKey(KeyBindings.Action.MOVE_LEFT));
		assertEquals(KeyEvent.VK_SPACE, keys.getKey(KeyBindings.Action.SHOOT));
		assertEquals(KeyEvent.VK_P, keys.getKey(KeyBindings.Action.PAUSE));
	}

	@Test
	public void bindingAKeyInUseSwapsTheTwoActions() {
		assertTrue(keys.bind(KeyBindings.Action.SHOOT, KeyEvent.VK_P));
		assertEquals(KeyEvent.VK_P, keys.getKey(KeyBindings.Action.SHOOT));
		assertEquals(KeyEvent.VK_SPACE, keys.getKey(KeyBindings.Action.PAUSE));
	}

	@Test
	public void escapeCannotBeBound() {
		assertFalse(keys.bind(KeyBindings.Action.PAUSE, KeyEvent.VK_ESCAPE));
		assertEquals(KeyEvent.VK_P, keys.getKey(KeyBindings.Action.PAUSE));
	}

	@Test
	public void keyNamesAreReadable() {
		keys.bind(KeyBindings.Action.SHOOT, KeyEvent.VK_Z);
		assertEquals("Z", keys.keyName(KeyBindings.Action.SHOOT));
		keys.resetToDefaults();
		assertEquals(KeyEvent.VK_SPACE, keys.getKey(KeyBindings.Action.SHOOT));
	}
}
