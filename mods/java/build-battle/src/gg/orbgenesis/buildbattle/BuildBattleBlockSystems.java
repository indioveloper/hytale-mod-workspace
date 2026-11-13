package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class BuildBattleBlockSystems {
  private BuildBattleBlockSystems() {}

  public static final class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private final BuildBattleManager manager;

    public Place(BuildBattleManager manager) {
      super(PlaceBlockEvent.class);
      this.manager = manager;
    }

    @Override
    public Query<EntityStore> getQuery() {
      return PlayerRef.getComponentType();
    }

    @Override
    public void handle(
        int index,
        ArchetypeChunk<EntityStore> chunk,
        Store<EntityStore> store,
        CommandBuffer<EntityStore> commands,
        PlaceBlockEvent event) {
      PlayerRef player = chunk.getComponent(index, PlayerRef.getComponentType());
      if (player != null
          && !manager.canModify(
              player.getUuid(), store.getExternalData().getWorld().getName(), event.getTargetBlock())) {
        event.setCancelled(true);
      }
    }
  }

  public static final class Break extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private final BuildBattleManager manager;

    public Break(BuildBattleManager manager) {
      super(BreakBlockEvent.class);
      this.manager = manager;
    }

    @Override
    public Query<EntityStore> getQuery() {
      return PlayerRef.getComponentType();
    }

    @Override
    public void handle(
        int index,
        ArchetypeChunk<EntityStore> chunk,
        Store<EntityStore> store,
        CommandBuffer<EntityStore> commands,
        BreakBlockEvent event) {
      PlayerRef player = chunk.getComponent(index, PlayerRef.getComponentType());
      if (player != null
          && !manager.canModify(
              player.getUuid(), store.getExternalData().getWorld().getName(), event.getTargetBlock())) {
        event.setCancelled(true);
      }
    }
  }
}
