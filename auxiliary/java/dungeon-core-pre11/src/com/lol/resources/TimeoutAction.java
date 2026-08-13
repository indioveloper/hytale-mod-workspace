/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.component.Resource
 *  com.hypixel.hytale.component.ResourceType
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.resources;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TimeoutAction
implements Resource<ChunkStore> {
    public static final BuilderCodec<TimeoutAction> CODEC = BuilderCodec.builder(TimeoutAction.class, TimeoutAction::new).build();
    static ResourceType<ChunkStore, TimeoutAction> resourceType;
    public Set<Task> tasks = new HashSet<Task>();

    public static ResourceType<ChunkStore, TimeoutAction> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<ChunkStore, TimeoutAction> c) {
        resourceType = c;
    }

    public void addTask(long ticks, Runnable command) {
        this.tasks.add(new Task(ticks, command));
    }

    @NullableDecl
    public Resource<ChunkStore> clone() {
        return new TimeoutAction();
    }

    public static class Task {
        public long ticksLeft;
        public Runnable command;
        public long lastTick = 0L;

        public Task(long ticks, Runnable command) {
            this.ticksLeft = ticks;
            this.command = command;
        }
    }
}
