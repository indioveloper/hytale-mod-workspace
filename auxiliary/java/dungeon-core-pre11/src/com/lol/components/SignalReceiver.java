/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.codec.codecs.array.ArrayCodec
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class SignalReceiver
implements Component<ChunkStore> {
    UUID id = null;
    public List<Boolean> prev = new ArrayList<Boolean>();
    public UUID[] channels = new UUID[0];
    public static final BuilderCodec<SignalReceiver> CODEC = BuilderCodec.builder(SignalReceiver.class, SignalReceiver::new)
        .append(new KeyedCodec<>("Prev", new ArrayCodec<>(Codec.BOOLEAN, Boolean[]::new)),
            (self, val) -> self.prev = new ArrayList<>(List.of(val)),
            self -> self.prev.toArray(Boolean[]::new)).add()
        .append(new KeyedCodec<>("Channels", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new)),
            (self, val) -> self.channels = val.clone(), self -> self.channels).add()
        .build();
    static ComponentType<ChunkStore, SignalReceiver> componentType;

    public static ComponentType<ChunkStore, SignalReceiver> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, SignalReceiver> c) {
        componentType = c;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        SignalReceiver self = new SignalReceiver();
        self.id = this.id;
        self.prev = new ArrayList<Boolean>(this.prev);
        self.channels = (UUID[])this.channels.clone();
        return self;
    }
}
