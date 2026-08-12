/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.codec.codecs.map.Int2ObjectMapCodec
 *  com.hypixel.hytale.codec.store.StoredCodec
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.Archetype
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.ComponentRegistry$Data
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.component.Holder
 *  com.hypixel.hytale.component.NonTicking
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.RefChangeSystem
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.protocol.ToClientPacket
 *  com.hypixel.hytale.server.core.modules.block.BlockModule$BlockStateInfo
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore$LoadPacketDataQuerySystem
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore$UnloadPacketDataQuerySystem
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMap
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMaps
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ReferenceCollection
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package com.lol.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Int2ObjectMapCodec;
import com.hypixel.hytale.codec.store.StoredCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonTicking;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DungeonChunkData
implements Component<ChunkStore> {
    public static final BuilderCodec<DungeonChunkData> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(DungeonChunkData.class, DungeonChunkData::new).append(new KeyedCodec("Components", (Codec)new Int2ObjectMapCodec((Codec)new StoredCodec(ChunkStore.HOLDER_CODEC_KEY), Int2ObjectOpenHashMap::new)), (entityChunk, map) -> {
        entityChunk.entityHolders.clear();
        entityChunk.entityHolders.putAll((Map)map);
    }, entityChunk -> {
        if (entityChunk.entityReferences.isEmpty()) {
            return entityChunk.entityHolders;
        }
        Int2ObjectOpenHashMap map = new Int2ObjectOpenHashMap(entityChunk.entityHolders.size() + entityChunk.entityReferences.size());
        map.putAll(entityChunk.entityHolders);
        for (Int2ReferenceMap.Entry entry : entityChunk.entityReferences.int2ReferenceEntrySet()) {
            Ref reference = (Ref)entry.getValue();
            Store store = reference.getStore();
            if (!store.getArchetype(reference).hasSerializableComponents(store.getRegistry().getData())) continue;
            map.put(entry.getIntKey(), (Object)store.copySerializableEntity(reference));
        }
        return map;
    }).add()).build();
    @Nonnull
    private final Int2ObjectMap<Holder<ChunkStore>> entityHolders;
    @Nonnull
    private final Int2ReferenceMap<Ref<ChunkStore>> entityReferences;
    @Nonnull
    private final Int2ObjectMap<Holder<ChunkStore>> entityHoldersUnmodifiable;
    @Nonnull
    private final Int2ReferenceMap<Ref<ChunkStore>> entityReferencesUnmodifiable;
    private boolean needsSaving;
    static ComponentType<ChunkStore, DungeonChunkData> componentType;

    public DungeonChunkData() {
        this.entityHolders = new Int2ObjectOpenHashMap();
        this.entityReferences = new Int2ReferenceOpenHashMap();
        this.entityHoldersUnmodifiable = Int2ObjectMaps.unmodifiable(this.entityHolders);
        this.entityReferencesUnmodifiable = Int2ReferenceMaps.unmodifiable(this.entityReferences);
    }

    public DungeonChunkData(@Nonnull Int2ObjectMap<Holder<ChunkStore>> entityHolders, @Nonnull Int2ReferenceMap<Ref<ChunkStore>> entityReferences) {
        this.entityHolders = entityHolders;
        this.entityReferences = entityReferences;
        this.entityHoldersUnmodifiable = Int2ObjectMaps.unmodifiable(entityHolders);
        this.entityReferencesUnmodifiable = Int2ReferenceMaps.unmodifiable(entityReferences);
    }

    @Nonnull
    public Component<ChunkStore> clone() {
        Int2ObjectOpenHashMap entityHoldersClone = new Int2ObjectOpenHashMap(this.entityHolders.size() + this.entityReferences.size());
        for (Int2ObjectMap.Entry entry : this.entityHolders.int2ObjectEntrySet()) {
            entityHoldersClone.put(entry.getIntKey(), (Object)((Holder)entry.getValue()).clone());
        }
        for (Int2ReferenceMap.Entry<Ref<ChunkStore>> entry : this.entityReferences.int2ReferenceEntrySet()) {
            Ref reference = (Ref)entry.getValue();
            entityHoldersClone.put(entry.getIntKey(), (Object)reference.getStore().copyEntity(reference));
        }
        return new DungeonChunkData((Int2ObjectMap<Holder<ChunkStore>>)entityHoldersClone, (Int2ReferenceMap<Ref<ChunkStore>>)new Int2ReferenceOpenHashMap());
    }

    @Nonnull
    public Component<ChunkStore> cloneSerializable() {
        ComponentRegistry.Data data = ChunkStore.REGISTRY.getData();
        Int2ObjectOpenHashMap entityHoldersClone = new Int2ObjectOpenHashMap(this.entityHolders.size() + this.entityReferences.size());
        for (Int2ObjectMap.Entry entry : this.entityHolders.int2ObjectEntrySet()) {
            Holder holder = (Holder)entry.getValue();
            if (!holder.getArchetype().hasSerializableComponents(data)) continue;
            entityHoldersClone.put(entry.getIntKey(), (Object)holder.cloneSerializable(data));
        }
        for (Int2ReferenceMap.Entry entryx : this.entityReferences.int2ReferenceEntrySet()) {
            Ref reference = (Ref)entryx.getValue();
            Store store = reference.getStore();
            if (!store.getArchetype(reference).hasSerializableComponents(data)) continue;
            entityHoldersClone.put(entryx.getIntKey(), (Object)store.copySerializableEntity(reference));
        }
        return new DungeonChunkData((Int2ObjectMap<Holder<ChunkStore>>)entityHoldersClone, (Int2ReferenceMap<Ref<ChunkStore>>)new Int2ReferenceOpenHashMap());
    }

    @Nonnull
    public Int2ObjectMap<Holder<ChunkStore>> getEntityHolders() {
        return this.entityHoldersUnmodifiable;
    }

    @Nullable
    public Holder<ChunkStore> getEntityHolder(int index) {
        return (Holder)this.entityHolders.get(index);
    }

    public void addEntityHolder(int index, @Nonnull Holder<ChunkStore> holder) {
        if (this.entityReferences.containsKey(index)) {
            throw new IllegalArgumentException("Duplicate block components at: " + index);
        }
        if (this.entityHolders.putIfAbsent(index, Objects.requireNonNull(holder)) != null) {
            throw new IllegalArgumentException("Duplicate block components (entity holder) at: " + index);
        }
        this.markNeedsSaving();
    }

    public void storeEntityHolder(int index, @Nonnull Holder<ChunkStore> holder) {
        if (this.entityHolders.putIfAbsent(index, Objects.requireNonNull(holder)) != null) {
            throw new IllegalArgumentException("Duplicate block components (entity holder) at: " + index);
        }
    }

    @Nullable
    public Holder<ChunkStore> removeEntityHolder(int index) {
        Holder reference = (Holder)this.entityHolders.remove(index);
        if (reference != null) {
            this.markNeedsSaving();
        }
        return reference;
    }

    @Nonnull
    public Int2ReferenceMap<Ref<ChunkStore>> getEntityReferences() {
        return this.entityReferencesUnmodifiable;
    }

    @Nullable
    public Ref<ChunkStore> getEntityReference(int index) {
        return (Ref)this.entityReferences.get(index);
    }

    public void addEntityReference(int index, @Nonnull Ref<ChunkStore> reference) {
        reference.validate();
        if (this.entityHolders.containsKey(index)) {
            throw new IllegalArgumentException("Duplicate block components at: " + index);
        }
        if (this.entityReferences.putIfAbsent(index, Objects.requireNonNull(reference)) != null) {
            throw new IllegalArgumentException("Duplicate block components (entity reference) at: " + index);
        }
        this.markNeedsSaving();
    }

    public void loadEntityReference(int index, @Nonnull Ref<ChunkStore> reference) {
        reference.validate();
        if (this.entityHolders.containsKey(index)) {
            throw new IllegalArgumentException("Duplicate block components at: " + index);
        }
        if (this.entityReferences.putIfAbsent(index, Objects.requireNonNull(reference)) != null) {
            throw new IllegalArgumentException("Duplicate block components (entity reference) at: " + index);
        }
    }

    public void removeEntityReference(int index, Ref<ChunkStore> reference) {
        if (this.entityReferences.remove(index, reference)) {
            this.markNeedsSaving();
        }
    }

    public void unloadEntityReference(int index, Ref<ChunkStore> reference) {
        this.entityReferences.remove(index, reference);
    }

    @Nullable
    public Int2ObjectMap<Holder<ChunkStore>> takeEntityHolders() {
        if (this.entityHolders.isEmpty()) {
            return null;
        }
        Int2ObjectOpenHashMap holders = new Int2ObjectOpenHashMap(this.entityHolders);
        this.entityHolders.clear();
        return holders;
    }

    @Nullable
    public Int2ObjectMap<Ref<ChunkStore>> takeEntityReferences() {
        if (this.entityReferences.isEmpty()) {
            return null;
        }
        Int2ObjectOpenHashMap holders = new Int2ObjectOpenHashMap(this.entityReferences);
        this.entityReferences.clear();
        return holders;
    }

    @Nullable
    public <T extends Component<ChunkStore>> T getComponent(int index, @Nonnull ComponentType<ChunkStore, T> componentType) {
        Ref reference = (Ref)this.entityReferences.get(index);
        if (reference != null) {
            return (T)reference.getStore().getComponent(reference, componentType);
        }
        Holder holder = (Holder)this.entityHolders.get(index);
        return (T)(holder != null ? holder.getComponent(componentType) : null);
    }

    public boolean hasComponents(int index) {
        return this.entityReferences.containsKey(index) || this.entityHolders.containsKey(index);
    }

    public boolean getNeedsSaving() {
        return this.needsSaving;
    }

    public void markNeedsSaving() {
        this.needsSaving = true;
    }

    public boolean consumeNeedsSaving() {
        boolean out = this.needsSaving;
        this.needsSaving = false;
        return out;
    }

    public static ComponentType<ChunkStore, DungeonChunkData> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<ChunkStore, DungeonChunkData> c) {
        componentType = c;
    }

    public static class UnloadBlockComponentPacketSystem
    extends ChunkStore.UnloadPacketDataQuerySystem {
        private final ComponentType<ChunkStore, DungeonChunkData> componentType;

        public UnloadBlockComponentPacketSystem(ComponentType<ChunkStore, DungeonChunkData> dungeonChunkDataComponentType) {
            this.componentType = dungeonChunkDataComponentType;
        }

        public Query<ChunkStore> getQuery() {
            return this.componentType;
        }

        public void fetch(int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, CommandBuffer<ChunkStore> commandBuffer, PlayerRef player, @Nonnull List<ToClientPacket> results) {
            DungeonChunkData component = (DungeonChunkData)archetypeChunk.getComponent(index, this.componentType);
            ReferenceCollection references = component.entityReferences.values();
            Store componentStore = ((ChunkStore)store.getExternalData()).getWorld().getChunkStore().getStore();
            componentStore.fetch((Collection)references, ChunkStore.UNLOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE, (Object)player, results);
        }
    }

    public static class LoadBlockComponentPacketSystem
    extends ChunkStore.LoadPacketDataQuerySystem {
        private final ComponentType<ChunkStore, DungeonChunkData> componentType;

        public LoadBlockComponentPacketSystem(ComponentType<ChunkStore, DungeonChunkData> dungeonChunkDataComponentType) {
            this.componentType = dungeonChunkDataComponentType;
        }

        public Query<ChunkStore> getQuery() {
            return this.componentType;
        }

        public void fetch(int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, CommandBuffer<ChunkStore> commandBuffer, PlayerRef player, @Nonnull List<ToClientPacket> results) {
            DungeonChunkData component = (DungeonChunkData)archetypeChunk.getComponent(index, this.componentType);
            ReferenceCollection references = component.entityReferences.values();
            Store componentStore = ((ChunkStore)store.getExternalData()).getWorld().getChunkStore().getStore();
            componentStore.fetch((Collection)references, ChunkStore.LOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE, (Object)player, results);
        }
    }

    public static class DungeonChunkDataLoadingSystem
    extends RefChangeSystem<ChunkStore, NonTicking<ChunkStore>> {
        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
        private final Archetype<ChunkStore> archetype = Archetype.of((ComponentType[])new ComponentType[]{WorldChunk.getComponentType(), DungeonChunkData.getComponentType()});

        public Query<ChunkStore> getQuery() {
            return this.archetype;
        }

        @Nonnull
        public ComponentType<ChunkStore, NonTicking<ChunkStore>> componentType() {
            return ChunkStore.REGISTRY.getNonTickingComponentType();
        }

        public void onComponentAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull NonTicking<ChunkStore> component, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            DungeonChunkData dungeonChunkData = (DungeonChunkData)store.getComponent(ref, DungeonChunkData.getComponentType());
            Int2ObjectMap<Ref<ChunkStore>> entityReferences = dungeonChunkData.takeEntityReferences();
            if (entityReferences != null) {
                int size = entityReferences.size();
                int[] indexes = new int[size];
                Ref[] references = new Ref[size];
                int j = 0;
                for (Int2ObjectMap.Entry entry : entityReferences.int2ObjectEntrySet()) {
                    indexes[j] = entry.getIntKey();
                    references[j] = (Ref)entry.getValue();
                    ++j;
                }
                ComponentRegistry.Data data = ChunkStore.REGISTRY.getData();
                for (int i = 0; i < size; ++i) {
                    if (store.getArchetype(references[i]).hasSerializableComponents(data)) {
                        Holder holder = ChunkStore.REGISTRY.newHolder();
                        commandBuffer.removeEntity(references[i], holder, RemoveReason.UNLOAD);
                        dungeonChunkData.storeEntityHolder(indexes[i], (Holder<ChunkStore>)holder);
                        continue;
                    }
                    commandBuffer.removeEntity(references[i], RemoveReason.UNLOAD);
                }
            }
        }

        public void onComponentSet(@Nonnull Ref<ChunkStore> ref, NonTicking<ChunkStore> oldComponent, @Nonnull NonTicking<ChunkStore> newComponent, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        }

        public void onComponentRemoved(@Nonnull Ref<ChunkStore> ref, @Nonnull NonTicking<ChunkStore> component, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            WorldChunk chunk = (WorldChunk)store.getComponent(ref, WorldChunk.getComponentType());
            DungeonChunkData dungeonChunkData = (DungeonChunkData)store.getComponent(ref, DungeonChunkData.getComponentType());
            Int2ObjectMap<Holder<ChunkStore>> entityHolders = dungeonChunkData.takeEntityHolders();
            if (entityHolders != null) {
                int holderCount = entityHolders.size();
                int[] indexes = new int[holderCount];
                Holder[] holders = new Holder[holderCount];
                int j = 0;
                for (Int2ObjectMap.Entry entry : entityHolders.int2ObjectEntrySet()) {
                    indexes[j] = entry.getIntKey();
                    holders[j] = (Holder)entry.getValue();
                    ++j;
                }
                for (int i = holderCount - 1; i >= 0; --i) {
                    Holder holder = holders[i];
                    if (holder.getArchetype().isEmpty()) {
                        LOGGER.at(Level.SEVERE).log("Empty archetype entity holder: %s (#%d)", (Object)holder, i);
                        holders[i] = holders[--holderCount];
                        holders[holderCount] = holder;
                        chunk.markNeedsSaving();
                        continue;
                    }
                    int index = indexes[i];
                    int x = ChunkUtil.xFromIndex(index);
                    int y = ChunkUtil.yFromIndex(index);
                    int z = ChunkUtil.zFromIndex(index);
                    holder.putComponent(BlockModule.BlockStateInfo.getComponentType(), (Component)new BlockModule.BlockStateInfo(index, ref));
                }
                commandBuffer.addEntities(holders, AddReason.LOAD);
            }
        }
    }
}
