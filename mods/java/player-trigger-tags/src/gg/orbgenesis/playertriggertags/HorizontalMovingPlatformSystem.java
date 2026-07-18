package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

public class HorizontalMovingPlatformSystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> query =
      Query.and(
          HorizontalMovingPlatformComponent.getComponentType(),
          TransformComponent.getComponentType());

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
    HorizontalMovingPlatformComponent platform =
        archetypeChunk.getComponent(index, HorizontalMovingPlatformComponent.getComponentType());
    TransformComponent transform =
        archetypeChunk.getComponent(index, TransformComponent.getComponentType());

    Vector3d current = transform.getPosition();
    platform.initializeMissingBases(current);
    transform.setPosition(platform.advanceAndGetCurrentPosition(dt));
  }
}
