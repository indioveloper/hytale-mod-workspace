/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.system.tick.TickingSystem
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.DungeonPlugin;
import com.lol.resources.TimeoutAction;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TimeoutSystem
extends TickingSystem<ChunkStore> {
    public void tick(float v, int i, @NonNullDecl Store<ChunkStore> store) {
        World w = ((ChunkStore)store.getExternalData()).getWorld();
        w.execute(() -> {
            TimeoutAction timeout = (TimeoutAction)store.getResource(TimeoutAction.getResourceType());
            Set<TimeoutAction.Task> tasks = timeout.tasks;
            HashSet<TimeoutAction.Task> toRemove = new HashSet<TimeoutAction.Task>();
            for (TimeoutAction.Task task : tasks) {
                long diff;
                long last = task.lastTick;
                if (last <= 1L) {
                    last = w.getTick() - 1L;
                }
                if ((diff = w.getTick() - last) < 1L) {
                    diff = 1L;
                }
                task.ticksLeft -= diff;
                task.lastTick = w.getTick();
                if (task.ticksLeft > 0L) continue;
                toRemove.add(task);
                try {
                    task.command.run();
                }
                catch (Exception e) {
                    StackTraceElement[] trace = e.getStackTrace();
                    StringBuilder out = new StringBuilder(e.toString());
                    for (StackTraceElement elem : trace) {
                        out.append("\n").append("at ").append(elem.toString());
                    }
                    DungeonPlugin.logger.log(Level.SEVERE, out.toString());
                }
            }
            for (TimeoutAction.Task task : toRemove) {
                tasks.remove(task);
            }
            toRemove.clear();
        });
    }
}
