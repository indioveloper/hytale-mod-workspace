/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
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
 *  com.hypixel.hytale.server.core.modules.debug.DebugUtils
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
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
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.MimicBlockComponent;
import com.lol.resources.DungeonBlocks;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ViewMimicsInteraction
extends SimpleInstantInteraction {
    public static final BuilderCodec<ViewMimicsInteraction> CODEC = BuilderCodec.builder(ViewMimicsInteraction.class, ViewMimicsInteraction::new).build();

    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        CommandBuffer commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            return;
        }
        Store store = commandBuffer.getStore();
        Player player = (Player)store.getComponent(interactionContext.getEntity(), Player.getComponentType());
        assert (player != null);
        World world = ((EntityStore)store.getExternalData()).getWorld();
        Store chunkStore = world.getChunkStore().getStore();
        DungeonBlocks db = (DungeonBlocks)chunkStore.getResource(DungeonBlocks.getResourceType());
        for (Vector3i target : db.blocks) {
            Ref<ChunkStore> ref = BlockHelper.getBlockRef(world, target);
            if (chunkStore.getComponent(ref, MimicBlockComponent.getComponentType()) == null) continue;
            int flags = DebugUtils.FLAG_NO_WIREFRAME;
            DebugUtils.add(world, DebugShape.Cube, DebugUtils.makeMatrix(new Vector3d(target).add(0.5, 0.5, 0.5), 1.1), DebugUtils.COLOR_CYAN, 0.5f, flags);
        }
    }
}
