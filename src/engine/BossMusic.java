package engine;

import engine.soundEngine.Sound;
import engine.soundEngine.SoundManager;

/**
 * Picks the background music of a boss stage: a tense loop while the boss
 * fights, a faster one for its last phase, and silence once it is beaten
 * or the run is over.
 *
 * The choice itself ({@link #trackFor}) has no side effect, so it can be
 * tested without an audio device.
 */
public final class BossMusic {

	private BossMusic() {
	}

	/**
	 * @param bossAlive
	 *            True while a boss is on screen and not destroyed.
	 * @param playerAlive
	 *            True while the player still has lives.
	 * @param phase
	 *            Current boss phase, from 1.
	 * @param phaseCount
	 *            Number of phases of the boss.
	 * @return Track to play, or null for no boss music.
	 */
	public static Sound trackFor(final boolean bossAlive,
			final boolean playerAlive, final int phase, final int phaseCount) {
		if (!bossAlive || !playerAlive)
			return null;
		if (phaseCount > 1 && phase >= phaseCount)
			return Sound.BGM_BOSS_FINAL_PHASE;
		return Sound.BGM_BOSS;
	}

	/**
	 * @param sound
	 *            A sound, or null.
	 * @return True for one of the boss tracks.
	 */
	public static boolean isBossTrack(final Sound sound) {
		return sound == Sound.BGM_BOSS || sound == Sound.BGM_BOSS_FINAL_PHASE;
	}

	/**
	 * Starts, switches or stops the boss music. Safe to call every frame:
	 * a track already playing is left alone, and music that is not a boss
	 * track is never stopped from here.
	 *
	 * @param track
	 *            Track from {@link #trackFor}, or null.
	 */
	public static void apply(final Sound track) {
		SoundManager sounds = SoundManager.getInstance();
		if (track != null)
			sounds.playBgm(track);
		else if (isBossTrack(sounds.getCurrentBgm()))
			sounds.stopBgm();
	}
}
