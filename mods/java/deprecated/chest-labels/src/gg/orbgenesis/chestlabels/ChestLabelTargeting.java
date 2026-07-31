package gg.orbgenesis.chestlabels;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3i;

final class ChestLabelTargeting {
  private static final int DISTANCE_MAX = 10;

  private ChestLabelTargeting() {}

  static Ref<ChunkStore> getTargetContainerRef(
      Ref<EntityStore> playerRef, World world, ComponentAccessor<EntityStore> accessor) {
    Vector3i block = getTargetBlock(playerRef, accessor);
    if (block == null) {
      return null;
    }

    return getContainerRefAt(world, block.x, block.y, block.z);
  }

  static Vector3i getTargetBlock(Ref<EntityStore> playerRef, ComponentAccessor<EntityStore> accessor) {
    Vector3i block = TargetUtil.getTargetBlock(playerRef, DISTANCE_MAX, accessor);
    return block == null ? null : new Vector3i(block);
  }

  static Ref<ChunkStore> getContainerRefAt(World world, int blockX, int blockY, int blockZ) {
    Vector3i block = new Vector3i(blockX, blockY, blockZ);
    long chunkIndex = ChunkUtil.indexChunkFromBlock(block.x, block.z);
    Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
    if (chunkRef == null || !chunkRef.isValid()) {
      return null;
    }

    var chunkStore = world.getChunkStore().getStore();
    BlockChunk blockChunk = chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());
    BlockComponentChunk componentChunk =
        chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
    if (blockChunk == null || componentChunk == null) {
      return null;
    }

    var section = blockChunk.getSectionAtBlockY(block.y);
    int filler = section.getFiller(block.x, block.y, block.z);
    if (filler != FillerBlockUtil.NO_FILLER) {
      block.x -= FillerBlockUtil.unpackX(filler);
      block.y -= FillerBlockUtil.unpackY(filler);
      block.z -= FillerBlockUtil.unpackZ(filler);
    }

    Ref<ChunkStore> blockEntityRef =
        componentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(block.x, block.y, block.z));
    if (blockEntityRef == null || !blockEntityRef.isValid()) {
      return null;
    }

    ItemContainerBlock container =
        blockEntityRef.getStore().getComponent(blockEntityRef, ItemContainerBlock.getComponentType());
    return container == null ? null : blockEntityRef;
  }

  static String toKey(Ref<ChunkStore> containerRef) {
    if (containerRef == null) {
      return "";
    }

    return containerRef.toString();
  }
}
