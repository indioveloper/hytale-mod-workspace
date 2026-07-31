package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class ScoreboardStateCondition extends TriggerCondition {
  public enum State {
    ACTIVE,
    INACTIVE,
    COMPLETED
  }

  public static final BuilderCodec<ScoreboardStateCondition> CODEC =
      BuilderCodec.builder(
              ScoreboardStateCondition.class,
              ScoreboardStateCondition::new,
              TriggerCondition.BASE_CODEC)
          .append(
              new KeyedCodec<>("ObjectiveId", Codec.STRING),
              (condition, value) -> condition.objectiveId = value,
              condition -> condition.objectiveId)
          .add()
          .append(
              new KeyedCodec<>("State", new EnumCodec<>(State.class), false),
              (condition, value) -> condition.state = value,
              condition -> condition.state)
          .add()
          .append(
              new KeyedCodec<>(
                  "InstanceScope", new EnumCodec<>(ObjectiveInstanceScope.class), false),
              (condition, value) -> condition.instanceScope = value,
              condition -> condition.instanceScope)
          .add()
          .build();

  private String objectiveId = "main";
  private State state = State.ACTIVE;
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
    boolean active =
        plugin
            .getManager()
            .isActive(
                objectiveId,
                player,
                instanceScope,
                ObjectiveTargetResolver.volumeId(context));
    return switch (state == null ? State.ACTIVE : state) {
      case ACTIVE -> active;
      case INACTIVE -> !active;
      case COMPLETED ->
          plugin
              .getManager()
              .hasCompleted(objectiveId, context.getEntityRef(), context.getStore());
    };
  }
}
