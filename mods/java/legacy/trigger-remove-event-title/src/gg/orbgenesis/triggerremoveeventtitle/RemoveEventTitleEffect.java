package gg.orbgenesis.triggerremoveeventtitle;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

public class RemoveEventTitleEffect extends TriggerEffect {
  public float fadeOutDuration = 1.5f;

  public static final BuilderCodec<RemoveEventTitleEffect> CODEC =
      BuilderCodec.builder(
              RemoveEventTitleEffect.class, RemoveEventTitleEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("FadeOutDuration", Codec.FLOAT, true),
              (effect, value) -> effect.fadeOutDuration = value,
              effect -> effect.fadeOutDuration)
          .add()
          .build();

  @Override
  public void execute(TriggerContext context) {
    PlayerRef player = getPlayer(context);
    if (player == null) {
      return;
    }

    EventTitleUtil.hideEventTitleFromPlayer(player, Math.max(0.0f, this.fadeOutDuration));
  }

  private PlayerRef getPlayer(TriggerContext context) {
    Ref<EntityStore> entityRef = context.getEntityRef();
    Store<EntityStore> store = context.getStore();
    if (entityRef == null || store == null) {
      return null;
    }

    return store.getComponent(entityRef, PlayerRef.getComponentType());
  }
}
