/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.codec.codecs.EnumCodec
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class PlayerDetector
implements Component<ChunkStore> {
    public static final BuilderCodec<PlayerDetector> CODEC = BuilderCodec.builder(PlayerDetector.class, PlayerDetector::new)
        .append(new KeyedCodec<>("Range", Codec.DOUBLE), (self, val) -> self.range = val, self -> self.range).add()
        .append(new KeyedCodec<>("Mode", new EnumCodec<>(DetectorMode.class)), (self, val) -> self.mode = val, self -> self.mode).add()
        .append(new KeyedCodec<>("Active", Codec.BOOLEAN), (self, val) -> self.active = val, self -> self.active).add()
        .build();
    static ComponentType<ChunkStore, PlayerDetector> componentType;
    public double range = 0.0;
    public DetectorMode mode = DetectorMode.SinglePulse;
    public boolean active = false;

    public static ComponentType<ChunkStore, PlayerDetector> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, PlayerDetector> c) {
        componentType = c;
    }

    public Component<ChunkStore> clone() {
        PlayerDetector self = new PlayerDetector();
        self.range = this.range;
        self.mode = this.mode;
        self.active = this.active;
        return self;
    }

    public static enum DetectorMode {
        SinglePulse,
        Pulse,
        Hold,
        Repeat;

    }
}
