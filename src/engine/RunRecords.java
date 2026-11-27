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
 * The player's personal records across runs: best combo and longest run
 * (Team ECE - KAN-58). Saved next to the game like the HUD settings; a
 * missing or broken file just means no record yet.
 */
public final class RunRecords {

	/** File the records are saved in. */
	private static final String FILE_NAME = "run-records";

	/** Best combo so far. */
	private int bestCombo;
	/** Longest run so far, in ms. */
	private long longestRunMillis;

	/** Loads the saved records. */
	public RunRecords() {
		load();
	}

	/** @return Best combo so far. */
	public int getBestCombo() {
		return this.bestCombo;
	}

	/** @return Longest run so far, in ms. */
	public long getLongestRunMillis() {
		return this.longestRunMillis;
	}

	/**
	 * Marks on the summary which records the run beats, keeps the new
	 * records and saves them.
	 *
	 * @param summary
	 *            Summary of the run that just ended.
	 */
	public void submit(final RunSummary summary) {
		summary.compareWith(this.bestCombo, this.longestRunMillis);
		if (!summary.isComboRecord() && !summary.isTimeRecord())
			return;
		this.bestCombo = Math.max(this.bestCombo, summary.getBestCombo());
		this.longestRunMillis = Math.max(this.longestRunMillis,
				summary.getPlayMillis());
		save();
	}

	/** @return The save file next to the running game. */
	private static File saveFile() throws IOException {
		String path = RunRecords.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		path = URLDecoder.decode(path, "UTF-8");
		return new File(new File(path).getParent(), FILE_NAME);
	}

	/** Reads the records; a missing or broken file keeps zeros. */
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
			this.bestCombo = Math.max(0, Integer.parseInt(properties
					.getProperty("bestCombo", "0").trim()));
			this.longestRunMillis = Math.max(0, Long.parseLong(properties
					.getProperty("longestRunMillis", "0").trim()));
		} catch (IOException | IllegalArgumentException e) {
			Logger.getLogger(RunRecords.class.getName())
					.warning("Couldn't load run records.");
		}
	}

	/** Writes the records; a failed save is logged. */
	private void save() {
		try {
			Properties properties = new Properties();
			properties.setProperty("bestCombo",
					Integer.toString(this.bestCombo));
			properties.setProperty("longestRunMillis",
					Long.toString(this.longestRunMillis));
			try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(saveFile()),
							Charset.forName("UTF-8")))) {
				properties.store(writer, "Personal run records");
			}
		} catch (IOException e) {
			Logger.getLogger(RunRecords.class.getName())
					.warning("Couldn't save run records.");
		}
	}
}
