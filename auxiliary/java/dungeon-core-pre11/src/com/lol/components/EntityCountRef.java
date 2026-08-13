/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EntityCountRef
implements Component<EntityStore> {
    public static BuilderCodec<EntityCountRef> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(EntityCountRef.class, EntityCountRef::new).append(new KeyedCodec("Id", (Codec)Codec.UUID_BINARY), (ent, val) -> {
        ent.id = val;
    }, ent -> ent.id).add()).build();
    static ComponentType<EntityStore, EntityCountRef> componentType;
    public UUID id = null;

    public static ComponentType<EntityStore, EntityCountRef> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<EntityStore, EntityCountRef> c) {
        componentType = c;
    }

    public EntityCountRef() {
    }

    public EntityCountRef(UUID id) {
        this.id = id;
    }

    @NullableDecl
    public Component<EntityStore> clone() {
        EntityCountRef self = new EntityCountRef();
        self.id = this.id;
        return self;
    }
}
