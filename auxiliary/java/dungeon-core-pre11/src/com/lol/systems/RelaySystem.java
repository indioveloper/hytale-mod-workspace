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
import com.lol.components.RelayComponent;
import com.lol.components.SignalReceiver;
import com.lol.components.SignalSender;
import com.lol.resources.SignalRouter;
import com.lol.resources.TimeoutAction;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class RelaySystem
extends RefSystem<ChunkStore> {
    @NullableDecl
    public Query<ChunkStore> getQuery() {
        return Query.and((Query[])new Query[]{SignalSender.getComponentType(), RelayComponent.getComponentType()});
    }

    public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
        SignalSender sender = (SignalSender)commandBuffer.getComponent(ref, SignalSender.getComponentType());
        RelayComponent relay = (RelayComponent)commandBuffer.getComponent(ref, RelayComponent.getComponentType());
        TimeoutAction timeout = (TimeoutAction)commandBuffer.getStore().getResource(TimeoutAction.getResourceType());
        assert (rec != null);
        assert (sender != null);
        assert (relay != null);
        if (rec.getId() == null) {
            rec.setId(UUID.randomUUID());
        }
        router.registerListener(new SignalRouter.ListenerRegister(rec.getId(), SignalRouter.OR, bools -> {
            boolean active = bools[0];
            if (!sender.active) {
                return;
            }
            timeout.addTask(relay.delay, () -> router.sendSignal(new SignalRouter.LogicSignal(sender.id, active)));
        }));
    }

    public void onEntityRemove(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
    }
}

