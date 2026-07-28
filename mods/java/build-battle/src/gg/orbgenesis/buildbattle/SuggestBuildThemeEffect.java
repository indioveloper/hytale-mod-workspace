package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SuggestBuildThemeEffect extends TriggerEffect {
  public static final BuilderCodec<SuggestBuildThemeEffect> CODEC =
      BuilderCodec.builder(
              SuggestBuildThemeEffect.class,
              SuggestBuildThemeEffect::new,
              TriggerEffect.BASE_CODEC)
          .build();

  @Override
  public void execute(TriggerContext context) {
    if (context == null) {
      return;
    }

    Ref<EntityStore> playerEntityRef = context.getEntityRef();
    Store<EntityStore> store = context.getStore();
    if (playerEntityRef == null || !playerEntityRef.isValid() || store == null) {
      return;
    }

    PlayerRef playerRef =
        store.getComponent(playerEntityRef, PlayerRef.getComponentType());
    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
    if (playerRef == null || player == null) {
      return;
    }

    if (player.getPageManager().getCustomPage() instanceof ThemeSuggestionPage) {
      return;
    }

    player
        .getPageManager()
        .openCustomPage(
            playerEntityRef,
            store,
            new ThemeSuggestionPage(playerRef, context.getVolume().getId()));
  }
}
