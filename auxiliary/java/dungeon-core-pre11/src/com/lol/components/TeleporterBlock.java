/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.codec.codecs.EnumCodec
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
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TeleporterBlock
implements Component<ChunkStore> {
    public static final BuilderCodec<TeleporterBlock> CODEC = BuilderCodec.builder(TeleporterBlock.class, TeleporterBlock::new)
        .append(new KeyedCodec<>("TpMode", new EnumCodec<>(TeleporterMode.class)), (self, val) -> self.tpMode = val, self -> self.tpMode).add()
        .append(new KeyedCodec<>("RecvMode", new EnumCodec<>(RecvMode.class)), (self, val) -> self.recvMode = val, self -> self.recvMode).add()
        .append(new KeyedCodec<>("SendMode", new EnumCodec<>(SendMode.class)), (self, val) -> self.sendMode = val, self -> self.sendMode).add()
        .append(new KeyedCodec<>("Id", Codec.UUID_BINARY), (self, val) -> self.id = val, self -> self.id).add()
        .append(new KeyedCodec<>("Connected", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new)), (self, val) -> self.connected = val.clone(), self -> self.connected).add()
        .append(new KeyedCodec<>("Next", Codec.INTEGER), (self, val) -> self.nextIdx = val, self -> self.nextIdx).add()
        .append(new KeyedCodec<>("Relative", Codec.BOOLEAN), (self, val) -> self.relative = val, self -> self.relative).add()
        .build();
    static ComponentType<ChunkStore, TeleporterBlock> componentType;
    public TeleporterMode tpMode = TeleporterMode.SEND;
    public SendMode sendMode = SendMode.Random;
    public RecvMode recvMode = RecvMode.Nearest;
    public UUID id = null;
    public UUID[] connected = new UUID[0];
    public boolean relative = false;
    public int nextIdx = 0;

    public static ComponentType<ChunkStore, TeleporterBlock> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, TeleporterBlock> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        TeleporterBlock self = new TeleporterBlock();
        self.tpMode = this.tpMode;
        self.recvMode = this.recvMode;
        self.sendMode = this.sendMode;
        self.id = this.id;
        self.connected = (UUID[])this.connected.clone();
        self.nextIdx = this.nextIdx;
        self.relative = this.relative;
        return self;
    }

    public static enum TeleporterMode {
        SEND,
        RECV;

    }

    public static enum SendMode {
        Random,
        RoundRobin;

    }

    public static enum RecvMode {
        Nearest,
        All,
        Random;

    }
}
