/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.common.util.RandomUtil
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.RefSystem
 *  com.hypixel.hytale.math.vector.Transform
 *  com.hypixel.hytale.math.vector.Vector3d
 *  com.hypixel.hytale.math.vector.Vector3f
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.common.util.RandomUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.DungeonPlugin;
import com.lol.components.SignalReceiver;
import com.lol.components.TeleporterBlock;
import com.lol.resources.DungeonBlocks;
import com.lol.resources.SignalRouter;
import com.lol.utils.BlockHelper;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TeleporterSystem
extends RefSystem<ChunkStore> {
    @NullableDecl
    public Query<ChunkStore> getQuery() {
        return Query.and((Query[])new Query[]{TeleporterBlock.getComponentType(), SignalReceiver.getComponentType()});
    }

    PlayerRef getNearestPlayer(World world, Vector3i blockPos) {
        PlayerRef pl = null;
        double dist = Double.MAX_VALUE;
        Collection<PlayerRef> refs = world.getPlayerRefs();
        for (PlayerRef player : refs) {
            Ref playerRef = player.getReference();
            TransformComponent transform = (TransformComponent)world.getEntityStore().getStore().getComponent(playerRef, TransformComponent.getComponentType());
            Vector3d offset = new Vector3d(transform.getPosition()).sub(new Vector3d(blockPos));
            double new_dist = offset.lengthSquared();
            if (!(new_dist < dist)) continue;
            dist = new_dist;
            pl = player;
        }
        return pl;
    }

    Vector3i findTargetRecv(UUID id, DungeonBlocks db, World world, Store<ChunkStore> store) {
        for (Vector3i target : db.blocks) {
            TeleporterBlock targetTp;
            Ref<ChunkStore> blockRef = BlockHelper.getBlockRef(world, target);
            if (blockRef == null || (targetTp = (TeleporterBlock)store.getComponent(blockRef, TeleporterBlock.getComponentType())) == null || targetTp.id == null || !targetTp.id.equals(id)) continue;
            return target;
        }
        return null;
    }

    void teleportPlayer(PlayerRef player, Vector3i pos, World world) {
        this.teleportPlayer(player, new Vector3d(pos), world);
    }

    void teleportPlayer(PlayerRef player, Vector3d pos, World world) {
        Transform transform = player.getTransform();
        Vector3d target_pos = new Vector3d(pos);
        transform.setPosition(target_pos);
        Teleport tp = Teleport.createExact(target_pos, transform.getRotation(), player.getHeadRotation()).withoutVelocityReset();
        world.getEntityStore().getStore().addComponent(player.getReference(), Teleport.getComponentType(), tp);
    }

    public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        DungeonBlocks db = (DungeonBlocks)store.getResource(DungeonBlocks.getResourceType());
        SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
        TeleporterBlock sender = (TeleporterBlock)commandBuffer.getComponent(ref, TeleporterBlock.getComponentType());
        World world = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        assert (rec != null);
        assert (sender != null);
        if (rec.getId() == null) {
            rec.setId(UUID.randomUUID());
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
            if (!active) {
                return;
            }
            Vector3i ownPos = BlockHelper.getPosForBlock(ref, (ComponentAccessor<ChunkStore>)store);
            if (ownPos == null) {
                return;
            }
            PlayerRef nearestPlayer = this.getNearestPlayer(world, ownPos);
            if (nearestPlayer == null) {
                return;
            }
            if (sender.tpMode == TeleporterBlock.TeleporterMode.SEND) {
                if (sender.connected.length < 1) {
                    return;
                }
                UUID id = null;
                if (sender.sendMode == TeleporterBlock.SendMode.Random) {
                    id = (UUID)RandomUtil.selectRandom((Object[])sender.connected, (Random)RandomUtil.getSecureRandom());
                } else if (sender.sendMode == TeleporterBlock.SendMode.RoundRobin) {
                    int idx = sender.nextIdx;
                    id = sender.connected[idx];
                    if (++idx >= sender.connected.length) {
                        idx = 0;
                    }
                    sender.nextIdx = idx;
                }
                assert (id != null);
                Vector3i foundTarget = this.findTargetRecv(id, db, world, store);
                if (foundTarget == null) {
                    return;
                }
                Vector3i target = new Vector3i(foundTarget);
                if (sender.relative) {
                    target.sub(ownPos);
                    Transform transform = nearestPlayer.getTransform();
                    Vector3d new_target = new Vector3d(transform.getPosition()).add(new Vector3d(target));
                    this.teleportPlayer(nearestPlayer, new_target, world);
                } else {
                    this.teleportPlayer(nearestPlayer, new Vector3i(target).add(0, 1, 0), world);
                }
            } else if (sender.tpMode == TeleporterBlock.TeleporterMode.RECV) {
                PlayerRef[] refs = world.getPlayerRefs().toArray(PlayerRef[]::new);
                DungeonPlugin.logger.info("there are " + refs.length + " players in this world!");
                switch (sender.recvMode) {
                    case Nearest: {
                        this.teleportPlayer(nearestPlayer, new Vector3i(ownPos).add(0, 1, 0), world);
                        break;
                    }
                    case Random: {
                        PlayerRef pl = (PlayerRef)RandomUtil.selectRandom((Object[])refs, (Random)RandomUtil.getSecureRandom());
                        DungeonPlugin.logger.info("chose " + pl.getUsername() + " to teleport at random!");
                        this.teleportPlayer(pl, new Vector3i(ownPos).add(0, 1, 0), world);
                        break;
                    }
                    case All: {
                        for (PlayerRef pl : refs) {
                            this.teleportPlayer(pl, new Vector3i(ownPos).add(0, 1, 0), world);
                        }
                        break;
                    }
                }
            }
        }));
    }

    public void onEntityRemove(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
        assert (rec != null);
        if (rec.getId() == null) {
            return;
        }
        router.unregisterListener(rec.getId());
    }
}
