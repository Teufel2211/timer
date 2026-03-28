package com.timerwave.timer;

import net.minecraft.nbt.NbtCompound;

public class TimerState {
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
        accumulatedMillis = 0L;
        runningSinceMillis = 0L;
        running = false;
        paused = false;
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
        return state;
    }
}
