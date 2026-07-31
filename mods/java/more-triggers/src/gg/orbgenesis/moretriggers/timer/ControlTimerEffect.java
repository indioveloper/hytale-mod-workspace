package gg.orbgenesis.moretriggers.timer;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import gg.orbgenesis.moretriggers.MoreTriggersPlugin;
import java.util.List;

public class ControlTimerEffect extends TriggerEffect {
  public static final BuilderCodec<ControlTimerEffect> CODEC =
      BuilderCodec.builder(ControlTimerEffect.class, ControlTimerEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Action", new EnumCodec<>(TimerAction.class), false),
              (effect, value) -> effect.action = value,
              effect -> effect.action)
          .add()
          .append(
              new KeyedCodec<>("DurationSeconds", Codec.INTEGER, false),
              (effect, value) -> effect.durationSeconds = value,
              effect -> effect.durationSeconds)
          .add()
          .append(
              new KeyedCodec<>("Recipient", new EnumCodec<>(TimerRecipient.class), false),
              (effect, value) -> effect.recipient = value,
              effect -> effect.recipient)
          .add()
          .build();

  private TimerAction action = TimerAction.START;
  private int durationSeconds = 60;
  private TimerRecipient recipient = TimerRecipient.TRIGGERING_PLAYER;

  @Override
  public void execute(TriggerContext context) {
    MoreTriggersPlugin plugin = MoreTriggersPlugin.get();
    if (plugin == null || context == null) {
      return;
    }
    List<PlayerRef> players = TimerTargetResolver.resolve(context, recipient);
    plugin.getTimerManager().apply(action, durationSeconds, players);
  }
}
