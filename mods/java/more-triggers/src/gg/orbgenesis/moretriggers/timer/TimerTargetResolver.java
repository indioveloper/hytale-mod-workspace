package gg.orbgenesis.moretriggers.timer;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeEntityQuery;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialData;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3d;

final class TimerTargetResolver {
  private TimerTargetResolver() {}

  static List<PlayerRef> resolve(TriggerContext context, TimerRecipient recipient) {
    if (context == null || context.getStore() == null) {
      return List.of();
    }
    TimerRecipient effective = recipient == null ? TimerRecipient.TRIGGERING_PLAYER : recipient;
    Store<EntityStore> store = context.getStore();
    return switch (effective) {
      case TRIGGERING_PLAYER -> {
        PlayerRef player = playerRefOf(store, context.getEntityRef());
        yield player == null ? List.of() : List.of(player);
      }
      case NEAREST_PLAYER -> nearestPlayer(store, context.getEventPosition());
      case PLAYERS_IN_VOLUME -> playersInVolumes(store, context);
      case ALL_PLAYERS -> allPlayers(store);
    };
  }

  private static List<PlayerRef> nearestPlayer(Store<EntityStore> store, Vector3d origin) {
    SpatialResource<Ref<EntityStore>, EntityStore> spatial =
        store.getResource(EntityModule.get().getPlayerSpatialResourceType());
    if (spatial == null || origin == null) {
      return List.of();
    }
    SpatialData<Ref<EntityStore>> data = spatial.getSpatialData();
    PlayerRef nearest = null;
    double nearestDistance = Double.MAX_VALUE;
    for (int i = 0; i < data.size(); i++) {
      double distance = data.getVector(i).distanceSquared(origin);
      if (distance < nearestDistance) {
        PlayerRef candidate = playerRefOf(store, data.getData(i));
        if (candidate != null) {
          nearest = candidate;
          nearestDistance = distance;
        }
      }
    }
    return nearest == null ? List.of() : List.of(nearest);
  }

  private static List<PlayerRef> playersInVolumes(Store<EntityStore> store, TriggerContext context) {
    List<com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry> volumes =
        context.getSpatialVolumes();
    if (volumes == null || volumes.isEmpty()) {
      volumes = context.getVolume() == null ? List.of() : List.of(context.getVolume());
    }
    return uniquePlayers(
        store, TriggerVolumeEntityQuery.collectTargets(store, volumes, false, true, ""));
  }

  private static List<PlayerRef> allPlayers(Store<EntityStore> store) {
    SpatialResource<Ref<EntityStore>, EntityStore> spatial =
        store.getResource(EntityModule.get().getPlayerSpatialResourceType());
    if (spatial == null) {
      return List.of();
    }
    SpatialData<Ref<EntityStore>> data = spatial.getSpatialData();
    List<Ref<EntityStore>> refs = new ArrayList<>(data.size());
    for (int i = 0; i < data.size(); i++) {
      refs.add(data.getData(i));
    }
    return uniquePlayers(store, refs);
  }

  private static List<PlayerRef> uniquePlayers(
      Store<EntityStore> store, List<Ref<EntityStore>> refs) {
    Map<java.util.UUID, PlayerRef> players = new LinkedHashMap<>();
    for (Ref<EntityStore> ref : refs) {
      PlayerRef player = playerRefOf(store, ref);
      if (player != null) {
        players.put(player.getUuid(), player);
      }
    }
    return List.copyOf(players.values());
  }

  private static PlayerRef playerRefOf(Store<EntityStore> store, Ref<EntityStore> ref) {
    if (ref == null || !ref.isValid()) {
      return null;
    }
    return store.getComponent(ref, PlayerRef.getComponentType());
  }
}
