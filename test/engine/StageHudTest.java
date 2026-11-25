package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Wave progress and boss levels (KAN-29, KAN-31). */
public class StageHudTest {

	@Test
	public void waveProgress() {
		StageHud stage = new StageHud();
		stage.startWave(10);
		stage.updateWave(4);
		assertEquals(10, stage.getWaveTotal());
		assertEquals(4, stage.getWaveRemaining());
		assertEquals(0.6f, stage.getWaveProgress(), 0.001f);
	}

	@Test
	public void bossLevels() {
		assertTrue(StageHud.isBossLevel(5));
		assertTrue(StageHud.isBossLevel(10));
		assertFalse(StageHud.isBossLevel(4));
	}
}
