/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.EntityEventSystem
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.components.SignalReceiver;
import com.lol.events.ResetEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class SignalSystems {
    public static void register(ComponentRegistryProxy<ChunkStore> registry) {
        registry.registerSystem((ISystem)new SignalEventSystem());
    }

    public static class SignalEventSystem
    extends EntityEventSystem<ChunkStore, ResetEvent> {
        public SignalEventSystem() {
            super(ResetEvent.class);
        }

        public void handle(int i, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer, @NonNullDecl ResetEvent resetEvent) {
            SignalReceiver signalReceiver = (SignalReceiver)archetypeChunk.getComponent(i, SignalReceiver.getComponentType());
            assert (signalReceiver != null);
            if (resetEvent.running) {
                signalReceiver.prev.clear();
            }
        }

        @NullableDecl
        public Query<ChunkStore> getQuery() {
            return SignalReceiver.getComponentType();
        }
    }
}

