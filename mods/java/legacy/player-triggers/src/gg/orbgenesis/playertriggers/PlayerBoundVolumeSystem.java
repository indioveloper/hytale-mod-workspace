package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;

public class PlayerBoundVolumeSystem extends TickingSystem<EntityStore> {
  private static final double POSITION_EPSILON_SQUARED = 0.000001D;
  private static final float VIEWER_UPDATE_INTERVAL = 0.1F;

  private final Map<TriggerVolumeManager, Float> viewerUpdateTimers =
      new ConcurrentHashMap<>();

  @Override
  public void tick(float deltaTime, int index, Store<EntityStore> store) {
    TriggerVolumeManager manager =
        store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (manager == null) {
      return;
    }

    boolean moved = false;
    EntityStore entityStore = store.getExternalData();
    for (VolumeEntry volume : manager.getVolumes()) {
      java.util.UUID playerId = PlayerBinding.getPlayerId(volume);
      if (playerId == null) {
        continue;
      }

      Ref<EntityStore> playerRef = entityStore.getRefFromUUID(playerId);
      if (playerRef == null
          || store.getComponent(playerRef, PlayerRef.getComponentType()) == null) {
        continue;
      }

      TransformComponent transform =
          store.getComponent(playerRef, TransformComponent.getComponentType());
      if (transform == null) {
        continue;
      }

      Vector3d nextPosition =
          new Vector3d(transform.getPosition()).add(PlayerBinding.getOffset(volume));
      if (volume.getPosition().distanceSquared(nextPosition) > POSITION_EPSILON_SQUARED) {
        volume.setPosition(nextPosition);
        moved = true;
      }
    }

    if (!moved) {
      return;
    }

    manager.markSpatialDirty();
    if (manager.getViewerCount() > 0) {
      float elapsed =
          viewerUpdateTimers.merge(manager, deltaTime, Float::sum);
      if (elapsed >= VIEWER_UPDATE_INTERVAL) {
        manager.notifyViewers();
        viewerUpdateTimers.put(manager, 0.0F);
      }
    }
  }
}
