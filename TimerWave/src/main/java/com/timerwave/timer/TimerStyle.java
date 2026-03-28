package com.timerwave.timer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum TimerStyle {
    MINIMAL("minimal", DisplayChannel.ACTIONBAR, false, 0.35, 520.0, false),
    SMOOTH("smooth", DisplayChannel.ACTIONBAR, false, 0.18, 920.0, false),
    PULSE("pulse", DisplayChannel.BOSSBAR, true, 0.58, 360.0, false),
    MONO("mono", DisplayChannel.ACTIONBAR, false, 0.22, 700.0, true),
    RETRO("retro", DisplayChannel.BOSSBAR, true, 0.20, 800.0, true);

    private final String id;
    private final DisplayChannel displayChannel;
    private final boolean bold;
    private final double phaseStep;
    private final double timeDivisor;
    private final boolean steppedGradient;

    TimerStyle(String id, DisplayChannel displayChannel, boolean bold, double phaseStep, double timeDivisor, boolean steppedGradient) {
        this.id = id;
        this.displayChannel = displayChannel;
        this.bold = bold;
        this.phaseStep = phaseStep;
        this.timeDivisor = timeDivisor;
        this.steppedGradient = steppedGradient;
    }

    public String id() {
        return id;
    }

    public DisplayChannel displayChannel() {
        return displayChannel;
    }

    public boolean bold() {
        return bold;
    }

    public double phaseStep() {
        return phaseStep;
    }

    public double timeDivisor() {
        return timeDivisor;
    }

    public boolean steppedGradient() {
        return steppedGradient;
    }

    public static Optional<TimerStyle> fromId(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(value -> value.id.equals(normalized)).findFirst();
    }
}
