/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.AddReason
 *  com.hypixel.hytale.component.Holder
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.RemoveReason
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.command.system.CommandContext
 *  com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
 *  com.hypixel.hytale.server.core.modules.block.BlockModule$BlockStateInfo
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.commands;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.DungeonBlock;
import com.lol.components.DungeonChunkData;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DungeonDebugCommand
extends AbstractPlayerCommand {
    public DungeonDebugCommand() {
        super("ddebug", "dungeon debug command for testing");
    }

    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        World w = ((EntityStore)store.getExternalData()).getWorld();
        Store chunkStore = w.getChunkStore().getStore();
        var playerPosition = playerRef.getTransform().getPosition();
        Vector3i pos = new Vector3i((int)Math.floor(playerPosition.x), (int)Math.floor(playerPosition.y), (int)Math.floor(playerPosition.z)).sub(0, 1, 0);
        Ref chunkRef = w.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock((int)pos.x, (int)pos.z));
        if (chunkRef == null) {
            playerRef.sendMessage(Message.raw((String)"Not in a chunk ???"));
            return;
        }
        DungeonChunkData chunkData = (DungeonChunkData)chunkStore.ensureAndGetComponent(chunkRef, DungeonChunkData.getComponentType());
        Ref<ChunkStore> blockRef = BlockHelper.getBlockRef(w, pos);
        if (blockRef == null) {
            return;
        }
        DungeonBlock db = (DungeonBlock)chunkStore.getComponent(blockRef, DungeonBlock.getComponentType());
        if (db == null) {
            return;
        }
        int idx = ChunkUtil.indexBlock((int)pos.x, (int)pos.y, (int)pos.z);
        Holder blockHolder = chunkStore.copyEntity(blockRef);
        blockHolder.removeComponent(BlockModule.BlockStateInfo.getComponentType());
        Ref newBlockRef = chunkStore.addEntity(blockHolder, AddReason.LOAD);
        if (newBlockRef == null) {
            return;
        }
        world.breakBlock(pos.x, pos.y, pos.z, 198);
        chunkStore.removeEntity(blockRef, RemoveReason.UNLOAD);
        chunkData.addEntityReference(idx, (Ref<ChunkStore>)newBlockRef);
        WorldChunk worldChunk = (WorldChunk)chunkStore.getComponent(chunkRef, WorldChunk.getComponentType());
        assert (worldChunk != null);
        worldChunk.markNeedsSaving();
    }
}
