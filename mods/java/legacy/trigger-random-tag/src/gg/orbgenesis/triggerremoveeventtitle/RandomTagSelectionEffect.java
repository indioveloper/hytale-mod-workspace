package gg.orbgenesis.triggerremoveeventtitle;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTagSelectionEffect extends TriggerEffect {
  public static final BuilderCodec<RandomTagSelectionEffect> CODEC =
      BuilderCodec.builder(
              RandomTagSelectionEffect.class,
              RandomTagSelectionEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("TagKey", Codec.STRING),
              (effect, value) -> effect.tagKey = value,
              effect -> effect.tagKey)
          .add()
          .append(
              new KeyedCodec<>("TagValues", Codec.STRING),
              (effect, value) -> effect.tagValues = value,
              effect -> effect.tagValues)
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
              new KeyedCodec<>(
                  "Center",
                  new com.hypixel.hytale.codec.codecs.EnumCodec<>(
                      TaggedVolumeEffectUtil.Center.class),
                  false),
              (effect, value) -> effect.center = value,
              effect -> effect.center)
          .add()
          .build();

  public String tagKey = "";
  public String tagValues = "";
  public String matchKey = "";
  public String matchValue = "";
  public double radius = 50.0D;
  public TaggedVolumeEffectUtil.Center center = TaggedVolumeEffectUtil.Center.VOLUME;

  @Override
  public void execute(TriggerContext context) {
    TriggerVolumeManager manager =
        context
            .getStore()
            .getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (manager == null || tagKey == null || tagKey.isBlank()) {
      return;
    }

    List<String> values = parseValues(tagValues);
    if (values.isEmpty()) {
      return;
    }

    String selected = values.get(ThreadLocalRandom.current().nextInt(values.size()));
    for (VolumeEntry target : collectTargets(context)) {
      manager.setTag(target.getId(), tagKey, selected, context.getEntityRef(), null);
    }
  }

  private List<VolumeEntry> collectTargets(TriggerContext context) {
    String filter = TaggedVolumeEffectUtil.composeTagFilter(matchKey, matchValue);
    if (filter == null) {
      VolumeEntry self = context.getVolume();
      return self == null ? List.of() : List.of(self);
    }
    return TaggedVolumeEffectUtil.collectTargets(context, filter, radius, center);
  }

  private static List<String> parseValues(String rawValues) {
    List<String> values = new ArrayList<>();
    if (rawValues == null || rawValues.isBlank()) {
      return values;
    }

    for (String candidate : rawValues.split("[,;|\\n\\r]+")) {
      String value = candidate.trim();
      if (!value.isEmpty()) {
        values.add(value);
      }
    }
    return values;
  }
}
