/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.tick.EntityTickingSystem
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.util.EventTitleUtil
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.lol.components.PlayerStart;
import com.lol.resources.DungeonBlocks;
import com.lol.resources.TimeoutAction;
import com.lol.utils.BlockHelper;
import java.util.Collection;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlayerStartSystem
extends EntityTickingSystem<ChunkStore> {
    @NullableDecl
    public Query<ChunkStore> getQuery() {
        return PlayerStart.getComponentType();
    }

    public void tick(float v, int i, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        PlayerStart ps = (PlayerStart)archetypeChunk.getComponent(i, PlayerStart.getComponentType());
        assert (ps != null);
        if (!ps.running) {
            return;
        }
        World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        Vector3i blockPos = BlockHelper.getPosForBlock((Ref<ChunkStore>)archetypeChunk.getReferenceTo(i), commandBuffer);
        if (blockPos == null) {
            return;
        }
        Collection<PlayerRef> playerRefs = w.getPlayerRefs();
        LoggerUtil.getLogger().info("Searching players..");
        w.execute(() -> {
            Store entityStore = w.getEntityStore().getStore();
            boolean foundPlayer = false;
            for (PlayerRef pl : playerRefs) {
                TransformComponent transform;
                LoggerUtil.getLogger().info("found: " + pl.getUsername());
                Ref playerHolder = pl.getReference();
                if (playerHolder == null || (transform = (TransformComponent)entityStore.getComponent(playerHolder, TransformComponent.getComponentType())) == null) continue;
                if (new org.joml.Vector3d(blockPos).distanceSquared(transform.getPosition()) < 100.0) {
                    foundPlayer = true;
                    LoggerUtil.getLogger().info("Starting your dungeon..");
                }
                EventTitleUtil.showEventTitleToPlayer((PlayerRef)pl, (Message)Message.raw((String)ps.bannerTitle), (Message)Message.raw((String)ps.bannerMessage), (boolean)true);
            }
            if (!foundPlayer) {
                return;
            }
            DungeonBlocks db = (DungeonBlocks)commandBuffer.getResource(DungeonBlocks.getResourceType());
            ps.running = false;
            TimeoutAction timeout = (TimeoutAction)commandBuffer.getResource(TimeoutAction.getResourceType());
            w.getWorldConfig().setGameplayConfig("Dungeon");
            timeout.addTask(2L, () -> db.activate(true, w));
            LoggerUtil.getLogger().info("Started your dungeon!");
        });
    }
}
