package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModifyPlayerTagEffect extends TriggerEffect {
  public enum Operation {
    SET,
    REMOVE,
    INCREMENT,
    TOGGLE,
    APPEND
  }

  public enum DispatchMode {
    NONE,
    CURRENT_VOLUME,
    TAGGED_VOLUMES
  }

  public static final BuilderCodec<ModifyPlayerTagEffect> CODEC =
      BuilderCodec.builder(
              ModifyPlayerTagEffect.class, ModifyPlayerTagEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Operation", new EnumCodec<>(Operation.class), false),
              (effect, value) -> effect.operation = value,
              effect -> effect.operation)
          .add()
          .append(
              new KeyedCodec<>("TagKey", Codec.STRING),
              (effect, value) -> effect.tagKey = value,
              effect -> effect.tagKey)
          .add()
          .append(
              new KeyedCodec<>("TagValue", Codec.STRING, false),
              (effect, value) -> effect.tagValue = value,
              effect -> effect.tagValue)
          .add()
          .append(
              new KeyedCodec<>("DispatchMode", new EnumCodec<>(DispatchMode.class), false),
              (effect, value) -> effect.dispatchMode = value,
              effect -> effect.dispatchMode)
          .add()
          .append(
              new KeyedCodec<>("MatchKey", Codec.STRING, false),
              (effect, value) -> effect.matchKey = value,
              effect -> effect.matchKey)
          .add()
          .append(
              new KeyedCodec<>("MatchValue", Codec.STRING, false),
              (effect, value) -> effect.matchValue = value,
              effect -> effect.matchValue)
          .add()
          .append(
              new KeyedCodec<>("Radius", Codec.DOUBLE, false),
              (effect, value) -> effect.radius = value,
              effect -> effect.radius)
          .add()
          .append(
              new KeyedCodec<>("Center", new EnumCodec<>(TaggedVolumeEffectUtil.Center.class), false),
              (effect, value) -> effect.center = value,
              effect -> effect.center)
          .add()
          .build();

  private Operation operation = Operation.SET;
  private String tagKey = "";
  private String tagValue = "";
  private DispatchMode dispatchMode = DispatchMode.CURRENT_VOLUME;
  private String matchKey;
  private String matchValue;
  private double radius = 50.0;
  private TaggedVolumeEffectUtil.Center center = TaggedVolumeEffectUtil.Center.ENTITY;

  @Override
  public void execute(TriggerContext context) {
    String key = PlayerTagAccess.normalizeKey(tagKey);
    PlayerRef player = PlayerTagAccess.getPlayer(context);
    PlayerTagsComponent component = PlayerTagAccess.getOrCreateTags(context);
    if (player == null || component == null || key.isEmpty()) {
      return;
    }

    TagChange change = apply(component.getTags(), key);
    if (change == null || dispatchMode == DispatchMode.NONE) {
      return;
    }

    TriggerVolumeManager manager =
        context.getStore().getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (manager == null) {
      return;
    }

    dispatchChange(context, manager, player, change);
  }

  private TagChange apply(Map<String, String> tags, String key) {
    return switch (operation) {
      case SET -> set(tags, key, tagValue == null ? "" : tagValue);
      case REMOVE -> remove(tags, key);
      case INCREMENT -> increment(tags, key);
      case TOGGLE -> toggle(tags, key);
      case APPEND -> append(tags, key, tagValue == null ? "" : tagValue);
    };
  }

  private static TagChange set(Map<String, String> tags, String key, String value) {
    String previous = tags.put(key, value);
    if (Objects.equals(previous, value)) {
      return null;
    }
    return new TagChange(TriggerEventType.TAG_ADDED, key, value);
  }

  private static TagChange remove(Map<String, String> tags, String key) {
    if (!tags.containsKey(key)) {
      return null;
    }
    String previous = tags.remove(key);
    return new TagChange(TriggerEventType.TAG_REMOVED, key, previous);
  }

  private TagChange increment(Map<String, String> tags, String key) {
    BigDecimal current = parseDecimal(tags.get(key));
    BigDecimal amount = parseDecimal(tagValue);
    if (amount == null) {
      return null;
    }
    if (current == null) {
      current = BigDecimal.ZERO;
    }
    return set(tags, key, current.add(amount).stripTrailingZeros().toPlainString());
  }

  private static TagChange toggle(Map<String, String> tags, String key) {
    boolean current = Boolean.parseBoolean(tags.getOrDefault(key, "false"));
    return set(tags, key, Boolean.toString(!current));
  }

  private static TagChange append(Map<String, String> tags, String key, String value) {
    String current = tags.getOrDefault(key, "");
    return set(tags, key, current + value);
  }

  private void dispatchChange(
      TriggerContext context, TriggerVolumeManager manager, PlayerRef player, TagChange change) {
    switch (dispatchMode) {
      case NONE -> {}
      case CURRENT_VOLUME ->
          manager.enqueueVolumeEvent(
              change.eventType(),
              context.getEntityRef(),
              player.getUuid(),
              context.getVolume().getId(),
              change.key(),
              change.value());
      case TAGGED_VOLUMES -> {
        List<VolumeEntry> targets =
            TaggedVolumeEffectUtil.collectTargets(
                context,
                TaggedVolumeEffectUtil.composeTagFilter(matchKey, matchValue),
                radius,
                center);
        for (VolumeEntry volume : targets) {
          manager.enqueueVolumeEvent(
              change.eventType(),
              context.getEntityRef(),
              player.getUuid(),
              volume.getId(),
              change.key(),
              change.value());
        }
      }
    }
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

  private record TagChange(TriggerEventType eventType, String key, String value) {}
}
