package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Removes the separate audio/light anchor shortly after the cutterray is released. */
public final class CutterrayContactAnchorSystem extends EntityTickingSystem<EntityStore> {
  @Override
  public Query<EntityStore> getQuery() {
    return CutterrayContactAnchorComponent.getComponentType();
  }

  @Override
  public void tick(
      float deltaSeconds,
      int index,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var expiry = archetypeChunk.getComponent(
        index, CutterrayContactAnchorComponent.getComponentType());
    if (expiry != null && expiry.tickAndIsExpired()) {
      commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
    }
  }
}
