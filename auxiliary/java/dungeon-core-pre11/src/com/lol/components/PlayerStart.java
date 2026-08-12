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

public class PlayerStart
implements Component<ChunkStore> {
    public static final BuilderCodec<PlayerStart> CODEC = BuilderCodec.builder(PlayerStart.class, PlayerStart::new)
        .append(new KeyedCodec<>("Running", Codec.BOOLEAN), (block, val) -> block.running = val, block -> block.running).add()
        .append(new KeyedCodec<>("BannerTitle", Codec.STRING), (block, val) -> block.bannerTitle = val, block -> block.bannerTitle).add()
        .append(new KeyedCodec<>("BannerMessage", Codec.STRING), (block, val) -> block.bannerMessage = val, block -> block.bannerMessage).add()
        .build();
    static ComponentType<ChunkStore, PlayerStart> componentType;
    public boolean running;
    public String bannerTitle;
    public String bannerMessage;

    public static ComponentType<ChunkStore, PlayerStart> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, PlayerStart> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        PlayerStart self = new PlayerStart();
        self.bannerTitle = this.bannerTitle;
        self.bannerMessage = this.bannerMessage;
        self.running = this.running;
        return self;
    }
}
