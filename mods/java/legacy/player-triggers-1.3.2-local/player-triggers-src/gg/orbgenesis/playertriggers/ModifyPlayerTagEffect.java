package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.math.BigDecimal;
import java.util.Map;

public class ModifyPlayerTagEffect extends TriggerEffect {
  public enum Operation {
    SET,
    REMOVE,
    INCREMENT,
    TOGGLE,
    APPEND
  }

  public static final BuilderCodec<ModifyPlayerTagEffect> CODEC =
      BuilderCodec.builder(
              ModifyPlayerTagEffect.class,
              ModifyPlayerTagEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Operation", new EnumCodec<>(Operation.class)),
              (effect, value) -> effect.operation = value,
              effect -> effect.operation)
          .add()
          .append(
              new KeyedCodec<>("Tag", Codec.STRING),
              (effect, value) -> effect.tag = value,
              effect -> effect.tag)
          .add()
          .append(
              new KeyedCodec<>("Value", Codec.STRING, false),
              (effect, value) -> effect.value = value,
              effect -> effect.value)
          .add()
          .build();

  private Operation operation = Operation.SET;
  private String tag = "";
  private String value = "";

  @Override
  public void execute(TriggerContext context) {
    String key = PlayerTagAccess.normalizeKey(tag);
    PlayerTagsComponent component = PlayerTagAccess.getTags(context);
    if (component == null || key.isEmpty()) {
      return;
    }

    Map<String, String> tags = component.getTags();
    switch (operation) {
      case SET -> tags.put(key, value == null ? "" : value);
      case REMOVE -> tags.remove(key);
      case INCREMENT -> increment(tags, key);
      case TOGGLE -> toggle(tags, key);
      case APPEND -> tags.merge(key, value == null ? "" : value, String::concat);
    }
    TagValueHud.refreshIfWatching(context, component);
  }

  private void increment(Map<String, String> tags, String key) {
    BigDecimal current = parseDecimal(tags.get(key));
    BigDecimal amount = parseDecimal(value);
    if (amount == null) {
      return;
    }
    if (current == null) {
      current = BigDecimal.ZERO;
    }
    tags.put(key, current.add(amount).stripTrailingZeros().toPlainString());
  }

  private static void toggle(Map<String, String> tags, String key) {
    boolean current = Boolean.parseBoolean(tags.getOrDefault(key, "false"));
    tags.put(key, Boolean.toString(!current));
  }

  private static BigDecimal parseDecimal(String raw) {
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
