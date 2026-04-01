package com.timerwave.timer;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimerStateSingleplayerTest {

    @Test
    void startPauseResumeAndLapsWork() {
        TimerState state = new TimerState(TimerStyle.MINIMAL, ColorPreset.BLUE, DisplayChannel.ACTIONBAR);
        long t0 = 1_000L;

        assertTrue(state.start(t0));
        assertEquals(5L, state.addLap(t0 + 5_000L));
        assertTrue(state.pause(t0 + 8_000L));
        assertEquals(8L, state.elapsedSeconds(t0 + 8_000L));

        assertTrue(state.resume(t0 + 10_000L));
        assertTrue(state.stop(t0 + 14_000L));
        assertEquals(12L, state.elapsedSeconds(t0 + 14_000L));
        assertEquals(12L, state.personalBestSeconds());
        assertFalse(state.running());
    }

    @Test
    void countdownFinishIncrementsCompletionCounter() {
        TimerState state = new TimerState(TimerStyle.MINIMAL, ColorPreset.BLUE, DisplayChannel.ACTIONBAR);
        long t0 = 1_000L;

        state.setCountdownSeconds(3);
        assertTrue(state.startCountdown(t0));
        state.tickCountdown(t0 + 3_100L);

        assertTrue(state.countdownFinished());
        assertFalse(state.countdownRunning());
        assertEquals(1L, state.completedCountdowns());
    }

    @Test
    void nbtRoundTripPreservesStatsAndLaps() {
        TimerState state = new TimerState(TimerStyle.SMOOTH, ColorPreset.GRAY, DisplayChannel.ACTIONBAR);
        long t0 = 2_000L;

        assertTrue(state.start(t0));
        assertEquals(4L, state.addLap(t0 + 4_000L));
        assertTrue(state.stop(t0 + 9_000L));
        state.setCountdownSeconds(2);
        assertTrue(state.startCountdown(t0 + 10_000L));
        state.tickCountdown(t0 + 12_200L);

        NbtCompound nbt = state.toNbt(t0 + 13_000L);
        TimerState restored = TimerState.fromNbt(
                nbt,
                t0 + 13_000L,
                TimerStyle.MINIMAL,
                ColorPreset.BLUE,
                DisplayChannel.ACTIONBAR
        );

        assertEquals(TimerStyle.SMOOTH, restored.style());
        assertEquals(ColorPreset.GRAY, restored.colorPreset());
        assertEquals(9L, restored.personalBestSeconds());
        assertEquals(1, restored.lapSplits().size());
        assertEquals(4L, restored.lapSplits().getFirst());
        assertEquals(1L, restored.completedCountdowns());
    }
}
