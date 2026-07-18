package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;

public class TagValueHudEffect extends TriggerEffect {
  public enum Mode {
    SHOW,
    HIDE,
    TOGGLE
  }

  public static final BuilderCodec<TagValueHudEffect> CODEC =
      BuilderCodec.builder(
              TagValueHudEffect.class,
              TagValueHudEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Mode", new EnumCodec<>(Mode.class)),
              (effect, value) -> effect.mode = value,
              effect -> effect.mode)
          .add()
          .append(
              new KeyedCodec<>("Tag", Codec.STRING),
              (effect, value) -> effect.tag = value,
              effect -> effect.tag)
          .add()
          .append(
              new KeyedCodec<>("Label", Codec.STRING, false),
              (effect, value) -> effect.label = value,
              effect -> effect.label)
          .add()
          .build();

  private Mode mode = Mode.TOGGLE;
  private String tag = "";
  private String label = "";

  @Override
  public void execute(TriggerContext context) {
    PlayerTagsComponent component = PlayerTagAccess.getTags(context);
    String key = PlayerTagAccess.normalizeKey(tag);
    if (component == null || key.isEmpty()) {
      return;
    }

    boolean enabled =
        switch (mode) {
          case SHOW -> true;
          case HIDE -> false;
          case TOGGLE -> !component.isTagValueHudEnabled();
        };

    component.configureTagValueHud(
        enabled,
        key,
        label == null || label.isBlank() ? key : label);
    TagValueHud.refreshIfWatching(context, component);
  }
}
