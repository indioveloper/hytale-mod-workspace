package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

public class ShowTagEventTitleEffect extends TriggerEffect {
  public static final BuilderCodec<ShowTagEventTitleEffect> CODEC =
      BuilderCodec.builder(
              ShowTagEventTitleEffect.class,
              ShowTagEventTitleEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("PrimaryTitle", Codec.STRING),
              (effect, value) -> effect.primaryTitle = value,
              effect -> effect.primaryTitle)
          .add()
          .append(
              new KeyedCodec<>("SecondaryTitle", Codec.STRING, false),
              (effect, value) -> effect.secondaryTitle = value,
              effect -> effect.secondaryTitle)
          .add()
          .append(
              new KeyedCodec<>("IsMajor", Codec.BOOLEAN, false),
              (effect, value) -> effect.isMajor = value,
              effect -> effect.isMajor)
          .add()
          .append(
              new KeyedCodec<>("Icon", Codec.STRING, false),
              (effect, value) -> effect.icon = value,
              effect -> effect.icon)
          .add()
          .append(
              new KeyedCodec<>("Duration", Codec.FLOAT, false),
              (effect, value) -> effect.duration = value,
              effect -> effect.duration)
          .add()
          .append(
              new KeyedCodec<>("FadeInDuration", Codec.FLOAT, false),
              (effect, value) -> effect.fadeInDuration = value,
              effect -> effect.fadeInDuration)
          .add()
          .append(
              new KeyedCodec<>("FadeOutDuration", Codec.FLOAT, false),
              (effect, value) -> effect.fadeOutDuration = value,
              effect -> effect.fadeOutDuration)
          .add()
          .append(
              new KeyedCodec<>("TagSource", new EnumCodec<>(TagSource.class), false),
              (effect, value) ->
                  effect.tagSource = value != null ? value : TagSource.SELF,
              effect -> effect.tagSource)
          .add()
          .append(
              new KeyedCodec<>("TagRadius", Codec.DOUBLE, false),
              (effect, value) -> effect.tagRadius = value,
              effect -> effect.tagRadius)
          .add()
          .build();

  public String primaryTitle = "";
  public String secondaryTitle = "";
  public boolean isMajor = true;
  public String icon;
  public float duration = 4.0F;
  public float fadeInDuration = 1.5F;
  public float fadeOutDuration = 1.5F;
  public TagSource tagSource = TagSource.SELF;
  public double tagRadius = 50.0D;

  @Override
  public void execute(TriggerContext context) {
    PlayerRef player = getPlayer(context);
    if (player == null || primaryTitle == null) {
      return;
    }

    Message primary =
        Message.raw(TagTemplateResolver.resolve(context, primaryTitle, tagSource, tagRadius));
    Message secondary =
        Message.raw(TagTemplateResolver.resolve(context, secondaryTitle, tagSource, tagRadius));
    EventTitleUtil.showEventTitleToPlayer(
        player,
        primary,
        secondary,
        isMajor,
        icon,
        Math.max(0.0F, duration),
        Math.max(0.0F, fadeInDuration),
        Math.max(0.0F, fadeOutDuration));
  }

  private PlayerRef getPlayer(TriggerContext context) {
    if (context == null) {
      return null;
    }
    Ref<EntityStore> entityRef = context.getEntityRef();
    Store<EntityStore> store = context.getStore();
    if (entityRef == null || store == null || !entityRef.isValid()) {
      return null;
    }
    return store.getComponent(entityRef, PlayerRef.getComponentType());
  }
}
