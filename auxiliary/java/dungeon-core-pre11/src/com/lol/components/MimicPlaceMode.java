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
 *  com.hypixel.hytale.component.Holder
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MimicPlaceMode
implements Component<EntityStore> {
    public Vector3i pos;
    public Holder<ChunkStore> holder;
    public static BuilderCodec<MimicPlaceMode> CODEC = BuilderCodec.builder(MimicPlaceMode.class, MimicPlaceMode::new)
        .append(new KeyedCodec<>("Position", Vector3iUtil.CODEC), (ent, val) -> ent.pos = val, ent -> ent.pos).add()
        .build();
    static ComponentType<EntityStore, MimicPlaceMode> componentType;

    public static ComponentType<EntityStore, MimicPlaceMode> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<EntityStore, MimicPlaceMode> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<EntityStore> clone() {
        MimicPlaceMode self = new MimicPlaceMode();
        self.pos = this.pos;
        self.holder = this.holder;
        return self;
    }
}
