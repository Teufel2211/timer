package com.timerwave;

import com.timerwave.command.TimerCommand;
import com.timerwave.config.TimerConfig;
import com.timerwave.display.TimerDisplayService;
import com.timerwave.timer.TimerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerWaveMod implements ModInitializer {
    public static final String MOD_ID = "timerwave";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static TimerManager timerManager;
    private static TimerDisplayService displayService;

    @Override
    public void onInitialize() {
        timerManager = new TimerManager();
        displayService = new TimerDisplayService(timerManager);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TimerCommand.register(dispatcher, timerManager));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() == 1) {
                TimerConfig.load(server);
                timerManager.attachServer(server);
            }
            timerManager.tick(server);
            displayService.tick(server);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                timerManager.pauseForDeath(player);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TimerConfig.save(server);
            timerManager.persistNow();
            displayService.shutdown();
            timerManager.clear();
        });

        LOGGER.info("TimerWave initialized.");
    }

    public static TimerManager getTimerManager() {
        return timerManager;
    }

    public static TimerDisplayService getDisplayService() {
        return displayService;
    }
}
