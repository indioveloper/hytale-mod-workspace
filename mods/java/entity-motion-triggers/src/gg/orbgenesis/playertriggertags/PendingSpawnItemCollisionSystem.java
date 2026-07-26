package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Applies collision after the spawned item prop has been sent to clients. */
public class PendingSpawnItemCollisionSystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> query =
      Query.and(PendingSpawnItemCollisionComponent.getComponentType());

  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  public void tick(
      float dt,
      int index,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    PendingSpawnItemCollisionComponent pending =
        archetypeChunk.getComponent(index, PendingSpawnItemCollisionComponent.getComponentType());
    if (!pending.tickAndIsReady()) {
      return;
    }

    var ref = archetypeChunk.getReferenceTo(index);
    HitboxCollisionConfig config =
        HitboxCollisionConfig.getAssetMap().getAsset(pending.getConfigId());
    if (config != null) {
      commandBuffer.putComponent(
          ref, HitboxCollision.getComponentType(), new HitboxCollision(config));
    }
    commandBuffer.removeComponent(
        ref, PendingSpawnItemCollisionComponent.getComponentType());
  }
}
