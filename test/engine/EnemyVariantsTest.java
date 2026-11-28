package engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Stages where divers and splitters appear (KAN-67). */
public class EnemyVariantsTest {

	@Test
	public void firstStagesHaveNoVariants() {
		assertEquals(0, EnemyVariants.diverInterval(1));
		assertEquals(0, EnemyVariants.diverInterval(2));
		assertEquals(0, EnemyVariants.splitterCount(5));
	}

	@Test
	public void diversComeMoreOftenLater() {
		assertEquals(7000, EnemyVariants.diverInterval(3));
		assertEquals(6500, EnemyVariants.diverInterval(4));
		assertEquals(4000, EnemyVariants.diverInterval(9));
	}

	@Test
	public void splittersGrowUpToThree() {
		assertEquals(1, EnemyVariants.splitterCount(6));
		assertEquals(1, EnemyVariants.splitterCount(7));
		assertEquals(2, EnemyVariants.splitterCount(8));
		assertEquals(2, EnemyVariants.splitterCount(9));
	}

	@Test
	public void survivalLoopsCountAsThreeMoreStages() {
		// Stage 11 is stage 1 of loop 1, played like stage 4.
		assertEquals(4, EnemyVariants.effectiveStage(11));
		assertEquals(6500, EnemyVariants.diverInterval(11));
		// Stage 23 is stage 3 of loop 2, played like stage 9.
		assertEquals(9, EnemyVariants.effectiveStage(23));
		assertEquals(EnemyVariants.MAX_SPLITTERS,
				EnemyVariants.splitterCount(39));
		assertEquals(EnemyVariants.MIN_DIVER_INTERVAL,
				EnemyVariants.diverInterval(39));
	}
}
