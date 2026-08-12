/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.InteractionType
 *  com.hypixel.hytale.server.core.entity.InteractionContext
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.inventory.container.ItemContainer
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.hypixel.hytale.server.core.util.TargetUtil
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.lol.DungeonPlugin;
import com.lol.components.SignalReceiver;
import com.lol.components.SignalSender;
import com.lol.resources.SignalRouter;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class LinkItemInteraction
extends SimpleInstantInteraction {
    public static final BuilderCodec<LinkItemInteraction> CODEC = BuilderCodec.builder(LinkItemInteraction.class, LinkItemInteraction::new).build();
    public static KeyedCodec<Vector3i> PositionMetadata = new KeyedCodec<>("Position", Vector3iUtil.CODEC);

    void resetItem(ItemContainer container, short slot) {
        ItemStack item = container.getItemStack(slot);
        if (item == null) {
            return;
        }
        item = item.withState("Normal");
        item = item.withMetadata(PositionMetadata, null);
        container.setItemStackForSlot(slot, item);
    }

    void setPosition(ItemContainer container, short slot, Vector3i pos, SignalSender signalSender, World w) {
        ItemStack item = container.getItemStack(slot);
        if (item == null) {
            return;
        }
        item = item.withState("Linking");
        item = item.withMetadata(PositionMetadata, pos);
        container.setItemStackForSlot(slot, item);
    }

    void addTarget(ItemContainer container, short slot, Vector3i pos, ComponentAccessor<ChunkStore> store, World w) {
        ItemStack item = container.getItemStack(slot);
        if (item == null) {
            return;
        }
        Vector3i source = (Vector3i)item.getFromMetadataOrNull(PositionMetadata);
        if (pos.equals((Object)source)) {
            return;
        }
        Ref<ChunkStore> sourceRef = BlockHelper.getBlockRef(w, source);
        Ref<ChunkStore> blockRef = BlockHelper.getBlockRef(w, pos);
        if (sourceRef == null || blockRef == null) {
            return;
        }
        SignalSender signalSender = (SignalSender)store.getComponent(sourceRef, SignalSender.getComponentType());
        SignalReceiver rec = (SignalReceiver)store.getComponent(blockRef, SignalReceiver.getComponentType());
        SignalRouter router = (SignalRouter)store.getResource(SignalRouter.getResourceType());
        if (signalSender == null || rec == null || router == null) {
            return;
        }
        if (rec.getId() == null) {
            return;
        }
        if (router.listensTo(rec.getId(), signalSender.id)) {
            router.unlisten(rec.getId(), signalSender.id, rec);
        } else {
            router.listen(rec.getId(), signalSender.id, rec);
        }
    }

    void handleNormal(World w, InteractionContext interactionContext, Vector3i blockPos) {
        Store chunkStore = w.getChunkStore().getStore();
        ItemContainer container = interactionContext.getHeldItemContainer();
        short slot = interactionContext.getHeldItemSlot();
        if (container == null) {
            return;
        }
        if (blockPos == null) {
            this.resetItem(container, slot);
            return;
        }
        Ref<ChunkStore> ref = BlockHelper.getBlockRef(w, blockPos);
        if (ref == null) {
            this.resetItem(container, slot);
            return;
        }
        SignalSender signalSender = (SignalSender)chunkStore.getComponent(ref, SignalSender.getComponentType());
        if (signalSender == null) {
            this.resetItem(container, slot);
            return;
        }
        this.setPosition(container, slot, blockPos, signalSender, w);
        DungeonPlugin.logger.info("signal sender " + String.valueOf(signalSender.id) + " at " + String.valueOf(blockPos));
    }

    void handelLinking(World w, InteractionContext interactionContext, Vector3i blockPos) {
        ItemContainer container = interactionContext.getHeldItemContainer();
        short slot = interactionContext.getHeldItemSlot();
        ItemStack item = interactionContext.getHeldItem();
        if (container == null) {
            return;
        }
        if (item == null) {
            return;
        }
        Vector3i source = (Vector3i)item.getFromMetadataOrNull(PositionMetadata);
        if (blockPos == null) {
            this.resetItem(container, slot);
            return;
        }
        Ref<ChunkStore> ref = BlockHelper.getBlockRef(w, blockPos);
        if (ref == null) {
            this.resetItem(container, slot);
            return;
        }
        if (source == null) {
            this.resetItem(container, slot);
            return;
        }
        Ref<ChunkStore> blockRef = BlockHelper.getBlockRef(w, source);
        if (blockRef == null) {
            this.resetItem(container, slot);
            return;
        }
        Store store = w.getChunkStore().getStore();
        this.addTarget(container, slot, blockPos, (ComponentAccessor<ChunkStore>)store, w);
    }

    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        Vector3i blockPos = TargetUtil.getTargetBlock((Ref)interactionContext.getEntity(), (double)10.0, (ComponentAccessor)interactionContext.getCommandBuffer());
        World w = ((EntityStore)interactionContext.getCommandBuffer().getExternalData()).getWorld();
        ItemStack readOnlyItem = interactionContext.getHeldItem();
        if (readOnlyItem == null) {
            return;
        }
        String state = readOnlyItem.getItem().getStateForItem(readOnlyItem.getItemId());
        if (state == null) {
            state = "Normal";
        }
        if (state.equalsIgnoreCase("Normal")) {
            this.handleNormal(w, interactionContext, blockPos);
        } else {
            this.handelLinking(w, interactionContext, blockPos);
        }
    }
}
