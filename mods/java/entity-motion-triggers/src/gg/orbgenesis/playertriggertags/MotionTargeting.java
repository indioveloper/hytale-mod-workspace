package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Selects any transformable world entity, excluding the player that activated the volume. */
final class MotionTargeting {
  private MotionTargeting() {}

  static boolean matches(
      com.hypixel.hytale.component.Store<EntityStore> store,
      Ref<EntityStore> candidate,
      org.joml.Vector3d origin,
      com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape shape) {
    TransformComponent transform =
        store.getComponent(candidate, TransformComponent.getComponentType());
    return transform != null
        && shape.contains(origin, transform.getPosition())
        && store.getComponent(candidate, Player.getComponentType()) == null;
  }
}
