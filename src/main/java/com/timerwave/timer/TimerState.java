package com.timerwave.timer;

import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;

public class TimerState {
    private static final int MAX_LAPS = 12;

    private long accumulatedMillis;
    private long runningSinceMillis;
    private boolean running;
    private boolean paused;
    private TimerStyle style;
    private ColorPreset colorPreset;
    private DisplayChannel displayChannel;
    private long countdownInitialSeconds;
    private long countdownRemainingSeconds;
    private long countdownRunningSinceMillis;
    private boolean countdownRunning;
    private boolean countdownFinished;
    private long personalBestSeconds;
    private long completedCountdowns;
    private long lastLapElapsedSeconds;
    private final List<Long> lapSplitsSeconds = new ArrayList<>();

    public TimerState(TimerStyle style, ColorPreset colorPreset, DisplayChannel displayChannel) {
        this.style = style;
        this.colorPreset = colorPreset;
        this.displayChannel = displayChannel;
    }

    public boolean start(long now) {
        if (running) {
            return false;
        }
        if (!paused) {
            accumulatedMillis = 0L;
            clearLaps();
        }
        running = true;
        paused = false;
        runningSinceMillis = now;
        return true;
    }

    public boolean stop(long now) {
        if (!running && !paused && accumulatedMillis == 0L) {
            return false;
        }
        if (running) {
            accumulatedMillis = elapsedMillis(now);
        }
        running = false;
        paused = false;
        runningSinceMillis = 0L;
        updatePersonalBestFromElapsedSeconds(accumulatedMillis / 1000L);
        return true;
    }

    public boolean pause(long now) {
        if (!running) {
            return false;
        }
        accumulatedMillis = elapsedMillis(now);
        running = false;
        paused = true;
        runningSinceMillis = 0L;
        return true;
    }

    public boolean resume(long now) {
        if (!paused || running) {
            return false;
        }
        running = true;
        paused = false;
        runningSinceMillis = now;
        return true;
    }

    public void reset() {
        updatePersonalBest(System.currentTimeMillis());
        accumulatedMillis = 0L;
        runningSinceMillis = 0L;
        running = false;
        paused = false;
        clearLaps();
    }

    public void setElapsedSeconds(long seconds, long now) {
        accumulatedMillis = Math.max(0L, seconds) * 1000L;
        clearLaps();
        if (running) {
            runningSinceMillis = now;
        } else {
            runningSinceMillis = 0L;
        }
    }

    public long elapsedMillis(long now) {
        if (running) {
            return Math.max(0L, accumulatedMillis + (now - runningSinceMillis));
        }
        return Math.max(0L, accumulatedMillis);
    }

    public long elapsedSeconds(long now) {
        return elapsedMillis(now) / 1000L;
    }

    public boolean running() {
        return running;
    }

    public boolean paused() {
        return paused;
    }

    public boolean active() {
        return running || paused || accumulatedMillis > 0L;
    }

    public TimerStyle style() {
        return style;
    }

    public void setStyle(TimerStyle style) {
        this.style = style;
        this.displayChannel = style.displayChannel();
    }

    public ColorPreset colorPreset() {
        return colorPreset;
    }

    public void setColorPreset(ColorPreset colorPreset) {
        this.colorPreset = colorPreset;
    }

    public DisplayChannel displayChannel() {
        return displayChannel;
    }

    public void setCountdownSeconds(long seconds) {
        countdownInitialSeconds = Math.max(1L, seconds);
        countdownRemainingSeconds = countdownInitialSeconds;
        countdownRunningSinceMillis = 0L;
        countdownRunning = false;
        countdownFinished = false;
    }

    public boolean startCountdown(long now) {
        if (countdownRunning || countdownInitialSeconds <= 0L) {
            return false;
        }
        if (countdownRemainingSeconds <= 0L) {
            countdownRemainingSeconds = countdownInitialSeconds;
        }
        countdownRunning = true;
        countdownRunningSinceMillis = now;
        countdownFinished = false;
        return true;
    }

    public boolean stopCountdown(long now) {
        if (!countdownRunning) {
            return false;
        }
        countdownRemainingSeconds = countdownRemainingSeconds(now);
        countdownRunning = false;
        countdownRunningSinceMillis = 0L;
        return true;
    }

    public void resetCountdown() {
        if (countdownInitialSeconds <= 0L) {
            return;
        }
        countdownRemainingSeconds = countdownInitialSeconds;
        countdownRunning = false;
        countdownRunningSinceMillis = 0L;
        countdownFinished = false;
    }

    public void tickCountdown(long now) {
        if (!countdownRunning) {
            return;
        }
        long remaining = countdownRemainingSeconds(now);
        if (remaining <= 0L) {
            countdownRemainingSeconds = 0L;
            countdownRunning = false;
            countdownRunningSinceMillis = 0L;
            countdownFinished = true;
            completedCountdowns++;
        }
    }

    public long countdownInitialSeconds() {
        return countdownInitialSeconds;
    }

    public long countdownRemainingSeconds(long now) {
        if (!countdownRunning) {
            return Math.max(0L, countdownRemainingSeconds);
        }
        long spent = Math.max(0L, (now - countdownRunningSinceMillis) / 1000L);
        return Math.max(0L, countdownRemainingSeconds - spent);
    }

    public boolean countdownRunning() {
        return countdownRunning;
    }

    public boolean countdownFinished() {
        return countdownFinished;
    }

    public boolean countdownConfigured() {
        return countdownInitialSeconds > 0L;
    }

    public long addLap(long now) {
        if (!running) {
            return -1L;
        }
        long elapsed = elapsedSeconds(now);
        long split = Math.max(0L, elapsed - lastLapElapsedSeconds);
        if (split <= 0L) {
            return 0L;
        }
        lastLapElapsedSeconds = elapsed;
        lapSplitsSeconds.add(split);
        if (lapSplitsSeconds.size() > MAX_LAPS) {
            lapSplitsSeconds.remove(0);
        }
        return split;
    }

    public void clearLaps() {
        lastLapElapsedSeconds = 0L;
        lapSplitsSeconds.clear();
    }

    public List<Long> lapSplits() {
        return List.copyOf(lapSplitsSeconds);
    }

    public long personalBestSeconds() {
        return personalBestSeconds;
    }

    public long completedCountdowns() {
        return completedCountdowns;
    }

    public void updatePersonalBest(long now) {
        updatePersonalBestFromElapsedSeconds(elapsedSeconds(now));
    }

    private void updatePersonalBestFromElapsedSeconds(long elapsedSeconds) {
        if (elapsedSeconds > personalBestSeconds) {
            personalBestSeconds = elapsedSeconds;
        }
    }

    public NbtCompound toNbt(long now) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("style", style.id());
        nbt.putString("colorPreset", colorPreset.id());
        nbt.putString("displayChannel", displayChannel.name().toLowerCase());
        nbt.putLong("elapsedMillis", elapsedMillis(now));
        nbt.putBoolean("timerRunning", running);
        nbt.putBoolean("timerPaused", paused);
        nbt.putLong("countdownInitialSeconds", countdownInitialSeconds);
        nbt.putLong("countdownRemainingSeconds", countdownRemainingSeconds(now));
        nbt.putBoolean("countdownRunning", countdownRunning);
        nbt.putBoolean("countdownFinished", countdownFinished);
        nbt.putLong("personalBestSeconds", personalBestSeconds);
        nbt.putLong("completedCountdowns", completedCountdowns);
        nbt.putLong("lastLapElapsedSeconds", lastLapElapsedSeconds);
        long[] lapArray = new long[lapSplitsSeconds.size()];
        for (int i = 0; i < lapSplitsSeconds.size(); i++) {
            lapArray[i] = lapSplitsSeconds.get(i);
        }
        nbt.putLongArray("lapSplitsSeconds", lapArray);
        return nbt;
    }

    public static TimerState fromNbt(NbtCompound nbt, long now, TimerStyle fallbackStyle, ColorPreset fallbackPreset, DisplayChannel fallbackChannel) {
        TimerStyle style = TimerStyle.fromId(nbt.getString("style", fallbackStyle.id())).orElse(fallbackStyle);
        ColorPreset preset = ColorPreset.fromId(nbt.getString("colorPreset", fallbackPreset.id())).orElse(fallbackPreset);
        DisplayChannel channel = DisplayChannel.fromId(nbt.getString("displayChannel", fallbackChannel.name().toLowerCase())).orElse(fallbackChannel);

        TimerState state = new TimerState(style, preset, channel);
        state.accumulatedMillis = Math.max(0L, nbt.getLong("elapsedMillis", 0L));
        state.running = nbt.getBoolean("timerRunning", false);
        state.paused = nbt.getBoolean("timerPaused", false);
        if (state.running) {
            state.runningSinceMillis = now;
        }

        state.countdownInitialSeconds = Math.max(0L, nbt.getLong("countdownInitialSeconds", 0L));
        state.countdownRemainingSeconds = Math.max(0L, nbt.getLong("countdownRemainingSeconds", 0L));
        state.countdownRunning = nbt.getBoolean("countdownRunning", false);
        state.countdownFinished = nbt.getBoolean("countdownFinished", false);
        if (state.countdownRunning) {
            state.countdownRunningSinceMillis = now;
        }

        state.personalBestSeconds = Math.max(0L, nbt.getLong("personalBestSeconds", 0L));
        state.completedCountdowns = Math.max(0L, nbt.getLong("completedCountdowns", 0L));
        state.lastLapElapsedSeconds = Math.max(0L, nbt.getLong("lastLapElapsedSeconds", 0L));
        long[] lapArray = nbt.getLongArray("lapSplitsSeconds").orElse(new long[0]);
        for (long lap : lapArray) {
            if (lap > 0L) {
                state.lapSplitsSeconds.add(lap);
            }
            if (state.lapSplitsSeconds.size() >= MAX_LAPS) {
                break;
            }
        }
        return state;
    }
}
