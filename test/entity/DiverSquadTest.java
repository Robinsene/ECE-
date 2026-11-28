package entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import engine.DrawManager.SpriteType;

/** Divers and splitter fragments (KAN-67). */
public class DiverSquadTest {

	@Test
	public void noDiversWhenTheIntervalIsZero() {
		DiverSquad squad = new DiverSquad(0);
		assertFalse(squad.isDiverDue(0));
		assertFalse(squad.isDiverDue(100000));
	}

	@Test
	public void diversComeAtTheInterval() {
		DiverSquad squad = new DiverSquad(5000);
		assertFalse(squad.isDiverDue(1000));
		assertFalse(squad.isDiverDue(5999));
		assertTrue(squad.isDiverDue(6000));
		assertFalse(squad.isDiverDue(6001));
		assertTrue(squad.isDiverDue(11000));
	}

	@Test
	public void diverFollowsThePlayerDown() {
		DiverSquad squad = new DiverSquad(5000);
		DivingEnemy diver = squad.dive(
				new EnemyShip(100, 100, SpriteType.EnemyShipB1));
		assertEquals(DivingEnemy.Kind.DIVER, diver.getKind());
		assertEquals(30, diver.getScore());
		for (int i = 0; i < 20; i++)
			squad.update(300, 1000, false);
		assertTrue(diver.getPositionY() > 140);
		assertTrue(diver.getPositionX() > 100);
		int steer = Math.round(20 * DivingEnemy.MAX_STEER);
		assertTrue(diver.getPositionX() <= 100 + steer);
	}

	@Test
	public void frozenEnemiesDoNotMove() {
		DiverSquad squad = new DiverSquad(5000);
		DivingEnemy diver = squad.dive(
				new EnemyShip(100, 100, SpriteType.EnemyShipA1));
		squad.update(300, 1000, true);
		assertEquals(100, diver.getPositionX());
		assertEquals(100, diver.getPositionY());
	}

	@Test
	public void splitterBreaksIntoTwoFragmentsGoingApart() {
		DiverSquad squad = new DiverSquad(0);
		squad.split(new EnemyShip(200, 100, SpriteType.EnemyShipA1));
		assertEquals(2, squad.size());
		Iterator<DivingEnemy> it = squad.iterator();
		DivingEnemy left = it.next();
		DivingEnemy right = it.next();
		assertEquals(DivingEnemy.Kind.FRAGMENT, left.getKind());
		assertEquals(10, left.getScore());
		int leftStart = left.getPositionX();
		int rightStart = right.getPositionX();
		for (int i = 0; i < 10; i++)
			squad.update(0, 1000, false);
		assertTrue(left.getPositionX() < leftStart);
		assertTrue(right.getPositionX() > rightStart);
	}

	@Test
	public void enemiesBelowTheScreenAreGone() {
		DiverSquad squad = new DiverSquad(0);
		squad.split(new EnemyShip(200, 100, SpriteType.EnemyShipA1));
		assertTrue(squad.hasAlive());
		for (int i = 0; i < 100; i++)
			squad.update(0, 200, false);
		assertEquals(0, squad.size());
		assertFalse(squad.hasAlive());
	}

	@Test
	public void splitterIsMarked() {
		EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipA1);
		assertFalse(ship.isSplitter());
		ship.markSplitter();
		assertTrue(ship.isSplitter());
	}
}
