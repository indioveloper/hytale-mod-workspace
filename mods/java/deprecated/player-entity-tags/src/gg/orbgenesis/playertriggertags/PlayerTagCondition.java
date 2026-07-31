package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.math.BigDecimal;
import java.util.Map;

public class PlayerTagCondition extends TriggerCondition {
  public enum Comparison {
    EQUALS,
    NOT_EQUALS,
    EXISTS,
    MISSING,
    GREATER_THAN,
    GREATER_OR_EQUAL,
    LESS_THAN,
    LESS_OR_EQUAL
  }

  public static final BuilderCodec<PlayerTagCondition> CODEC =
      BuilderCodec.builder(
              PlayerTagCondition.class, PlayerTagCondition::new, TriggerCondition.BASE_CODEC)
          .append(
              new KeyedCodec<>("TagKey", Codec.STRING),
              (condition, value) -> condition.tagKey = value,
              condition -> condition.tagKey)
          .add()
          .append(
              new KeyedCodec<>("Comparison", new EnumCodec<>(Comparison.class), false),
              (condition, value) -> condition.comparison = value,
              condition -> condition.comparison)
          .add()
          .append(
              new KeyedCodec<>("TagValue", Codec.STRING, false),
              (condition, value) -> condition.tagValue = value,
              condition -> condition.tagValue)
          .add()
          .append(
              new KeyedCodec<>("CaseSensitive", Codec.BOOLEAN, false),
              (condition, value) -> condition.caseSensitive = value,
              condition -> condition.caseSensitive)
          .add()
          .build();

  private String tagKey = "";
  private Comparison comparison = Comparison.EQUALS;
  private String tagValue = "";
  private boolean caseSensitive;

  @Override
  public boolean test(TriggerContext context) {
    String key = PlayerTagAccess.normalizeKey(tagKey);
    if (key.isEmpty() || PlayerTagAccess.getPlayer(context) == null) {
      return false;
    }

    PlayerTagsComponent component = PlayerTagAccess.getTags(context);
    Map<String, String> tags = component == null ? Map.of() : component.getTags();
    boolean exists = tags.containsKey(key);
    String actual = tags.get(key);

    return switch (comparison) {
      case EXISTS -> exists;
      case MISSING -> !exists;
      case EQUALS -> exists && equalsValue(actual, tagValue);
      case NOT_EQUALS -> !exists || !equalsValue(actual, tagValue);
      case GREATER_THAN -> compareNumbers(actual, tagValue, Comparison.GREATER_THAN);
      case GREATER_OR_EQUAL -> compareNumbers(actual, tagValue, Comparison.GREATER_OR_EQUAL);
      case LESS_THAN -> compareNumbers(actual, tagValue, Comparison.LESS_THAN);
      case LESS_OR_EQUAL -> compareNumbers(actual, tagValue, Comparison.LESS_OR_EQUAL);
    };
  }

  private boolean equalsValue(String left, String right) {
    String expected = right == null ? "" : right;
    if (left == null) {
      return false;
    }
    return caseSensitive ? left.equals(expected) : left.equalsIgnoreCase(expected);
  }

  private boolean compareNumbers(String left, String right, Comparison operation) {
    BigDecimal actual = parseNumber(left);
    BigDecimal expected = parseNumber(right);
    if (actual == null || expected == null) {
      return false;
    }
    int result = actual.compareTo(expected);
    return switch (operation) {
      case GREATER_THAN -> result > 0;
      case GREATER_OR_EQUAL -> result >= 0;
      case LESS_THAN -> result < 0;
      case LESS_OR_EQUAL -> result <= 0;
      default -> false;
    };
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
