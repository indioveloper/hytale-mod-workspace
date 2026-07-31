package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ScoreboardEditorPage
    extends InteractiveCustomUIPage<ScoreboardEditorPage.EditorEventData> {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private final ScoreboardsPlugin plugin;
  private final String originalId;
  private String id;
  private String title;
  private String description;
  private final String[] taskIds = new String[ScoreboardConfig.MAX_TASKS];
  private final String[] taskLabels = new String[ScoreboardConfig.MAX_TASKS];
  private final int[] initialValues = new int[ScoreboardConfig.MAX_TASKS];
  private final int[] goals = new int[ScoreboardConfig.MAX_TASKS];
  private int currentTaskIndex;

  public ScoreboardEditorPage(
      PlayerRef playerRef, ScoreboardsPlugin plugin, ScoreboardDefinition definition) {
    super(playerRef, CustomPageLifetime.CanDismiss, EditorEventData.CODEC);
    this.plugin = plugin;
    this.originalId = definition == null ? null : definition.getId();
    this.id = definition == null ? "new_scoreboard" : definition.getId();
    this.title = definition == null ? "New scoreboard" : definition.getTitle();
    this.description = definition == null ? "" : definition.getDescription();
    for (int i = 0; i < ScoreboardConfig.MAX_TASKS; i++) {
      goals[i] = 100;
    }
    if (definition != null) {
      ScoreboardTaskDefinition[] tasks = definition.getTasks();
      for (int i = 0; i < Math.min(tasks.length, ScoreboardConfig.MAX_TASKS); i++) {
        taskIds[i] = tasks[i].getId();
        taskLabels[i] = tasks[i].getLabel();
        initialValues[i] = tasks[i].getInitialValue();
        goals[i] = tasks[i].getGoal();
      }
    } else {
      taskIds[0] = "score";
      taskLabels[0] = "Score";
    }
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commands,
      UIEventBuilder events,
    Store<EntityStore> store) {
    commands.append("Pages/Scoreboards/ScoreboardEditor.ui");
    fill(commands);
    bindAction(events, "#PreviousTaskButton", "PreviousTask");
    bindAction(events, "#NextTaskButton", "NextTask");
    bindAction(events, "#SaveButton", "Save");
    bindAction(events, "#StartButton", "Start");
    bindAction(events, "#DeleteButton", "Delete");
    bindAction(events, "#BackButton", "Back");
    bindValue(events, "#DefinitionIdInput", "@DefinitionId");
    bindValue(events, "#TitleInput", "@Title");
    bindValue(events, "#DescriptionInput", "@Description");
    bindValue(events, "#TaskIdInput", "@TaskId");
    bindValue(events, "#TaskLabelInput", "@TaskLabel");
    bindValue(events, "#TaskInitialInput", "@TaskInitial");
    bindValue(events, "#TaskGoalInput", "@TaskGoal");
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, EditorEventData data) {
    if (data == null) {
      return;
    }
    if (data.action != null) {
      LOGGER
          .at(Level.INFO)
          .log(
              "Objective editor event: action=%s, definition=%s",
              data.action,
              originalId == null ? "<new>" : originalId);
    }
    if (data.action == null) {
      read(data);
      return;
    }
    if ("Back".equals(data.action)) {
      openList(ref, store);
      return;
    }
    if ("Delete".equals(data.action)) {
      if (originalId != null) {
        plugin.getManager().deleteDefinition(originalId, store);
        playerRef.sendMessage(Message.raw("Scoreboard deleted: " + originalId));
      }
      openList(ref, store);
      return;
    }

    read(data);
    if ("PreviousTask".equals(data.action)) {
      currentTaskIndex = Math.max(0, currentTaskIndex - 1);
      rebuild();
      return;
    }
    if ("NextTask".equals(data.action)) {
      currentTaskIndex =
          Math.min(ScoreboardConfig.MAX_TASKS - 1, currentTaskIndex + 1);
      rebuild();
      return;
    }

    ScoreboardDefinition definition = buildDefinition();
    if (definition == null) {
      playerRef.sendMessage(Message.raw("At least one task with an ID is required."));
      return;
    }
    if (originalId != null && !originalId.equals(definition.getId())) {
      plugin.getManager().deleteDefinition(originalId, store);
    }
    CompletableFuture<Boolean> saveFuture =
        plugin.getManager().upsertDefinition(definition);
    World world = store.getExternalData().getWorld();
    boolean startAfterSave = "Start".equals(data.action);

    saveFuture.whenComplete(
        (saved, error) ->
            finishSaveOnWorldThread(world, definition, startAfterSave, saved, error));

    if (startAfterSave) {
      close();
    } else {
      openList(ref, store);
    }
  }

  private void finishSaveOnWorldThread(
      World world,
      ScoreboardDefinition definition,
      boolean startAfterSave,
      Boolean saved,
      Throwable error) {
    if (world == null || !world.isAlive()) {
      return;
    }
    world.execute(
        () -> {
          if (error != null || !Boolean.TRUE.equals(saved)) {
            LOGGER
                .at(Level.SEVERE)
                .log("Failed to save scoreboard '%s'", definition.getId(), error);
            playerRef.sendMessage(
                Message.raw("Could not save scoreboard: " + definition.getId()));
            return;
          }

          plugin.getManager().refreshActiveObjectives(definition.getId());
          playerRef.sendMessage(Message.raw("Scoreboard saved: " + definition.getId()));
          if (!startAfterSave || !playerRef.isValid()) {
            return;
          }

          Store<EntityStore> currentStore = world.getEntityStore().getStore();
          boolean started =
              !plugin
                  .getManager()
                  .start(
                      definition.getId(),
                      List.of(playerRef),
                      ObjectiveInstanceScope.INDIVIDUAL,
                      null,
                      currentStore)
                  .isEmpty();
          playerRef.sendMessage(
              Message.raw(
                  started
                      ? "Objective started: " + definition.getId()
                      : "Could not start Objective: " + definition.getId()));
        });
  }

  private void fill(UICommandBuilder commands) {
    commands.set("#DefinitionIdInput.Value", id);
    commands.set("#TitleInput.Value", title);
    commands.set("#DescriptionInput.Value", description);
    commands.set(
        "#TaskPosition.Text",
        (currentTaskIndex + 1) + " / " + ScoreboardConfig.MAX_TASKS);
    commands.set("#TaskIdInput.Value", safe(taskIds[currentTaskIndex]));
    commands.set("#TaskLabelInput.Value", safe(taskLabels[currentTaskIndex]));
    commands.set(
        "#TaskInitialInput.Value", String.valueOf(initialValues[currentTaskIndex]));
    commands.set("#TaskGoalInput.Value", String.valueOf(goals[currentTaskIndex]));
  }

  private void bindAction(UIEventBuilder events, String selector, String action) {
    events.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        EventData.of("Action", action),
        false);
  }

  private void bindValue(UIEventBuilder events, String selector, String key) {
    events.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        selector,
        EventData.of(key, selector + ".Value"),
        false);
  }

  private void read(EditorEventData data) {
    id = fallback(data.definitionId, id);
    title = fallback(data.title, title);
    description = fallback(data.description, description);
    taskIds[currentTaskIndex] =
        fallback(data.taskId, taskIds[currentTaskIndex]);
    taskLabels[currentTaskIndex] =
        fallback(data.taskLabel, taskLabels[currentTaskIndex]);
    initialValues[currentTaskIndex] =
        parseInt(data.taskInitial, initialValues[currentTaskIndex], 0);
    goals[currentTaskIndex] =
        parseInt(data.taskGoal, goals[currentTaskIndex], 1);
  }

  private ScoreboardDefinition buildDefinition() {
    List<ScoreboardTaskDefinition> tasks = new ArrayList<>();
    for (int i = 0; i < ScoreboardConfig.MAX_TASKS; i++) {
      if (taskIds[i] == null || taskIds[i].isBlank()) {
        continue;
      }
      tasks.add(
          new ScoreboardTaskDefinition(
              taskIds[i], fallback(taskLabels[i], taskIds[i]), initialValues[i], goals[i]));
    }
    if (tasks.isEmpty()) {
      return null;
    }
    return new ScoreboardDefinition(
        id, title, description, tasks.toArray(ScoreboardTaskDefinition[]::new));
  }

  private void openList(Ref<EntityStore> ref, Store<EntityStore> store) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player != null) {
      player.getPageManager().clearCustomPageAcknowledgements();
      player
          .getPageManager()
          .openCustomPage(ref, store, new ScoreboardListPage(playerRef, plugin));
    }
  }

  private static String fallback(String value, String fallback) {
    return value == null ? fallback : value.trim();
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }

  private static int parseInt(String value, int fallback, int minimum) {
    if (value == null || value.isBlank()) {
      return Math.max(minimum, fallback);
    }
    try {
      return Math.max(minimum, Integer.parseInt(value.trim()));
    } catch (NumberFormatException ignored) {
      return Math.max(minimum, fallback);
    }
  }

  public static class EditorEventData {
    public static final BuilderCodec<EditorEventData> CODEC =
        buildCodec();

    private static BuilderCodec<EditorEventData> buildCodec() {
      BuilderCodec.Builder<EditorEventData> builder =
          BuilderCodec.builder(EditorEventData.class, EditorEventData::new)
              .append(
                  new KeyedCodec<>("Action", Codec.STRING, false),
                  (data, value) -> data.action = value,
                  data -> data.action)
              .add()
              .append(
                  new KeyedCodec<>("@DefinitionId", Codec.STRING, false),
                  (data, value) -> data.definitionId = value,
                  data -> data.definitionId)
              .add()
              .append(
                  new KeyedCodec<>("@Title", Codec.STRING, false),
                  (data, value) -> data.title = value,
                  data -> data.title)
              .add()
              .append(
                  new KeyedCodec<>("@Description", Codec.STRING, false),
                  (data, value) -> data.description = value,
                  data -> data.description)
              .add()
              .append(
                  new KeyedCodec<>("@TaskId", Codec.STRING, false),
                  (data, value) -> data.taskId = value,
                  data -> data.taskId)
              .add()
              .append(
                  new KeyedCodec<>("@TaskLabel", Codec.STRING, false),
                  (data, value) -> data.taskLabel = value,
                  data -> data.taskLabel)
              .add()
              .append(
                  new KeyedCodec<>("@TaskInitial", Codec.STRING, false),
                  (data, value) -> data.taskInitial = value,
                  data -> data.taskInitial)
              .add()
              .append(
                  new KeyedCodec<>("@TaskGoal", Codec.STRING, false),
                  (data, value) -> data.taskGoal = value,
                  data -> data.taskGoal)
              .add();
      return builder.build();
    }

    public String action;
    public String definitionId;
    public String title;
    public String description;
    public String taskId;
    public String taskLabel;
    public String taskInitial;
    public String taskGoal;
  }
}
