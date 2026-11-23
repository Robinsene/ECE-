package engine;

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
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Player options for the gameplay HUD (Requirement 8 - Gameplay HUD,
 * KAN-28): opacity and size. Saved next to the game, like the high
 * scores, so the choice survives a restart.
 */
public final class HudSettings {

	/** Opacity steps, in percent, cycled with left / right. */
	public static final int[] OPACITY_STEPS = { 50, 75, 100 };

	/** HUD sizes. */
	public enum Size {
		/** 85 % of the normal size, for more room on screen. */
		SMALL("SMALL", 0.85f),
		/** Original size. */
		NORMAL("NORMAL", 1f);

		/** Name shown in the settings. */
		private final String label;
		/** Scale factor. */
		private final float scale;

		Size(final String label, final float scale) {
			this.label = label;
			this.scale = scale;
		}

		/** @return Name shown in the settings. */
		public String getLabel() {
			return this.label;
		}

		/** @return Scale factor. */
		public float getScale() {
			return this.scale;
		}
	}

	/** Name of the save file, next to the running game. */
	private static final String FILE_NAME = "hud-settings";

	/** Singleton instance. */
	private static HudSettings instance;

	/** Logger. */
	private final Logger logger = Core.getLogger();
	/** Opacity in percent. */
	private int opacity = 100;
	/** HUD size. */
	private Size size = Size.NORMAL;

	/** Loads the saved options, or keeps the defaults. */
	private HudSettings() {
		load();
	}

	/**
	 * @return The shared instance.
	 */
	public static synchronized HudSettings getInstance() {
		if (instance == null)
			instance = new HudSettings();
		return instance;
	}

	/**
	 * @return Opacity in percent.
	 */
	public int getOpacityPercent() {
		return this.opacity;
	}

	/**
	 * @return Opacity from 0 to 1.
	 */
	public float getOpacity() {
		return this.opacity / 100f;
	}

	/**
	 * @return HUD size.
	 */
	public Size getSize() {
		return this.size;
	}

	/**
	 * Moves the opacity one step and saves it.
	 *
	 * @param direction
	 *            -1 for less opaque, +1 for more opaque.
	 */
	public void stepOpacity(final int direction) {
		int index = 0;
		for (int i = 0; i < OPACITY_STEPS.length; i++)
			if (OPACITY_STEPS[i] == this.opacity)
				index = i;
		index = Math.max(0, Math.min(OPACITY_STEPS.length - 1,
				index + Integer.signum(direction)));
		this.opacity = OPACITY_STEPS[index];
		save();
	}

	/**
	 * Moves the size one step and saves it.
	 *
	 * @param direction
	 *            -1 for smaller, +1 for bigger.
	 */
	public void stepSize(final int direction) {
		Size[] sizes = Size.values();
		int index = Math.max(0, Math.min(sizes.length - 1,
				this.size.ordinal() + Integer.signum(direction)));
		this.size = sizes[index];
		save();
	}

	/** @return The save file next to the running game. */
	private File saveFile() throws IOException {
		String path = HudSettings.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		path = URLDecoder.decode(path, "UTF-8");
		return new File(new File(path).getParent(), FILE_NAME);
	}

	/** Reads the saved options. A missing or broken file keeps defaults. */
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
			int savedOpacity = Integer.parseInt(properties.getProperty(
					"opacity", "100").trim());
			for (int step : OPACITY_STEPS)
				if (step == savedOpacity)
					this.opacity = step;
			this.size = Size.valueOf(properties.getProperty("size",
					"NORMAL").trim());
		} catch (IOException | IllegalArgumentException e) {
			this.logger.warning("Couldn't load HUD settings, using defaults.");
		}
	}

	/** Writes the options. A failed save is logged and the game goes on. */
	private void save() {
		try {
			Properties properties = new Properties();
			properties.setProperty("opacity", Integer.toString(this.opacity));
			properties.setProperty("size", this.size.name());
			try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(saveFile()),
							Charset.forName("UTF-8")))) {
				properties.store(writer, "Gameplay HUD options");
			}
		} catch (IOException e) {
			this.logger.warning("Couldn't save HUD settings: "
					+ e.getMessage());
		}
	}
}
