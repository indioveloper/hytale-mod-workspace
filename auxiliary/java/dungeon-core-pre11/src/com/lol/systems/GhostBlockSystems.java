/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.EntityEventSystem
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.component.system.RefSystem
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.RefSystem;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.DungeonPlugin;
import com.lol.components.GhostBlock;
import com.lol.components.SignalReceiver;
import com.lol.events.ResetEvent;
import com.lol.resources.SignalRouter;
import com.lol.utils.BlockHelper;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class GhostBlockSystems {
    public static void register(ComponentRegistryProxy<ChunkStore> registry) {
        registry.registerSystem((ISystem)new GhostRefSystem());
        registry.registerSystem((ISystem)new GhostEventSystem());
    }

    public static class GhostRefSystem
    extends RefSystem<ChunkStore> {
        @NullableDecl
        public Query<ChunkStore> getQuery() {
            return Query.and((Query[])new Query[]{GhostBlock.getComponentType(), SignalReceiver.getComponentType()});
        }

        public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            Vector3i blockPos;
            DungeonPlugin.logger.info("New GhostBlock!");
            SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
            SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
            GhostBlock gb = (GhostBlock)commandBuffer.getComponent(ref, GhostBlock.getComponentType());
            assert (rec != null);
            assert (gb != null);
            if (rec.getId() == null) {
                rec.setId(UUID.randomUUID());
            }
            if ((blockPos = BlockHelper.getPosForBlock(ref, commandBuffer)) == null) {
                return;
            }
            World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
            BlockType backupType = w.getBlockType(blockPos);
            if (gb.bt == null || backupType != BlockType.EMPTY) {
                gb.bt = backupType.getId();
            }
            router.registerListener(new SignalRouter.ListenerRegister(rec.getId(), SignalRouter.OR, bools -> {
                boolean prev;
                boolean active = bools[0];
                boolean bl = prev = !rec.prev.isEmpty() ? rec.prev.getFirst() : false;
                if (prev == active) {
                    return;
                }
                rec.prev.clear();
                rec.prev.add(active);
                BlockType bt = w.getBlockType(blockPos);
                int settings = 198;
                if (!active && bt == BlockType.EMPTY && gb.bt != null && gb.bt.length() > 1) {
                    w.setBlock(blockPos.x, blockPos.y, blockPos.z, gb.bt, settings);
                } else {
                    w.breakBlock(blockPos.x, blockPos.y, blockPos.z, settings);
                }
            }));
        }

        public void onEntityRemove(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            DungeonPlugin.logger.info("Remove GhostBlock!");
            SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
            SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
            assert (rec != null);
            if (rec.getId() == null) {
                return;
            }
            router.unregisterListener(rec.getId());
        }
    }

    public static class GhostEventSystem
    extends EntityEventSystem<ChunkStore, ResetEvent> {
        public GhostEventSystem() {
            super(ResetEvent.class);
        }

        public void handle(int i, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer, @NonNullDecl ResetEvent resetEvent) {
            if (resetEvent.running) {
                return;
            }
            GhostBlock gb = (GhostBlock)archetypeChunk.getComponent(i, GhostBlock.getComponentType());
            assert (gb != null);
            World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
            Vector3i blockPos = BlockHelper.getPosForBlock((Ref<ChunkStore>)archetypeChunk.getReferenceTo(i), commandBuffer);
            if (blockPos == null) {
                return;
            }
            int settings = 198;
            if (gb.bt != null && gb.bt.length() > 1) {
                w.setBlock(blockPos.x, blockPos.y, blockPos.z, gb.bt, settings);
            }
        }

        @NullableDecl
        public Query getQuery() {
            return Query.and((Query[])new Query[]{GhostBlock.getComponentType(), SignalReceiver.getComponentType()});
        }
    }
}

