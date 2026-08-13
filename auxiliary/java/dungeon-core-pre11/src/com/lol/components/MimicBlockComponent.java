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
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MimicBlockComponent
implements Component<ChunkStore> {
    private String blockType = "EMPTY";
    public static BuilderCodec<MimicBlockComponent> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(MimicBlockComponent.class, MimicBlockComponent::new).append(new KeyedCodec("Type", (Codec)Codec.STRING), (self, val) -> {
        self.blockType = val;
    }, self -> self.blockType).add()).build();
    static ComponentType<ChunkStore, MimicBlockComponent> componentType;

    public static ComponentType<ChunkStore, MimicBlockComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, MimicBlockComponent> c) {
        componentType = c;
    }

    public BlockType getBlockType() {
        return BlockType.getAssetMap().getAsset(this.blockType);
    }

    public void setBlockType(BlockType bt) {
        this.blockType = bt.getId();
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        MimicBlockComponent self = new MimicBlockComponent();
        self.blockType = this.blockType;
        return self;
    }
}
