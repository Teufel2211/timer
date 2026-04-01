package com.timerwave.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.timerwave.config.TimerConfig;
import com.timerwave.timer.ColorPreset;
import com.timerwave.timer.TimerManager;
import com.timerwave.timer.TimerState;
import com.timerwave.timer.TimerStyle;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.command.argument.EntityArgumentType;

public final class TimerCommand {
    private static final SuggestionProvider<ServerCommandSource> STYLE_SUGGESTIONS = (context, builder) -> {
        for (TimerStyle style : TimerStyle.values()) {
            builder.suggest(style.id());
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> COLOR_SUGGESTIONS = (context, builder) -> {
        for (ColorPreset preset : ColorPreset.values()) {
            builder.suggest(preset.id());
        }
        return builder.buildFuture();
    };

    private TimerCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, TimerManager manager) {
        dispatcher.register(CommandManager.literal("timer")
                .executes(context -> status(context, manager))
                .then(CommandManager.literal("start")
                        .executes(context -> start(context, manager)))
                .then(CommandManager.literal("stop")
                        .executes(context -> stop(context, manager)))
                .then(CommandManager.literal("pause")
                        .executes(context -> pause(context, manager)))
                .then(CommandManager.literal("resume")
                        .executes(context -> resume(context, manager)))
                .then(CommandManager.literal("reset")
                        .executes(context -> reset(context, manager)))
                .then(CommandManager.literal("lap")
                        .executes(context -> lap(context, manager))
                        .then(CommandManager.literal("clear")
                                .executes(context -> lapClear(context, manager))))
                .then(CommandManager.literal("laps")
                        .executes(context -> laps(context, manager)))
                .then(CommandManager.literal("best")
                        .executes(context -> best(context, manager)))
                .then(CommandManager.literal("compare")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> compare(context, manager))))
                .then(CommandManager.literal("top")
                        .then(CommandManager.argument("limit", IntegerArgumentType.integer(1, 10))
                                .executes(context -> top(context, manager)))
                        .executes(context -> top(context, manager)))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0))
                                .executes(context -> setTimer(context, manager)))
                        .then(CommandManager.argument("days", IntegerArgumentType.integer(0))
                                .then(CommandManager.argument("hours", IntegerArgumentType.integer(0, 23))
                                        .then(CommandManager.argument("minutes", IntegerArgumentType.integer(0, 59))
                                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0, 59))
                                                        .executes(context -> setTimerDetailed(context, manager)))))))
                .then(CommandManager.literal("status")
                        .executes(context -> status(context, manager)))
                .then(CommandManager.literal("style")
                        .then(CommandManager.argument("style", StringArgumentType.word())
                                .suggests(STYLE_SUGGESTIONS)
                                .executes(context -> style(context, manager))))
                .then(CommandManager.literal("color")
                        .then(CommandManager.argument("preset", StringArgumentType.word())
                                .suggests(COLOR_SUGGESTIONS)
                                .executes(context -> color(context, manager))))
                .then(CommandManager.literal("config")
                        .executes(TimerCommand::config))
                .then(CommandManager.literal("help")
                        .executes(TimerCommand::help))
                .then(CommandManager.literal("countdown")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1))
                                        .executes(context -> countdownSet(context, manager))))
                        .then(CommandManager.literal("start")
                                .executes(context -> countdownStart(context, manager)))
                        .then(CommandManager.literal("stop")
                                .executes(context -> countdownStop(context, manager)))
                        .then(CommandManager.literal("reset")
                                .executes(context -> countdownReset(context, manager)))
                        .then(CommandManager.literal("status")
                                .executes(context -> countdownStatus(context, manager))))
        );
    }

    private static int start(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            int started = manager.syncStartAllOnline(context.getSource().getServer());
            context.getSource().sendFeedback(() ->
                    Text.literal("Synchronized start executed for " + started + " online player(s)."), true);
            return 1;
        }

        boolean started = manager.start(player);
        if (started) {
            context.getSource().sendFeedback(() -> Text.literal("Timer started."), false);
        } else {
            context.getSource().sendError(Text.literal("Timer is already running."));
        }
        return 1;
    }

    private static int stop(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        boolean stopped = manager.stop(player);
        if (stopped) {
            context.getSource().sendFeedback(() -> Text.literal("Timer stopped."), false);
        } else {
            context.getSource().sendError(Text.literal("Timer is already stopped."));
        }
        return 1;
    }

    private static int pause(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        boolean paused = manager.pause(player);
        if (paused) {
            context.getSource().sendFeedback(() -> Text.literal("Timer paused."), false);
        } else {
            context.getSource().sendError(Text.literal("Timer is not running."));
        }
        return 1;
    }

    private static int resume(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        boolean resumed = manager.resume(player);
        if (resumed) {
            context.getSource().sendFeedback(() -> Text.literal("Timer resumed."), false);
        } else {
            context.getSource().sendError(Text.literal("Timer is not paused."));
        }
        return 1;
    }

    private static int reset(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        manager.reset(player);
        context.getSource().sendFeedback(() -> Text.literal("Timer reset to 0d 0h 0m 0s."), false);
        return 1;
    }

    private static int setTimer(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        long seconds = IntegerArgumentType.getInteger(context, "seconds");
        manager.setElapsed(player, seconds);
        context.getSource().sendFeedback(() ->
                Text.literal("Timer set to " + TimerManager.formatDuration(seconds) + "."), false);
        return 1;
    }

    private static int setTimerDetailed(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        long days = IntegerArgumentType.getInteger(context, "days");
        long hours = IntegerArgumentType.getInteger(context, "hours");
        long minutes = IntegerArgumentType.getInteger(context, "minutes");
        long seconds = IntegerArgumentType.getInteger(context, "seconds");
        long totalSeconds = (days * 86400L) + (hours * 3600L) + (minutes * 60L) + seconds;
        manager.setElapsed(player, totalSeconds);
        context.getSource().sendFeedback(() ->
                Text.literal("Timer set to " + TimerManager.formatDuration(totalSeconds) + "."), false);
        return 1;
    }

    private static int style(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        String raw = StringArgumentType.getString(context, "style");
        TimerStyle style = TimerStyle.fromId(raw).orElse(null);
        if (style == null) {
            context.getSource().sendError(Text.literal("Unknown style. Available: " + listStyles()));
            return 0;
        }

        manager.setStyle(player, style);
        context.getSource().sendFeedback(() -> Text.literal("Timer style set to " + style.id() + "."), false);
        return 1;
    }

    private static int color(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        String raw = StringArgumentType.getString(context, "preset");
        ColorPreset preset = ColorPreset.fromId(raw).orElse(null);
        if (preset == null) {
            context.getSource().sendError(Text.literal("Unknown color preset. Available: " + listPresets()));
            return 0;
        }

        manager.setColorPreset(player, preset);
        context.getSource().sendFeedback(() -> Text.literal("Color preset set to " + preset.id() + "."), false);
        return 1;
    }

    private static int status(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        TimerState state = manager.getOrCreate(player);
        long seconds = state.elapsedSeconds(System.currentTimeMillis());
        String mode;
        if (state.running()) {
            mode = "running";
        } else if (state.paused()) {
            mode = "paused";
        } else {
            mode = "stopped";
        }

        Text status = Text.literal("Timer: " + TimerManager.formatDuration(seconds)
                + " | state=" + mode
                + " | style=" + state.style().id()
                + " | color=" + state.colorPreset().id()
                + " | best=" + TimerManager.formatDuration(state.personalBestSeconds()));
        context.getSource().sendFeedback(() -> status, false);
        return 1;
    }

    private static int config(CommandContext<ServerCommandSource> context) {
        TimerConfig config = TimerConfig.current();
        Text text = Text.literal("Config: defaultStyle=" + config.defaultStyle
                + ", defaultColorPreset=" + config.defaultColorPreset
                + ", defaultDisplay=" + config.defaultDisplay
                + ", showWhilePaused=" + config.showWhilePaused);
        context.getSource().sendFeedback(() -> text, false);
        return 1;
    }

    private static int help(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Timer commands: /timer start|stop|pause|resume|reset|set|status"), false);
        context.getSource().sendFeedback(() -> Text.literal("Splits: /timer lap | /timer laps | /timer lap clear"), false);
        context.getSource().sendFeedback(() -> Text.literal("Stats: /timer best | /timer compare <player> | /timer top [limit]"), false);
        context.getSource().sendFeedback(() -> Text.literal("Set time: /timer set <seconds> or /timer set <days> <hours> <minutes> <seconds>"), false);
        context.getSource().sendFeedback(() -> Text.literal("Countdown: /timer countdown set <seconds>|start|stop|reset|status"), false);
        context.getSource().sendFeedback(() -> Text.literal("Style: /timer style <" + listStyles() + ">"), false);
        context.getSource().sendFeedback(() -> Text.literal("Colors: /timer color <" + listPresets() + ">"), false);
        context.getSource().sendFeedback(() -> Text.literal("Config: /timer config"), false);
        return 1;
    }

    private static int lap(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        long split = manager.addLap(player);
        if (split < 0L) {
            context.getSource().sendError(Text.literal("Timer must be running to add a lap."));
            return 0;
        }
        if (split == 0L) {
            context.getSource().sendError(Text.literal("Lap ignored because no time passed."));
            return 0;
        }
        TimerState state = manager.getOrCreate(player);
        int lapIndex = state.lapSplits().size();
        context.getSource().sendFeedback(() ->
                Text.literal("Lap " + lapIndex + ": +" + TimerManager.formatDuration(split)
                        + " | total " + TimerManager.formatDuration(state.elapsedSeconds(System.currentTimeMillis()))), false);
        return 1;
    }

    private static int laps(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        TimerState state = manager.getOrCreate(player);
        List<Long> laps = state.lapSplits();
        if (laps.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("No laps recorded yet."), false);
            return 1;
        }
        context.getSource().sendFeedback(() -> Text.literal("Recent laps (" + laps.size() + "):"), false);
        for (int i = 0; i < laps.size(); i++) {
            int lapNumber = i + 1;
            long lapSeconds = laps.get(i);
            context.getSource().sendFeedback(() ->
                    Text.literal("Lap " + lapNumber + ": +" + TimerManager.formatDuration(lapSeconds)), false);
        }
        return 1;
    }

    private static int lapClear(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        manager.clearLaps(player);
        context.getSource().sendFeedback(() -> Text.literal("Laps cleared."), false);
        return 1;
    }

    private static int best(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        TimerState state = manager.getOrCreate(player);
        context.getSource().sendFeedback(() ->
                Text.literal("Best: " + TimerManager.formatDuration(state.personalBestSeconds())
                        + " | countdown completions=" + state.completedCountdowns()), false);
        return 1;
    }

    private static int compare(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity self = requirePlayer(context);
        ServerPlayerEntity other = EntityArgumentType.getPlayer(context, "player");
        TimerState selfState = manager.getOrCreate(self);
        TimerState otherState = manager.getOrCreate(other);
        long now = System.currentTimeMillis();
        long selfSeconds = selfState.elapsedSeconds(now);
        long otherSeconds = otherState.elapsedSeconds(now);
        long diff = Math.abs(selfSeconds - otherSeconds);
        String relation = selfSeconds == otherSeconds ? "equal to" : (selfSeconds > otherSeconds ? "ahead of" : "behind");

        context.getSource().sendFeedback(() ->
                Text.literal("You are " + relation + " " + other.getName().getString()
                        + " by " + TimerManager.formatDuration(diff)
                        + " (you: " + TimerManager.formatDuration(selfSeconds)
                        + ", " + other.getName().getString() + ": " + TimerManager.formatDuration(otherSeconds) + ")"), false);
        return 1;
    }

    private static int top(CommandContext<ServerCommandSource> context, TimerManager manager) {
        int limit = context.getNodes().stream().anyMatch(node -> "limit".equals(node.getNode().getName()))
                ? IntegerArgumentType.getInteger(context, "limit")
                : 5;
        long now = System.currentTimeMillis();
        List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList().stream()
                .sorted(Comparator.comparingLong((ServerPlayerEntity p) -> manager.getOrCreate(p).elapsedSeconds(now)).reversed())
                .limit(limit)
                .toList();

        if (players.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("No online players to rank."), false);
            return 1;
        }

        context.getSource().sendFeedback(() -> Text.literal("Timer leaderboard (online):"), false);
        for (int i = 0; i < players.size(); i++) {
            ServerPlayerEntity player = players.get(i);
            long seconds = manager.getOrCreate(player).elapsedSeconds(now);
            int rank = i + 1;
            context.getSource().sendFeedback(() ->
                    Text.literal("#" + rank + " " + player.getName().getString() + " - " + TimerManager.formatDuration(seconds)), false);
        }
        return 1;
    }

    private static int countdownSet(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        manager.setCountdown(player, seconds);
        context.getSource().sendFeedback(() ->
                Text.literal("Countdown set to " + TimerManager.formatDuration(seconds) + "."), false);
        return 1;
    }

    private static int countdownStart(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        boolean started = manager.startCountdown(player);
        if (started) {
            context.getSource().sendFeedback(() -> Text.literal("Countdown started."), false);
        } else {
            context.getSource().sendError(Text.literal("Set a countdown first or stop the current one."));
        }
        return 1;
    }

    private static int countdownStop(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        boolean stopped = manager.stopCountdown(player);
        if (stopped) {
            context.getSource().sendFeedback(() -> Text.literal("Countdown stopped."), false);
        } else {
            context.getSource().sendError(Text.literal("Countdown is not running."));
        }
        return 1;
    }

    private static int countdownReset(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        manager.resetCountdown(player);
        context.getSource().sendFeedback(() -> Text.literal("Countdown reset."), false);
        return 1;
    }

    private static int countdownStatus(CommandContext<ServerCommandSource> context, TimerManager manager) throws CommandSyntaxException {
        ServerPlayerEntity player = requirePlayer(context);
        TimerState state = manager.getOrCreate(player);
        if (!state.countdownConfigured()) {
            context.getSource().sendFeedback(() -> Text.literal("Countdown: not configured."), false);
            return 1;
        }

        long now = System.currentTimeMillis();
        String status;
        if (state.countdownRunning()) {
            status = "running";
        } else if (state.countdownFinished()) {
            status = "finished";
        } else {
            status = "stopped";
        }

        context.getSource().sendFeedback(() ->
                Text.literal("Countdown: " + TimerManager.formatDuration(state.countdownRemainingSeconds(now))
                        + " | state=" + status), false);
        return 1;
    }

    private static ServerPlayerEntity requirePlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrThrow();
    }

    private static String listStyles() {
        return Arrays.stream(TimerStyle.values()).map(TimerStyle::id).collect(Collectors.joining(", "));
    }

    private static String listPresets() {
        return Arrays.stream(ColorPreset.values()).map(ColorPreset::id).collect(Collectors.joining(", "));
    }
}
