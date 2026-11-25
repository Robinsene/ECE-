package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import engine.soundEngine.Sound;

/** Boss music track choice (KAN-47). */
public class BossMusicTest {

	@Test
	public void tenseLoopWhileTheBossFights() {
		assertEquals(Sound.BGM_BOSS, BossMusic.trackFor(true, true, 1, 2));
		assertEquals(Sound.BGM_BOSS, BossMusic.trackFor(true, true, 2, 3));
	}

	@Test
	public void fasterTrackInTheLastPhase() {
		assertEquals(Sound.BGM_BOSS_FINAL_PHASE,
				BossMusic.trackFor(true, true, 2, 2));
		assertEquals(Sound.BGM_BOSS_FINAL_PHASE,
				BossMusic.trackFor(true, true, 3, 3));
	}

	@Test
	public void silentOnceTheBossOrThePlayerIsDead() {
		assertNull(BossMusic.trackFor(false, true, 2, 2));
		assertNull(BossMusic.trackFor(true, false, 1, 2));
	}

	@Test
	public void onlyBossTracksAreBossTracks() {
		assertTrue(BossMusic.isBossTrack(Sound.BGM_BOSS));
		assertTrue(BossMusic.isBossTrack(Sound.BGM_BOSS_FINAL_PHASE));
		assertFalse(BossMusic.isBossTrack(Sound.BGM_GAME));
		assertFalse(BossMusic.isBossTrack(null));
	}
}
