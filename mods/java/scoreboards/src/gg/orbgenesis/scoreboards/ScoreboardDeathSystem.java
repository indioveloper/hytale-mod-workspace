package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ScoreboardDeathSystem extends DeathSystems.OnDeathSystem {
  private final ScoreboardTracker tracker;

  public ScoreboardDeathSystem(ScoreboardTracker tracker) {
    this.tracker = tracker;
  }

  @Override
  public Query<EntityStore> getQuery() {
    return ModelComponent.getComponentType();
  }

  public void onComponentAdded(
      Ref<EntityStore> ref,
      DeathComponent death,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commands) {
    tracker.handleDeath(ref, death, store);
  }
}
