/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class DungeonBlock
implements Component<ChunkStore> {
    public static final BuilderCodec<DungeonBlock> CODEC = BuilderCodec.builder(DungeonBlock.class, DungeonBlock::new).build();
    static ComponentType<ChunkStore, DungeonBlock> componentType;

    public static ComponentType<ChunkStore, DungeonBlock> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, DungeonBlock> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        return new DungeonBlock();
    }
}

