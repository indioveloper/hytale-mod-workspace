package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.TaskSet;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.Objective;
import com.hypixel.hytale.protocol.ObjectiveTask;
import com.hypixel.hytale.protocol.packets.assets.TrackOrUpdateObjective;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.EntityRemoveEvent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardTracker {
  private final Map<UUID, TrackedScoreboard> activeScoreboards = new ConcurrentHashMap<>();

  public void apply(PlayerRef playerRef, Store<EntityStore> store, ScoreboardDefinition definition) {
    TrackedScoreboard tracked = new TrackedScoreboard(definition);
    activeScoreboards.put(playerRef.getUuid(), tracked);
    registerNativeObjective(playerRef, store, definition);
    playerRef.getPacketHandler().write(new TrackOrUpdateObjective(tracked.toObjective()));
  }

  public void hide(PlayerRef playerRef, Store<EntityStore> store) {
    activeScoreboards.remove(playerRef.getUuid());
    unregisterNativeObjective(playerRef, store, UUID.nameUUIDFromBytes("orbgenesis-scoreboard-ui".getBytes()));
  }

  public void handleDeath(Ref<EntityStore> ref, DeathComponent death, Store<EntityStore> store) {
    if (ref == null || death == null || death.getDeathInfo() == null) {
      return;
    }

    PlayerRef killer = getKiller(store, death.getDeathInfo());
    if (killer == null) {
      return;
    }

    TrackedScoreboard tracked = activeScoreboards.get(killer.getUuid());
    if (tracked == null || tracked.definition.taskCount == 0) {
      return;
    }

    boolean updated = false;
    String modelAssetId = getModelAssetId(store, ref);
    for (int i = 0; i < tracked.definition.taskCount; i++) {
      ScoreboardDefinition.TriggerType trigger = tracked.definition.triggers[i];
      if (trigger == ScoreboardDefinition.TriggerType.SKELETON_KILL && isSkeleton(modelAssetId)) {
        tracked.current[i] += 1;
        updated = true;
        killer.sendMessage(
            Message.raw("Skeleton kill counted: " + tracked.current[i] + "/" + tracked.needed[i]));
      }
    }

    if (updated) {
      killer.getPacketHandler().write(new TrackOrUpdateObjective(tracked.toObjective()));
    }
  }

  public void handleEntityRemove(EntityRemoveEvent event) {
    Entity entity = event.getEntity();
    Ref<EntityStore> removedRef = entity.getReference();
    if (removedRef == null || !removedRef.isValid()) {
      return;
    }

    Store<EntityStore> store = removedRef.getStore();
    handleDeath(removedRef, store.getComponent(removedRef, DeathComponent.getComponentType()), store);
  }

  private PlayerRef getKiller(Store<EntityStore> store, Damage damage) {
    Damage.Source source = damage.getSource();
    if (!(source instanceof Damage.EntitySource entitySource)) {
      return null;
    }

    Ref<EntityStore> sourceRef = entitySource.getRef();
    if (sourceRef == null || !sourceRef.isValid()) {
      return null;
    }

    return store.getComponent(sourceRef, PlayerRef.getComponentType());
  }

  private boolean isSkeleton(Store<EntityStore> store, Ref<EntityStore> ref) {
    return isSkeleton(getModelAssetId(store, ref));
  }

  private boolean isSkeleton(String modelAssetId) {
    return modelAssetId != null && modelAssetId.toLowerCase().contains("skeleton");
  }

  private String getModelAssetId(Store<EntityStore> store, Ref<EntityStore> ref) {
    ModelComponent model = store.getComponent(ref, ModelComponent.getComponentType());
    if (model == null || model.getModel() == null || model.getModel().getModelAssetId() == null) {
      return null;
    }

    return model.getModel().getModelAssetId();
  }

  private void registerNativeObjective(
      PlayerRef playerRef, Store<EntityStore> store, ScoreboardDefinition definition) {
    ObjectivePlugin objectivePlugin = ObjectivePlugin.get();
    if (objectivePlugin == null || objectivePlugin.getObjectiveDataStore() == null) {
      return;
    }

    ObjectiveDataStore dataStore = objectivePlugin.getObjectiveDataStore();
    com.hypixel.hytale.builtin.adventure.objectives.Objective existing =
        dataStore.getObjective(definition.objectiveUuid);
    if (existing != null) {
      existing.cancel();
      dataStore.removeObjective(definition.objectiveUuid);
    }

    String assetId = "Scoreboards_" + cleanAssetId(definition.lineId);
    ObjectiveAsset asset =
        new ObjectiveAsset(
            assetId,
            "Scoreboards",
            new TaskSet[0],
            new ObjectiveCompletionAsset[0],
            definition.title,
            definition.description,
            false);
    try {
      ObjectiveAsset.getAssetMap().getAssetMap().put(assetId, asset);
    } catch (UnsupportedOperationException ignored) {
      // Some asset maps expose a read-only view. The live Objective still keeps the asset instance.
    }

    Set<UUID> players = new HashSet<>();
    players.add(playerRef.getUuid());
    com.hypixel.hytale.builtin.adventure.objectives.Objective nativeObjective =
        new com.hypixel.hytale.builtin.adventure.objectives.Objective(
            asset, definition.objectiveUuid, players, playerRef.getWorldUuid(), null);
    nativeObjective.addActivePlayerUUID(playerRef.getUuid());
    nativeObjective.markDirty();
    dataStore.addObjective(definition.objectiveUuid, nativeObjective);
    addActiveObjectiveToPlayerConfig(playerRef, store, definition.objectiveUuid);
  }

  private void unregisterNativeObjective(PlayerRef playerRef, Store<EntityStore> store, UUID objectiveUuid) {
    ObjectivePlugin objectivePlugin = ObjectivePlugin.get();
    if (objectivePlugin != null && objectivePlugin.getObjectiveDataStore() != null) {
      com.hypixel.hytale.builtin.adventure.objectives.Objective objective =
          objectivePlugin.getObjectiveDataStore().getObjective(objectiveUuid);
      if (objective != null) {
        objective.removeActivePlayerUUID(playerRef.getUuid());
        objective.cancel();
      }
      objectivePlugin.getObjectiveDataStore().removeObjective(objectiveUuid);
    }

    removeActiveObjectiveFromPlayerConfig(playerRef, store, objectiveUuid);
  }

  private void addActiveObjectiveToPlayerConfig(PlayerRef playerRef, Store<EntityStore> store, UUID objectiveUuid) {
    Player player = getPlayer(playerRef, store);
    if (player == null) {
      return;
    }

    Set<UUID> activeObjectiveUUIDs = new HashSet<>(player.getPlayerConfigData().getActiveObjectiveUUIDs());
    activeObjectiveUUIDs.add(objectiveUuid);
    player.getPlayerConfigData().setActiveObjectiveUUIDs(activeObjectiveUUIDs);
  }

  private void removeActiveObjectiveFromPlayerConfig(PlayerRef playerRef, Store<EntityStore> store, UUID objectiveUuid) {
    Player player = getPlayer(playerRef, store);
    if (player == null) {
      return;
    }

    Set<UUID> activeObjectiveUUIDs = new HashSet<>(player.getPlayerConfigData().getActiveObjectiveUUIDs());
    activeObjectiveUUIDs.remove(objectiveUuid);
    player.getPlayerConfigData().setActiveObjectiveUUIDs(activeObjectiveUUIDs);
  }

  private Player getPlayer(PlayerRef playerRef, Store<EntityStore> store) {
    Ref<EntityStore> playerEntityRef = playerRef.getReference();
    if (playerEntityRef == null || !playerEntityRef.isValid()) {
      return null;
    }

    return store.getComponent(playerEntityRef, Player.getComponentType());
  }

  private String cleanAssetId(String value) {
    String cleaned = value == null ? "scoreboard" : value.trim();
    if (cleaned.isEmpty()) {
      return "scoreboard";
    }

    return cleaned.replaceAll("[^A-Za-z0-9_]", "_");
  }

  public static class ScoreboardDefinition {
    public final UUID objectiveUuid;
    public final String title;
    public final String description;
    public final String lineId;
    public final String[] tasks;
    public final int[] current;
    public final int[] needed;
    public final int taskCount;
    public final TriggerType[] triggers;

    public ScoreboardDefinition(
        UUID objectiveUuid,
        String title,
        String description,
        String lineId,
        String[] tasks,
        int[] current,
        int[] needed,
        int taskCount,
        TriggerType[] triggers) {
      this.objectiveUuid = objectiveUuid;
      this.title = title;
      this.description = description;
      this.lineId = lineId;
      this.tasks = Arrays.copyOf(tasks, taskCount);
      this.current = Arrays.copyOf(current, taskCount);
      this.needed = Arrays.copyOf(needed, taskCount);
      this.taskCount = taskCount;
      this.triggers = Arrays.copyOf(triggers, taskCount);
    }

    public enum TriggerType {
      NONE,
      SKELETON_KILL
    }
  }

  private static class TrackedScoreboard {
    private final ScoreboardDefinition definition;
    private final int[] current;
    private final int[] needed;

    private TrackedScoreboard(ScoreboardDefinition definition) {
      this.definition = definition;
      this.current = Arrays.copyOf(definition.current, definition.taskCount);
      this.needed = Arrays.copyOf(definition.needed, definition.taskCount);
    }

    private Objective toObjective() {
      ObjectiveTask[] objectiveTasks = new ObjectiveTask[definition.taskCount];
      for (int i = 0; i < definition.taskCount; i++) {
        objectiveTasks[i] = new ObjectiveTask(formatText(definition.tasks[i]), current[i], needed[i]);
      }

      return new Objective(
          definition.objectiveUuid,
          formatText(definition.title),
          formatText(definition.description),
          definition.lineId,
          objectiveTasks);
    }
  }

  private static FormattedMessage formatText(String text) {
    FormattedMessage message = new FormattedMessage();
    message.rawText = text == null ? "" : text;
    return message;
  }
}
