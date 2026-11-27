package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import engine.Core;
import engine.GameMode;
import engine.Score;
import engine.soundEngine.MenuSounds;

/**
 * Implements the high scores screen, it shows player records.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class HighScoreScreen extends Screen {

	/** List of past high scores. */
	private List<Score> highScores;
	/** Mode whose scores are shown (Team ECE - KAN-59). */
	private GameMode mode;
	/** Whether left or right was down on the previous frame. */
	private boolean arrowWasDown = true;

	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public HighScoreScreen(final int width, final int height, final int fps) {
		super(width, height, fps);

		this.returnCode = 1;
		// Opens on the mode played last (Team ECE - KAN-59).
		showMode(GameMode.current());
	}

	/**
	 * Loads and shows the high scores of a mode (Team ECE - KAN-59).
	 *
	 * @param shown
	 *            Mode to show.
	 */
	private void showMode(final GameMode shown) {
		this.mode = shown;
		try {
			this.highScores = Core.getFileManager().loadHighScores(shown);
		} catch (NumberFormatException | IOException e) {
			logger.warning("Couldn't load high scores!");
			this.highScores = new java.util.ArrayList<Score>();
		}
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		// Left / right switch between the modes (Team ECE - KAN-59).
		boolean left = inputManager.isKeyDown(KeyEvent.VK_LEFT)
				|| inputManager.isKeyDown(KeyEvent.VK_A);
		boolean right = inputManager.isKeyDown(KeyEvent.VK_RIGHT)
				|| inputManager.isKeyDown(KeyEvent.VK_D);
		if ((left || right) && !this.arrowWasDown) {
			GameMode[] modes = GameMode.values();
			int step = right ? 1 : modes.length - 1;
			showMode(modes[(this.mode.ordinal() + step) % modes.length]);
			MenuSounds.playMove();
		}
		this.arrowWasDown = left || right;

		draw();
		if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished())
			this.isRunning = false;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawScreenTitle(this, MenuItem.HIGH_SCORES.getTitle());
		drawManager.drawCenteredSmallString(this, "<  "
				+ this.mode.getTitle().toUpperCase() + "  >",
				getHeight() / 4 + 4, java.awt.Color.YELLOW);
		drawManager.drawHighScores(this, this.highScores);
		drawManager.drawKeyHints(this, "left/right mode, esc back");

		drawManager.completeDrawing(this);
	}
}
