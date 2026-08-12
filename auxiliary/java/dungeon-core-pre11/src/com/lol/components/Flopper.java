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

public class Flopper
implements Component<ChunkStore> {
    public static final BuilderCodec<Flopper> CODEC = BuilderCodec.builder(Flopper.class, Flopper::new)
        .append(new KeyedCodec<>("InitalState", Codec.BOOLEAN), (block, val) -> block.initalState = val, block -> block.initalState).add()
        .append(new KeyedCodec<>("LastState", Codec.BOOLEAN), (block, val) -> block.lastState = val, block -> block.lastState).add()
        .build();
    static ComponentType<ChunkStore, Flopper> componentType;
    public boolean initalState = false;
    public boolean lastState = false;

    public static ComponentType<ChunkStore, Flopper> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, Flopper> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        Flopper self = new Flopper();
        self.initalState = this.initalState;
        self.lastState = this.lastState;
        return self;
    }
}
