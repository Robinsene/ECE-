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
import java.util.logging.Logger;

/**
 * Difficulty chosen in the settings (Team ECE - KAN-68). It scales how
 * fast the enemies move and shoot, and how many lives a run starts with.
 * Normal keeps the balance of the stage table.
 */
public enum Difficulty {

	/** Slower enemies that shoot less often. */
	EASY("Easy", 'E', 1.4, 1.5, 3),
	/** The stage table as it is. */
	NORMAL("Normal", 'N', 1.0, 1.0, 3),
	/** Faster enemies, more bullets and one life less. */
	HARD("Hard", 'H', 0.7, 0.65, 2);

	/** File the choice is saved in, next to the game. */
	private static final String FILE_NAME = "difficulty";
	/** Shortest shooting interval allowed, in ms. */
	static final int MIN_SHOOTING_INTERVAL = 200;

	/** Difficulty of the next runs. */
	private static Difficulty current;
	/** Whether to read and write the save file (off in unit tests). */
	private static boolean persistent = true;

	/** Name shown in the settings and on the score screen. */
	private final String label;
	/** Letter shown next to a high score. */
	private final char letter;
	/** Factor on the movement interval: above 1 means slower enemies. */
	private final double speedFactor;
	/** Factor on the shooting interval: above 1 means fewer bullets. */
	private final double shootingFactor;
	/** Lives at the start of a run, also the cap for bonus lives. */
	private final int lives;

	Difficulty(final String label, final char letter,
			final double speedFactor, final double shootingFactor,
			final int lives) {
		this.label = label;
		this.letter = letter;
		this.speedFactor = speedFactor;
		this.shootingFactor = shootingFactor;
		this.lives = lives;
	}

	/** @return Name shown to the player. */
	public String getLabel() {
		return this.label;
	}

	/** @return Letter shown next to a high score. */
	public char getLetter() {
		return this.letter;
	}

	/** @return Lives at the start of a run. */
	public int getLives() {
		return this.lives;
	}

	/**
	 * Scales the settings of a stage.
	 *
	 * @param settings
	 *            Settings of the stage at Normal.
	 * @return Settings to play the stage with at this difficulty.
	 */
	public GameSettings apply(final GameSettings settings) {
		if (this == NORMAL)
			return settings;
		int speed = Math.max(1,
				(int) Math.round(settings.getBaseSpeed() * this.speedFactor));
		int shooting = Math.max(MIN_SHOOTING_INTERVAL, (int) Math.round(
				settings.getShootingFrecuency() * this.shootingFactor));
		return new GameSettings(settings.getFormationWidth(),
				settings.getFormationHeight(), speed, shooting,
				settings.isDiagonalMovement(),
				settings.getTwoHitEnemyCount());
	}

	/**
	 * @param direction
	 *            -1 for the easier one, +1 for the harder one.
	 * @return The next difficulty in that direction, without wrapping.
	 */
	public Difficulty step(final int direction) {
		int index = ordinal() + Integer.signum(direction);
		return values()[Math.max(0, Math.min(values().length - 1, index))];
	}

	/**
	 * @param letter
	 *            Letter saved with a high score.
	 * @return The matching difficulty, Normal when unknown.
	 */
	public static Difficulty fromLetter(final char letter) {
		for (Difficulty difficulty : values())
			if (difficulty.letter == Character.toUpperCase(letter))
				return difficulty;
		return NORMAL;
	}

	/** @return Difficulty of the next runs, loaded on first use. */
	public static synchronized Difficulty current() {
		if (current == null)
			current = persistent ? load() : NORMAL;
		return current;
	}

	/**
	 * Sets the difficulty of the next runs and saves it.
	 *
	 * @param difficulty
	 *            New difficulty.
	 */
	public static synchronized void select(final Difficulty difficulty) {
		current = difficulty == null ? NORMAL : difficulty;
		if (persistent)
			save(current);
	}

	/**
	 * Turns the save file off and resets to Normal, for unit tests.
	 */
	static synchronized void useMemoryOnly() {
		persistent = false;
		current = NORMAL;
	}

	/** @return The save file next to the running game. */
	private static File saveFile() throws IOException {
		String path = Difficulty.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		path = URLDecoder.decode(path, "UTF-8");
		return new File(new File(path).getParent(), FILE_NAME);
	}

	/** @return The saved difficulty, Normal when there is none. */
	private static Difficulty load() {
		try {
			File file = saveFile();
			if (!file.exists())
				return NORMAL;
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file),
							Charset.forName("UTF-8")))) {
				String line = reader.readLine();
				return line == null ? NORMAL : valueOf(line.trim());
			}
		} catch (IOException | IllegalArgumentException e) {
			Logger.getLogger(Difficulty.class.getName())
					.warning("Couldn't load the difficulty, using Normal.");
			return NORMAL;
		}
	}

	/**
	 * @param difficulty
	 *            Difficulty to save.
	 */
	private static void save(final Difficulty difficulty) {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(saveFile()),
						Charset.forName("UTF-8")))) {
			writer.write(difficulty.name());
		} catch (IOException e) {
			Logger.getLogger(Difficulty.class.getName())
					.warning("Couldn't save the difficulty.");
		}
	}
}
