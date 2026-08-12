/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Component
 *  com.hypixel.hytale.component.Holder
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.BlockPosition
 *  com.hypixel.hytale.protocol.InteractionType
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.entity.InteractionContext
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Holder;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.DungeonBlock;
import com.lol.components.MimicBlockComponent;
import com.lol.components.MimicPlaceMode;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MakeMimicInteraction
extends SimpleInstantInteraction {
    public static final BuilderCodec<MakeMimicInteraction> CODEC = BuilderCodec.builder(MakeMimicInteraction.class, MakeMimicInteraction::new).build();

    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        BlockPosition blockPos = interactionContext.getTargetBlock();
        CommandBuffer commandBuffer = interactionContext.getCommandBuffer();
        assert (commandBuffer != null);
        if (blockPos == null) {
            return;
        }
        World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
        Holder holder = world.getBlockComponentHolder(blockPos.x, blockPos.y, blockPos.z);
        BlockType bt = world.getBlockType(blockPos.x, blockPos.y, blockPos.z);
        if (holder == null) {
            return;
        }
        if (holder.getComponent(DungeonBlock.getComponentType()) == null) {
            return;
        }
        MimicBlockComponent mimic = new MimicBlockComponent();
        mimic.setBlockType(bt);
        if (holder.getComponent(MimicBlockComponent.getComponentType()) == null) {
            holder.addComponent(MimicBlockComponent.getComponentType(), (Component)mimic);
        }
        int settings = 2049;
        world.breakBlock(blockPos.x, blockPos.y, blockPos.z, settings);
        MimicPlaceMode mp = new MimicPlaceMode();
        mp.pos = new Vector3i(blockPos.x, blockPos.y, blockPos.z);
        mp.holder = holder;
        commandBuffer.putComponent(interactionContext.getEntity(), MimicPlaceMode.getComponentType(), (Component)mp);
    }
}

