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

public class RelayComponent
implements Component<ChunkStore> {
    public static final BuilderCodec<RelayComponent> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(RelayComponent.class, RelayComponent::new).append(new KeyedCodec("Delay", (Codec)Codec.INTEGER), (block, val) -> {
        block.delay = val;
    }, block -> block.delay).add()).build();
    static ComponentType<ChunkStore, RelayComponent> componentType;
    public int delay = 1;

    public static ComponentType<ChunkStore, RelayComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, RelayComponent> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        RelayComponent self = new RelayComponent();
        self.delay = this.delay;
        return self;
    }
}

