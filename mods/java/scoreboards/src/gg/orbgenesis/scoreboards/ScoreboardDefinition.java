package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScoreboardDefinition {
  public static final BuilderCodec<ScoreboardDefinition> CODEC =
      BuilderCodec.builder(ScoreboardDefinition.class, ScoreboardDefinition::new)
          .append(
              new KeyedCodec<>("Id", Codec.STRING),
              (value, field) -> value.id = field,
              value -> value.id)
          .add()
          .append(
              new KeyedCodec<>("Title", Codec.STRING),
              (value, field) -> value.title = field,
              value -> value.title)
          .add()
          .append(
              new KeyedCodec<>("Description", Codec.STRING, false),
              (value, field) -> value.description = field,
              value -> value.description)
          .add()
          .append(
              new KeyedCodec<>(
                  "Tasks",
                  new ArrayCodec<>(ScoreboardTaskDefinition.CODEC, ScoreboardTaskDefinition[]::new)),
              (value, field) -> value.tasks = field,
              value -> value.tasks)
          .add()
          .build();

  private String id = "main";
  private String title = "Scoreboard";
  private String description = "";
  private ScoreboardTaskDefinition[] tasks = {
    new ScoreboardTaskDefinition("score", "Score", 0, 100)
  };

  public ScoreboardDefinition() {}

  public ScoreboardDefinition(
      String id, String title, String description, ScoreboardTaskDefinition[] tasks) {
    this.id = ScoreboardIds.cleanDefinitionId(id);
    this.title = title == null || title.isBlank() ? this.id : title.trim();
    this.description = description == null ? "" : description.trim();
    Map<String, ScoreboardTaskDefinition> uniqueTasks = new LinkedHashMap<>();
    if (tasks != null) {
      for (ScoreboardTaskDefinition task : tasks) {
        if (task == null || uniqueTasks.size() >= ScoreboardConfig.MAX_TASKS) {
          continue;
        }
        ScoreboardTaskDefinition normalized = task.copy();
        uniqueTasks.putIfAbsent(normalized.getId(), normalized);
      }
    }
    if (uniqueTasks.isEmpty()) {
      uniqueTasks.put("score", new ScoreboardTaskDefinition("score", "Score", 0, 100));
    }
    this.tasks = uniqueTasks.values().toArray(ScoreboardTaskDefinition[]::new);
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public ScoreboardTaskDefinition[] getTasks() {
    return tasks;
  }

  public ScoreboardDefinition copy() {
    return new ScoreboardDefinition(id, title, description, tasks);
  }
}
