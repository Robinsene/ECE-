package engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Floating "+N" popups for coins and diamonds (Requirement 8 - Gameplay
 * HUD, KAN-39). Each popup starts where the pickup happened and flies to
 * its balance in the HUD, fading out on the way, so the player sees where
 * the gain went.
 *
 * No drawing code here, and every method takes the current time, so the
 * class can be driven by a fake clock in tests.
 */
public class PickupPopups {

	/** Flight time of a popup, in ms. */
	public static final long LIFE_MS = 700;
	/** Share of the flight after which the popup starts to fade. */
	public static final float FADE_START = 0.6f;
	/** Most popups on screen at once; the oldest goes first. */
	public static final int MAX_POPUPS = 12;

	/** One popup in flight. */
	public static final class Popup {
		/** Text shown, e.g. "+5". */
		private final String text;
		/** Colour of the text. */
		private final Color color;
		/** Start and target positions. */
		private final int fromX, fromY, toX, toY;
		/** Start time, in ms. */
		private final long start;

		Popup(final String text, final Color color, final int fromX,
				final int fromY, final int toX, final int toY,
				final long start) {
			this.text = text;
			this.color = color;
			this.fromX = fromX;
			this.fromY = fromY;
			this.toX = toX;
			this.toY = toY;
			this.start = start;
		}

		/** @return Text shown. */
		public String getText() {
			return this.text;
		}

		/** @return Colour of the text. */
		public Color getColor() {
			return this.color;
		}

		/** @return Share of the flight done, from 0 to 1. */
		private float progress(final long now) {
			return Math.max(0f, Math.min(1f,
					(float) (now - this.start) / LIFE_MS));
		}

		/**
		 * @param now
		 *            Current time in ms.
		 * @return Current X, easing in toward the HUD.
		 */
		public int getX(final long now) {
			float t = progress(now);
			return Math.round(this.fromX + (this.toX - this.fromX) * t * t);
		}

		/**
		 * @param now
		 *            Current time in ms.
		 * @return Current Y, easing in toward the HUD.
		 */
		public int getY(final long now) {
			float t = progress(now);
			return Math.round(this.fromY + (this.toY - this.fromY) * t * t);
		}

		/**
		 * @param now
		 *            Current time in ms.
		 * @return Opacity, from 1 down to 0 at the end of the flight.
		 */
		public float getAlpha(final long now) {
			float t = progress(now);
			if (t <= FADE_START)
				return 1f;
			return Math.max(0f, 1f - (t - FADE_START) / (1f - FADE_START));
		}

		/** @return True once the flight is over. */
		boolean isDone(final long now) {
			return now - this.start >= LIFE_MS;
		}
	}

	/** Popups in flight, oldest first. */
	private final List<Popup> popups = new ArrayList<Popup>();

	/**
	 * Starts a popup.
	 *
	 * @param amount
	 *            Amount gained; nothing happens if it is not positive.
	 * @param color
	 *            Colour of the text.
	 * @param fromX
	 *            Pickup X.
	 * @param fromY
	 *            Pickup Y.
	 * @param toX
	 *            X of the balance in the HUD.
	 * @param toY
	 *            Y of the balance in the HUD.
	 * @param now
	 *            Current time in ms.
	 */
	public void add(final int amount, final Color color, final int fromX,
			final int fromY, final int toX, final int toY, final long now) {
		if (amount <= 0)
			return;
		if (this.popups.size() >= MAX_POPUPS)
			this.popups.remove(0);
		this.popups.add(new Popup("+" + amount, color, fromX, fromY, toX,
				toY, now));
	}

	/**
	 * Removes finished popups and returns the ones to draw.
	 *
	 * @param now
	 *            Current time in ms.
	 * @return Popups still in flight.
	 */
	public List<Popup> getActive(final long now) {
		for (Iterator<Popup> it = this.popups.iterator(); it.hasNext();)
			if (it.next().isDone(now))
				it.remove();
		return this.popups;
	}

	/** Drops every popup, e.g. when the stage restarts. */
	public void clear() {
		this.popups.clear();
	}
}
