package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.AndQuery;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerKillCounterHudSystem extends RefSystem<EntityStore> {
  private static final Query<EntityStore> QUERY =
      new AndQuery<>(Player.getComponentType(), PlayerRef.getComponentType());

  @Override
  public Query<EntityStore> getQuery() {
    return QUERY;
  }

  @Override
  public void onEntityAdded(
      Ref<EntityStore> ref,
      AddReason reason,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commands) {
    commands.ensureComponent(
        ref, PlayerTriggersPlugin.get().getPlayerTagsComponentType());
    commands.run(liveStore -> initialize(ref, liveStore));
  }

  @Override
  public void onEntityRemove(
      Ref<EntityStore> ref,
      RemoveReason reason,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commands) {}

  private void initialize(Ref<EntityStore> ref, Store<EntityStore> store) {
    if (ref == null || !ref.isValid()) {
      return;
    }

    Player player = store.getComponent(ref, Player.getComponentType());
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (player == null || playerRef == null) {
      return;
    }

    PlayerTagsComponent tags =
        store.ensureAndGetComponent(
            ref, PlayerTriggersPlugin.get().getPlayerTagsComponentType());
    String count =
        tags.getTags().computeIfAbsent(KillCounterHud.TOTAL_KILLS_TAG, key -> "0");
    KillCounterHud.getOrCreate(player, playerRef).updateCount(count);
  }
}
