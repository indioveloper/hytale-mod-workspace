/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Component
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
 *  com.hypixel.hytale.server.core.entity.UUIDComponent
 *  com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier
 *  com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier$ModifierTarget
 *  com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier
 *  com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier$CalculationType
 *  com.hypixel.hytale.server.core.modules.physics.component.Velocity
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.hypixel.hytale.server.npc.entities.NPCEntity
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 *  org.jline.utils.Log
 */
package com.lol.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
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
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lol.DungeonPlugin;
import com.lol.components.EntityCountRef;
import com.lol.components.MobSpawner;
import com.lol.components.SignalReceiver;
import com.lol.components.SignalSender;
import com.lol.resources.EntityCountResource;
import com.lol.resources.SignalRouter;
import com.lol.resources.TimeoutAction;
import com.lol.utils.BlockHelper;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jline.utils.Log;

public class MobSpawnerSystem
extends RefSystem<ChunkStore> {
    public Holder<EntityStore> makeEntity(String role, UUID id) {
        Holder holder = EntityStore.REGISTRY.newHolder();
        NPCEntity npc = new NPCEntity();
        npc.setRoleName(role);
        HeadRotation hr = new HeadRotation(Rotation3f.IDENTITY);
        holder.addComponent(NPCEntity.getComponentType(), (Component)npc);
        holder.addComponent(HeadRotation.getComponentType(), (Component)hr);
        holder.addComponent(TransformComponent.getComponentType(), (Component)new TransformComponent(new Vector3d(), Rotation3f.IDENTITY));
        holder.ensureComponent(Velocity.getComponentType());
        holder.ensureComponent(UUIDComponent.getComponentType());
        holder.addComponent(EntityCountRef.getComponentType(), (Component)new EntityCountRef(id));
        return holder;
    }

    public Ref<EntityStore> spawn(Vector3d position, World world, Holder<EntityStore> entity) {
        Rotation3f rotation = new Rotation3f();
        float randomRot = (float)(Math.random() * 2.0) - 1.0f;
        rotation.setYaw(randomRot);
        TransformComponent transformComponent = (TransformComponent)entity.ensureAndGetComponent(TransformComponent.getComponentType());
        transformComponent.setPosition(position);
        transformComponent.setRotation(rotation);
        return world.getEntityStore().getStore().addEntity(entity, AddReason.SPAWN);
    }

    public void handle(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer, UUID id) {
        Vector3i pos = BlockHelper.getPosForBlock(ref, commandBuffer);
        if (pos == null) {
            return;
        }
        ++pos.y;
        World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        MobSpawner ms = (MobSpawner)commandBuffer.getComponent(ref, MobSpawner.getComponentType());
        if (ms == null) {
            return;
        }
        for (int j = 0; j < ms.count; ++j) {
            double xOff = MathUtil.randomDouble((double)(-ms.range), (double)ms.range);
            double zOff = MathUtil.randomDouble((double)(-ms.range), (double)ms.range);
            Vector3d spawnPos = new Vector3d((double)pos.x + xOff, (double)pos.y, (double)pos.z + zOff);
            w.execute(() -> {
                Ref<EntityStore> mob = this.spawn(spawnPos, w, this.makeEntity(ms.role, id));
                EntityStatMap stats = (EntityStatMap)w.getEntityStore().getStore().getComponent(mob, EntityStatMap.getComponentType());
                if (stats == null) {
                    return;
                }
                Log.info((Object[])new Object[]{"multiplier.."});
                float mult = (float)ms.health / 100.0f;
                StaticModifier modifier = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.MULTIPLICATIVE, mult);
                stats.putModifier(DefaultEntityStatTypes.getHealth(), "Lol.HpModifier", (Modifier)modifier);
                stats.maximizeStatValue(DefaultEntityStatTypes.getHealth());
            });
        }
    }

    public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        TimeoutAction timer = (TimeoutAction)store.getResource(TimeoutAction.getResourceType());
        SignalReceiver rec = (SignalReceiver)commandBuffer.getComponent(ref, SignalReceiver.getComponentType());
        MobSpawner ms = (MobSpawner)commandBuffer.getComponent(ref, MobSpawner.getComponentType());
        SignalSender sender = (SignalSender)commandBuffer.ensureAndGetComponent(ref, SignalSender.getComponentType());
        World w = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
        EntityCountResource entityCounter = (EntityCountResource)w.getEntityStore().getStore().getResource(EntityCountResource.getResourceType());
        assert (rec != null);
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
            DungeonPlugin.logger.info("prev: " + prev + " | current: " + active);
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
        return Query.and((Query[])new Query[]{SignalReceiver.getComponentType(), MobSpawner.getComponentType()});
    }
}
