package com.timerwave.timer;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimerWorldState extends PersistentState {
    private static final String STATE_ID = "timerwave_state";
    private static final Codec<Map<String, NbtCompound>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, NbtCompound.CODEC);
    private static final Codec<TimerWorldState> CODEC = MAP_CODEC.xmap(TimerWorldState::fromSerializedMap, TimerWorldState::toSerializedMap);

    public static final PersistentStateType<TimerWorldState> TYPE =
            new PersistentStateType<>(STATE_ID, TimerWorldState::new, CODEC, DataFixTypes.LEVEL);

    public static TimerWorldState get(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(TYPE);
    }

    private final Map<UUID, NbtCompound> playerStates = new HashMap<>();

    public TimerWorldState() {
    }

    private static TimerWorldState fromSerializedMap(Map<String, NbtCompound> serialized) {
        TimerWorldState state = new TimerWorldState();
        for (Map.Entry<String, NbtCompound> entry : serialized.entrySet()) {
            try {
                UUID uuid = UUID.fromString(entry.getKey());
                state.playerStates.put(uuid, entry.getValue().copy());
            } catch (IllegalArgumentException ignored) {
                // Skip invalid UUID entries.
            }
        }
        return state;
    }

    private Map<String, NbtCompound> toSerializedMap() {
        Map<String, NbtCompound> serialized = new HashMap<>();
        for (Map.Entry<UUID, NbtCompound> entry : playerStates.entrySet()) {
            serialized.put(entry.getKey().toString(), entry.getValue().copy());
        }
        return serialized;
    }

    public Map<UUID, NbtCompound> copyPlayerStates() {
        Map<UUID, NbtCompound> copy = new HashMap<>();
        for (Map.Entry<UUID, NbtCompound> entry : playerStates.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    public void replacePlayerStates(Map<UUID, NbtCompound> updated) {
        playerStates.clear();
        for (Map.Entry<UUID, NbtCompound> entry : updated.entrySet()) {
            playerStates.put(entry.getKey(), entry.getValue().copy());
        }
        markDirty();
    }
}
