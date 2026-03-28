package com.timerwave.display;

import com.timerwave.timer.ColorPreset;
import com.timerwave.timer.TimerManager;
import com.timerwave.timer.TimerState;
import com.timerwave.timer.TimerStyle;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TimerTextFactory {
    private TimerTextFactory() {
    }

    public static MutableText buildDisplay(TimerState state, long nowMillis) {
        return buildDisplay(state, nowMillis, false);
    }

    public static MutableText buildDisplay(TimerState state, long nowMillis, boolean redBlinkPhase) {
        if (state.countdownConfigured() && (state.countdownRunning() || state.countdownFinished())) {
            return buildCountdownDisplay(state, nowMillis, redBlinkPhase);
        }

        long elapsedSeconds = state.elapsedSeconds(nowMillis);
        String base = styleLabel(TimerManager.formatDuration(elapsedSeconds));
        MutableText wave = waveText(base, state.colorPreset(), nowMillis, state.style());

        if (state.paused()) {
            wave.append(Text.literal(" [PAUSED]").formatted(Formatting.GRAY));
        } else if (!state.running() && elapsedSeconds == 0L) {
            wave.append(Text.literal(" [IDLE]").formatted(Formatting.DARK_GRAY));
        }

        return wave;
    }

    private static MutableText buildCountdownDisplay(TimerState state, long nowMillis, boolean redBlinkPhase) {
        if (state.countdownFinished()) {
            Formatting flashColor = redBlinkPhase ? Formatting.RED : Formatting.DARK_RED;
            return Text.literal("0d 0h 0m 0s").formatted(flashColor, Formatting.BOLD);
        }

        long remaining = state.countdownRemainingSeconds(nowMillis);
        return waveText(TimerManager.formatDuration(remaining), state.colorPreset(), nowMillis, state.style());
    }

    private static String styleLabel(String time) {
        return time;
    }

    private static MutableText waveText(String text, ColorPreset preset, long nowMillis, TimerStyle style) {
        MutableText result = Text.empty();
        double timeFactor = nowMillis / style.timeDivisor();
        double globalPulse = (Math.sin(timeFactor * 1.45) + 1.0) / 2.0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            double wave = (Math.sin(timeFactor + i * style.phaseStep()) + 1.0) / 2.0;
            boolean italic = false;
            boolean underline = false;

            switch (style) {
                case SMOOTH -> {
                    // Two slow waves blended + smoothstep for very soft transitions.
                    double secondary = (Math.sin((timeFactor * 0.55) + i * (style.phaseStep() * 0.45)) + 1.0) / 2.0;
                    wave = smoothstep((wave * 0.7) + (secondary * 0.3));
                }
                case PULSE -> {
                    wave = (wave * 0.55) + (globalPulse * 0.45);
                    underline = globalPulse > 0.82;
                }
                case MONO -> {
                    wave = Math.round(wave * 3.0) / 3.0;
                }
                case RETRO -> {
                    wave = Math.round(wave * 4.0) / 4.0;
                    if (((nowMillis / 150L) + i) % 2L == 0L) {
                        wave = Math.max(0.0, wave - 0.12);
                    }
                    italic = (i % 2) == 0;
                }
                case MINIMAL -> {
                    // Keep minimal style intentionally clean.
                }
            }

            if (style.steppedGradient()) {
                wave = Math.round(wave * 5.0) / 5.0;
            }

            int rgb = preset.sample(clamp01(wave));
            if (style == TimerStyle.MONO) {
                rgb = toMonochrome(rgb);
            }

            final int finalRgb = rgb;
            final boolean finalItalic = italic;
            final boolean finalUnderline = underline;
            MutableText part = Text.literal(String.valueOf(c)).styled(textStyle -> textStyle
                    .withColor(finalRgb)
                    .withBold(style.bold())
                    .withItalic(finalItalic)
                    .withUnderline(finalUnderline));
            result.append(part);
        }
        return result;
    }

    private static double smoothstep(double value) {
        double t = clamp01(value);
        return t * t * (3.0 - 2.0 * t);
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static int toMonochrome(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int gray = (int) Math.round((r * 0.299) + (g * 0.587) + (b * 0.114));
        return (gray << 16) | (gray << 8) | gray;
    }
}
