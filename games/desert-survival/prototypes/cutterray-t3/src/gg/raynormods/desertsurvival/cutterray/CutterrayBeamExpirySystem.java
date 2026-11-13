package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.builtin.beam.BeamComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class CutterrayBeamExpirySystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> query =
      Query.and(
          CutterrayBeamExpiryComponent.getComponentType(),
          BeamComponent.getComponentType());

  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  public void tick(
      float deltaSeconds,
      int index,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var expiry = archetypeChunk.getComponent(
        index, CutterrayBeamExpiryComponent.getComponentType());
    if (expiry == null || !expiry.tickAndIsExpired()) {
      return;
    }
    var ref = archetypeChunk.getReferenceTo(index);
    commandBuffer.removeComponent(ref, BeamComponent.getComponentType());
    commandBuffer.removeComponent(ref, CutterrayBeamExpiryComponent.getComponentType());
  }
}
