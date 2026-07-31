package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class ScoreboardTaskValueCondition extends TriggerCondition {
  public enum Comparison {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_OR_EQUAL,
    LESS_THAN,
    LESS_OR_EQUAL
  }

  public static final BuilderCodec<ScoreboardTaskValueCondition> CODEC =
      BuilderCodec.builder(
              ScoreboardTaskValueCondition.class,
              ScoreboardTaskValueCondition::new,
              TriggerCondition.BASE_CODEC)
          .append(
              new KeyedCodec<>("ObjectiveId", Codec.STRING),
              (condition, value) -> condition.objectiveId = value,
              condition -> condition.objectiveId)
          .add()
          .append(
              new KeyedCodec<>("TaskId", Codec.STRING),
              (condition, value) -> condition.taskId = value,
              condition -> condition.taskId)
          .add()
          .append(
              new KeyedCodec<>("Comparison", new EnumCodec<>(Comparison.class), false),
              (condition, value) -> condition.comparison = value,
              condition -> condition.comparison)
          .add()
          .append(
              new KeyedCodec<>("Value", Codec.INTEGER),
              (condition, value) -> condition.value = value,
              condition -> condition.value)
          .add()
          .append(
              new KeyedCodec<>(
                  "InstanceScope", new EnumCodec<>(ObjectiveInstanceScope.class), false),
              (condition, value) -> condition.instanceScope = value,
              condition -> condition.instanceScope)
          .add()
          .build();

  private String objectiveId = "main";
  private String taskId = "score";
  private Comparison comparison = Comparison.GREATER_OR_EQUAL;
  private int value;
  private ObjectiveInstanceScope instanceScope = ObjectiveInstanceScope.INDIVIDUAL;

  @Override
  public boolean test(TriggerContext context) {
    ScoreboardsPlugin plugin = ScoreboardsPlugin.get();
    if (plugin == null || context == null || context.getEntityRef() == null) {
      return false;
    }
    PlayerRef player =
        context
            .getStore()
            .getComponent(context.getEntityRef(), PlayerRef.getComponentType());
    if (player == null) {
      return false;
    }
    Integer current =
        plugin
            .getManager()
            .getTaskValue(
                objectiveId,
                taskId,
                player,
                instanceScope,
                ObjectiveTargetResolver.volumeId(context));
    if (current == null) {
      return false;
    }
    return switch (comparison == null ? Comparison.GREATER_OR_EQUAL : comparison) {
      case EQUALS -> current == value;
      case NOT_EQUALS -> current != value;
      case GREATER_THAN -> current > value;
      case GREATER_OR_EQUAL -> current >= value;
      case LESS_THAN -> current < value;
      case LESS_OR_EQUAL -> current <= value;
    };
  }
}
