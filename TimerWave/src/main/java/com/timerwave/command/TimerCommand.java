package com.timerwave.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.timerwave.config.TimerConfig;
import com.timerwave.timer.ColorPreset;
import com.timerwave.timer.TimerManager;
import com.timerwave.timer.TimerState;
import com.timerwave.timer.TimerStyle;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.stream.Collectors;

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
        ServerPlayerEntity player = requirePlayer(context);
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
                + " | color=" + state.colorPreset().id());
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
        context.getSource().sendFeedback(() -> Text.literal("Timer commands: /timer start|stop|pause|resume|reset|status"), false);
        context.getSource().sendFeedback(() -> Text.literal("Countdown: /timer countdown set <seconds>|start|stop|reset|status"), false);
        context.getSource().sendFeedback(() -> Text.literal("Style: /timer style <" + listStyles() + ">"), false);
        context.getSource().sendFeedback(() -> Text.literal("Colors: /timer color <" + listPresets() + ">"), false);
        context.getSource().sendFeedback(() -> Text.literal("Config: /timer config"), false);
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
