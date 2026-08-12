/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem$Ticking
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.dependency.Dependency
 *  com.hypixel.hytale.component.dependency.Order
 *  com.hypixel.hytale.component.dependency.SystemDependency
 *  com.hypixel.hytale.component.system.tick.TickingSystem
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.systems;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.resources.SignalRouter;
import java.util.Set;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SignalUpdateSystem
extends TickingSystem<ChunkStore> {
    @NonNullDecl
    public Set<Dependency<ChunkStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.AFTER, ChunkBlockTickSystem.Ticking.class));
    }

    public void tick(float v, int i, @NonNullDecl Store<ChunkStore> store) {
        ((SignalRouter)store.getResource(SignalRouter.getResourceType())).update();
    }
}

