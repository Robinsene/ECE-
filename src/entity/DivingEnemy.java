package entity;

import java.awt.Color;

import engine.DrawManager.SpriteType;

/**
 * Enemy that moves on its own, outside the formation (Team ECE - KAN-67).
 * A diver leaves the formation and dives toward the player; a fragment is
 * one half of a destroyed splitter and falls on a slant. Both leave the
 * screen at the bottom if they are not shot.
 */
public final class DivingEnemy extends EnemyShip {

	/** Kind of free enemy. */
	public enum Kind {
		/** Leaves the formation and follows the player down. */
		DIVER(30, 2.5f, Color.ORANGE),
		/** Half of a splitter, falls on a slant. */
		FRAGMENT(10, 2f, Color.MAGENTA);

		/** Points for shooting it. */
		private final int points;
		/** Pixels down per frame. */
		private final float speedY;
		/** Colour it is drawn in. */
		private final Color color;

		Kind(final int points, final float speedY, final Color color) {
			this.points = points;
			this.speedY = speedY;
			this.color = color;
		}

		/** @return Points for shooting it. */
		public int getPoints() {
			return this.points;
		}
	}

	/** Fastest sideways speed of a diver, in pixels per frame. */
	static final float MAX_STEER = 1.8f;
	/** How strongly a diver turns toward the player. */
	private static final float STEER = 0.04f;

	/** Kind of enemy. */
	private final Kind kind;
	/** Exact position, kept as floats so slow moves add up. */
	private float exactX;
	/** Exact position, kept as floats so slow moves add up. */
	private float exactY;
	/** Sideways speed, in pixels per frame. */
	private float speedX;

	/**
	 * @param kind
	 *            Diver or fragment.
	 * @param positionX
	 *            Start position.
	 * @param positionY
	 *            Start position.
	 * @param spriteType
	 *            Sprite to draw.
	 * @param speedX
	 *            Starting sideways speed, in pixels per frame.
	 */
	public DivingEnemy(final Kind kind, final int positionX,
			final int positionY, final SpriteType spriteType,
			final float speedX) {
		super(positionX, positionY, spriteType);
		this.kind = kind;
		this.exactX = positionX;
		this.exactY = positionY;
		this.speedX = speedX;
		setColor(kind.color);
	}

	/** @return Diver or fragment. */
	public Kind getKind() {
		return this.kind;
	}

	/** @return Points for shooting it. */
	public int getScore() {
		return this.kind.points;
	}

	/**
	 * Moves one frame.
	 *
	 * @param targetX
	 *            Centre of the player ship, followed by divers.
	 */
	public void step(final int targetX) {
		if (isDestroyed())
			return;
		if (this.kind == Kind.DIVER) {
			float gap = targetX - (this.exactX + getWidth() / 2f);
			this.speedX = Math.max(-MAX_STEER,
					Math.min(MAX_STEER, gap * STEER));
		}
		this.exactX += this.speedX;
		this.exactY += this.kind.speedY;
		move(Math.round(this.exactX) - getPositionX(),
				Math.round(this.exactY) - getPositionY());
		update();
	}
}
