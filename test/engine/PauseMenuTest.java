package engine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/** Pause menu: settings and confirmations (KAN-69). */
public class PauseMenuTest {

	/** Options kept in memory instead of the sound manager. */
	private static final class FakeOptions implements PauseMenu.Options {
		int bgm = 50;
		int sfx = 70;
		boolean effects = true;
		boolean animations = true;

		@Override public int bgm() { return bgm; }
		@Override public void setBgm(final int p) { bgm = p; }
		@Override public int sfx() { return sfx; }
		@Override public void setSfx(final int p) { sfx = p; }
		@Override public boolean effects() { return effects; }
		@Override public void setEffects(final boolean on) { effects = on; }
		@Override public boolean animations() { return animations; }
		@Override public void setAnimations(final boolean on) { animations = on; }
	}

	private FakeOptions options;
	private PauseMenu menu;

	@Before
	public void setUp() {
		options = new FakeOptions();
		menu = new PauseMenu(options);
	}

	@Test
	public void mainListHasSettings() {
		assertEquals("PAUSED", menu.title());
		assertArrayEquals(new String[] { "Resume", "Settings", "Restart",
				"Quit to menu" }, menu.lines());
		assertSame(PauseMenu.Action.RESUME, menu.confirm());
	}

	@Test
	public void escapeResumesFromTheMainList() {
		assertSame(PauseMenu.Action.RESUME, menu.back());
	}

	@Test
	public void cursorWraps() {
		menu.up();
		assertEquals(3, menu.selected());
		menu.down();
		assertEquals(0, menu.selected());
	}

	@Test
	public void settingsChangeVolumesAndSwitches() {
		menu.down();
		assertSame(PauseMenu.Action.NONE, menu.confirm());
		assertEquals("SETTINGS", menu.title());
		assertFalse(menu.isMain());

		menu.adjust(1);
		assertEquals(60, options.bgm);
		menu.down();
		for (int i = 0; i < 10; i++)
			menu.adjust(1);
		assertEquals(100, options.sfx);

		menu.down();
		menu.adjust(-1);
		assertFalse(options.effects);
		menu.confirm();
		assertTrue(options.effects);

		menu.down();
		menu.confirm();
		assertFalse(options.animations);
		assertEquals("HUD Animations: OFF", menu.lines()[3]);
	}

	@Test
	public void backFromSettingsReturnsToSettingsRow() {
		menu.down();
		menu.confirm();
		menu.up();
		assertEquals("Back", menu.lines()[menu.selected()]);
		menu.confirm();
		assertTrue(menu.isMain());
		assertEquals(PauseMenu.SETTINGS_ROW, menu.selected());
	}

	@Test
	public void adjustDoesNothingOnTheMainList() {
		menu.adjust(1);
		assertEquals(50, options.bgm);
	}

	@Test
	public void restartAsksFirstAndDefaultsToNo() {
		menu.down();
		menu.down();
		assertSame(PauseMenu.Action.NONE, menu.confirm());
		assertEquals("RESTART?", menu.title());
		assertEquals(1, menu.selected());
		assertSame(PauseMenu.Action.NONE, menu.confirm());
		assertTrue(menu.isMain());
		assertEquals(PauseMenu.RESTART_ROW, menu.selected());

		menu.confirm();
		menu.up();
		assertSame(PauseMenu.Action.RESTART, menu.confirm());
		assertTrue(menu.isMain());
		assertEquals(0, menu.selected());
	}

	@Test
	public void quitAsksFirstAndEscapeCancels() {
		menu.up();
		menu.confirm();
		assertEquals("QUIT?", menu.title());
		assertSame(PauseMenu.Action.NONE, menu.back());
		assertEquals(PauseMenu.QUIT_ROW, menu.selected());

		menu.confirm();
		menu.down();
		assertSame(PauseMenu.Action.QUIT, menu.confirm());
	}

	@Test
	public void resetGoesBackToResume() {
		menu.down();
		menu.confirm();
		menu.reset();
		assertTrue(menu.isMain());
		assertEquals(0, menu.selected());
	}
}
