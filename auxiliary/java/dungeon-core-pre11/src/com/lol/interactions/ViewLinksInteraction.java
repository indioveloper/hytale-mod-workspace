/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.codec.codecs.EnumCodec
 *  com.hypixel.hytale.common.util.ArrayUtil
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.matrix.Matrix4d
 *  com.hypixel.hytale.math.vector.Vector3d
 *  com.hypixel.hytale.math.vector.Vector3f
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.DebugShape
 *  com.hypixel.hytale.protocol.InteractionType
 *  com.hypixel.hytale.server.core.entity.InteractionContext
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.modules.debug.DebugUtils
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.SignalReceiver;
import com.lol.components.SignalSender;
import com.lol.components.TeleporterBlock;
import com.lol.interactions.LinkItemInteraction;
import com.lol.resources.DungeonBlocks;
import com.lol.resources.SignalRouter;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ViewLinksInteraction
extends SimpleInstantInteraction {
    public static final BuilderCodec<ViewLinksInteraction> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ViewLinksInteraction.class, ViewLinksInteraction::new).append(new KeyedCodec("Mode", (Codec)new EnumCodec(LinkMode.class)), (self, val) -> {
        self.mode = val;
    }, self -> self.mode).add()).build();
    public LinkMode mode = LinkMode.None;

    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        CommandBuffer commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            return;
        }
        Store store = commandBuffer.getStore();
        Player player = (Player)store.getComponent(interactionContext.getEntity(), Player.getComponentType());
        assert (player != null);
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null) {
            return;
        }
        String state = item.getItem().getStateForItem(item.getItemId());
        if (!"Linking".equalsIgnoreCase(state)) {
            return;
        }
        World world = ((EntityStore)store.getExternalData()).getWorld();
        Store chunkStore = world.getChunkStore().getStore();
        DungeonBlocks db = (DungeonBlocks)chunkStore.getResource(DungeonBlocks.getResourceType());
        SignalRouter router = (SignalRouter)chunkStore.getResource(SignalRouter.getResourceType());
        Vector3i source = (Vector3i)item.getFromMetadataOrNull(LinkItemInteraction.PositionMetadata);
        if (source == null) {
            return;
        }
        Ref<ChunkStore> sourceRef = BlockHelper.getBlockRef(world, source);
        if (sourceRef == null) {
            return;
        }
        TeleporterBlock teleporterBlock = (TeleporterBlock)chunkStore.getComponent(sourceRef, TeleporterBlock.getComponentType());
        SignalSender signalSender = (SignalSender)chunkStore.getComponent(sourceRef, SignalSender.getComponentType());
        if (this.mode == LinkMode.None) {
            return;
        }
        for (Vector3i target : db.blocks) {
            TeleporterBlock rec;
            SignalReceiver signalReceiver;
            Ref<ChunkStore> ref = BlockHelper.getBlockRef(world, target);
            if (ref == null || (this.mode == LinkMode.Signal && signalSender != null ? (signalReceiver = (SignalReceiver)chunkStore.getComponent(ref, SignalReceiver.getComponentType())) == null || !router.listensTo(signalReceiver.getId(), signalSender.id) : this.mode == LinkMode.Teleporter && teleporterBlock != null && ((rec = (TeleporterBlock)chunkStore.getComponent(ref, TeleporterBlock.getComponentType())) == null || !ArrayUtil.contains((Object[])teleporterBlock.connected, (Object)rec.id)))) continue;
            int flags = DebugUtils.FLAG_NO_WIREFRAME;
            DebugUtils.add(world, DebugShape.Cube, DebugUtils.makeMatrix(new Vector3d(target).add(0.5, 0.5, 0.5), 1.1), DebugUtils.COLOR_LIME, 0.5f, flags);
        }
    }

    public static enum LinkMode {
        Signal,
        Teleporter,
        None;

    }
}
