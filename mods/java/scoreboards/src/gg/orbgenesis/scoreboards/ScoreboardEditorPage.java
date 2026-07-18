package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.assets.UntrackObjective;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;

public class ScoreboardEditorPage extends InteractiveCustomUIPage<ScoreboardEditorPage.PageEventData> {
  private static final int TASK_COUNT = 5;

  private final UUID objectiveUuid = UUID.nameUUIDFromBytes("orbgenesis-scoreboard-ui".getBytes());
  private final ScoreboardTracker tracker;
  private String title = "Skeleton Hunter";
  private String description = "Counts skeleton kills.";
  private String lineId = "skeleton_kills";
  private int taskCount = 1;
  private final String[] taskText = {
    "Skeletons killed", "Line 2", "Line 3", "Line 4", "Line 5"
  };
  private final int[] current = {0, 0, 0, 0, 0};
  private final int[] needed = {1, 1, 1, 1, 1};

  public ScoreboardEditorPage(PlayerRef playerRef, ScoreboardTracker tracker) {
    super(playerRef, CustomPageLifetime.CanDismiss, PageEventData.CODEC);
    this.tracker = tracker;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commands,
      UIEventBuilder events,
      Store<EntityStore> store) {
    commands.append("Pages/Scoreboards/ScoreboardEditor" + taskCount + ".ui");
    bind(events);
    fill(commands);
  }

  @Override
  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, PageEventData data) {
    readValues(data);

    if ("Apply".equals(data.action) || data.apply != null) {
      tracker.apply(playerRef, store, buildDefinition());
      playerRef.sendMessage(Message.raw("Scoreboard applied."));
      close();
    } else if ("Hide".equals(data.action) || data.hide != null) {
      tracker.hide(playerRef, store);
      playerRef.getPacketHandler().write(new UntrackObjective(objectiveUuid));
      playerRef.sendMessage(Message.raw("Scoreboard hidden."));
      close();
    } else if ("Reset".equals(data.action) || data.reset != null) {
      reset();
      reopen(store, ref);
    } else if ("AddLine".equals(data.action)) {
      if (taskCount < TASK_COUNT) {
        taskCount++;
      }
      reopen(store, ref);
    }
  }

  private void bind(UIEventBuilder events) {
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TitleInput", EventData.of("@Title", "#TitleInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#DescriptionInput", EventData.of("@Description", "#DescriptionInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LineIdInput", EventData.of("@LineId", "#LineIdInput.Value"), false);

    for (int i = 0; i < taskCount; i++) {
      events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TaskInput" + i, EventData.of("@Task" + i, "#TaskInput" + i + ".Value"), false);
      events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CurrentInput" + i, EventData.of("@Current" + i, "#CurrentInput" + i + ".Value"), false);
      events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#NeededInput" + i, EventData.of("@Needed" + i, "#NeededInput" + i + ".Value"), false);
    }

    bindSnapshot(events, "#ApplyTopButton", "Apply");
    events.addEventBinding(CustomUIEventBindingType.Activating, "#HideTopButton", EventData.of("Action", "Hide"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#ResetTopButton", EventData.of("Action", "Reset"), false);
    bindSnapshot(events, "#AddLineButton", "AddLine");
  }

  private void fill(UICommandBuilder commands) {
    commands.set("#TitleInput.Value", title);
    commands.set("#DescriptionInput.Value", description);
    commands.set("#LineIdInput.Value", lineId);

    for (int i = 0; i < taskCount; i++) {
      commands.set("#TaskInput" + i + ".Value", taskText[i]);
      commands.set("#CurrentInput" + i + ".Value", String.valueOf(current[i]));
      commands.set("#NeededInput" + i + ".Value", String.valueOf(needed[i]));
    }
  }

  private void readValues(PageEventData data) {
    title = coalesce(data.title, title);
    description = coalesce(data.description, description);
    lineId = cleanLineId(coalesce(data.lineId, lineId));

    for (int i = 0; i < TASK_COUNT; i++) {
      taskText[i] = coalesce(data.tasks[i], taskText[i]);
      current[i] = parseInt(coalesce(data.current[i], null), current[i]);
      needed[i] = Math.max(1, parseInt(coalesce(data.needed[i], null), needed[i]));
    }
  }

  private ScoreboardTracker.ScoreboardDefinition buildDefinition() {
    ScoreboardTracker.ScoreboardDefinition.TriggerType[] triggers =
        new ScoreboardTracker.ScoreboardDefinition.TriggerType[TASK_COUNT];
    for (int i = 0; i < TASK_COUNT; i++) {
      triggers[i] =
          i == 0
              ? ScoreboardTracker.ScoreboardDefinition.TriggerType.SKELETON_KILL
              : ScoreboardTracker.ScoreboardDefinition.TriggerType.NONE;
    }

    return new ScoreboardTracker.ScoreboardDefinition(
        objectiveUuid,
        title,
        description,
        lineId,
        taskText,
        current,
        needed,
        taskCount,
        triggers);
  }

  private void reset() {
    title = "Skeleton Hunter";
    description = "Counts skeleton kills.";
    lineId = "skeleton_kills";
    taskCount = 1;
    for (int i = 0; i < TASK_COUNT; i++) {
      taskText[i] = i == 0 ? "Skeletons killed" : "Line " + (i + 1);
      current[i] = 0;
      needed[i] = 1;
    }
  }

  private String coalesce(String value, String fallback) {
    return value == null ? fallback : value;
  }

  private int parseInt(String value, int fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }

    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private String cleanLineId(String value) {
    String cleaned = value == null ? "scoreboard" : value.trim();
    return cleaned.isEmpty() ? "scoreboard" : cleaned;
  }

  private void bindSnapshot(UIEventBuilder events, String selector, String action) {
    events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Action", action), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("@Title", "#TitleInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("@Description", "#DescriptionInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("@LineId", "#LineIdInput.Value"), false);

    for (int i = 0; i < taskCount; i++) {
      events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("@Task" + i, "#TaskInput" + i + ".Value"), false);
      events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("@Current" + i, "#CurrentInput" + i + ".Value"), false);
      events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("@Needed" + i, "#NeededInput" + i + ".Value"), false);
    }
  }

  private void reopen(Store<EntityStore> store, Ref<EntityStore> playerEntityRef) {
    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
    if (player == null) {
      close();
      return;
    }

    ScoreboardEditorPage page = new ScoreboardEditorPage(playerRef, tracker);
    page.copyStateFrom(this);
    close();
    player.getPageManager().openCustomPage(playerEntityRef, store, page);
  }

  private void copyStateFrom(ScoreboardEditorPage source) {
    title = source.title;
    description = source.description;
    lineId = source.lineId;
    taskCount = source.taskCount;
    System.arraycopy(source.taskText, 0, taskText, 0, TASK_COUNT);
    System.arraycopy(source.current, 0, current, 0, TASK_COUNT);
    System.arraycopy(source.needed, 0, needed, 0, TASK_COUNT);
  }

  public static class PageEventData {
    public static final BuilderCodec<PageEventData> CODEC =
        BuilderCodec.builder(PageEventData.class, PageEventData::new)
            .addField(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value, data -> data.action)
            .addField(new KeyedCodec<>("Apply", Codec.STRING), (data, value) -> data.apply = value, data -> data.apply)
            .addField(new KeyedCodec<>("Hide", Codec.STRING), (data, value) -> data.hide = value, data -> data.hide)
            .addField(new KeyedCodec<>("Reset", Codec.STRING), (data, value) -> data.reset = value, data -> data.reset)
            .addField(new KeyedCodec<>("@Title", Codec.STRING), (data, value) -> data.title = value, data -> data.title)
            .addField(new KeyedCodec<>("@Description", Codec.STRING), (data, value) -> data.description = value, data -> data.description)
            .addField(new KeyedCodec<>("@LineId", Codec.STRING), (data, value) -> data.lineId = value, data -> data.lineId)
            .addField(new KeyedCodec<>("@Task0", Codec.STRING), (data, value) -> data.tasks[0] = value, data -> data.tasks[0])
            .addField(new KeyedCodec<>("@Task1", Codec.STRING), (data, value) -> data.tasks[1] = value, data -> data.tasks[1])
            .addField(new KeyedCodec<>("@Task2", Codec.STRING), (data, value) -> data.tasks[2] = value, data -> data.tasks[2])
            .addField(new KeyedCodec<>("@Task3", Codec.STRING), (data, value) -> data.tasks[3] = value, data -> data.tasks[3])
            .addField(new KeyedCodec<>("@Task4", Codec.STRING), (data, value) -> data.tasks[4] = value, data -> data.tasks[4])
            .addField(new KeyedCodec<>("@Current0", Codec.STRING), (data, value) -> data.current[0] = value, data -> data.current[0])
            .addField(new KeyedCodec<>("@Current1", Codec.STRING), (data, value) -> data.current[1] = value, data -> data.current[1])
            .addField(new KeyedCodec<>("@Current2", Codec.STRING), (data, value) -> data.current[2] = value, data -> data.current[2])
            .addField(new KeyedCodec<>("@Current3", Codec.STRING), (data, value) -> data.current[3] = value, data -> data.current[3])
            .addField(new KeyedCodec<>("@Current4", Codec.STRING), (data, value) -> data.current[4] = value, data -> data.current[4])
            .addField(new KeyedCodec<>("@Needed0", Codec.STRING), (data, value) -> data.needed[0] = value, data -> data.needed[0])
            .addField(new KeyedCodec<>("@Needed1", Codec.STRING), (data, value) -> data.needed[1] = value, data -> data.needed[1])
            .addField(new KeyedCodec<>("@Needed2", Codec.STRING), (data, value) -> data.needed[2] = value, data -> data.needed[2])
            .addField(new KeyedCodec<>("@Needed3", Codec.STRING), (data, value) -> data.needed[3] = value, data -> data.needed[3])
            .addField(new KeyedCodec<>("@Needed4", Codec.STRING), (data, value) -> data.needed[4] = value, data -> data.needed[4])
            .build();

    public String action;
    public String apply;
    public String hide;
    public String reset;
    public String title;
    public String description;
    public String lineId;
    public String[] tasks = new String[TASK_COUNT];
    public String[] current = new String[TASK_COUNT];
    public String[] needed = new String[TASK_COUNT];
  }
}
