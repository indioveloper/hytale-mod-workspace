package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.adventure.objectives.config.task.CountObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.joml.Vector3i;

public class ManualCountObjectiveTaskAsset extends CountObjectiveTaskAsset {
  public static final BuilderCodec<ManualCountObjectiveTaskAsset> CODEC =
      BuilderCodec.builder(
              ManualCountObjectiveTaskAsset.class,
              ManualCountObjectiveTaskAsset::new,
              CountObjectiveTaskAsset.CODEC)
          .append(
              new KeyedCodec<>("TaskId", Codec.STRING),
              (value, field) -> value.taskId = field,
              value -> value.taskId)
          .add()
          .append(
              new KeyedCodec<>("RawLabel", Codec.STRING),
              (value, field) -> value.rawLabel = field,
              value -> value.rawLabel)
          .add()
          .build();

  private String taskId = "score";
  private String rawLabel = "Score";

  public ManualCountObjectiveTaskAsset() {}

  public ManualCountObjectiveTaskAsset(String taskId, String rawLabel, int goal) {
    super(null, new TaskConditionAsset[0], new Vector3i[0], Math.max(1, goal));
    this.taskId = ScoreboardIds.cleanTaskId(taskId);
    this.rawLabel = rawLabel == null ? "" : rawLabel;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getRawLabel() {
    return rawLabel;
  }

  @Override
  public TaskScope getTaskScope() {
    return TaskScope.PLAYER;
  }

  @Override
  protected boolean matchesAsset0(ObjectiveTaskAsset other) {
    return super.matchesAsset0(other)
        && other instanceof ManualCountObjectiveTaskAsset manual
        && taskId.equals(manual.taskId);
  }
}
