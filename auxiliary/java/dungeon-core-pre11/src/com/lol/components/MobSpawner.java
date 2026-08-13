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
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MobSpawner
implements Component<ChunkStore> {
    public static final BuilderCodec<MobSpawner> CODEC = BuilderCodec.builder(MobSpawner.class, MobSpawner::new)
        .append(new KeyedCodec<>("Role", Codec.STRING), (self, val) -> self.role = val, self -> self.role).add()
        .append(new KeyedCodec<>("Count", Codec.INTEGER), (self, val) -> self.count = val, self -> self.count).add()
        .append(new KeyedCodec<>("Health", Codec.INTEGER), (self, val) -> self.health = val, self -> self.health).add()
        .append(new KeyedCodec<>("Range", Codec.DOUBLE), (self, val) -> self.range = val, self -> self.range).add()
        .build();
    static ComponentType<ChunkStore, MobSpawner> componentType;
    public String role = "Horse";
    public int count = 0;
    public double range = 1.0;
    public int health = 100;

    public static ComponentType<ChunkStore, MobSpawner> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, MobSpawner> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        MobSpawner self = new MobSpawner();
        self.role = this.role;
        self.count = this.count;
        self.range = this.range;
        self.health = this.health;
        return self;
    }
}
