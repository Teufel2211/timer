package com.timerwave.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.timerwave.TimerWaveMod;
import com.timerwave.timer.ColorPreset;
import com.timerwave.timer.TimerStyle;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TimerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "timerwave.json";

    public String defaultStyle = "minimal";
    public String defaultColorPreset = "blue";
    public String defaultDisplay = "actionbar";
    public boolean showWhilePaused = true;

    private static TimerConfig current = new TimerConfig();

    private TimerConfig() {
    }

    public static TimerConfig current() {
        return current;
    }

    public static void load(MinecraftServer server) {
        Path path = configPath(server);
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                current = new TimerConfig();
                save(server);
                return;
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                TimerConfig loaded = GSON.fromJson(reader, TimerConfig.class);
                if (loaded == null) {
                    current = new TimerConfig();
                } else {
                    current = loaded;
                }
            }
        } catch (IOException e) {
            TimerWaveMod.LOGGER.error("Could not load timer config. Using defaults.", e);
            current = new TimerConfig();
        }
    }

    public static void save(MinecraftServer server) {
        Path path = configPath(server);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(current, writer);
            }
        } catch (IOException e) {
            TimerWaveMod.LOGGER.error("Could not save timer config.", e);
        }
    }

    private static Path configPath(MinecraftServer server) {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public TimerStyle resolveDefaultStyle() {
        return TimerStyle.fromId(defaultStyle).orElse(TimerStyle.MINIMAL);
    }

    public ColorPreset resolveDefaultColorPreset() {
        return ColorPreset.fromId(defaultColorPreset).orElse(ColorPreset.BLUE);
    }
}
