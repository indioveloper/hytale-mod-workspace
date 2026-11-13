package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickableSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class BuildBattleTickSystem implements TickableSystem<EntityStore> {
  private final BuildBattleManager manager;

  public BuildBattleTickSystem(BuildBattleManager manager) {
    this.manager = manager;
  }

  @Override
  public void tick(float deltaTime, int systemIndex, Store<EntityStore> store) {
    manager.tick(store);
  }
}
