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
import com.lol.DungeonPlugin;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class SignalSender
implements Component<ChunkStore> {
    public static final BuilderCodec<SignalSender> CODEC = BuilderCodec.builder(SignalSender.class, SignalSender::new)
        .append(new KeyedCodec<>("LastSignal", Codec.BOOLEAN), (self, val) -> self.lastSignal = val, self -> self.lastSignal).add()
        .append(new KeyedCodec<>("Active", Codec.BOOLEAN), (self, val) -> self.active = val, self -> self.active).add()
        .append(new KeyedCodec<>("Id", Codec.UUID_BINARY), (self, val) -> self.id = val, self -> self.id).add()
        .build();
    static ComponentType<ChunkStore, SignalSender> componentType;
    public boolean lastSignal = false;
    public boolean active = true;
    public UUID id = null;

    public static ComponentType<ChunkStore, SignalSender> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, SignalSender> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        SignalSender self = new SignalSender();
        self.lastSignal = this.lastSignal;
        self.active = this.active;
        if (this.id == null) {
            self.id = UUID.randomUUID();
            DungeonPlugin.logger.info("SIGNAL SENDER UUID: " + String.valueOf(self.id));
        } else {
            self.id = this.id;
        }
        return self;
    }
}
