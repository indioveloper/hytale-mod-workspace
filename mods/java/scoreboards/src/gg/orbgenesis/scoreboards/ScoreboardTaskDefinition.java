package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class ScoreboardTaskDefinition {
  public static final BuilderCodec<ScoreboardTaskDefinition> CODEC =
      BuilderCodec.builder(ScoreboardTaskDefinition.class, ScoreboardTaskDefinition::new)
          .append(
              new KeyedCodec<>("Id", Codec.STRING),
              (value, field) -> value.id = field,
              value -> value.id)
          .add()
          .append(
              new KeyedCodec<>("Label", Codec.STRING),
              (value, field) -> value.label = field,
              value -> value.label)
          .add()
          .append(
              new KeyedCodec<>("InitialValue", Codec.INTEGER, false),
              (value, field) -> value.initialValue = field,
              value -> value.initialValue)
          .add()
          .append(
              new KeyedCodec<>("Goal", Codec.INTEGER, false),
              (value, field) -> value.goal = field,
              value -> value.goal)
          .add()
          .build();

  private String id = "score";
  private String label = "Score";
  private int initialValue;
  private int goal = 100;

  public ScoreboardTaskDefinition() {}

  public ScoreboardTaskDefinition(String id, String label, int initialValue, int goal) {
    this.id = ScoreboardIds.cleanTaskId(id);
    this.label = label == null ? "" : label.trim();
    this.initialValue = Math.max(0, initialValue);
    this.goal = Math.max(1, goal);
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public int getInitialValue() {
    return initialValue;
  }

  public int getGoal() {
    return goal;
  }

  public ScoreboardTaskDefinition copy() {
    return new ScoreboardTaskDefinition(id, label, initialValue, goal);
  }
}
