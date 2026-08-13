/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.tick.EntityTickingSystem
 *  com.hypixel.hytale.math.vector.Vector3d
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.components.PlayerDetector;
import com.lol.components.SignalSender;
import com.lol.resources.SignalRouter;
import com.lol.resources.TimeoutAction;
import com.lol.utils.BlockHelper;
import java.util.Collection;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlayerDetecorSystem
extends EntityTickingSystem<ChunkStore> {
    boolean blocked = false;

    public void tick(float v, int i, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        if (this.blocked) {
            return;
        }
        World world = ((ChunkStore)store.getExternalData()).getWorld();
        Collection<PlayerRef> refs = world.getPlayerRefs();
        PlayerDetector pd = (PlayerDetector)archetypeChunk.getComponent(i, PlayerDetector.getComponentType());
        SignalSender signalSender = (SignalSender)archetypeChunk.getComponent(i, SignalSender.getComponentType());
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        assert (signalSender != null);
        assert (pd != null);
        if (!signalSender.active) {
            return;
        }
        Vector3i blockPosI = BlockHelper.getPosForBlock((Ref<ChunkStore>)archetypeChunk.getReferenceTo(i), commandBuffer);
        if (blockPosI == null) {
            return;
        }
        Vector3d blockPos = new Vector3d(blockPosI).add(0.5, 0.5, 0.5);
        world.execute(() -> {
            boolean inRange = false;
            for (PlayerRef ref : refs) {
                Vector3d pos = new Vector3d(ref.getTransform().getPosition());
                double rangesq = pd.range * pd.range;
                if (!(pos.distanceSquared(blockPos) < rangesq)) continue;
                inRange = true;
                break;
            }
            if (pd.mode == PlayerDetector.DetectorMode.Hold) {
                router.sendSignal(new SignalRouter.LogicSignal(signalSender.id, inRange));
            } else if (pd.mode == PlayerDetector.DetectorMode.Repeat) {
                router.sendSignal(new SignalRouter.LogicSignal(signalSender.id, inRange));
                TimeoutAction timeout = (TimeoutAction)commandBuffer.getResource(TimeoutAction.getResourceType());
                this.blocked = true;
                timeout.addTask(5L, () -> {
                    router.sendSignal(new SignalRouter.LogicSignal(signalSender.id, false));
                    this.blocked = false;
                });
            } else if (!pd.active && inRange) {
                router.sendSignal(new SignalRouter.LogicSignal(signalSender.id, true));
                TimeoutAction timeout = (TimeoutAction)commandBuffer.getResource(TimeoutAction.getResourceType());
                timeout.addTask(2L, () -> {
                    router.sendSignal(new SignalRouter.LogicSignal(signalSender.id, false));
                    if (pd.mode == PlayerDetector.DetectorMode.SinglePulse) {
                        signalSender.active = false;
                    }
                });
            }
            pd.active = inRange;
        });
    }

    @NullableDecl
    public Query<ChunkStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerDetector.getComponentType(), SignalSender.getComponentType()});
    }
}
