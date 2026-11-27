package engine;

/**
 * Implements a high score record.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class Score implements Comparable<Score> {

	/** Player's name. */
	private String name;
	/** Score points. */
	private int score;
	/** Stage reached, 0 if not recorded (Team ECE - KAN-59, Survival). */
	private int stage;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Player name, three letters.
	 * @param score
	 *            Player score.
	 */
	public Score(final String name, final int score) {
		this(name, score, 0);
	}

	/**
	 * @param name
	 *            Player name.
	 * @param score
	 *            Points.
	 * @param stage
	 *            Stage reached, 0 if not recorded (Team ECE - KAN-59).
	 */
	public Score(final String name, final int score, final int stage) {
		this.name = name;
		this.score = score;
		this.stage = Math.max(0, stage);
	}

	/** @return Stage reached, 0 if not recorded (Team ECE - KAN-59). */
	public final int getStage() {
		return this.stage;
	}

	/**
	 * Score line of the save file: the points, then the stage when there
	 * is one (Team ECE - KAN-59). Old files only have the points.
	 *
	 * @return Line to save.
	 */
	public final String toSaveLine() {
		return this.stage > 0 ? this.score + " " + this.stage
				: Integer.toString(this.score);
	}

	/**
	 * Reads one entry of a save file (Team ECE - KAN-59).
	 *
	 * @param name
	 *            Name line.
	 * @param line
	 *            Score line: "points" or "points stage".
	 * @return The score.
	 */
	public static Score fromSaveLines(final String name, final String line) {
		String[] parts = line.trim().split("\\s+");
		int stage = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
		return new Score(name, Integer.parseInt(parts[0]), stage);
	}

	/**
	 * Getter for the player's name.
	 * 
	 * @return Name of the player.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Getter for the player's score.
	 * 
	 * @return High score.
	 */
	public final int getScore() {
		return this.score;
	}

	/**
	 * Orders the scores descending by score.
	 * 
	 * @param score
	 *            Score to compare the current one with.
	 * @return Comparison between the two scores. Positive if the current one is
	 *         smaller, positive if its bigger, zero if its the same.
	 */
	@Override
	public final int compareTo(final Score score) {
		int comparison = this.score < score.getScore() ? 1 : this.score > score
				.getScore() ? -1 : 0;
		return comparison;
	}

}
