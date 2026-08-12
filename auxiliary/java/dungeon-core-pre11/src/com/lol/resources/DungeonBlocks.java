/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.codec.codecs.set.SetCodec
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Resource
 *  com.hypixel.hytale.component.ResourceType
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.system.EcsEvent
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.resources;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.DungeonPlugin;
import com.lol.components.EntityCountRef;
import com.lol.components.SignalSender;
import com.lol.events.ResetEvent;
import com.lol.utils.BlockHelper;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class DungeonBlocks
implements Resource<ChunkStore> {
    public static final BuilderCodec<DungeonBlocks> CODEC = BuilderCodec.builder(DungeonBlocks.class, DungeonBlocks::new)
        .append(new KeyedCodec<>("Active", Codec.BOOLEAN), (res, val) -> res.active = val, res -> res.active).add()
        .append(new KeyedCodec<>("Blocks", new SetCodec<>(Vector3iUtil.CODEC, HashSet::new, false)),
            (res, val) -> res.blocks = val, res -> res.blocks).add()
        .build();
    static ResourceType<ChunkStore, DungeonBlocks> resourceType;
    public boolean active = false;
    public Set<Vector3i> blocks = new HashSet<Vector3i>();

    public static ResourceType<ChunkStore, DungeonBlocks> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<ChunkStore, DungeonBlocks> c) {
        resourceType = c;
    }

    public void setState(boolean active, World w, Vector3i pos, ComponentAccessor<ChunkStore> store) {
        Ref<ChunkStore> blockRef = BlockHelper.getBlockRef(w, pos);
        if (active) {
            BlockHelper.setStateForBlock("Active", w, pos);
        } else {
            BlockHelper.setStateForBlock("Normal", w, pos);
        }
        if (blockRef != null) {
            w.execute(() -> {
                SignalSender signal = (SignalSender)store.getComponent(blockRef, SignalSender.getComponentType());
                if (signal != null) {
                    signal.active = active;
                }
                if (!active) {
                    w.getEntityStore().getStore().forEachChunk(EntityCountRef.getComponentType(), (archetypeChunk, commandBuffer) -> {
                        for (int idx = 0; idx < archetypeChunk.size(); ++idx) {
                            Ref ref = archetypeChunk.getReferenceTo(idx);
                            DungeonPlugin.logger.info("entity: " + String.valueOf(ref));
                            w.execute(() -> {
                                if (ref.isValid()) {
                                    w.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
                                }
                            });
                        }
                    });
                }
                w.getChunkStore().getStore().invoke(blockRef, (EcsEvent)new ResetEvent(active));
            });
        }
    }

    public void addBlock(Vector3i pos, ComponentAccessor<ChunkStore> store) {
        this.blocks.add(pos);
        World w = ((ChunkStore)store.getExternalData()).getWorld();
        this.setState(this.active, w, pos, store);
    }

    public void activate(boolean active, World w) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        Store store = w.getChunkStore().getStore();
        for (Vector3i block : this.blocks) {
            w.execute(() -> this.setState(active, w, block, (ComponentAccessor<ChunkStore>)store));
        }
    }

    @NullableDecl
    public Resource<ChunkStore> clone() {
        return new DungeonBlocks();
    }
}
