/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.component.Resource
 *  com.hypixel.hytale.component.ResourceType
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.resources;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EntityCountResource
implements Resource<EntityStore> {
    public static final BuilderCodec<EntityCountResource> CODEC = BuilderCodec.builder(EntityCountResource.class, EntityCountResource::new).build();
    static ResourceType<EntityStore, EntityCountResource> resourceType;
    Map<UUID, Consumer<Integer>> listeners = new HashMap<UUID, Consumer<Integer>>();
    Map<UUID, Integer> counts = new HashMap<UUID, Integer>();

    public static ResourceType<EntityStore, EntityCountResource> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, EntityCountResource> c) {
        resourceType = c;
    }

    public void register(UUID id, Consumer<Integer> consumer) {
        this.listeners.put(id, consumer);
    }

    public void unregister(UUID id) {
        this.listeners.remove(id);
    }

    public void addEntity(UUID id) {
        int count = this.counts.getOrDefault(id, 0) + 1;
        this.counts.put(id, count);
        Consumer<Integer> callback = this.listeners.get(id);
        if (callback != null) {
            callback.accept(count);
        }
    }

    public void removeEntity(UUID id) {
        int count = this.counts.getOrDefault(id, 0) - 1;
        this.counts.put(id, count);
        Consumer<Integer> callback = this.listeners.get(id);
        if (callback != null) {
            callback.accept(count);
        }
    }

    @NullableDecl
    public Resource<EntityStore> clone() {
        return new EntityCountResource();
    }
}

