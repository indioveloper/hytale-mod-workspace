package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.List;

public class ControlScoreboardEffect extends TriggerEffect {
  public enum Action {
    START,
    SHOW,
    HIDE,
    COMPLETE,
    CANCEL
  }

  public static final BuilderCodec<ControlScoreboardEffect> CODEC =
      BuilderCodec.builder(
              ControlScoreboardEffect.class,
              ControlScoreboardEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("ObjectiveId", Codec.STRING),
              (effect, value) -> effect.objectiveId = value,
              effect -> effect.objectiveId)
          .add()
          .append(
              new KeyedCodec<>("Action", new EnumCodec<>(Action.class), false),
              (effect, value) -> effect.action = value,
              effect -> effect.action)
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
  private Action action = Action.START;
  private ObjectiveRecipient recipient = ObjectiveRecipient.TRIGGERING_PLAYER;
  private ObjectiveInstanceScope instanceScope = ObjectiveInstanceScope.INDIVIDUAL;

  @Override
  public void execute(TriggerContext context) {
    ScoreboardsPlugin plugin = ScoreboardsPlugin.get();
    if (plugin == null || context == null) {
      return;
    }
    List<PlayerRef> players = ObjectiveTargetResolver.resolve(context, recipient);
    String volumeId = ObjectiveTargetResolver.volumeId(context);
    ScoreboardManager manager = plugin.getManager();
    switch (action == null ? Action.START : action) {
      case START ->
          manager.start(objectiveId, players, instanceScope, volumeId, context.getStore());
      case SHOW ->
          manager.setTracked(
              objectiveId, true, players, instanceScope, volumeId, context.getStore());
      case HIDE ->
          manager.setTracked(
              objectiveId, false, players, instanceScope, volumeId, context.getStore());
      case COMPLETE ->
          manager.finish(
              objectiveId, false, players, instanceScope, volumeId, context.getStore());
      case CANCEL ->
          manager.finish(
              objectiveId, true, players, instanceScope, volumeId, context.getStore());
    }
  }
}
