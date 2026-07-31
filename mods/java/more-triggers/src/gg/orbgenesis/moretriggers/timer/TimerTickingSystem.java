package gg.orbgenesis.moretriggers.timer;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TimerTickingSystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> query =
      Query.and(Player.getComponentType(), PlayerRef.getComponentType());
  private final TimerManager timerManager;

  public TimerTickingSystem(TimerManager timerManager) {
    this.timerManager = timerManager;
  }

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
    Player player = archetypeChunk.getComponent(index, Player.getComponentType());
    PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
    if (player != null && playerRef != null) {
      timerManager.tick(player, playerRef, System.nanoTime());
    }
  }
}
