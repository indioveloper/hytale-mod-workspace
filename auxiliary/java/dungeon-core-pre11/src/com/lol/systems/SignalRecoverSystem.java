/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.RefSystem
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.DungeonPlugin;
import com.lol.components.SignalReceiver;
import com.lol.resources.SignalRouter;
import com.lol.resources.TimeoutAction;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class SignalRecoverSystem
extends RefSystem<ChunkStore> {
    public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        SignalReceiver signalReceiver = (SignalReceiver)store.getComponent(ref, SignalReceiver.getComponentType());
        TimeoutAction ta = (TimeoutAction)store.getResource(TimeoutAction.getResourceType());
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        assert (signalReceiver != null);
        ta.addTask(5L, () -> {
            if (signalReceiver.getId() == null) {
                return;
            }
            DungeonPlugin.logger.info("Recover connections.. " + signalReceiver.channels.length);
            for (UUID channel : signalReceiver.channels) {
                router.listen(signalReceiver.getId(), channel);
            }
        });
    }

    public void onEntityRemove(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
    }

    @NullableDecl
    public Query<ChunkStore> getQuery() {
        return SignalReceiver.getComponentType();
    }
}

