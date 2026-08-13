/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.EntityEventSystem
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.math.matrix.Matrix4d
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.math.vector.Vector3d
 *  com.hypixel.hytale.math.vector.Vector3f
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.DebugShape
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
 *  com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent
 *  com.hypixel.hytale.server.core.modules.debug.DebugUtils
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk
 *  com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.ISystem;
import org.joml.Matrix4d;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.MimicPlaceMode;
import com.lol.resources.TimeoutAction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MimicPlaceSystems {
    public static void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)new MimicPlaceSystem());
        registry.registerSystem((ISystem)new MimicBreakSystem());
    }

    static class MimicPlaceSystem
    extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public MimicPlaceSystem() {
            super(PlaceBlockEvent.class);
        }

        public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl PlaceBlockEvent placeBlockEvent) {
            MimicPlaceMode pm = (MimicPlaceMode)archetypeChunk.getComponent(i, MimicPlaceMode.getComponentType());
            assert (pm != null);
            World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
            PlayerRef playerRef = (PlayerRef)archetypeChunk.getComponent(i, PlayerRef.getComponentType());
            assert (playerRef != null);
            if (pm.holder == null) {
                world.execute(() -> store.removeComponent(archetypeChunk.getReferenceTo(i), MimicPlaceMode.getComponentType()));
                return;
            }
            if (!placeBlockEvent.getTargetBlock().equals((Object)pm.pos)) {
                int flags = DebugUtils.FLAG_NO_WIREFRAME;
                DebugUtils.add(world, DebugShape.Cube, DebugUtils.makeMatrix(new Vector3d(pm.pos).add(0.5, 0.5, 0.5), 1.1), DebugUtils.COLOR_CYAN, 0.5f, flags);
                playerRef.sendMessage(Message.raw((String)"You have to place the mimic block in the area!"));
                placeBlockEvent.setCancelled(true);
                return;
            }
            Vector3i blockPos = placeBlockEvent.getTargetBlock();
            WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock((int)blockPos.x, (int)blockPos.z));
            if (chunk == null) {
                return;
            }
            String blockKey = placeBlockEvent.getItemInHand().getBlockKey();
            BlockType bt = BlockType.getAssetMap().getAsset(blockKey);
            if (bt == null) {
                return;
            }
            if (bt.getBlockEntity() != null) {
                playerRef.sendMessage(Message.raw((String)"Cannot use BlockEntities, try using a normal block without functionality!"));
                placeBlockEvent.setCancelled(true);
                return;
            }
            String ht = bt.getHitboxType();
            BlockBoundingBoxes boundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(ht);
            if (boundingBoxes != null && boundingBoxes.get(0).getBoundingBox().getMaximumThickness() > 1.0) {
                playerRef.sendMessage(Message.raw((String)"Cannot use Multiblocks, use a block that is smaller!"));
                placeBlockEvent.setCancelled(true);
                return;
            }
            TimeoutAction timeout = (TimeoutAction)world.getChunkStore().getStore().getResource(TimeoutAction.getResourceType());
            timeout.addTask(2L, () -> {
                int rotation = world.getBlockRotationIndex(blockPos.x, blockPos.y, blockPos.z);
                chunk.setState(blockPos.x, blockPos.y, blockPos.z, bt, rotation, pm.holder);
                chunk.markNeedsSaving();
            });
            world.execute(() -> store.removeComponent(archetypeChunk.getReferenceTo(i), MimicPlaceMode.getComponentType()));
        }

        @NullableDecl
        public Query<EntityStore> getQuery() {
            return MimicPlaceMode.getComponentType();
        }
    }

    static class MimicBreakSystem
    extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        public MimicBreakSystem() {
            super(BreakBlockEvent.class);
        }

        public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl BreakBlockEvent breakBlockEvent) {
            MimicPlaceMode pm = (MimicPlaceMode)archetypeChunk.getComponent(i, MimicPlaceMode.getComponentType());
            assert (pm != null);
            World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
            int flags = DebugUtils.FLAG_NO_WIREFRAME;
            DebugUtils.add(world, DebugShape.Cube, DebugUtils.makeMatrix(new Vector3d(pm.pos).add(0.5, 0.5, 0.5), 1.1), DebugUtils.COLOR_CYAN, 0.5f, flags);
            PlayerRef playerRef = (PlayerRef)archetypeChunk.getComponent(i, PlayerRef.getComponentType());
            assert (playerRef != null);
            playerRef.sendMessage(Message.raw((String)"You have to place the mimic block in the area!"));
            breakBlockEvent.setCancelled(true);
        }

        @NullableDecl
        public Query<EntityStore> getQuery() {
            return MimicPlaceMode.getComponentType();
        }
    }
}
