package screen;

import java.awt.Color;
import java.awt.event.KeyEvent;

import engine.Cooldown;
import engine.Core;
import engine.GameMode;
import engine.soundEngine.MenuSounds;

/**
 * "Modes" menu (Team ECE - KAN-56): pick Classic, Space Opera,
 * Survival or Boss Rush, then the run starts in that mode.
 *
 * Up / down (or the mouse) choose a mode, space / enter or a click start
 * it, esc goes back to the title screen.
 */
public class ModeSelectScreen extends Screen {

	/** Milliseconds between two cursor moves. */
	private static final int SELECTION_TIME = 200;
	/** Vertical distance between two modes. */
	private static final int ROW_SPACING = 62;
	/** Gap between a mode's name and its description. */
	private static final int DESCRIPTION_GAP = 20;

	/** Stars scrolling behind the menu, like on the title screen. */
	private final Starfield starfield;
	/** Cooldown between cursor moves. */
	private final Cooldown selectionCooldown;
	/** Mode under the cursor. */
	private int selected;
	/** Whether a confirm key was down on the previous frame. */
	private boolean confirmWasDown = true;
	/** Whether the mouse button was down on the previous frame. */
	private boolean mouseWasDown = true;

	/**
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second.
	 */
	public ModeSelectScreen(final int width, final int height,
			final int fps) {
		super(width, height, fps);
		this.returnCode = 1;
		this.starfield = new Starfield(width, height);
		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();
		this.selected = GameMode.current().ordinal();
	}

	/**
	 * @return 2 to start the run in the chosen mode, 1 to go back.
	 */
	@Override
	public final int run() {
		super.run();
		return this.returnCode;
	}

	@Override
	protected final void update() {
		super.update();
		this.starfield.update();

		boolean confirmDown = this.inputManager.isKeyDown(KeyEvent.VK_SPACE)
				|| this.inputManager.isKeyDown(KeyEvent.VK_ENTER);
		boolean mouseDown = this.inputManager.isMousePressed();

		if (this.inputDelay.checkFinished()) {
			if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				MenuSounds.playCancel();
				this.returnCode = 1;
				this.isRunning = false;
			} else {
				moveCursor();
				if (this.inputManager.isMouseMoved()) {
					int row = rowAt(this.inputManager.getMouseY());
					if (row >= 0)
						this.selected = row;
				}
				// Keys and clicks held from the title screen don't count.
				if (confirmDown && !this.confirmWasDown)
					start();
				else if (mouseDown && !this.mouseWasDown
						&& rowAt(this.inputManager.getMouseY()) >= 0) {
					this.selected = rowAt(this.inputManager.getMouseY());
					start();
				}
			}
		}
		this.confirmWasDown = confirmDown;
		this.mouseWasDown = mouseDown;
		draw();
	}

	/** Moves the cursor with the arrow keys or W / S. */
	private void moveCursor() {
		if (!this.selectionCooldown.checkFinished())
			return;
		int count = GameMode.values().length;
		if (this.inputManager.isKeyDown(KeyEvent.VK_UP)
				|| this.inputManager.isKeyDown(KeyEvent.VK_W)) {
			this.selected = (this.selected + count - 1) % count;
			MenuSounds.playMove();
			this.selectionCooldown.reset();
		} else if (this.inputManager.isKeyDown(KeyEvent.VK_DOWN)
				|| this.inputManager.isKeyDown(KeyEvent.VK_S)) {
			this.selected = (this.selected + 1) % count;
			MenuSounds.playMove();
			this.selectionCooldown.reset();
		}
	}

	/** Starts the run in the mode under the cursor. */
	private void start() {
		MenuSounds.playSelect();
		GameMode.select(GameMode.values()[this.selected]);
		this.returnCode = 2;
		this.isRunning = false;
	}

	/**
	 * @param row
	 *            Mode index.
	 * @return Baseline of that mode's name.
	 */
	private int rowBaseline(final int row) {
		return this.getHeight() / 4 + 20 + ROW_SPACING * row;
	}

	/**
	 * @param mouseY
	 *            Mouse height.
	 * @return Mode drawn at that height, or -1.
	 */
	private int rowAt(final int mouseY) {
		for (int row = 0; row < GameMode.values().length; row++) {
			int top = rowBaseline(row) - 24;
			if (mouseY >= top && mouseY < top + ROW_SPACING)
				return row;
		}
		return -1;
	}

	/** Draws the stars, the modes and their descriptions. */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawStarfield(this, this.starfield);
		this.drawManager.drawScreenTitle(this, MenuItem.MODES.getTitle());

		GameMode[] modes = GameMode.values();
		for (int row = 0; row < modes.length; row++) {
			boolean selectedRow = row == this.selected;
			this.drawManager.drawMenuRow(this, modes[row].getTitle(),
					rowBaseline(row), selectedRow);
			this.drawManager.drawCenteredSmallString(this,
					modes[row].getDescription(),
					rowBaseline(row) + DESCRIPTION_GAP,
					selectedRow ? Color.LIGHT_GRAY : Color.GRAY);
		}
		this.drawManager.drawKeyHints(this,
				"arrows choose, space start, esc back");
		this.drawManager.completeDrawing(this);
	}
}
