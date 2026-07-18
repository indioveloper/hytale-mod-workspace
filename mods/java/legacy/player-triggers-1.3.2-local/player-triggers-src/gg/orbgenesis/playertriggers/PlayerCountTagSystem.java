package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PlayerCountTagSystem extends RefSystem<EntityStore> {
  public static final String PLAYERS_ONLINE_TAG = "players_online";

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
    commands.run(liveStore -> updatePlayerCountTags(liveStore, null));
  }

  @Override
  public void onEntityRemove(
      Ref<EntityStore> ref,
      RemoveReason reason,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commands) {
    commands.run(liveStore -> updatePlayerCountTags(liveStore, ref));
  }

  private void updatePlayerCountTags(
      Store<EntityStore> store, Ref<EntityStore> removedRef) {
    List<Ref<EntityStore>> players = getCurrentPlayers(store, removedRef);
    String count = Integer.toString(players.size());
    for (Ref<EntityStore> player : players) {
      PlayerTagsComponent tags =
          store.ensureAndGetComponent(
              player, PlayerTriggersPlugin.get().getPlayerTagsComponentType());
      tags.getTags().put(PLAYERS_ONLINE_TAG, count);
      updateHud(store, player, tags, count);
      TagValueHud.refreshIfWatching(store, player, tags);
    }
  }

  private void updateHud(
      Store<EntityStore> store,
      Ref<EntityStore> playerRef,
      PlayerTagsComponent tags,
      String count) {
    Player player = store.getComponent(playerRef, Player.getComponentType());
    PlayerRef networkPlayer = store.getComponent(playerRef, PlayerRef.getComponentType());
    if (player == null || networkPlayer == null) {
      return;
    }

    if (!tags.isPlayerCountHudEnabled()) {
      PlayerCountHud.remove(player, networkPlayer);
      return;
    }

    PlayerCountHud.getOrCreate(player, networkPlayer).updateCount(count);
  }

  private List<Ref<EntityStore>> getCurrentPlayers(
      Store<EntityStore> store, Ref<EntityStore> removedRef) {
    List<Ref<EntityStore>> players = new ArrayList<>();
    store.forEachChunk(
        QUERY,
        (BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>>)
            (chunk, commands) -> collectPlayers(chunk, removedRef, players));
    return players;
  }

  private void collectPlayers(
      ArchetypeChunk<EntityStore> chunk,
      Ref<EntityStore> removedRef,
      List<Ref<EntityStore>> players) {
    for (int index = 0; index < chunk.size(); index++) {
      Ref<EntityStore> ref = chunk.getReferenceTo(index);
      if (ref == null || !ref.isValid() || sameRef(ref, removedRef)) {
        continue;
      }
      players.add(ref);
    }
  }

  private boolean sameRef(Ref<EntityStore> left, Ref<EntityStore> right) {
    return left != null && right != null && left.getIndex() == right.getIndex();
  }
}
