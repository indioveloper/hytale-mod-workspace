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
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.EntityCountRef;
import com.lol.resources.EntityCountResource;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EntityCountSystem
extends RefSystem<EntityStore> {
    public void onEntityAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        EntityCountResource counter = (EntityCountResource)commandBuffer.getResource(EntityCountResource.getResourceType());
        EntityCountRef entity = (EntityCountRef)commandBuffer.getComponent(ref, EntityCountRef.getComponentType());
        assert (entity != null);
        if (entity.id == null) {
            entity.id = UUID.randomUUID();
        }
        counter.addEntity(entity.id);
    }

    public void onEntityRemove(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        EntityCountResource counter = (EntityCountResource)commandBuffer.getResource(EntityCountResource.getResourceType());
        EntityCountRef entity = (EntityCountRef)commandBuffer.getComponent(ref, EntityCountRef.getComponentType());
        assert (entity != null);
        if (entity.id == null) {
            entity.id = UUID.randomUUID();
        }
        counter.removeEntity(entity.id);
    }

    @NullableDecl
    public Query<EntityStore> getQuery() {
        return EntityCountRef.getComponentType();
    }
}
