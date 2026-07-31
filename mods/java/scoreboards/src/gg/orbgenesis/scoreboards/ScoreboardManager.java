package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.components.ObjectiveHistoryComponent;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.TaskSet;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveHistoryData;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.TrackOrUpdateObjective;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ScoreboardManager {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public enum ModifyOperation {
    SET,
    ADD,
    SUBTRACT
  }

  private static final String RUNTIME_ASSET_PACK = "OrbGenesis:ScoreboardsRuntime";

  private final Config<ScoreboardConfig> config;
  private final Map<String, ScoreboardDefinition> definitions = new LinkedHashMap<>();
  private CompletableFuture<Void> assetUpdateTail = CompletableFuture.completedFuture(null);

  public ScoreboardManager(Config<ScoreboardConfig> config) {
    this.config = config;
  }

  public synchronized void initialize() {
    definitions.clear();
    ScoreboardConfig loaded = config.get();
    if (loaded != null && loaded.getDefinitions() != null) {
      for (ScoreboardDefinition definition : loaded.getDefinitions()) {
        if (definition != null) {
          definitions.put(
              ScoreboardIds.cleanDefinitionId(definition.getId()), definition.copy());
        }
      }
    }
    reloadAssets();
  }

  public synchronized List<ScoreboardDefinition> listDefinitions() {
    return definitions.values().stream()
        .map(ScoreboardDefinition::copy)
        .sorted(Comparator.comparing(ScoreboardDefinition::getId))
        .toList();
  }

  public synchronized ScoreboardDefinition getDefinition(String id) {
    ScoreboardDefinition value = definitions.get(ScoreboardIds.cleanDefinitionId(id));
    return value == null ? null : value.copy();
  }

  public synchronized CompletableFuture<Boolean> upsertDefinition(
      ScoreboardDefinition definition) {
    if (definition == null) {
      return CompletableFuture.completedFuture(false);
    }
    ScoreboardDefinition normalized =
        new ScoreboardDefinition(
            definition.getId(),
            definition.getTitle(),
            definition.getDescription(),
            definition.getTasks());
    definitions.put(normalized.getId(), normalized);
    persistDefinitions();
    return queueAssetUpdate(List.of())
        .thenApply(ignored -> true);
  }

  public synchronized boolean deleteDefinition(String id, Store<EntityStore> store) {
    String cleanId = ScoreboardIds.cleanDefinitionId(id);
    if (definitions.remove(cleanId) == null) {
      return false;
    }

    ObjectivePlugin plugin = ObjectivePlugin.get();
    if (plugin != null && plugin.getObjectiveDataStore() != null) {
      List<UUID> toCancel =
          plugin.getObjectiveDataStore().getObjectiveCollection().stream()
              .filter(objective -> ScoreboardIds.assetId(cleanId).equals(objective.getObjectiveId()))
              .map(Objective::getObjectiveUUID)
              .toList();
      for (UUID objectiveUuid : toCancel) {
        plugin.cancelObjective(objectiveUuid, store);
      }
    }

    persistDefinitions();
    queueAssetUpdate(List.of(ScoreboardIds.assetId(cleanId)));
    return true;
  }

  public List<Objective> start(
      String definitionId,
      Collection<PlayerRef> recipients,
      ObjectiveInstanceScope scope,
      String volumeId,
      Store<EntityStore> store) {
    ScoreboardDefinition definition = getDefinition(definitionId);
    ObjectivePlugin plugin = ObjectivePlugin.get();
    if (definition == null
        || plugin == null
        || plugin.getObjectiveDataStore() == null
        || recipients == null
        || recipients.isEmpty()) {
      return List.of();
    }

    List<PlayerRef> validPlayers =
        recipients.stream()
            .filter(player -> player != null && player.isValid() && player.getReference() != null)
            .toList();
    if (validPlayers.isEmpty()) {
      return List.of();
    }

    ObjectiveInstanceScope effectiveScope =
        scope == null ? ObjectiveInstanceScope.INDIVIDUAL : scope;
    List<Objective> started = new ArrayList<>();
    if (effectiveScope == ObjectiveInstanceScope.INDIVIDUAL) {
      for (PlayerRef player : validPlayers) {
        Objective objective =
            startInstance(
                definition,
                List.of(player),
                instanceUuid(
                    definition.getId(),
                    effectiveScope,
                    player.getWorldUuid(),
                    volumeId,
                    player.getUuid()),
                store);
        if (objective != null) {
          started.add(objective);
        }
      }
    } else {
      PlayerRef first = validPlayers.get(0);
      Objective objective =
          startInstance(
              definition,
              validPlayers,
              instanceUuid(
                  definition.getId(),
                  effectiveScope,
                  first.getWorldUuid(),
                  volumeId,
                  null),
              store);
      if (objective != null) {
        started.add(objective);
      }
    }
    return started;
  }

  private Objective startInstance(
      ScoreboardDefinition definition,
      Collection<PlayerRef> recipients,
      UUID objectiveUuid,
      Store<EntityStore> store) {
    ObjectivePlugin plugin = ObjectivePlugin.get();
    ObjectiveDataStore dataStore = plugin.getObjectiveDataStore();
    for (PlayerRef player : recipients) {
      sendDefinitionTranslations(player, definition);
    }
    Objective existing = dataStore.loadObjective(objectiveUuid, store);
    if (existing != null) {
      for (PlayerRef player : recipients) {
        if (!existing.getActivePlayerUUIDs().contains(player.getUuid())) {
          plugin.addPlayerToExistingObjective(store, player.getUuid(), objectiveUuid);
        }
        sendDisplay(player, existing);
      }
      return existing;
    }

    Set<UUID> playerIds = new LinkedHashSet<>();
    for (PlayerRef player : recipients) {
      playerIds.add(player.getUuid());
    }
    PlayerRef first = recipients.iterator().next();
    Objective objective =
        plugin.startObjective(
            ScoreboardIds.assetId(definition.getId()),
            objectiveUuid,
            playerIds,
            first.getWorldUuid(),
            null,
            store);
    if (objective == null) {
      return null;
    }

    applyInitialValues(definition, objective, first.getReference(), store);
    for (PlayerRef player : recipients) {
      sendDisplay(player, objective);
    }
    return objective;
  }

  private void applyInitialValues(
      ScoreboardDefinition definition,
      Objective objective,
      Ref<EntityStore> actor,
      Store<EntityStore> store) {
    ObjectiveTask[] tasks = objective.getCurrentTasks();
    ScoreboardTaskDefinition[] definitions = definition.getTasks();
    if (tasks == null) {
      return;
    }
    for (int i = 0; i < Math.min(tasks.length, definitions.length); i++) {
      if (tasks[i] instanceof ManualCountObjectiveTask manual
          && definitions[i].getInitialValue() > 0) {
        manual.setTaskCompletion(store, actor, definitions[i].getInitialValue(), objective);
      }
    }
  }

  public int modify(
      String definitionId,
      String taskId,
      int value,
      ModifyOperation operation,
      Collection<PlayerRef> recipients,
      ObjectiveInstanceScope scope,
      String volumeId,
      Store<EntityStore> store) {
    int changed = 0;
    for (Objective objective :
        resolveInstances(definitionId, recipients, scope, volumeId, store)) {
      ManualCountObjectiveTask task = findTask(objective, taskId);
      Ref<EntityStore> actor = firstValidRef(recipients);
      if (task == null || actor == null) {
        continue;
      }
      int next =
          switch (operation == null ? ModifyOperation.SET : operation) {
            case SET -> value;
            case ADD -> task.getCurrentValue() + value;
            case SUBTRACT -> task.getCurrentValue() - value;
          };
      task.setTaskCompletion(store, actor, next, objective);
      changed++;
    }
    return changed;
  }

  public int setTracked(
      String definitionId,
      boolean tracked,
      Collection<PlayerRef> recipients,
      ObjectiveInstanceScope scope,
      String volumeId,
      Store<EntityStore> store) {
    ObjectivePlugin plugin = ObjectivePlugin.get();
    if (plugin == null || recipients == null) {
      return 0;
    }
    int changed = 0;
    for (PlayerRef player : recipients) {
      ObjectiveInstanceScope effectiveScope =
          scope == null ? ObjectiveInstanceScope.INDIVIDUAL : scope;
      UUID uuid =
          instanceUuid(
              definitionId,
              effectiveScope,
              player.getWorldUuid(),
              volumeId,
              effectiveScope == ObjectiveInstanceScope.INDIVIDUAL ? player.getUuid() : null);
      Objective objective = plugin.getObjectiveDataStore().loadObjective(uuid, store);
      if (tracked) {
        if (objective == null) {
          continue;
        }
        plugin.addPlayerToExistingObjective(store, player.getUuid(), uuid);
        sendDisplay(player, objective);
      } else {
        if (objective == null) {
          continue;
        }
        plugin.removePlayerFromExistingObjective(store, player.getUuid(), uuid);
      }
      changed++;
    }
    return changed;
  }

  public int finish(
      String definitionId,
      boolean cancel,
      Collection<PlayerRef> recipients,
      ObjectiveInstanceScope scope,
      String volumeId,
      Store<EntityStore> store) {
    ObjectivePlugin plugin = ObjectivePlugin.get();
    if (plugin == null) {
      return 0;
    }
    List<Objective> objectives =
        resolveInstances(definitionId, recipients, scope, volumeId, store);
    for (Objective objective : objectives) {
      if (cancel) {
        plugin.cancelObjective(objective.getObjectiveUUID(), store);
      } else {
        objective.complete(store);
      }
    }
    return objectives.size();
  }

  public boolean isActive(
      String definitionId,
      PlayerRef player,
      ObjectiveInstanceScope scope,
      String volumeId) {
    Objective objective = getInstance(definitionId, player, scope, volumeId);
    return objective != null && objective.getActivePlayerUUIDs().contains(player.getUuid());
  }

  public boolean hasCompleted(
      String definitionId, Ref<EntityStore> playerRef, Store<EntityStore> store) {
    if (playerRef == null || !playerRef.isValid()) {
      return false;
    }
    ObjectiveHistoryComponent history =
        store.getComponent(
            playerRef, ObjectivePlugin.get().getObjectiveHistoryComponentType());
    if (history == null) {
      return false;
    }
    ObjectiveHistoryData data =
        history.getObjectiveHistoryMap().get(ScoreboardIds.assetId(definitionId));
    return data != null && data.getTimesCompleted() > 0;
  }

  public Integer getTaskValue(
      String definitionId,
      String taskId,
      PlayerRef player,
      ObjectiveInstanceScope scope,
      String volumeId) {
    Objective objective = getInstance(definitionId, player, scope, volumeId);
    ManualCountObjectiveTask task = findTask(objective, taskId);
    return task == null ? null : task.getCurrentValue();
  }

  public void resyncPlayer(PlayerRef player) {
    if (player == null || !player.isValid()) {
      return;
    }
    ObjectivePlugin plugin = ObjectivePlugin.get();
    if (plugin == null || plugin.getObjectiveDataStore() == null) {
      return;
    }
    for (Objective objective : plugin.getObjectiveDataStore().getObjectiveCollection()) {
      if (objective.getActivePlayerUUIDs().contains(player.getUuid())
          && objective.getObjectiveId().startsWith("OrbGenesis_Scoreboard_")) {
        sendDisplay(player, objective);
      }
    }
  }

  private List<Objective> resolveInstances(
      String definitionId,
      Collection<PlayerRef> recipients,
      ObjectiveInstanceScope scope,
      String volumeId,
      Store<EntityStore> store) {
    if (recipients == null || recipients.isEmpty()) {
      return List.of();
    }
    ObjectivePlugin plugin = ObjectivePlugin.get();
    if (plugin == null || plugin.getObjectiveDataStore() == null) {
      return List.of();
    }
    Set<UUID> ids = new LinkedHashSet<>();
    ObjectiveInstanceScope effectiveScope =
        scope == null ? ObjectiveInstanceScope.INDIVIDUAL : scope;
    for (PlayerRef player : recipients) {
      if (player == null) {
        continue;
      }
      ids.add(
          instanceUuid(
              definitionId,
              effectiveScope,
              player.getWorldUuid(),
              volumeId,
              effectiveScope == ObjectiveInstanceScope.INDIVIDUAL ? player.getUuid() : null));
    }
    List<Objective> result = new ArrayList<>();
    for (UUID id : ids) {
      Objective objective = plugin.getObjectiveDataStore().loadObjective(id, store);
      if (objective != null) {
        result.add(objective);
      }
    }
    return result;
  }

  private Objective getInstance(
      String definitionId,
      PlayerRef player,
      ObjectiveInstanceScope scope,
      String volumeId) {
    if (player == null || ObjectivePlugin.get() == null) {
      return null;
    }
    UUID uuid =
        instanceUuid(
            definitionId,
            scope == null ? ObjectiveInstanceScope.INDIVIDUAL : scope,
            player.getWorldUuid(),
            volumeId,
            scope == null || scope == ObjectiveInstanceScope.INDIVIDUAL
                ? player.getUuid()
                : null);
    return ObjectivePlugin.get().getObjectiveDataStore().getObjective(uuid);
  }

  private UUID instanceUuid(
      String definitionId,
      ObjectiveInstanceScope scope,
      UUID worldUuid,
      String volumeId,
      UUID playerUuid) {
    ObjectiveInstanceScope effective =
        scope == null ? ObjectiveInstanceScope.INDIVIDUAL : scope;
    String suffix =
        switch (effective) {
          case INDIVIDUAL -> "player:" + playerUuid;
          case WORLD_SHARED -> "world";
          case VOLUME_SHARED -> "volume:" + (volumeId == null ? "unknown" : volumeId);
        };
    return ScoreboardIds.deterministicUuid(
        worldUuid + ":" + ScoreboardIds.cleanDefinitionId(definitionId) + ":" + suffix);
  }

  private ManualCountObjectiveTask findTask(Objective objective, String taskId) {
    if (objective == null || objective.getCurrentTasks() == null) {
      return null;
    }
    String cleanTaskId = ScoreboardIds.cleanTaskId(taskId);
    for (ObjectiveTask task : objective.getCurrentTasks()) {
      if (task instanceof ManualCountObjectiveTask manual
          && cleanTaskId.equals(manual.getAsset().getTaskId())) {
        return manual;
      }
    }
    return null;
  }

  private Ref<EntityStore> firstValidRef(Collection<PlayerRef> players) {
    if (players == null) {
      return null;
    }
    for (PlayerRef player : players) {
      if (player != null && player.getReference() != null && player.getReference().isValid()) {
        return player.getReference();
      }
    }
    return null;
  }

  private synchronized void persistDefinitions() {
    config.get().setDefinitions(
        definitions.values().stream()
            .map(ScoreboardDefinition::copy)
            .toArray(ScoreboardDefinition[]::new));
    config.save();
  }

  private synchronized void reloadAssets() {
    List<ObjectiveAsset> assets =
        definitions.values().stream().map(this::toAsset).toList();
    if (!assets.isEmpty()) {
      ObjectiveAsset.getAssetStore().loadAssets(RUNTIME_ASSET_PACK, assets);
    }
  }

  private synchronized CompletableFuture<Void> queueAssetUpdate(
      Collection<String> assetIdsToRemove) {
    List<String> removals = List.copyOf(assetIdsToRemove);
    List<ObjectiveAsset> assets =
        definitions.values().stream().map(this::toAsset).toList();

    assetUpdateTail =
        assetUpdateTail
            .handle(
                (ignored, error) -> {
                  if (error != null) {
                    LOGGER
                        .at(Level.SEVERE)
                        .log("Previous Objective asset update failed", error);
                  }
                  return null;
                })
            .thenRunAsync(
                () -> {
                  if (!removals.isEmpty()) {
                    ObjectiveAsset.getAssetStore().removeAssets(removals);
                  }
                  if (!assets.isEmpty()) {
                    ObjectiveAsset.getAssetStore().loadAssets(RUNTIME_ASSET_PACK, assets);
                  }
                },
                HytaleServer.SCHEDULED_EXECUTOR);
    return assetUpdateTail;
  }

  private ObjectiveAsset toAsset(ScoreboardDefinition definition) {
    ObjectiveTaskAsset[] taskAssets =
        Arrays.stream(definition.getTasks())
            .map(
                task ->
                    new ManualCountObjectiveTaskAsset(
                        task.getId(), task.getLabel(), task.getGoal()))
            .toArray(ObjectiveTaskAsset[]::new);
    String keyBase = translationKeyBase(definition.getId());
    return new ObjectiveAsset(
        ScoreboardIds.assetId(definition.getId()),
        "OrbGenesisScoreboards",
        new TaskSet[] {new TaskSet(null, taskAssets)},
        new ObjectiveCompletionAsset[0],
        keyBase + ".title",
        keyBase + ".desc",
        false);
  }

  public void refreshActiveObjectives(String definitionId) {
    ObjectivePlugin plugin = ObjectivePlugin.get();
    if (plugin == null || plugin.getObjectiveDataStore() == null) {
      return;
    }
    for (Objective objective : plugin.getObjectiveDataStore().getObjectiveCollection()) {
      if (ScoreboardIds.assetId(definitionId).equals(objective.getObjectiveId())) {
        objective.reloadObjectiveAsset(ObjectiveAsset.getAssetMap().getAssetMap());
        objective.forEachParticipant(
            ref -> {
              PlayerRef player = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
              if (player != null) {
                sendDisplay(player, objective);
              }
            });
      }
    }
  }

  private void sendDisplay(PlayerRef player, Objective objective) {
    ScoreboardDefinition definition = definitionForAsset(objective.getObjectiveId());
    if (definition == null || player == null) {
      return;
    }
    sendDefinitionTranslations(player, definition);
    com.hypixel.hytale.protocol.Objective packet = objective.toPacket();
    packet.objectiveLineId = definition.getId();
    player.getPacketHandler().writeNoCache(new TrackOrUpdateObjective(packet));
  }

  private void sendDefinitionTranslations(
      PlayerRef player, ScoreboardDefinition definition) {
    if (player == null || definition == null) {
      return;
    }
    String keyBase = translationKeyBase(definition.getId());
    Map<String, String> translations =
        Map.of(
            keyBase + ".title", safeText(definition.getTitle()),
            keyBase + ".desc", safeText(definition.getDescription()));
    player
        .getPacketHandler()
        .writeNoCache(new UpdateTranslations(UpdateType.AddOrUpdate, translations));
  }

  private static String translationKeyBase(String definitionId) {
    return "server.objectives." + ScoreboardIds.assetId(definitionId);
  }

  private static String safeText(String value) {
    return value == null ? "" : value;
  }

  private synchronized ScoreboardDefinition definitionForAsset(String assetId) {
    for (ScoreboardDefinition definition : definitions.values()) {
      if (ScoreboardIds.assetId(definition.getId()).equals(assetId)) {
        return definition;
      }
    }
    return null;
  }
}
