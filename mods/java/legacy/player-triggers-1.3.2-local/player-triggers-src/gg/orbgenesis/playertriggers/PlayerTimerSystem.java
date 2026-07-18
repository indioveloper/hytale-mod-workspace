package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerTimerSystem extends TickingSystem<EntityStore> {
  private final PlayerTimerService timerService;

  public PlayerTimerSystem(PlayerTimerService timerService) {
    this.timerService = timerService;
  }

  @Override
  public void tick(float deltaTime, int index, Store<EntityStore> store) {
    timerService.tick(store);
  }
}
