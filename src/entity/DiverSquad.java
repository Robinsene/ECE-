package entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import engine.DrawManager.SpriteType;

/**
 * Divers and fragments of the current stage (Team ECE - KAN-67). It
 * releases a diver from the formation at a fixed interval, splits
 * splitters, moves everything and drops what left the screen.
 */
public final class DiverSquad implements Iterable<DivingEnemy> {

	/** Sideways speed of a fragment, in pixels per frame. */
	static final float FRAGMENT_SPEED_X = 1.5f;

	/** Enemies on screen, alive or fading out. */
	private final List<DivingEnemy> enemies = new ArrayList<DivingEnemy>();
	/** Time between two divers in ms, 0 when the stage has none. */
	private final int interval;
	/** Time of the next diver, or -1 before the first update. */
	private long nextDiverAt = -1;

	/**
	 * @param interval
	 *            Time between two divers in ms, 0 for none.
	 */
	public DiverSquad(final int interval) {
		this.interval = Math.max(0, interval);
	}

	/**
	 * Called once per frame while the stage is played.
	 *
	 * @param now
	 *            Current time, in ms.
	 * @return True when a diver should leave the formation now.
	 */
	public boolean isDiverDue(final long now) {
		if (this.interval == 0)
			return false;
		if (this.nextDiverAt < 0) {
			this.nextDiverAt = now + this.interval;
			return false;
		}
		if (now < this.nextDiverAt)
			return false;
		this.nextDiverAt = now + this.interval;
		return true;
	}

	/**
	 * Turns a ship that left the formation into a diver.
	 *
	 * @param ship
	 *            Ship released by the formation.
	 * @return The diver.
	 */
	public DivingEnemy dive(final EnemyShip ship) {
		DivingEnemy diver = new DivingEnemy(DivingEnemy.Kind.DIVER,
				ship.getPositionX(), ship.getPositionY(),
				ship.getSpriteType(), 0f);
		this.enemies.add(diver);
		return diver;
	}

	/**
	 * Splits a destroyed splitter into two fragments going apart.
	 *
	 * @param ship
	 *            Splitter that was shot.
	 */
	public void split(final EnemyShip ship) {
		for (int side = -1; side <= 1; side += 2)
			this.enemies.add(new DivingEnemy(DivingEnemy.Kind.FRAGMENT,
					ship.getPositionX() + side * ship.getWidth() / 3,
					ship.getPositionY(), SpriteType.EnemyShipA1,
					side * FRAGMENT_SPEED_X));
	}

	/**
	 * Moves the enemies and drops the ones that are gone.
	 *
	 * @param targetX
	 *            Centre of the player ship.
	 * @param bottom
	 *            Lowest y before an enemy counts as gone.
	 * @param frozen
	 *            True while an item freezes the enemies.
	 */
	public void update(final int targetX, final int bottom,
			final boolean frozen) {
		if (!frozen)
			for (DivingEnemy enemy : this.enemies)
				enemy.step(targetX);
		this.enemies.removeIf(enemy -> enemy.isFadeFinished()
				|| !enemy.isDestroyed() && enemy.getPositionY() > bottom);
	}

	/** @return True while an enemy that can still be shot is on screen. */
	public boolean hasAlive() {
		for (DivingEnemy enemy : this.enemies)
			if (!enemy.isDestroyed())
				return true;
		return false;
	}

	/** @return Number of enemies on screen, fading ones included. */
	public int size() {
		return this.enemies.size();
	}

	@Override
	public Iterator<DivingEnemy> iterator() {
		return new ArrayList<DivingEnemy>(this.enemies).iterator();
	}
}
