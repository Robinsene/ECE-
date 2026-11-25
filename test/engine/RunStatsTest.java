package engine;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/** Run timer (KAN-37). */
public class RunStatsTest {

	@Before
	public void setUp() {
		RunStats.startRun();
	}

	@Test
	public void countsFrameTime() {
		RunStats.tick(1000);
		RunStats.tick(1016);
		RunStats.tick(1032);
		assertEquals(32, RunStats.getPlayMillis());
	}

	@Test
	public void ignoresPausesAndLongGaps() {
		RunStats.tick(0);
		RunStats.tick(100);
		RunStats.hold();
		RunStats.tick(60000);
		RunStats.tick(60100);
		RunStats.tick(90000); // loading screen or debugger stop
		assertEquals(200, RunStats.getPlayMillis());
	}

	@Test
	public void formatsAsMinutesAndSeconds() {
		assertEquals("0:00", RunStats.format(999));
		assertEquals("2:05", RunStats.format(125000));
	}
}
