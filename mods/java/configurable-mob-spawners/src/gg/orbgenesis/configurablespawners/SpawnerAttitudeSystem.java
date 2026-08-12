package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.AttitudeView;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.IAttitudeProvider;
import javax.annotation.Nonnull;

final class SpawnerAttitudeSystem extends StoreSystem<EntityStore> {
  @Override
  public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
    AttitudeView view = store.getResource(Blackboard.getResourceType()).getView(AttitudeView.class, 0);
    view.registerProvider(IAttitudeProvider.OVERRIDE_PRIORITY + 50, (ref, role, target, accessor) -> {
      SpawnedBySpawnerComponent tracker = accessor.getComponent(
          ref, SpawnedBySpawnerComponent.getComponentType());
      if (tracker == null || accessor.getComponent(target, Player.getComponentType()) == null) return null;
      return switch (tracker.aggressionMode) {
        case ROLE_DEFAULT -> null;
        case HOSTILE -> Attitude.HOSTILE;
        case PASSIVE -> Attitude.FRIENDLY;
        case RETALIATE -> {
          UUIDComponent targetUuid = accessor.getComponent(target, UUIDComponent.getComponentType());
          yield targetUuid != null && targetUuid.getUuid().equals(tracker.retaliationTarget)
              ? Attitude.HOSTILE
              : Attitude.NEUTRAL;
        }
      };
    });
  }

  @Override
  public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {}
}
