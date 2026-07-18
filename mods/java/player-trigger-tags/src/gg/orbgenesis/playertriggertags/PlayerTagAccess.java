package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Locale;

final class PlayerTagAccess {
  private PlayerTagAccess() {}

  static PlayerRef getPlayer(TriggerContext context) {
    if (context == null || context.getEntityRef() == null || !context.getEntityRef().isValid()) {
      return null;
    }
    return context.getStore().getComponent(context.getEntityRef(), PlayerRef.getComponentType());
  }

  static PlayerTagsComponent getOrCreateTags(TriggerContext context) {
    PlayerRef player = getPlayer(context);
    if (player == null) {
      return null;
    }

    PlayerTagsComponent component =
        context
            .getStore()
            .getComponent(
                context.getEntityRef(),
                PlayerTriggerTagsPlugin.get().getPlayerTagsComponentType());
    if (component != null) {
      return component;
    }

    component = new PlayerTagsComponent();
    context
        .getStore()
        .addComponent(
            context.getEntityRef(),
            PlayerTriggerTagsPlugin.get().getPlayerTagsComponentType(),
            component);
    return component;
  }

  static PlayerTagsComponent getTags(TriggerContext context) {
    if (getPlayer(context) == null) {
      return null;
    }
    return context
        .getStore()
        .getComponent(
            context.getEntityRef(), PlayerTriggerTagsPlugin.get().getPlayerTagsComponentType());
  }

  static String normalizeKey(String raw) {
    return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
  }
}
