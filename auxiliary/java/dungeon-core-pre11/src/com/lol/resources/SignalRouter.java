/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.common.util.ArrayUtil
 *  com.hypixel.hytale.component.Resource
 *  com.hypixel.hytale.component.ResourceType
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.resources;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.DungeonPlugin;
import com.lol.components.SignalReceiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class SignalRouter
implements Resource<ChunkStore> {
    public static final BuilderCodec<SignalRouter> CODEC = BuilderCodec.builder(SignalRouter.class, SignalRouter::new).build();
    static ResourceType<ChunkStore, SignalRouter> resourceType;
    Map<UUID, LogicChannel> channels = new HashMap<UUID, LogicChannel>();
    Map<UUID, Listener> registeredListeners = new HashMap<UUID, Listener>();
    public static Function<LogicSignal[], Optional<boolean[]>> OR;
    public static Function<LogicSignal[], Optional<boolean[]>> AND;
    public static Function<LogicSignal[], Optional<boolean[]>> NOR;
    public static Function<LogicSignal[], Optional<boolean[]>> NAND;

    public static ResourceType<ChunkStore, SignalRouter> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<ChunkStore, SignalRouter> c) {
        resourceType = c;
    }

    public boolean listensTo(UUID id, UUID channel) {
        Listener listener = this.registeredListeners.get(id);
        if (listener == null) {
            return false;
        }
        for (UUID chan : listener.channels) {
            if (!chan.equals(channel)) continue;
            return true;
        }
        return false;
    }

    public void sendSignal(LogicSignal signal) {
        if (this.channels.containsKey(signal.channel)) {
            this.channels.get((Object)signal.channel).signal = signal.signal;
        }
    }

    public void registerListener(ListenerRegister listenerRegister) {
        Listener listener = new Listener(listenerRegister.id, listenerRegister.folder, listenerRegister.callback);
        this.registeredListeners.put(listener.id, listener);
    }

    public void listen(UUID id, UUID channel) {
        this.listen(id, channel, null);
    }

    public void listen(UUID id, UUID channel, SignalReceiver signalReceiver) {
        LogicChannel chan = this.channels.get(channel);
        Listener listener = this.registeredListeners.get(id);
        if (listener == null) {
            DungeonPlugin.logger.info("ERROR: listener not found " + String.valueOf(id));
            return;
        }
        if (chan == null) {
            chan = new LogicChannel(channel);
            DungeonPlugin.logger.info("creating channel " + String.valueOf(channel));
            this.channels.put(channel, chan);
        } else {
            DungeonPlugin.logger.info("got channel " + String.valueOf(channel));
        }
        if (listener.channels.contains(channel)) {
            return;
        }
        chan.listeners.add(listener);
        listener.channels.add(channel);
        if (signalReceiver != null) {
            DungeonPlugin.logger.info("testing channel " + String.valueOf(channel));
            if (!ArrayUtil.contains((Object[])signalReceiver.channels, (Object)channel)) {
                DungeonPlugin.logger.info("adding channel to memory " + String.valueOf(channel));
                signalReceiver.channels = (UUID[])ArrayUtil.append((Object[])signalReceiver.channels, (Object)channel);
            }
        }
    }

    public void unlisten(UUID id, UUID channel) {
        this.unlisten(id, channel, null);
    }

    public void unlisten(UUID id, UUID channel, SignalReceiver signalReceiver) {
        int index;
        LogicChannel chan = this.channels.get(channel);
        Listener listener = this.registeredListeners.get(id);
        if (chan == null || listener == null) {
            return;
        }
        chan.listeners.remove(listener);
        listener.channels.remove(channel);
        if (signalReceiver != null && (index = ArrayUtil.indexOf((Object[])signalReceiver.channels, (Object)channel)) >= 0) {
            signalReceiver.channels = (UUID[])ArrayUtil.remove((Object[])signalReceiver.channels, (int)index);
        }
    }

    public void unregisterListener(UUID id) {
        Listener listener = this.registeredListeners.get(id);
        if (listener == null) {
            return;
        }
        this.registeredListeners.remove(id);
        for (UUID channel : listener.channels) {
            LogicChannel chan = this.channels.get(channel);
            if (chan == null) continue;
            chan.listeners.remove(listener);
            if (!chan.listeners.isEmpty()) continue;
            this.channels.remove(channel);
        }
    }

    public void update() {
        for (Listener listener : this.registeredListeners.values()) {
            ArrayList<LogicSignal> signals = new ArrayList<LogicSignal>();
            for (UUID channelId : listener.channels) {
                LogicChannel channel = this.channels.get(channelId);
                assert (channel != null);
                signals.add(new LogicSignal(channelId, channel.signal));
            }
            Optional<boolean[]> res = listener.folder.apply(signals.toArray(new LogicSignal[0]));
            res.ifPresent(booleans -> listener.callback.accept((boolean[])booleans));
        }
    }

    @NullableDecl
    public Resource<ChunkStore> clone() {
        return new SignalRouter();
    }

    static {
        OR = signals -> {
            for (LogicSignal signal : signals) {
                if (!signal.signal) continue;
                return Optional.of(new boolean[]{true});
            }
            return Optional.of(new boolean[]{false});
        };
        AND = signals -> {
            for (LogicSignal signal : signals) {
                if (signal.signal) continue;
                return Optional.of(new boolean[]{false});
            }
            return Optional.of(new boolean[]{true});
        };
        NOR = signals -> {
            for (LogicSignal signal : signals) {
                if (!signal.signal) continue;
                return Optional.of(new boolean[]{false});
            }
            return Optional.of(new boolean[]{true});
        };
        NAND = signals -> {
            for (LogicSignal signal : signals) {
                if (signal.signal) continue;
                return Optional.of(new boolean[]{true});
            }
            return Optional.of(new boolean[]{false});
        };
    }

    static class Listener {
        public UUID id;
        public Function<LogicSignal[], Optional<boolean[]>> folder;
        public List<UUID> channels;
        Consumer<boolean[]> callback;

        Listener(UUID id, Function<LogicSignal[], Optional<boolean[]>> folder, Consumer<boolean[]> callback) {
            this.id = id;
            this.folder = folder;
            this.callback = callback;
            this.channels = new ArrayList<UUID>();
        }
    }

    public record LogicSignal(UUID channel, boolean signal) {
    }

    static class LogicChannel {
        public UUID id;
        public boolean signal;
        public List<Listener> listeners;

        public LogicChannel(UUID id) {
            this.id = id;
            this.listeners = new ArrayList<Listener>();
        }
    }

    public record ListenerRegister(UUID id, Function<LogicSignal[], Optional<boolean[]>> folder, Consumer<boolean[]> callback) {
    }
}
