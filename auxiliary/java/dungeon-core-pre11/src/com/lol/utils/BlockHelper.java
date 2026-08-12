/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.modules.block.BlockModule$BlockStateInfo
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk
 *  com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  org.jline.utils.Log
 */
package com.lol.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jline.utils.Log;

public class BlockHelper {
    public static Ref<ChunkStore> getBlockRef(World w, Vector3i pos) {
        if (pos == null) {
            return null;
        }
        WorldChunk chunk = w.getChunk(ChunkUtil.indexChunkFromBlock((int)pos.x, (int)pos.z));
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockComponentEntity(pos.x, pos.y, pos.z);
    }

    public static Vector3i getPosForBlock(Ref<ChunkStore> ref, ComponentAccessor<ChunkStore> store) {
        BlockModule.BlockStateInfo blockstate = (BlockModule.BlockStateInfo)store.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
        if (blockstate == null) {
            return null;
        }
        Vector3i position = new Vector3i();
        return blockstate.fillWorldPos(store, position) ? position : null;
    }

    public static String getStateForBlock(Vector3i targetBlock, World world) {
        if (targetBlock == null) {
            return "default";
        }
        WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock((int)targetBlock.x, (int)targetBlock.z));
        if (chunk == null) {
            return "default";
        }
        BlockType current = chunk.getBlockType(targetBlock);
        if (current == null) {
            return "default";
        }
        String ret = current.getStateForBlock(current);
        return ret == null ? "default" : ret;
    }

    public static boolean setStateForBlock(String newState, World world, Vector3i targetBlock) {
        if (targetBlock == null) {
            return false;
        }
        if (BlockHelper.getStateForBlock(targetBlock, world).equalsIgnoreCase(newState)) {
            return false;
        }
        WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock((int)targetBlock.x, (int)targetBlock.z));
        if (chunk == null) {
            return false;
        }
        BlockType current = chunk.getBlockType(targetBlock);
        if (current == null) {
            return false;
        }
        String newBlock = current.getBlockKeyForState(newState);
        if (newBlock == null) {
            return false;
        }
        int newBlockId = BlockType.getAssetMap().getIndex(newBlock);
        if (newBlockId == Integer.MIN_VALUE) {
            return false;
        }
        BlockType newBlockType = (BlockType)BlockType.getAssetMap().getAsset(newBlockId);
        if (newBlockType == null) {
            return false;
        }
        int rotation = world.getBlockRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
        int settings = 198;
        Log.info((Object[])new Object[]{"setState: " + newState});
        chunk.setBlock(targetBlock.x, targetBlock.y, targetBlock.z, newBlockId, newBlockType, rotation, 0, settings);
        return true;
    }
}
