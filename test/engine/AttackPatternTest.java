package engine;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/** Enemy attack patterns per stage (KAN-60). */
public class AttackPatternTest {

	@Test
	public void earlyStagesKeepTheClassicShot() {
		for (int level = 1; level < AttackPattern.FAN_FROM; level++)
			assertEquals(Arrays.asList(AttackPattern.STRAIGHT),
					AttackPattern.patternsFor(level));
	}

	@Test
	public void fanFromStageFourAimedFromStageSeven() {
		assertEquals(Arrays.asList(AttackPattern.STRAIGHT, AttackPattern.FAN),
				AttackPattern.patternsFor(4));
		assertEquals(Arrays.asList(AttackPattern.STRAIGHT, AttackPattern.FAN,
				AttackPattern.AIMED), AttackPattern.patternsFor(7));
	}

	@Test
	public void survivalLoopsMoveOneTierUp() {
		assertEquals(Arrays.asList(AttackPattern.STRAIGHT, AttackPattern.FAN),
				AttackPattern.patternsFor(11));
		assertEquals(3, AttackPattern.patternsFor(14).size());
		assertEquals(3, AttackPattern.patternsFor(21).size());
	}

	@Test
	public void aimedShotGoesTowardThePlayerButStaysDodgeable() {
		assertEquals(0, AttackPattern.aimSpeedX(200, 100, 200, 480, 4));
		assertEquals(1, AttackPattern.aimSpeedX(200, 100, 300, 480, 4));
		assertEquals(-1, AttackPattern.aimSpeedX(200, 100, 100, 480, 4));
		assertEquals(AttackPattern.MAX_AIM_SPEED_X,
				AttackPattern.aimSpeedX(0, 470, 440, 480, 4));
		assertEquals(-AttackPattern.MAX_AIM_SPEED_X,
				AttackPattern.aimSpeedX(440, 470, 0, 480, 4));
	}
}
