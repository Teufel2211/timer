package com.timerwave.timer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimerMultiplayerLogicTest {

    @Test
    void playerStatesAreIndependent() {
        TimerState playerA = new TimerState(TimerStyle.MINIMAL, ColorPreset.BLUE, DisplayChannel.ACTIONBAR);
        TimerState playerB = new TimerState(TimerStyle.MINIMAL, ColorPreset.BLUE, DisplayChannel.ACTIONBAR);
        long t0 = 1_000L;

        assertTrue(playerA.start(t0));
        assertTrue(playerB.start(t0 + 3_000L));
        assertTrue(playerA.pause(t0 + 10_000L)); // disconnect-like pause for player A

        assertEquals(10L, playerA.elapsedSeconds(t0 + 10_000L));
        assertEquals(7L, playerB.elapsedSeconds(t0 + 10_000L));
    }

    @Test
    void durationFormatIsStableForServerAndClientDisplays() {
        assertEquals("0d 0h 0m 0s", TimerManager.formatDuration(0L));
        assertEquals("0d 1h 1m 1s", TimerManager.formatDuration(3661L));
        assertEquals("2d 3h 4m 5s", TimerManager.formatDuration(183_845L));
    }
}
