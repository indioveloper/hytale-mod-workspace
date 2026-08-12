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
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ItemSpawner
implements Component<ChunkStore> {
    public static final BuilderCodec<ItemSpawner> CODEC = BuilderCodec.builder(ItemSpawner.class, ItemSpawner::new)
        .append(new KeyedCodec<>("Item", ItemStack.CODEC), (self, val) -> self.item = val, self -> self.item).add()
        .append(new KeyedCodec<>("Count", Codec.INTEGER), (self, val) -> self.count = val, self -> self.count).add()
        .append(new KeyedCodec<>("Range", Codec.DOUBLE), (self, val) -> self.range = val, self -> self.range).add()
        .append(new KeyedCodec<>("Despawn", Codec.BOOLEAN), (self, val) -> self.despawn = val, self -> self.despawn).add()
        .build();
    static ComponentType<ChunkStore, ItemSpawner> componentType;
    public ItemStack item = ItemStack.EMPTY;
    public int count = 0;
    public double range = 1.0;
    public int health = 100;
    public boolean despawn = true;

    public static ComponentType<ChunkStore, ItemSpawner> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, ItemSpawner> c) {
        componentType = c;
    }

    @NullableDecl
    public Component<ChunkStore> clone() {
        ItemSpawner self = new ItemSpawner();
        self.item = this.item;
        self.count = this.count;
        self.range = this.range;
        self.health = this.health;
        self.despawn = this.despawn;
        return self;
    }
}
