package engine;

import engine.soundEngine.Sound;
import engine.soundEngine.SoundManager;

/**
 * State of the in-game pause menu (Team ECE - KAN-69): the main list,
 * a settings panel that can be changed without leaving the run, and a
 * confirmation before restarting or quitting. It only keeps the cursor
 * and the panel; the game screen reads the keys and carries out the
 * returned action.
 */
public final class PauseMenu {

	/** What the game screen has to do after an input. */
	public enum Action {
		/** Nothing, the menu stays open. */
		NONE,
		/** Close the menu and continue. */
		RESUME,
		/** Restart the stage. */
		RESTART,
		/** End the run and go to the score screen. */
		QUIT
	}

	/** Panel shown in the menu box. */
	enum Panel {
		/** Resume, Settings, Restart, Quit. */
		MAIN,
		/** Volumes and effects. */
		SETTINGS,
		/** Yes / No before restarting. */
		CONFIRM_RESTART,
		/** Yes / No before quitting. */
		CONFIRM_QUIT
	}

	/**
	 * Settings the pause menu can change. The difficulty is not here: it
	 * only applies to the next run.
	 */
	public interface Options {
		/** @return BGM volume, 0 to 100. */
		int bgm();

		/** @param percent New BGM volume, 0 to 100. */
		void setBgm(int percent);

		/** @return SFX volume, 0 to 100. */
		int sfx();

		/** @param percent New SFX volume, 0 to 100. */
		void setSfx(int percent);

		/** @return True when screen shake and slow motion are on. */
		boolean effects();

		/** @param on True to turn screen shake and slow motion on. */
		void setEffects(boolean on);

		/** @return True when HUD animations are on. */
		boolean animations();

		/** @param on True to animate the HUD. */
		void setAnimations(boolean on);
	}

	/** Entries of the main panel. */
	static final String[] MAIN_ROWS = { "Resume", "Settings", "Restart",
			"Quit to menu" };
	/** Main panel row of Settings. */
	static final int SETTINGS_ROW = 1;
	/** Main panel row of Restart. */
	static final int RESTART_ROW = 2;
	/** Main panel row of Quit. */
	static final int QUIT_ROW = 3;
	/** Settings rows: BGM, SFX, effects, HUD animations, Back. */
	static final int SETTINGS_ROWS = 5;
	/** Volume change for one left / right press. */
	static final int VOLUME_STEP = 10;

	/** Settings changed by the menu. */
	private final Options options;
	/** Panel shown. */
	private Panel panel = Panel.MAIN;
	/** Row the cursor is on. */
	private int row;

	/**
	 * @param options
	 *            Settings the menu changes.
	 */
	public PauseMenu(final Options options) {
		this.options = options;
	}

	/** Back to the main panel, cursor on Resume. Called on each pause. */
	public void reset() {
		this.panel = Panel.MAIN;
		this.row = 0;
	}

	/** Moves the cursor up, wrapping around. */
	public void up() {
		this.row = (this.row - 1 + rowCount()) % rowCount();
	}

	/** Moves the cursor down, wrapping around. */
	public void down() {
		this.row = (this.row + 1) % rowCount();
	}

	/**
	 * Changes the setting under the cursor, on the settings panel only.
	 *
	 * @param direction
	 *            -1 for left, +1 for right.
	 */
	public void adjust(final int direction) {
		if (this.panel != Panel.SETTINGS)
			return;
		switch (this.row) {
		case 0:
			this.options.setBgm(clampVolume(this.options.bgm()
					+ VOLUME_STEP * direction));
			break;
		case 1:
			this.options.setSfx(clampVolume(this.options.sfx()
					+ VOLUME_STEP * direction));
			break;
		case 2:
			this.options.setEffects(direction > 0);
			break;
		case 3:
			this.options.setAnimations(direction > 0);
			break;
		default:
			break;
		}
	}

	/**
	 * Space or Enter on the row under the cursor.
	 *
	 * @return What the game screen has to do.
	 */
	public Action confirm() {
		switch (this.panel) {
		case MAIN:
			return confirmMain();
		case SETTINGS:
			confirmSettings();
			return Action.NONE;
		default:
			return confirmChoice();
		}
	}

	/**
	 * Escape: closes the menu from the main panel, otherwise goes back to
	 * the main panel.
	 *
	 * @return What the game screen has to do.
	 */
	public Action back() {
		if (this.panel == Panel.MAIN)
			return Action.RESUME;
		backToMain();
		return Action.NONE;
	}

	/** @return Title of the menu box. */
	public String title() {
		switch (this.panel) {
		case SETTINGS:
			return "SETTINGS";
		case CONFIRM_RESTART:
			return "RESTART?";
		case CONFIRM_QUIT:
			return "QUIT?";
		default:
			return "PAUSED";
		}
	}

	/** @return Lines of the menu box. */
	public String[] lines() {
		switch (this.panel) {
		case SETTINGS:
			return new String[] { "BGM: " + this.options.bgm() + "%",
					"SFX: " + this.options.sfx() + "%",
					"Screen Effects: " + onOff(this.options.effects()),
					"HUD Animations: " + onOff(this.options.animations()),
					"Back" };
		case CONFIRM_RESTART:
			return new String[] { "Yes, restart the stage", "No" };
		case CONFIRM_QUIT:
			return new String[] { "Yes, end the run", "No" };
		default:
			return MAIN_ROWS.clone();
		}
	}

	/** @return Row the cursor is on. */
	public int selected() {
		return this.row;
	}

	/** @return True on the main panel, where the run summary is shown. */
	public boolean isMain() {
		return this.panel == Panel.MAIN;
	}

	/** @return Key hints for the panel shown. */
	public String hints() {
		return this.panel == Panel.SETTINGS
				? "left/right change, esc back"
				: this.panel == Panel.MAIN
						? "arrows move, space select, esc resume"
						: "arrows move, space select, esc back";
	}

	/** @return Panel shown, for tests. */
	Panel panel() {
		return this.panel;
	}

	/** @return Action for the main panel. */
	private Action confirmMain() {
		if (this.row == SETTINGS_ROW)
			open(Panel.SETTINGS, 0);
		else if (this.row == RESTART_ROW)
			open(Panel.CONFIRM_RESTART, 1);
		else if (this.row == QUIT_ROW)
			open(Panel.CONFIRM_QUIT, 1);
		else
			return Action.RESUME;
		return Action.NONE;
	}

	/** Toggles a switch, or goes back on the last row. */
	private void confirmSettings() {
		if (this.row == 2)
			this.options.setEffects(!this.options.effects());
		else if (this.row == 3)
			this.options.setAnimations(!this.options.animations());
		else if (this.row == SETTINGS_ROWS - 1)
			backToMain();
	}

	/** @return Action for a Yes / No panel. */
	private Action confirmChoice() {
		if (this.row != 0) {
			backToMain();
			return Action.NONE;
		}
		Action action = this.panel == Panel.CONFIRM_RESTART ? Action.RESTART
				: Action.QUIT;
		reset();
		return action;
	}

	/** Back to the main panel, cursor on the entry that was opened. */
	private void backToMain() {
		int from = this.panel == Panel.SETTINGS ? SETTINGS_ROW
				: this.panel == Panel.CONFIRM_RESTART ? RESTART_ROW
						: QUIT_ROW;
		open(Panel.MAIN, from);
	}

	/**
	 * @param next
	 *            Panel to show.
	 * @param cursor
	 *            Row to put the cursor on.
	 */
	private void open(final Panel next, final int cursor) {
		this.panel = next;
		this.row = cursor;
	}

	/** @return Number of rows of the panel shown. */
	private int rowCount() {
		switch (this.panel) {
		case SETTINGS:
			return SETTINGS_ROWS;
		case MAIN:
			return MAIN_ROWS.length;
		default:
			return 2;
		}
	}

	/**
	 * @param percent
	 *            Volume.
	 * @return The volume between 0 and 100.
	 */
	private static int clampVolume(final int percent) {
		return Math.max(0, Math.min(100, percent));
	}

	/**
	 * @param on
	 *            Switch value.
	 * @return "ON" or "OFF".
	 */
	private static String onOff(final boolean on) {
		return on ? "ON" : "OFF";
	}

	/** @return Options backed by the sound manager and the HUD settings. */
	public static Options liveOptions() {
		return new Options() {
			@Override
			public int bgm() {
				return volume(Sound.Category.BGM);
			}

			@Override
			public void setBgm(final int percent) {
				SoundManager.getInstance().setVolume(Sound.Category.BGM,
						percent / 100f);
			}

			@Override
			public int sfx() {
				return volume(Sound.Category.SFX);
			}

			@Override
			public void setSfx(final int percent) {
				SoundManager.getInstance().setVolume(Sound.Category.SFX,
						percent / 100f);
			}

			@Override
			public boolean effects() {
				return HudSettings.getInstance().areEffectsOn();
			}

			@Override
			public void setEffects(final boolean on) {
				HudSettings.getInstance().setEffects(on);
			}

			@Override
			public boolean animations() {
				return HudSettings.getInstance().isAnimated();
			}

			@Override
			public void setAnimations(final boolean on) {
				HudSettings.getInstance().setAnimated(on);
			}

			private int volume(final Sound.Category category) {
				return Math.round(SoundManager.getInstance()
						.getVolume(category) * 100f);
			}
		};
	}
}
