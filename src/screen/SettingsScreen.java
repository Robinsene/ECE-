package screen;

import engine.Cooldown;
import engine.Core;
import engine.HudSettings;
import java.awt.event.KeyEvent;
import java.awt.Color;
import engine.soundEngine.Sound;
import engine.soundEngine.SoundManager;

/**
 * Implements the settings screen.
 */
public class SettingsScreen extends Screen {
	/** Milliseconds between selection steps. */
	private static final int SELECTION_TIME = 200;
	/** Cooldown for menu selection and volume changes. */
	private final Cooldown selectionCooldown;
	/** get volume from soundManager */
	private final SoundManager soundManager = SoundManager.getInstance();
	/** Current selected menu item. */
	private int currentMenuItem = 0;
	/** Vertical distance between the BGM and SFX sliders. */
	private static final int SLIDER_ROW_SPACING = 55;
	/** Vertical distance between two option rows. */
	private static final int ROW_SPACING = 27;
	/** Rows that can be selected: BGM and SFX sliders, difficulty
	 * (KAN-68), HUD opacity, HUD size and HUD animations (Team ECE -
	 * KAN-28, KAN-41), then the key bindings (KAN-63). */
	private static final int SELECTABLE_ROWS = 10;
	/** Row of the difficulty (Team ECE - KAN-68). */
	private static final int DIFFICULTY_ROW = 2;
	/** First row of the HUD options. */
	private static final int FIRST_HUD_ROW = 3;
	/** First row of the key bindings (Team ECE - KAN-63). */
	private static final int FIRST_BINDING_ROW = 6;
	/** Action waiting for its new key, or null (Team ECE - KAN-63). */
	private engine.KeyBindings.Action capturing;
	/** Whether space / enter was down on the previous frame. */
	private boolean confirmWasDown = true;
	/** Gameplay HUD options (Team ECE - KAN-28). */
	private final HudSettings hudSettings = HudSettings.getInstance();
	/** Slider currently being dragged, or -1 when none is active. */
	private int draggingSlider = -1;
	/** Whether the left mouse button was pressed on the previous frame. */
	private boolean mouseWasPressed;

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
	public SettingsScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.returnCode = 1;
		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();

		this.mouseWasPressed = this.inputManager.isLeftMousePressed();
	}

	/** Returns the current volume as a percentage. */
	private int getVolumePercent(final Sound.Category category) {
		return Math.round(this.soundManager.getVolume(category) * 100f);
	}

	/** Adjusts the selected category by the given percentage points. */
	private void adjustSelectedVolume(final int delta) {
		Sound.Category category = this.currentMenuItem == 0
				? Sound.Category.BGM
				: Sound.Category.SFX;

		int percent = Math.max(0,
				Math.min(100, getVolumePercent(category) + delta));

		this.soundManager.setVolume(category, percent / 100f);
	}

	/**
	 * Changes the selected row: volume by 10 points, or the next HUD
	 * opacity / size step, or HUD animations on/off (Team ECE - KAN-28,
	 * KAN-41).
	 *
	 * @param direction
	 *            -1 for left, +1 for right.
	 */
	private void adjustSelected(final int direction) {
		if (this.currentMenuItem == DIFFICULTY_ROW)
			engine.Difficulty.select(
					engine.Difficulty.current().step(direction));
		else if (this.currentMenuItem == FIRST_HUD_ROW)
			this.hudSettings.stepOpacity(direction);
		else if (this.currentMenuItem == FIRST_HUD_ROW + 1)
			this.hudSettings.stepSize(direction);
		else if (this.currentMenuItem == FIRST_HUD_ROW + 2)
			this.hudSettings.setAnimated(direction > 0);
		else if (this.currentMenuItem < 2)
			adjustSelectedVolume(10 * direction);
		// Key binding rows: left / right do nothing (KAN-63).
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
	 * Updates keyboard input, slider dragging and screen drawing.
	 */
	@Override
	protected final void update() {
		super.update();

		boolean mousePressed = this.inputManager.isLeftMousePressed();
		boolean confirmDown = this.inputManager.isKeyDown(KeyEvent.VK_SPACE)
				|| this.inputManager.isKeyDown(KeyEvent.VK_ENTER);

		if (this.capturing != null) {
			// Waiting for the new key of an action (Team ECE - KAN-63).
			updateCapture(confirmDown);
		} else if (this.inputDelay.checkFinished() && confirmDown
				&& !this.confirmWasDown
				&& this.currentMenuItem >= FIRST_BINDING_ROW) {
			this.capturing = engine.KeyBindings.Action.values()[
					this.currentMenuItem - FIRST_BINDING_ROW];
			engine.soundEngine.MenuSounds.playSelect();
		} else if (this.inputDelay.checkFinished()) {
			if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				engine.soundEngine.MenuSounds.playCancel();
				this.draggingSlider = -1;
				this.isRunning = false;
			} else {
				updateMouseVolume(mousePressed);

				// Keyboard changes are paused while a slider is being dragged.
				if (this.draggingSlider == -1
						&& this.selectionCooldown.checkFinished()) {

					if (this.inputManager.isKeyDown(KeyEvent.VK_UP)) {
						this.currentMenuItem = (this.currentMenuItem
								+ SELECTABLE_ROWS - 1) % SELECTABLE_ROWS;
						this.selectionCooldown.reset();
					} else if (this.inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
						this.currentMenuItem = (this.currentMenuItem + 1)
								% SELECTABLE_ROWS;
						this.selectionCooldown.reset();
					}

					if (this.inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
						adjustSelected(-1);
						this.selectionCooldown.reset();
					} else if (this.inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
						adjustSelected(1);
						this.selectionCooldown.reset();
					}
				}
			}
		}

		this.mouseWasPressed = mousePressed;
		this.confirmWasDown = confirmDown;
		draw();
	}

	/**
	 * Takes the next key pressed as the new key of the action being
	 * remapped; esc cancels (Team ECE - KAN-63). The space / enter press
	 * that started the remap is ignored until it is released.
	 *
	 * @param confirmDown
	 *            Space or enter down this frame.
	 */
	private void updateCapture(final boolean confirmDown) {
		if (this.confirmWasDown && confirmDown) {
			this.inputManager.takeLastPressed();
			return;
		}
		int key = this.inputManager.takeLastPressed();
		if (key < 0)
			return;
		if (key != KeyEvent.VK_ESCAPE)
			engine.KeyBindings.getInstance().bind(this.capturing, key);
		else
			engine.soundEngine.MenuSounds.playCancel();
		this.capturing = null;
		// The key that was just bound must not also move the cursor.
		this.selectionCooldown.reset();
	}

	/**
	 * Adjusts volume while dragging and updates mouse hover selection.
	 *
	 * @param mousePressed
	 *            Whether the left mouse button is currently held down.
	 */
	private void updateMouseVolume(final boolean mousePressed) {
		int mouseX = this.inputManager.getMouseX();
		int mouseY = this.inputManager.getMouseY();
		boolean moved = this.inputManager.isMouseMoved();

		// Start dragging only when a new press occurs on a slider.
		if (mousePressed && !this.mouseWasPressed) {
			for (int index = 0; index < 2; index++) {
				boolean onTrack =
						mouseX >= sliderLeft() - 6
								&& mouseX <= sliderLeft() + sliderWidth() + 6
								&& Math.abs(mouseY - sliderY(index)) <= 14;

				if (onTrack) {
					this.draggingSlider = index;
					this.currentMenuItem = index;
					break;
				}
			}
		}

		// Keep adjusting the original slider until the button is released.
		if (this.draggingSlider != -1) {
			int percent = Math.round(
					(mouseX - sliderLeft()) * 100f / sliderWidth());

			percent = Math.max(0, Math.min(100, percent));

			Sound.Category category = this.draggingSlider == 0
					? Sound.Category.BGM
					: Sound.Category.SFX;

			this.soundManager.setVolume(category, percent / 100f);

			if (!mousePressed)
				this.draggingSlider = -1;
		} else if (moved && !mousePressed) {
			for (int index = 0; index < 2; index++) {
				boolean insideControl =
						mouseX >= sliderLeft() - 6
								&& mouseX <= sliderLeft() + sliderWidth() + 6
								&& mouseY >= sliderY(index) - 36
								&& mouseY <= sliderY(index) + 14;

				if (insideControl) {
					this.currentMenuItem = index;
					break;
				}
			}
			// Difficulty and HUD rows below the sliders (KAN-28, KAN-68).
			for (int row = 0; row < 4; row++)
				if (this.drawManager.isOnMenuRow(optionRowY(row),
						ROW_SPACING, mouseY))
					this.currentMenuItem = DIFFICULTY_ROW + row;
		}
	}

	/**
	 * Returns the horizontal starting position of the volume sliders.
	 *
	 * @return Left edge of the slider tracks.
	 */
	private int sliderLeft() {
		return this.getWidth() / 5;
	}

	/**
	 * Returns the distance between the 0% and 100% positions.
	 *
	 * @return Slider track width in pixels.
	 */
	private int sliderWidth() {
		return Math.max(1, this.getWidth() * 3 / 5);
	}

	/**
	 * Returns the vertical centre of a volume slider.
	 *
	 * @param index
	 *            Slider index: 0 for BGM, 1 for SFX.
	 * @return Vertical centre of the slider track.
	 */
	private int sliderY(final int index) {
		return this.getHeight() / 4 + index * SLIDER_ROW_SPACING;
	}

	/**
	 * Returns the baseline of an option row, below the sliders (Team ECE -
	 * KAN-28, KAN-68). Drawing and mouse hover both use it.
	 *
	 * @param row
	 *            0 for difficulty, 1 for HUD opacity, 2 for HUD size, 3 for
	 *            HUD animations.
	 * @return Vertical position of the text baseline.
	 */
	private int optionRowY(final int row) {
		return sliderY(1) + 42 + ROW_SPACING * row;
	}

	/**
	 * Returns the baseline of a key-binding row below the sliders.
	 *
	 * @param row
	 *            Key-binding row index, starting from zero.
	 * @return Vertical position of the text baseline.
	 */
	private int bindingY(final int row) {
		return optionRowY(3) + 33 + ROW_SPACING * row;
	}

	/**
	 * Draws a horizontal volume slider with its label and percentage.
	 *
	 * @param index
	 *            Slider index: 0 for BGM, 1 for SFX.
	 * @param label
	 *            Name displayed above the slider.
	 * @param category
	 *            Sound category whose volume is displayed.
	 */
	private void drawVolumeSlider(final int index, final String label,
								  final Sound.Category category) {

		int left = sliderLeft();
		int width = sliderWidth();
		int centerY = sliderY(index);
		int percent = getVolumePercent(category);
		int thumbX = left + Math.round(width * percent / 100f);

		Color color = this.currentMenuItem == index
				? Color.GREEN
				: Color.WHITE;

		// Draw the label and percentage using the shared selection style.
		this.drawManager.drawMenuRow(
				this, label + "  " + percent + "%",
				centerY - 22, this.currentMenuItem == index);

		// Unfilled track.
		this.drawManager.fillBox(
				left, centerY - 3, width + 1, 6, Color.DARK_GRAY);

		// Filled portion showing the current volume.
		this.drawManager.fillBox(
				left, centerY - 3, thumbX - left, 6, color);

		// Slider handle.
		this.drawManager.fillBox(
				thumbX - 5, centerY - 9, 11, 18, Color.WHITE);
	}

	/**
	 * Draws the volume sliders, key bindings and footer.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(
				this, MenuItem.SETTINGS.getTitle());

		drawVolumeSlider(0, "BGM", Sound.Category.BGM);
		drawVolumeSlider(1, "SFX", Sound.Category.SFX);

		// Difficulty of the next runs (Team ECE - KAN-68).
		this.drawManager.drawMenuRow(this, "Difficulty: "
				+ engine.Difficulty.current().getLabel(), optionRowY(0),
				this.currentMenuItem == DIFFICULTY_ROW);

		// Gameplay HUD options (Team ECE - KAN-28, KAN-41).
		this.drawManager.drawMenuRow(this, "HUD Opacity: "
				+ this.hudSettings.getOpacityPercent() + "%", optionRowY(1),
				this.currentMenuItem == FIRST_HUD_ROW);
		this.drawManager.drawMenuRow(this, "HUD Size: "
				+ this.hudSettings.getSize().getLabel(), optionRowY(2),
				this.currentMenuItem == FIRST_HUD_ROW + 1);
		this.drawManager.drawMenuRow(this, "HUD Animations: "
				+ (this.hudSettings.isAnimated() ? "ON" : "OFF"),
				optionRowY(3), this.currentMenuItem == FIRST_HUD_ROW + 2);

		this.drawManager.drawMenuRow(
				this, "Key Bindings", bindingY(0), false);

		// Remappable keys (Team ECE - KAN-63).
		engine.KeyBindings keys = engine.KeyBindings.getInstance();
		engine.KeyBindings.Action[] actions = engine.KeyBindings.Action
				.values();
		for (int i = 0; i < actions.length; i++) {
			String key = actions[i] == this.capturing ? "press a key"
					: keys.keyName(actions[i]);
			this.drawManager.drawMenuRow(this, actions[i].getLabel() + ": "
					+ key, bindingY(i + 1),
					this.currentMenuItem == FIRST_BINDING_ROW + i);
		}

		this.drawManager.drawKeyHints(this, this.capturing != null
				? "press the new key, esc cancel"
				: this.currentMenuItem >= FIRST_BINDING_ROW
						? "space change key, esc back"
						: "drag volume, arrows adjust, esc back");

		this.drawManager.completeDrawing(this);
	}
}
