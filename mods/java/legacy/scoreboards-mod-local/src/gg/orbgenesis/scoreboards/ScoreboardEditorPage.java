package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.Objective;
import com.hypixel.hytale.protocol.ObjectiveTask;
import com.hypixel.hytale.protocol.packets.assets.TrackOrUpdateObjective;
import com.hypixel.hytale.protocol.packets.assets.UntrackObjective;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
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
  private String title = "My Scoreboard";
  private String description = "Edited from the scoreboard UI";
  private String lineId = "scoreboard";
  private final String[] taskText = {
    "First line", "Second line", "Third line", "Fourth line", "Fifth line"
  };
  private final int[] current = {0, 0, 0, 0, 0};
  private final int[] needed = {1, 1, 1, 1, 1};

  public ScoreboardEditorPage(PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, PageEventData.CODEC);
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commands,
      UIEventBuilder events,
      Store<EntityStore> store) {
    commands.append("Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui");
    bind(events);
    fill(commands);
  }

  @Override
  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, PageEventData data) {
    readValues(data);

    if ("Apply".equals(data.action)) {
      playerRef.getPacketHandler().write(new TrackOrUpdateObjective(buildObjective()));
      playerRef.sendMessage(Message.raw("Scoreboard applied."));
    } else if ("Hide".equals(data.action)) {
      playerRef.getPacketHandler().write(new UntrackObjective(objectiveUuid));
      playerRef.sendMessage(Message.raw("Scoreboard hidden."));
    } else if ("Reset".equals(data.action)) {
      reset();
      UICommandBuilder commands = new UICommandBuilder();
      fill(commands);
      sendUpdate(commands, false);
    }
  }

  private void bind(UIEventBuilder events) {
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TitleInput", EventData.of("Title", "#TitleInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#DescriptionInput", EventData.of("Description", "#DescriptionInput.Value"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LineIdInput", EventData.of("LineId", "#LineIdInput.Value"), false);

    for (int i = 0; i < TASK_COUNT; i++) {
      events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TaskInput" + i, EventData.of("Task" + i, "#TaskInput" + i + ".Value"), false);
      events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CurrentInput" + i, EventData.of("Current" + i, "#CurrentInput" + i + ".Value"), false);
      events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#NeededInput" + i, EventData.of("Needed" + i, "#NeededInput" + i + ".Value"), false);
    }

    events.addEventBinding(CustomUIEventBindingType.Activating, "#ApplyButton", EventData.of("Action", "Apply"));
    events.addEventBinding(CustomUIEventBindingType.Activating, "#HideButton", EventData.of("Action", "Hide"));
    events.addEventBinding(CustomUIEventBindingType.Activating, "#ResetButton", EventData.of("Action", "Reset"));
  }

  private void fill(UICommandBuilder commands) {
    commands.set("#TitleInput.Value", title);
    commands.set("#DescriptionInput.Value", description);
    commands.set("#LineIdInput.Value", lineId);

    for (int i = 0; i < TASK_COUNT; i++) {
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

  private Objective buildObjective() {
    ObjectiveTask[] tasks = new ObjectiveTask[TASK_COUNT];
    for (int i = 0; i < TASK_COUNT; i++) {
      tasks[i] = new ObjectiveTask(message(taskText[i]), current[i], needed[i]);
    }

    return new Objective(objectiveUuid, message(title), message(description), lineId, tasks);
  }

  private FormattedMessage message(String text) {
    return Message.raw(text == null ? "" : text).getFormattedMessage();
  }

  private void reset() {
    title = "My Scoreboard";
    description = "Edited from the scoreboard UI";
    lineId = "scoreboard";
    for (int i = 0; i < TASK_COUNT; i++) {
      taskText[i] = "Line " + (i + 1);
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

  public static class PageEventData {
    public static final BuilderCodec<PageEventData> CODEC =
        BuilderCodec.builder(PageEventData.class, PageEventData::new)
            .addField(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value, data -> data.action)
            .addField(new KeyedCodec<>("Title", Codec.STRING), (data, value) -> data.title = value, data -> data.title)
            .addField(new KeyedCodec<>("Description", Codec.STRING), (data, value) -> data.description = value, data -> data.description)
            .addField(new KeyedCodec<>("LineId", Codec.STRING), (data, value) -> data.lineId = value, data -> data.lineId)
            .addField(new KeyedCodec<>("Task0", Codec.STRING), (data, value) -> data.tasks[0] = value, data -> data.tasks[0])
            .addField(new KeyedCodec<>("Task1", Codec.STRING), (data, value) -> data.tasks[1] = value, data -> data.tasks[1])
            .addField(new KeyedCodec<>("Task2", Codec.STRING), (data, value) -> data.tasks[2] = value, data -> data.tasks[2])
            .addField(new KeyedCodec<>("Task3", Codec.STRING), (data, value) -> data.tasks[3] = value, data -> data.tasks[3])
            .addField(new KeyedCodec<>("Task4", Codec.STRING), (data, value) -> data.tasks[4] = value, data -> data.tasks[4])
            .addField(new KeyedCodec<>("Current0", Codec.STRING), (data, value) -> data.current[0] = value, data -> data.current[0])
            .addField(new KeyedCodec<>("Current1", Codec.STRING), (data, value) -> data.current[1] = value, data -> data.current[1])
            .addField(new KeyedCodec<>("Current2", Codec.STRING), (data, value) -> data.current[2] = value, data -> data.current[2])
            .addField(new KeyedCodec<>("Current3", Codec.STRING), (data, value) -> data.current[3] = value, data -> data.current[3])
            .addField(new KeyedCodec<>("Current4", Codec.STRING), (data, value) -> data.current[4] = value, data -> data.current[4])
            .addField(new KeyedCodec<>("Needed0", Codec.STRING), (data, value) -> data.needed[0] = value, data -> data.needed[0])
            .addField(new KeyedCodec<>("Needed1", Codec.STRING), (data, value) -> data.needed[1] = value, data -> data.needed[1])
            .addField(new KeyedCodec<>("Needed2", Codec.STRING), (data, value) -> data.needed[2] = value, data -> data.needed[2])
            .addField(new KeyedCodec<>("Needed3", Codec.STRING), (data, value) -> data.needed[3] = value, data -> data.needed[3])
            .addField(new KeyedCodec<>("Needed4", Codec.STRING), (data, value) -> data.needed[4] = value, data -> data.needed[4])
            .build();

    public String action;
    public String title;
    public String description;
    public String lineId;
    public String[] tasks = new String[TASK_COUNT];
    public String[] current = new String[TASK_COUNT];
    public String[] needed = new String[TASK_COUNT];
  }
}
