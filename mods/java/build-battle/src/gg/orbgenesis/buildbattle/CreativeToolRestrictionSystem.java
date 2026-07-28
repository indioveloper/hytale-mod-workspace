package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickableSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class CreativeToolRestrictionSystem implements TickableSystem<EntityStore> {
  private final CreativeToolRestrictionManager manager;

  public CreativeToolRestrictionSystem(CreativeToolRestrictionManager manager) {
    this.manager = manager;
  }

  @Override
  public void tick(float deltaTime, int systemIndex, Store<EntityStore> store) {
    manager.tick(store);
  }
}
