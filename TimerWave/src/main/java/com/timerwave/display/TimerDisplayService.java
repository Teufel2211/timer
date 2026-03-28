package com.timerwave.display;

import com.timerwave.config.TimerConfig;
import com.timerwave.timer.DisplayChannel;
import com.timerwave.timer.TimerManager;
import com.timerwave.timer.TimerState;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TimerDisplayService {
    private final TimerManager timerManager;
    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();

    public TimerDisplayService(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    public void tick(MinecraftServer server) {
        if (server.getTicks() % 4 != 0) {
            return;
        }

        long now = System.currentTimeMillis();
        Set<UUID> online = new HashSet<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            online.add(player.getUuid());
            TimerState state = timerManager.getOrCreate(player);
            updateForPlayer(player, state, now);
        }

        // Cleanup bars for offline players.
        bossBars.entrySet().removeIf(entry -> {
            if (online.contains(entry.getKey())) {
                return false;
            }
            entry.getValue().clearPlayers();
            return true;
        });
    }

    public void shutdown() {
        for (ServerBossBar bar : bossBars.values()) {
            bar.clearPlayers();
        }
        bossBars.clear();
    }

    private void updateForPlayer(ServerPlayerEntity player, TimerState state, long nowMillis) {
        state.tickCountdown(nowMillis);
        boolean timerVisible = state.running() || (TimerConfig.current().showWhilePaused && state.paused());
        boolean countdownVisible = state.countdownRunning() || state.countdownFinished();
        boolean shouldShow = timerVisible || countdownVisible;
        boolean countdownBarVisible = state.countdownConfigured() && (state.countdownRunning() || state.countdownFinished());
        DisplayChannel channel = state.displayChannel();
        boolean redBlinkPhase = ((nowMillis / 350L) % 2L) == 0L;
        Text displayText = TimerTextFactory.buildDisplay(state, nowMillis, redBlinkPhase);

        if (countdownBarVisible || channel == DisplayChannel.BOSSBAR) {
            hideActionbar(player);
            if (shouldShow) {
                showBossbar(player, state, nowMillis, displayText, countdownBarVisible);
            } else {
                hideBossbar(player.getUuid());
            }
            return;
        }

        hideBossbar(player.getUuid());
        if (shouldShow) {
            player.sendMessage(displayText, true);
        } else {
            hideActionbar(player);
        }
    }

    private void showBossbar(ServerPlayerEntity player, TimerState state, long nowMillis, Text text, boolean countdownBarVisible) {
        ServerBossBar bossBar = bossBars.computeIfAbsent(player.getUuid(), ignored ->
                new ServerBossBar(Text.empty(), BossBar.Color.BLUE, BossBar.Style.PROGRESS));

        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }

        float progress;
        if (countdownBarVisible) {
            long initial = Math.max(1L, state.countdownInitialSeconds());
            long remaining = state.countdownFinished() ? 0L : state.countdownRemainingSeconds(nowMillis);
            progress = Math.max(0.0f, Math.min(1.0f, remaining / (float) initial));
            bossBar.setColor(BossBar.Color.BLUE);
        } else {
            long cycleSeconds = 3600L;
            progress = Math.min(1.0f, (state.elapsedSeconds(nowMillis) % cycleSeconds) / (float) cycleSeconds);
            bossBar.setColor(BossBar.Color.BLUE);
        }

        bossBar.setPercent(progress);
        bossBar.setName(text);
        bossBar.setVisible(true);
    }

    private void hideBossbar(UUID playerUuid) {
        ServerBossBar bossBar = bossBars.remove(playerUuid);
        if (bossBar != null) {
            bossBar.clearPlayers();
        }
    }

    private void hideActionbar(ServerPlayerEntity player) {
        player.sendMessage(Text.empty(), true);
    }
}
