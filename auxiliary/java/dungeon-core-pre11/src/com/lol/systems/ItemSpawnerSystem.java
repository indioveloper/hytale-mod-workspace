/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Holder
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.RefSystem
 *  com.hypixel.hytale.math.util.MathUtil
 *  com.hypixel.hytale.math.vector.Vector3d
 *  com.hypixel.hytale.math.vector.Vector3f
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.modules.entity.DespawnComponent
 *  com.hypixel.hytale.server.core.modules.entity.item.ItemComponent
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.EntityCountRef;
import com.lol.components.ItemSpawner;
import com.lol.components.SignalReceiver;
import com.lol.components.SignalSender;
import com.lol.resources.EntityCountResource;
import com.lol.resources.SignalRouter;
import com.lol.resources.TimeoutAction;
import com.lol.utils.BlockHelper;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ItemSpawnerSystem
extends RefSystem<ChunkStore> {
    public Ref<EntityStore> spawn(Vector3d position, World world, ItemStack item, boolean despawn, UUID id) {
        Rotation3f rotation = new Rotation3f();
        Holder holder = ItemComponent.generateItemDrop(world.getEntityStore().getStore(), item.withQuantity(1), position, rotation, 0.0f, 0.0f, 0.0f);
        if (holder == null) {
            return null;
        }
        if (!despawn) {
            holder.removeComponent(DespawnComponent.getComponentType());
        }
        holder.addComponent(EntityCountRef.getComponentType(), (Component)new EntityCountRef(id));
        return world.getEntityStore().getStore().addEntity(holder, AddReason.SPAWN);
    }

    public void handle(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer, UUID id) {
        Vector3i pos = BlockHelper.getPosForBlock(ref, commandBuffer);
        if (pos == null) {
            return;
        }
        ++pos.y;
        World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        ItemSpawner ms = (ItemSpawner)commandBuffer.getComponent(ref, ItemSpawner.getComponentType());
        if (ms == null) {
            return;
        }
        for (int j = 0; j < ms.count; ++j) {
            double xOff = MathUtil.randomDouble((double)(-ms.range), (double)ms.range);
            double zOff = MathUtil.randomDouble((double)(-ms.range), (double)ms.range);
            Vector3d spawnPos = new Vector3d((double)pos.x + xOff, (double)pos.y, (double)pos.z + zOff);
            w.execute(() -> this.spawn(spawnPos, w, ms.item, ms.despawn, id));
        }
    }

    public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        TimeoutAction timer = (TimeoutAction)store.getResource(TimeoutAction.getResourceType());
        SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
        SignalSender sender = (SignalSender)commandBuffer.getComponent(ref, SignalSender.getComponentType());
        ItemSpawner ms = (ItemSpawner)commandBuffer.getComponent(ref, ItemSpawner.getComponentType());
        World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        EntityCountResource entityCounter = (EntityCountResource)w.getEntityStore().getStore().getResource(EntityCountResource.getResourceType());
        assert (rec != null);
        assert (sender != null);
        assert (ms != null);
        if (rec.getId() == null) {
            rec.setId(UUID.randomUUID());
        }
        if (sender.id == null) {
            sender.id = UUID.randomUUID();
        }
        entityCounter.register(sender.id, count -> {
            if (count > 0) {
                return;
            }
            if (!sender.active) {
                return;
            }
            router.sendSignal(new SignalRouter.LogicSignal(sender.id, true));
            timer.addTask(1L, () -> router.sendSignal(new SignalRouter.LogicSignal(sender.id, false)));
        });
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
            this.handle(ref, store, commandBuffer, sender.id);
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

    @NullableDecl
    public Query<ChunkStore> getQuery() {
        return Query.and((Query[])new Query[]{SignalReceiver.getComponentType(), ItemSpawner.getComponentType(), SignalSender.getComponentType()});
    }
}
