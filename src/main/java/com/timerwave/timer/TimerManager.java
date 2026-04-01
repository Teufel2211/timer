package com.timerwave.timer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.timerwave.config.TimerConfig;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class TimerManager {
    private final Map<UUID, TimerState> states = new ConcurrentHashMap<>();
    private MinecraftServer server;
    private TimerWorldState worldState;
    private long lastPersistTick;
    private boolean dirty;

    public TimerState getOrCreate(ServerPlayerEntity player) {
        return states.computeIfAbsent(player.getUuid(), ignored -> {
            TimerConfig config = TimerConfig.current();
            DisplayChannel channel = DisplayChannel.fromId(config.defaultDisplay)
                    .orElse(config.resolveDefaultStyle().displayChannel());
            return new TimerState(config.resolveDefaultStyle(), config.resolveDefaultColorPreset(), channel);
        });
    }

    public void attachServer(MinecraftServer server) {
        this.server = server;
        this.worldState = TimerWorldState.get(server);
        this.lastPersistTick = server.getTicks();
        loadFromWorldState();
    }

    public void tick(MinecraftServer server) {
        if (this.server == null || this.server != server) {
            attachServer(server);
        }

        if (server.getTicks() - lastPersistTick >= 100L) {
            persistNow();
        }
    }

    public Collection<Map.Entry<UUID, TimerState>> entries() {
        return states.entrySet();
    }

    public boolean start(ServerPlayerEntity player) {
        boolean result = getOrCreate(player).start(System.currentTimeMillis());
        if (result) {
            dirty = true;
        }
        return result;
    }

    public int syncStartAllOnline(MinecraftServer server) {
        long now = System.currentTimeMillis();
        int changed = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            TimerState state = getOrCreate(player);
            state.reset();
            if (state.start(now)) {
                changed++;
            }
        }
        if (changed > 0) {
            dirty = true;
        }
        return changed;
    }

    public boolean stop(ServerPlayerEntity player) {
        boolean result = getOrCreate(player).stop(System.currentTimeMillis());
        if (result) {
            dirty = true;
        }
        return result;
    }

    public boolean pause(ServerPlayerEntity player) {
        boolean result = getOrCreate(player).pause(System.currentTimeMillis());
        if (result) {
            dirty = true;
        }
        return result;
    }

    public boolean resume(ServerPlayerEntity player) {
        boolean result = getOrCreate(player).resume(System.currentTimeMillis());
        if (result) {
            dirty = true;
        }
        return result;
    }

    public void reset(ServerPlayerEntity player) {
        getOrCreate(player).reset();
        dirty = true;
    }

    public void setElapsed(ServerPlayerEntity player, long totalSeconds) {
        TimerState state = getOrCreate(player);
        state.setElapsedSeconds(totalSeconds, System.currentTimeMillis());
        state.updatePersonalBest(System.currentTimeMillis());
        dirty = true;
    }

    public void pauseForDeath(ServerPlayerEntity player) {
        if (getOrCreate(player).pause(System.currentTimeMillis())) {
            dirty = true;
        }
    }

    public void pauseForLogout(ServerPlayerEntity player) {
        if (getOrCreate(player).pause(System.currentTimeMillis())) {
            dirty = true;
        }
    }

    public void setStyle(ServerPlayerEntity player, TimerStyle style) {
        getOrCreate(player).setStyle(style);
        dirty = true;
    }

    public void setColorPreset(ServerPlayerEntity player, ColorPreset preset) {
        getOrCreate(player).setColorPreset(preset);
        dirty = true;
    }

    public void setCountdown(ServerPlayerEntity player, long seconds) {
        getOrCreate(player).setCountdownSeconds(seconds);
        dirty = true;
    }

    public boolean startCountdown(ServerPlayerEntity player) {
        boolean result = getOrCreate(player).startCountdown(System.currentTimeMillis());
        if (result) {
            dirty = true;
        }
        return result;
    }

    public boolean stopCountdown(ServerPlayerEntity player) {
        boolean result = getOrCreate(player).stopCountdown(System.currentTimeMillis());
        if (result) {
            dirty = true;
        }
        return result;
    }

    public void resetCountdown(ServerPlayerEntity player) {
        getOrCreate(player).resetCountdown();
        dirty = true;
    }

    public long addLap(ServerPlayerEntity player) {
        long split = getOrCreate(player).addLap(System.currentTimeMillis());
        if (split > 0L) {
            dirty = true;
        }
        return split;
    }

    public void clearLaps(ServerPlayerEntity player) {
        getOrCreate(player).clearLaps();
        dirty = true;
    }

    public void persistNow() {
        if (server == null || worldState == null) {
            return;
        }
        if (!dirty && server.getTicks() - lastPersistTick < 100L) {
            return;
        }

        long now = System.currentTimeMillis();
        Map<UUID, NbtCompound> serialized = new HashMap<>();
        for (Map.Entry<UUID, TimerState> entry : states.entrySet()) {
            serialized.put(entry.getKey(), entry.getValue().toNbt(now));
        }

        worldState.replacePlayerStates(serialized);
        lastPersistTick = server.getTicks();
        dirty = false;
    }

    public void clear() {
        persistNow();
        states.clear();
        worldState = null;
        server = null;
        dirty = false;
    }

    private void loadFromWorldState() {
        if (worldState == null) {
            return;
        }
        Map<UUID, NbtCompound> serialized = worldState.copyPlayerStates();
        if (serialized.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        TimerConfig config = TimerConfig.current();
        DisplayChannel fallbackChannel = DisplayChannel.fromId(config.defaultDisplay)
                .orElse(config.resolveDefaultStyle().displayChannel());

        states.clear();
        for (Map.Entry<UUID, NbtCompound> entry : serialized.entrySet()) {
            TimerState loaded = TimerState.fromNbt(
                    entry.getValue(),
                    now,
                    config.resolveDefaultStyle(),
                    config.resolveDefaultColorPreset(),
                    fallbackChannel
            );
            states.put(entry.getKey(), loaded);
        }
    }

    public static String formatDuration(long totalSeconds) {
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }
}
