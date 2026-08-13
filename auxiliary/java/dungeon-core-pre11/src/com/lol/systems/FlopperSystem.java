/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.EntityEventSystem
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.component.system.RefSystem
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.components.Flopper;
import com.lol.components.SignalReceiver;
import com.lol.components.SignalSender;
import com.lol.events.ResetEvent;
import com.lol.resources.SignalRouter;
import com.lol.resources.TimeoutAction;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class FlopperSystem {
    public static void register(ComponentRegistryProxy<ChunkStore> registry) {
        registry.registerSystem((ISystem)new FlopperRefSystem());
        registry.registerSystem((ISystem)new FlopperEventSystem());
    }

    public static class FlopperRefSystem
    extends RefSystem<ChunkStore> {
        public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
            SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
            SignalSender sender = (SignalSender)commandBuffer.getComponent(ref, SignalSender.getComponentType());
            Flopper flopper = (Flopper)commandBuffer.getComponent(ref, Flopper.getComponentType());
            assert (rec != null);
            assert (sender != null);
            assert (flopper != null);
            if (rec.getId() == null) {
                rec.setId(UUID.randomUUID());
            }
            router.registerListener(new SignalRouter.ListenerRegister(rec.getId(), SignalRouter.OR, bools -> {
                boolean prev;
                boolean active = bools[0];
                if (!sender.active) {
                    return;
                }
                boolean bl = prev = !rec.prev.isEmpty() ? rec.prev.getFirst() : false;
                if (prev == active) {
                    return;
                }
                rec.prev.clear();
                rec.prev.add(active);
                if (!active) {
                    return;
                }
                flopper.lastState = !flopper.lastState;
                router.sendSignal(new SignalRouter.LogicSignal(sender.id, flopper.lastState));
            }));
        }

        public void onEntityRemove(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
            SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
            assert (rec != null);
            if (rec.getId() == null) {
                return;
            }
            router.unregisterListener(rec.getId());
        }

        @NullableDecl
        public Query<ChunkStore> getQuery() {
            return Query.and((Query[])new Query[]{SignalSender.getComponentType(), Flopper.getComponentType()});
        }
    }

    public static class FlopperEventSystem
    extends EntityEventSystem<ChunkStore, ResetEvent> {
        public FlopperEventSystem() {
            super(ResetEvent.class);
        }

        public void handle(int i, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer, @NonNullDecl ResetEvent resetEvent) {
            SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
            TimeoutAction timeout = (TimeoutAction)store.getResource(TimeoutAction.getResourceType());
            SignalSender sender = (SignalSender)archetypeChunk.getComponent(i, SignalSender.getComponentType());
            Flopper flopper = (Flopper)archetypeChunk.getComponent(i, Flopper.getComponentType());
            assert (sender != null);
            assert (flopper != null);
            World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
            flopper.lastState = flopper.initalState;
            timeout.addTask(2L, () -> {
                boolean signal = flopper.initalState;
                if (!sender.active) {
                    signal = false;
                }
                router.sendSignal(new SignalRouter.LogicSignal(sender.id, signal));
            });
        }

        @NullableDecl
        public Query<ChunkStore> getQuery() {
            return Query.and((Query[])new Query[]{SignalSender.getComponentType(), Flopper.getComponentType()});
        }
    }
}
