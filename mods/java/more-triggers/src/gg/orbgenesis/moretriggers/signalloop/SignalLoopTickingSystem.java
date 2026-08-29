package gg.orbgenesis.moretriggers.signalloop;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.system.TriggerVolumeTickingSystem;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;

public final class SignalLoopTickingSystem extends TickingSystem<EntityStore> {
  private final SignalLoopManager signalLoopManager;

  public SignalLoopTickingSystem(SignalLoopManager signalLoopManager) {
    this.signalLoopManager = signalLoopManager;
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(new SystemDependency<>(Order.BEFORE, TriggerVolumeTickingSystem.class));
  }

  @Override
  public void tick(float deltaSeconds, int systemIndex, Store<EntityStore> store) {
    TriggerVolumeManager volumeManager =
        store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (volumeManager != null) {
      signalLoopManager.tick(volumeManager, deltaSeconds);
    }
  }
}
