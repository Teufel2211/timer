package com.timerwave.timer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum ColorPreset {
    BLUE("blue", new int[]{0x082567, 0x1E4DB7, 0x56C4FF}),
    BLUE_CYAN_WHITE("blue_cyan_white", new int[]{0x113C8D, 0x14B8FF, 0xEAFBFF}),
    VIOLET_BLUE("violet_blue", new int[]{0x602E9E, 0x3A4CE2, 0x58B8FF}),
    TURQUOISE_BLUE("turquoise_blue", new int[]{0x00B8A9, 0x00D9E8, 0x3572EF}),
    RAINBOW("rainbow", new int[]{0xFF3B30, 0xFF9500, 0xFFCC00, 0x34C759, 0x00C7BE, 0x0A84FF, 0xAF52DE}),
    GRAY("gray", new int[]{0x2A2A2A, 0x7C7C7C, 0xD6D6D6}),
    GRAPHITE("graphite", new int[]{0x1B1F24, 0x4A5666, 0x9EA8B3}),
    SILVER_BLUE("silver_blue", new int[]{0x5D6778, 0x8EA8C3, 0xDCEBFF}),
    FIRE("fire", new int[]{0x5C1400, 0xB52A00, 0xFF6B00, 0xFFD166}),
    FOREST("forest", new int[]{0x123524, 0x2D6A4F, 0x52B788, 0xB7E4C7}),
    SUNSET("sunset", new int[]{0x5F0F40, 0x9A031E, 0xFB8B24, 0xFFBA08}),
    ICE("ice", new int[]{0x0A2A43, 0x23658A, 0x4FB0C6, 0xD9F7FF}),
    PINK_PURPLE("pink_purple", new int[]{0x7B2CBF, 0xC77DFF, 0xFF70A6, 0xFFD6E7});

    private final String id;
    private final int[] colors;

    ColorPreset(String id, int[] colors) {
        this.id = id;
        this.colors = colors;
    }

    public String id() {
        return id;
    }

    public int sample(double t) {
        if (colors.length == 1) {
            return colors[0];
        }

        double wrapped = t - Math.floor(t);
        double scaled = wrapped * (colors.length - 1);
        int lower = (int) Math.floor(scaled);
        int upper = Math.min(colors.length - 1, lower + 1);
        double localT = scaled - lower;
        return lerpRgb(colors[lower], colors[upper], localT);
    }

    public static Optional<ColorPreset> fromId(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(value -> value.id.equals(normalized)).findFirst();
    }

    private static int lerpRgb(int a, int b, double t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;

        int rr = (int) Math.round(ar + (br - ar) * t);
        int rg = (int) Math.round(ag + (bg - ag) * t);
        int rb = (int) Math.round(ab + (bb - ab) * t);
        return (rr << 16) | (rg << 8) | rb;
    }
}
