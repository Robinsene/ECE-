package engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * How a formation enemy shoots (Team ECE - KAN-60). Early stages keep the
 * classic straight shot; later stages add a three-bullet fan and a shot
 * aimed at the player. Survival loops move one tier up.
 *
 * The rules here have no side effect and no randomness, so they are unit
 * tested; the formation picks one of the allowed patterns at random.
 */
public enum AttackPattern {

	/** One bullet straight down (the original shot). */
	STRAIGHT,
	/** Three bullets: left, straight, right. */
	FAN,
	/** One bullet toward the player's position. */
	AIMED;

	/** First stage rank (1 to 10) with the fan shot. */
	public static final int FAN_FROM = 4;
	/** First stage rank (1 to 10) with the aimed shot. */
	public static final int AIMED_FROM = 7;
	/** Sideways speed of the fan's outer bullets, in px per frame. */
	public static final int FAN_SPEED_X = 1;
	/** Fastest sideways speed of an aimed bullet, in px per frame. */
	public static final int MAX_AIM_SPEED_X = 3;

	/**
	 * @param level
	 *            Stage number, any size (Survival goes past 10).
	 * @return Patterns the enemies of that stage can use.
	 */
	public static List<AttackPattern> patternsFor(final int level) {
		int rank = GameMode.baseLevel(Math.max(1, level));
		// Each Survival loop counts as 3 more stages, so loop 1 already
		// has the fan and loop 2 everything.
		int effective = rank + 3 * GameMode.loopOf(Math.max(1, level));
		List<AttackPattern> patterns = new ArrayList<AttackPattern>();
		patterns.add(STRAIGHT);
		if (effective >= FAN_FROM)
			patterns.add(FAN);
		if (effective >= AIMED_FROM)
			patterns.add(AIMED);
		return Collections.unmodifiableList(patterns);
	}

	/**
	 * Sideways speed of a bullet aimed from one point at another, clamped
	 * so the shot stays dodgeable.
	 *
	 * @param fromX
	 *            Shooter X.
	 * @param fromY
	 *            Shooter Y.
	 * @param targetX
	 *            Player X.
	 * @param targetY
	 *            Player Y.
	 * @param speedY
	 *            Downward speed of the bullet.
	 * @return Sideways speed, from -MAX_AIM_SPEED_X to MAX_AIM_SPEED_X.
	 */
	public static int aimSpeedX(final int fromX, final int fromY,
			final int targetX, final int targetY, final int speedY) {
		int distanceY = Math.max(1, targetY - fromY);
		int frames = Math.max(1, distanceY / Math.max(1, speedY));
		int speedX = Math.round((float) (targetX - fromX) / frames);
		return Math.max(-MAX_AIM_SPEED_X, Math.min(MAX_AIM_SPEED_X, speedX));
	}
}
