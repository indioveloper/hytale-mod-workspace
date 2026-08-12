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

public class GhostBlock
implements Component<ChunkStore> {
    public String bt;
    public static final BuilderCodec<GhostBlock> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(GhostBlock.class, GhostBlock::new).append(new KeyedCodec("Type", (Codec)Codec.STRING), (self, val) -> {
        self.bt = val;
    }, self -> self.bt).add()).build();
    static ComponentType<ChunkStore, GhostBlock> componentType;

    public static ComponentType<ChunkStore, GhostBlock> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, GhostBlock> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        GhostBlock self = new GhostBlock();
        self.bt = this.bt;
        return self;
    }
}

