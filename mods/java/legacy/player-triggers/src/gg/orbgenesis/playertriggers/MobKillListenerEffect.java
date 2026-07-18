package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.math.BigDecimal;

public class MobKillListenerEffect extends TriggerEffect {
  public static final BuilderCodec<MobKillListenerEffect> CODEC =
      BuilderCodec.builder(
              MobKillListenerEffect.class,
              MobKillListenerEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Enabled", Codec.BOOLEAN, false),
              (effect, value) -> effect.enabled = value,
              effect -> effect.enabled)
          .add()
          .append(
              new KeyedCodec<>("CounterTag", Codec.STRING),
              (effect, value) -> effect.counterTag = value,
              effect -> effect.counterTag)
          .add()
          .append(
              new KeyedCodec<>("PointsPerKill", Codec.STRING, false),
              (effect, value) -> effect.pointsPerKill = value,
              effect -> effect.pointsPerKill)
          .add()
          .append(
              new KeyedCodec<>("MobFilter", Codec.STRING, false),
              (effect, value) -> effect.mobFilter = value,
              effect -> effect.mobFilter)
          .add()
          .build();

  private boolean enabled = true;
  private String counterTag = "mob_kills";
  private String pointsPerKill = "1";
  private String mobFilter = "";

  @Override
  public void execute(TriggerContext context) {
    PlayerTagsComponent component = PlayerTagAccess.getTags(context);
    String key = PlayerTagAccess.normalizeKey(counterTag);
    if (component == null || key.isEmpty() || parseNumber(pointsPerKill) == null) {
      return;
    }

    component.configureMobKillListener(enabled, key, pointsPerKill, mobFilter);
    if (enabled) {
      component.getTags().putIfAbsent(key, "0");
    }
  }

  private static BigDecimal parseNumber(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(raw.trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
