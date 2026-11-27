package engine;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Keys of the gameplay actions, changed in the settings screen (Team ECE -
 * KAN-63). A and D stay as second keys for moving, whatever the arrows are
 * bound to. Saved next to the game; a missing or broken file keeps the
 * defaults.
 */
public final class KeyBindings {

	/** Gameplay actions that can be remapped. */
	public enum Action {
		/** Move the ship left. */
		MOVE_LEFT("Move Left", KeyEvent.VK_LEFT),
		/** Move the ship right. */
		MOVE_RIGHT("Move Right", KeyEvent.VK_RIGHT),
		/** Shoot. */
		SHOOT("Shoot", KeyEvent.VK_SPACE),
		/** Pause and resume. */
		PAUSE("Pause", KeyEvent.VK_P);

		/** Name shown in the settings. */
		private final String label;
		/** Default key. */
		private final int defaultKey;

		Action(final String label, final int defaultKey) {
			this.label = label;
			this.defaultKey = defaultKey;
		}

		/** @return Name shown in the settings. */
		public String getLabel() {
			return this.label;
		}

		/** @return Default key. */
		public int getDefaultKey() {
			return this.defaultKey;
		}
	}

	/** File the bindings are saved in. */
	private static final String FILE_NAME = "key-bindings";
	/** Shared instance. */
	private static KeyBindings instance;

	/** Key of each action. */
	private final Map<Action, Integer> keys = new EnumMap<Action, Integer>(
			Action.class);
	/** Whether to read and write the save file. */
	private final boolean persistent;

	/**
	 * @param persistent
	 *            True to load and save the bindings file.
	 */
	KeyBindings(final boolean persistent) {
		this.persistent = persistent;
		resetToDefaults();
		if (persistent)
			load();
	}

	/** @return The shared bindings, loaded on first use. */
	public static synchronized KeyBindings getInstance() {
		if (instance == null)
			instance = new KeyBindings(true);
		return instance;
	}

	/** Puts every action back on its default key (not saved). */
	void resetToDefaults() {
		for (Action action : Action.values())
			this.keys.put(action, action.getDefaultKey());
	}

	/**
	 * @param action
	 *            An action.
	 * @return Its key code.
	 */
	public int getKey(final Action action) {
		return this.keys.get(action);
	}

	/**
	 * Binds a key to an action. If another action had that key, the two
	 * swap keys, so no key does two things. Esc cannot be bound (it backs
	 * out of every screen).
	 *
	 * @param action
	 *            Action to change.
	 * @param keyCode
	 *            New key.
	 * @return True if the binding changed.
	 */
	public boolean bind(final Action action, final int keyCode) {
		if (keyCode == KeyEvent.VK_ESCAPE || keyCode <= 0)
			return false;
		int previous = this.keys.get(action);
		for (Action other : Action.values())
			if (other != action && this.keys.get(other) == keyCode)
				this.keys.put(other, previous);
		this.keys.put(action, keyCode);
		if (this.persistent)
			save();
		return true;
	}

	/**
	 * @param input
	 *            Keyboard state.
	 * @param action
	 *            Action to check.
	 * @return True while the key of the action is down (A and D also move).
	 */
	public boolean isDown(final InputManager input, final Action action) {
		if (input.isKeyDown(this.keys.get(action)))
			return true;
		if (action == Action.MOVE_LEFT)
			return input.isKeyDown(KeyEvent.VK_A);
		if (action == Action.MOVE_RIGHT)
			return input.isKeyDown(KeyEvent.VK_D);
		return false;
	}

	/**
	 * @param action
	 *            An action.
	 * @return Name of its key, e.g. "Space" or "P".
	 */
	public String keyName(final Action action) {
		return KeyEvent.getKeyText(this.keys.get(action));
	}

	/** @return The save file next to the running game. */
	private static File saveFile() throws IOException {
		String path = KeyBindings.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		path = URLDecoder.decode(path, "UTF-8");
		return new File(new File(path).getParent(), FILE_NAME);
	}

	/** Reads the saved keys; unknown or broken entries keep the default. */
	private void load() {
		try {
			File file = saveFile();
			if (!file.exists())
				return;
			Properties properties = new Properties();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file),
							Charset.forName("UTF-8")))) {
				properties.load(reader);
			}
			for (Action action : Action.values()) {
				String value = properties.getProperty(action.name());
				if (value != null)
					bind(action, Integer.parseInt(value.trim()));
			}
		} catch (IOException | IllegalArgumentException e) {
			Logger.getLogger(KeyBindings.class.getName())
					.warning("Couldn't load key bindings, using defaults.");
		}
	}

	/** Writes the keys; a failed save is logged. */
	private void save() {
		try {
			Properties properties = new Properties();
			for (Action action : Action.values())
				properties.setProperty(action.name(),
						Integer.toString(this.keys.get(action)));
			try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(saveFile()),
							Charset.forName("UTF-8")))) {
				properties.store(writer, "Gameplay key bindings");
			}
		} catch (IOException e) {
			Logger.getLogger(KeyBindings.class.getName())
					.warning("Couldn't save key bindings.");
		}
	}
}
