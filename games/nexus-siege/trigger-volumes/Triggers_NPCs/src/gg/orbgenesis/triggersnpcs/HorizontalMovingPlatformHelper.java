package gg.orbgenesis.triggersnpcs;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class HorizontalMovingPlatformHelper {
  private HorizontalMovingPlatformHelper() {}

  public static boolean attach(
      Store<EntityStore> store,
      Ref<EntityStore> entityRef,
      double amplitude,
      double speed,
      double phase) {
    if (entityRef == null || !entityRef.isValid()) {
      return false;
    }

    TransformComponent transform =
        store.getComponent(entityRef, TransformComponent.getComponentType());
    if (transform == null) {
      return false;
    }

    HorizontalMovingPlatformComponent component =
        new HorizontalMovingPlatformComponent(transform.getPosition().x, amplitude, speed, phase);
    Archetype<EntityStore> archetype = store.getArchetype(entityRef);
    if (archetype.contains(HorizontalMovingPlatformComponent.getComponentType())) {
      store.tryRemoveComponent(entityRef, HorizontalMovingPlatformComponent.getComponentType());
    }
    store.addComponent(
        entityRef, HorizontalMovingPlatformComponent.getComponentType(), component);
    return true;
  }
}
