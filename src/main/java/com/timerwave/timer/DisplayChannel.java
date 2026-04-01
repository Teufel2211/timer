package com.timerwave.timer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum DisplayChannel {
    ACTIONBAR,
    BOSSBAR;

    public static Optional<DisplayChannel> fromId(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(channel -> channel.name().equalsIgnoreCase(normalized))
                .findFirst();
    }
}
