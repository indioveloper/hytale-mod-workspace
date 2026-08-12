package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

final class SpawnerBlockHelper {
  private SpawnerBlockHelper() {}

  static Vector3i getPosition(Ref<ChunkStore> ref, ComponentAccessor<ChunkStore> accessor) {
    BlockModule.BlockStateInfo state =
        accessor.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
    if (state == null) {
      return null;
    }
    Vector3i position = new Vector3i();
    return state.fillWorldPos(accessor, position) ? position : null;
  }
}
