package engine;

import entity.Boss;

/**
 * State of the boss health bar (Requirement 8 - Gameplay HUD, KAN-20):
 * phases, damage trail, hit flash and shield.
 *
 * Bosses come from the Boss teams and only expose their HP through
 * {@link Boss}. The HUD splits that HP into phases on its own: a boss with
 * 6 HP or more has 3 phases, a smaller one has 2. A boss that also
 * implements {@link Shielded} gets a shield gauge drawn over its bar; none
 * does yet, so the gauge stays hidden until a Boss team adds one.
 *
 * No drawing code here, and every method takes the current time, so the
 * class can be driven by a fake clock in tests.
 */
public class BossHud {

	/**
	 * Optional interface for a boss with a shield. The HUD draws a shield
	 * gauge over the health bar when the boss implements it.
	 */
	public interface Shielded {
		/** @return Current shield points. */
		int getShield();

		/** @return Shield points when full. */
		int getMaxShield();
	}

	/** How long, in ms, the bar flashes white after a hit. */
	public static final long HIT_FLASH_MS = 120;
	/** How long, in ms, the damage trail takes to catch up with the HP. */
	public static final long TRAIL_MS = 600;
	/** How long, in ms, the phase label pulses after a phase change. */
	public static final long PHASE_PULSE_MS = 900;

	/** Boss being tracked, or null. */
	private Boss boss;
	/** HP seen on the last update. */
	private int lastHp;
	/** HP the damage trail starts from. */
	private int trailFrom;
	/** Time of the last hit, or -1. */
	private long hitAt = -1;
	/** Phase seen on the last update. */
	private int lastPhase = 1;
	/** Time of the last phase change, or -1. */
	private long phaseChangedAt = -1;

	/**
	 * Reads the boss state. Called once per frame.
	 *
	 * @param boss
	 *            Boss of the stage, or null.
	 * @param now
	 *            Current time in ms.
	 */
	public void update(final Boss boss, final long now) {
		if (boss != this.boss) {
			this.boss = boss;
			this.lastHp = boss == null ? 0 : boss.getHp();
			this.trailFrom = this.lastHp;
			this.hitAt = -1;
			this.lastPhase = boss == null ? 1 : getPhase();
			this.phaseChangedAt = -1;
			return;
		}
		if (boss == null)
			return;
		int hp = Math.max(0, boss.getHp());
		if (hp < this.lastHp) {
			// Start the trail from where it is now, so quick hits chain.
			this.trailFrom = Math.max(hp, Math.round(getTrailHp(now)));
			this.hitAt = now;
		}
		this.lastHp = hp;
		int phase = getPhase();
		if (phase != this.lastPhase) {
			this.lastPhase = phase;
			this.phaseChangedAt = now;
		}
	}

	/**
	 * @param maxHp
	 *            Boss HP when full.
	 * @return Number of phases the bar is split into.
	 */
	public static int phaseCount(final int maxHp) {
		return maxHp >= 6 ? 3 : 2;
	}

	/**
	 * @param hp
	 *            Current HP.
	 * @param maxHp
	 *            HP when full.
	 * @return Phase for that HP, from 1 (full health) to phaseCount.
	 */
	public static int phaseFor(final int hp, final int maxHp) {
		int count = phaseCount(maxHp);
		int max = Math.max(1, maxHp);
		int lost = max - Math.max(0, Math.min(hp, max));
		return Math.min(count, 1 + lost * count / max);
	}

	/**
	 * @return Number of phases of the tracked boss.
	 */
	public int getPhaseCount() {
		return this.boss == null ? 1 : phaseCount(this.boss.getMaxHp());
	}

	/**
	 * @return Current phase of the tracked boss, from 1.
	 */
	public int getPhase() {
		if (this.boss == null)
			return 1;
		return phaseFor(this.boss.getHp(), this.boss.getMaxHp());
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return HP the damage trail is at, between the current HP and the HP
	 *         before the last hit.
	 */
	public float getTrailHp(final long now) {
		if (this.hitAt < 0 || now - this.hitAt >= TRAIL_MS)
			return this.lastHp;
		float t = (float) (now - this.hitAt) / TRAIL_MS;
		return this.trailFrom + (this.lastHp - this.trailFrom) * t;
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True right after a hit, while the bar flashes white.
	 */
	public boolean isHitFlashOn(final long now) {
		return this.hitAt >= 0 && now - this.hitAt < HIT_FLASH_MS;
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True on the bright frames of the phase label pulse.
	 */
	public boolean isPhasePulseOn(final long now) {
		return this.phaseChangedAt >= 0
				&& now - this.phaseChangedAt < PHASE_PULSE_MS
				&& ((now - this.phaseChangedAt) / 150) % 2 == 0;
	}

	/**
	 * @return Shield share from 0 to 1, or -1 when the boss has no shield.
	 */
	public float getShieldRatio() {
		if (!(this.boss instanceof Shielded))
			return -1f;
		Shielded shielded = (Shielded) this.boss;
		int max = shielded.getMaxShield();
		if (max <= 0)
			return -1f;
		return Math.max(0f, Math.min(1f, (float) shielded.getShield() / max));
	}
}
