package engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Boss phases (KAN-20). */
public class BossHudTest {

	@Test
	public void phaseCountDependsOnMaxHp() {
		assertEquals(2, BossHud.phaseCount(2));
		assertEquals(2, BossHud.phaseCount(5));
		assertEquals(3, BossHud.phaseCount(6));
	}

	@Test
	public void phaseGoesUpAsHpGoesDown() {
		assertEquals(1, BossHud.phaseFor(6, 6));
		assertEquals(2, BossHud.phaseFor(4, 6));
		assertEquals(3, BossHud.phaseFor(1, 6));
		assertEquals(3, BossHud.phaseFor(0, 6));
		assertEquals(2, BossHud.phaseFor(1, 2));
	}
}
