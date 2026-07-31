package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.List;

public class ModifyScoreboardTaskEffect extends TriggerEffect {
  public static final BuilderCodec<ModifyScoreboardTaskEffect> CODEC =
      BuilderCodec.builder(
              ModifyScoreboardTaskEffect.class,
              ModifyScoreboardTaskEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("ObjectiveId", Codec.STRING),
              (effect, value) -> effect.objectiveId = value,
              effect -> effect.objectiveId)
          .add()
          .append(
              new KeyedCodec<>("TaskId", Codec.STRING),
              (effect, value) -> effect.taskId = value,
              effect -> effect.taskId)
          .add()
          .append(
              new KeyedCodec<>(
                  "Operation", new EnumCodec<>(ScoreboardManager.ModifyOperation.class), false),
              (effect, value) -> effect.operation = value,
              effect -> effect.operation)
          .add()
          .append(
              new KeyedCodec<>("Value", Codec.INTEGER),
              (effect, value) -> effect.value = value,
              effect -> effect.value)
          .add()
          .append(
              new KeyedCodec<>(
                  "Recipient", new EnumCodec<>(ObjectiveRecipient.class), false),
              (effect, value) -> effect.recipient = value,
              effect -> effect.recipient)
          .add()
          .append(
              new KeyedCodec<>(
                  "InstanceScope", new EnumCodec<>(ObjectiveInstanceScope.class), false),
              (effect, value) -> effect.instanceScope = value,
              effect -> effect.instanceScope)
          .add()
          .build();

  private String objectiveId = "main";
  private String taskId = "score";
  private ScoreboardManager.ModifyOperation operation = ScoreboardManager.ModifyOperation.ADD;
  private int value = 1;
  private ObjectiveRecipient recipient = ObjectiveRecipient.TRIGGERING_PLAYER;
  private ObjectiveInstanceScope instanceScope = ObjectiveInstanceScope.INDIVIDUAL;

  @Override
  public void execute(TriggerContext context) {
    ScoreboardsPlugin plugin = ScoreboardsPlugin.get();
    if (plugin == null || context == null) {
      return;
    }
    List<PlayerRef> players = ObjectiveTargetResolver.resolve(context, recipient);
    plugin
        .getManager()
        .modify(
            objectiveId,
            taskId,
            value,
            operation,
            players,
            instanceScope,
            ObjectiveTargetResolver.volumeId(context),
            context.getStore());
  }
}
