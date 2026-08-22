package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import com.hypixel.hytale.server.npc.systems.RoleSystems;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

/** Keeps custom nameplates visible and announces elite mobs when a player approaches them. */
final class SpawnerElitePresentationSystem extends EntityTickingSystem<EntityStore> {
  private static final double TITLE_RADIUS_SQUARED = 100.0;
  private static final double CHECK_INTERVAL_SECONDS = 0.25;
  private static final long PLAYER_TITLE_COOLDOWN_NANOS = 6_000_000_000L;
  private static final Map<UUID, Long> PLAYER_TITLE_COOLDOWNS = new HashMap<>();

  private final Query<EntityStore> query = Query.and(
      SpawnedBySpawnerComponent.getComponentType(),
      NPCEntity.getComponentType(),
      TransformComponent.getComponentType());

  @Override
  @Nonnull
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  @Nonnull
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(new SystemDependency<>(
        Order.AFTER, RoleSystems.PostBehaviourSupportTickSystem.class));
  }

  @Override
  public boolean isParallel(int archetypeChunkSize, int taskCount) {
    return false;
  }

  @Override
  public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    SpawnedBySpawnerComponent tracker = archetypeChunk.getComponent(
        index, SpawnedBySpawnerComponent.getComponentType());
    TransformComponent transform = archetypeChunk.getComponent(
        index, TransformComponent.getComponentType());
    if (tracker == null || transform == null) return;

    tracker.presentationCheckSeconds -= dt;
    if (tracker.presentationCheckSeconds > 0.0) return;
    tracker.presentationCheckSeconds = CHECK_INTERVAL_SECONDS;

    var ref = archetypeChunk.getReferenceTo(index);
    if (!tracker.mobName.isBlank()) {
      Nameplate nameplate = commandBuffer.getComponent(ref, Nameplate.getComponentType());
      if (nameplate == null || !tracker.mobName.equals(nameplate.getText())) {
        DisplayNameSupport.setDisplayName(ref, tracker.mobName, true, commandBuffer);
      }
    }
    if (!tracker.elite) return;

    long now = System.nanoTime();
    PLAYER_TITLE_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= now);
    Set<UUID> currentlyNearby = new HashSet<>();
    for (var player : store.getExternalData().getWorld().getPlayerRefs()) {
      if (player.getTransform().getPosition().distanceSquared(transform.getPosition())
          > TITLE_RADIUS_SQUARED) {
        continue;
      }
      UUID playerId = player.getUuid();
      currentlyNearby.add(playerId);
      if (tracker.titleViewers.contains(playerId)
          || PLAYER_TITLE_COOLDOWNS.containsKey(playerId)) {
        continue;
      }
      EventTitleUtil.showEventTitleToPlayer(
          player,
          Message.raw(tracker.mobName),
          Message.translation("server.configurableSpawners.elite.nearby"),
          false,
          null,
          2.8f,
          0.2f,
          0.45f);
      PLAYER_TITLE_COOLDOWNS.put(playerId, now + PLAYER_TITLE_COOLDOWN_NANOS);
    }
    tracker.titleViewers.retainAll(currentlyNearby);
    tracker.titleViewers.addAll(currentlyNearby);
  }
}
