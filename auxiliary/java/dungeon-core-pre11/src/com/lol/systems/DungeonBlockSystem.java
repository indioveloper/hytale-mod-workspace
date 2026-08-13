/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.RefSystem
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 *  org.jline.utils.Log
 */
package com.lol.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.components.DungeonBlock;
import com.lol.resources.DungeonBlocks;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jline.utils.Log;

public class DungeonBlockSystem
extends RefSystem<ChunkStore> {
    public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        DungeonBlocks db = (DungeonBlocks)commandBuffer.getResource(DungeonBlocks.getResourceType());
        Vector3i pos = BlockHelper.getPosForBlock(ref, commandBuffer);
        w.execute(() -> {
            Log.info((Object[])new Object[]{"adding block..."});
            db.addBlock(pos, (ComponentAccessor<ChunkStore>)commandBuffer);
        });
    }

    public void onEntityRemove(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        DungeonBlocks db = (DungeonBlocks)commandBuffer.getResource(DungeonBlocks.getResourceType());
        Vector3i pos = BlockHelper.getPosForBlock(ref, commandBuffer);
        db.blocks.remove(pos);
    }

    @NullableDecl
    public Query<ChunkStore> getQuery() {
        return DungeonBlock.getComponentType();
    }
}
