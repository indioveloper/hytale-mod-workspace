/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.BlockPosition
 *  com.hypixel.hytale.protocol.InteractionType
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.entity.InteractionChain
 *  com.hypixel.hytale.server.core.entity.InteractionContext
 *  com.hypixel.hytale.server.core.entity.InteractionManager
 *  com.hypixel.hytale.server.core.modules.interaction.InteractionModule
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
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
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.DungeonPlugin;
import com.lol.components.MimicBlockComponent;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MimicInteractInteraction
extends SimpleInstantInteraction {
    public static final BuilderCodec<MimicInteractInteraction> CODEC = BuilderCodec.builder(MimicInteractInteraction.class, MimicInteractInteraction::new).build();

    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        BlockPosition blockPos = interactionContext.getTargetBlock();
        if (blockPos == null) {
            return;
        }
        CommandBuffer commandBuffer = interactionContext.getCommandBuffer();
        assert (commandBuffer != null);
        World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
        world.execute(() -> {
            Ref<ChunkStore> blockRef = BlockHelper.getBlockRef(world, new Vector3i(blockPos.x, blockPos.y, blockPos.z));
            if (blockRef == null) {
                return;
            }
            Store chunkStore = world.getChunkStore().getStore();
            MimicBlockComponent mimic = (MimicBlockComponent)chunkStore.getComponent(blockRef, MimicBlockComponent.getComponentType());
            BlockType bt = mimic != null ? mimic.getBlockType() : world.getBlockType(new Vector3i(blockPos.x, blockPos.y, blockPos.z));
            DungeonPlugin.logger.info("blocktype: " + String.valueOf(bt));
            if (bt == null) {
                return;
            }
            String interactionString = (String)bt.getInteractions().get(InteractionType.Use);
            RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(interactionString);
            if (rootInteraction == null) {
                return;
            }
            Store store = commandBuffer.getStore();
            InteractionManager interactionManager = (InteractionManager)store.getComponent(interactionContext.getEntity(), InteractionModule.get().getInteractionManagerComponent());
            InteractionChain chain = interactionManager.initChain(InteractionType.Use, interactionContext, rootInteraction, true);
            interactionManager.executeChain(interactionContext.getEntity(), commandBuffer, chain);
        });
    }
}
