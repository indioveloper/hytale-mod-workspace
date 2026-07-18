package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class StartPlayerTimerEffect extends TriggerEffect {
  public static final BuilderCodec<StartPlayerTimerEffect> CODEC =
      BuilderCodec.builder(
              StartPlayerTimerEffect.class,
              StartPlayerTimerEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("DurationSeconds", Codec.DOUBLE),
              (effect, value) -> effect.durationSeconds = value,
              effect -> effect.durationSeconds)
          .add()
          .append(
              new KeyedCodec<>("Label", Codec.STRING, false),
              (effect, value) -> effect.label = value,
              effect -> effect.label)
          .add()
          .append(
              new KeyedCodec<>("SuccessTag", Codec.STRING),
              (effect, value) -> effect.successTag = value,
              effect -> effect.successTag)
          .add()
          .append(
              new KeyedCodec<>("RestartIfRunning", Codec.BOOLEAN, false),
              (effect, value) -> effect.restartIfRunning = value,
              effect -> effect.restartIfRunning)
          .add()
          .append(
              new KeyedCodec<>("CancelOnExit", Codec.BOOLEAN, false),
              (effect, value) -> effect.cancelOnExit = value,
              effect -> effect.cancelOnExit)
          .add()
          .build();

  private double durationSeconds = 180.0D;
  private String label = "Tiempo restante";
  private String successTag = "timer_complete";
  private boolean restartIfRunning = true;
  private boolean cancelOnExit;

  @Override
  public void execute(TriggerContext context) {
    PlayerRef player = PlayerTagAccess.getPlayer(context);
    String normalizedTag = PlayerTagAccess.normalizeKey(successTag);
    if (player == null
        || context.getVolume() == null
        || !Double.isFinite(durationSeconds)
        || durationSeconds <= 0.0D
        || normalizedTag.isEmpty()) {
      return;
    }

    PlayerTriggersPlugin.get()
        .getTimerService()
        .start(
            context,
            player,
            durationSeconds,
            label,
            normalizedTag,
            restartIfRunning,
            this);
  }

  @Override
  public void onEntityExit(java.util.UUID playerId) {
    if (cancelOnExit) {
      PlayerTriggersPlugin.get().getTimerService().cancel(playerId, this);
    }
  }
}
