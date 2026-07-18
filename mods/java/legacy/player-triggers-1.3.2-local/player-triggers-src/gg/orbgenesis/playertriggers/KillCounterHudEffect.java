package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class KillCounterHudEffect extends TriggerEffect {
  public enum Mode {
    SHOW,
    HIDE,
    TOGGLE
  }

  public static final BuilderCodec<KillCounterHudEffect> CODEC =
      BuilderCodec.builder(
              KillCounterHudEffect.class,
              KillCounterHudEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Mode", new EnumCodec<>(Mode.class)),
              (effect, value) -> effect.mode = value,
              effect -> effect.mode)
          .add()
          .build();

  private Mode mode = Mode.TOGGLE;

  @Override
  public void execute(TriggerContext context) {
    PlayerTagsComponent component = PlayerTagAccess.getTags(context);
    PlayerRef playerRef = PlayerTagAccess.getPlayer(context);
    if (component == null || playerRef == null) {
      return;
    }

    boolean enabled =
        switch (mode) {
          case SHOW -> true;
          case HIDE -> false;
          case TOGGLE -> !component.isKillCounterHudEnabled();
        };
    component.setKillCounterHudEnabled(enabled);
    updateHud(context, component, playerRef, enabled);
  }

  private void updateHud(
      TriggerContext context,
      PlayerTagsComponent component,
      PlayerRef playerRef,
      boolean enabled) {
    Store<EntityStore> store = context.getStore();
    Ref<EntityStore> ref = context.getEntityRef();
    if (store == null || ref == null || !ref.isValid()) {
      return;
    }

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) {
      return;
    }

    if (!enabled) {
      KillCounterHud.remove(player, playerRef);
      return;
    }

    KillCounterHud.getOrCreate(player, playerRef)
        .updateCount(
            component.getTags().getOrDefault(KillCounterHud.TOTAL_KILLS_TAG, "0"));
  }
}
